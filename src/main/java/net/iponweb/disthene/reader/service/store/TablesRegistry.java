package net.iponweb.disthene.reader.service.store;

import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.iponweb.disthene.reader.config.StoreConfiguration;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.regex.Pattern;

/**
 * @author Andrei Ivanov
 */
@SuppressWarnings("UnstableApiUsage")
class TablesRegistry {
    private final Logger logger = Logger.getLogger(TablesRegistry.class);


    private static final String TABLE_QUERY = "SELECT COUNT(1) FROM SYSTEM.SCHEMA_COLUMNFAMILIES WHERE KEYSPACE_NAME=? AND COLUMNFAMILY_NAME=?";
    private static final String SELECT_QUERY_TEMPLATE = "SELECT time, data FROM %s.%s where path = ? and time >= ? and time <= ? order by time";


    private final Session session;
    private final StoreConfiguration storeConfiguration;
    private final PreparedStatement queryStatement;

    private final Map<String, PreparedStatement> statements = new HashMap<>();
    private final Cache<String, Boolean> tablesCache;
    private final ConcurrentMap<String, String> tenants = new ConcurrentHashMap<>();
    private final String tableTemplate;
    private final Pattern normalizationPattern = Pattern.compile("[^0-9a-zA-Z_]");

    TablesRegistry(Session session, StoreConfiguration storeConfiguration) {
        this.session = session;
        this.storeConfiguration = storeConfiguration;
        this.tableTemplate = storeConfiguration.getTenantTableTemplate();

        queryStatement = session.prepare(TABLE_QUERY);

        tablesCache = CacheBuilder.newBuilder()
                .expireAfterWrite(storeConfiguration.getCacheExpiration(), TimeUnit.SECONDS)
                .build();
    }

    PreparedStatement getStatement(String tenant, int rollup) {
        String table = String.format(tableTemplate, getNormalizedTenant(tenant), rollup);

        if (statements.containsKey(table)) return statements.get(table);

        synchronized (this) {
            if (!statements.containsKey(table)) {
                statements.put(table, session.prepare(String.format(SELECT_QUERY_TEMPLATE, storeConfiguration.getTenantKeyspace(), table)));
            }
        }

        return statements.get(table);
    }

    boolean globalTableExists() throws ExecutionException {
        return checkTable(storeConfiguration.getKeyspace(), storeConfiguration.getColumnFamily());
    }

    boolean tenantTableExists(String tenant, int rollup) throws ExecutionException {
        return checkTable(storeConfiguration.getTenantKeyspace(), String.format(tableTemplate, getNormalizedTenant(tenant), rollup));
    }

    private boolean checkTable(final String keyspace, final String table) throws ExecutionException {
        return tablesCache.get(keyspace + "." + table, () -> {
            logger.debug("Table " + keyspace + "." + table + " not found in cache. Checking.");
            ResultSet resultSet = session.execute(queryStatement.bind(keyspace, table));
            Boolean result = resultSet.one().getLong(0) > 0;
            tablesCache.put(keyspace + "." + table, result);
            logger.debug("Table " + keyspace + "." + table + (result ? " " : " not ")  + "found.");
            return result;
        });
    }

    private String getNormalizedTenant(String tenant) {
        if (tenants.containsKey(tenant)) return tenants.get(tenant);

        String normalizedTenant = normalizationPattern.matcher(tenant).replaceAll("_");
        tenants.putIfAbsent(tenant, normalizedTenant);
        return normalizedTenant;
    }
}
