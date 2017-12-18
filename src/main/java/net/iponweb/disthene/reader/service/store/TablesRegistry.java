package net.iponweb.disthene.reader.service.store;

import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import net.iponweb.disthene.reader.config.StoreConfiguration;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Andrei Ivanov
 */
public class TablesRegistry {

    private static final String TABLE_QUERY = "SELECT COUNT(1) FROM SYSTEM.SCHEMA_COLUMNFAMILIES WHERE KEYSPACE_NAME=? AND COLUMNFAMILY_NAME=?";
    private static final String TABLE_TEMPLATE = "%s_%d_metric";
    private static final String SELECT_QUERY_TEMPLATE = "SELECT time, data FROM %s.%s where path = ? and time >= ? and time <= ? order by time";


    private Session session;
    private StoreConfiguration storeConfiguration;
    private final PreparedStatement queryStatement;

    private final Map<String, PreparedStatement> statements = new HashMap<>();


    public TablesRegistry(Session session, StoreConfiguration storeConfiguration) {
        this.session = session;
        this.storeConfiguration = storeConfiguration;

        queryStatement = session.prepare(TABLE_QUERY);
    }

    public PreparedStatement getStatement(String tenant, int rollup) {
        String table = String.format(TABLE_TEMPLATE, tenant, rollup);

        if (statements.containsKey(table)) return statements.get(table);

        synchronized (this) {
            if (!statements.containsKey(table)) {
                statements.put(table, session.prepare(String.format(SELECT_QUERY_TEMPLATE, storeConfiguration.getTenantKeyspace(), table)));
            }
        }

        return statements.get(table);
    }

    public boolean globalTableExists() {
        return checkTable(storeConfiguration.getKeyspace(), storeConfiguration.getColumnFamily());
    }

    public boolean tenantTableExists(String tenant, int rollup) {
        return checkTable(storeConfiguration.getTenantKeyspace(), String.format(TABLE_TEMPLATE, tenant, rollup));
    }

    private boolean checkTable(String keyspace, String table) {
        ResultSet resultSet = session.execute(queryStatement.bind(keyspace, table));
        return resultSet.one().getLong(0) > 0;
    }

}
