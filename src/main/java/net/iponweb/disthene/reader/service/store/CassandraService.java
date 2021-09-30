package net.iponweb.disthene.reader.service.store;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.CqlSessionBuilder;
import com.datastax.oss.driver.api.core.config.DefaultDriverOption;
import com.datastax.oss.driver.api.core.config.DriverConfigLoader;
import com.datastax.oss.driver.api.core.cql.AsyncResultSet;
import com.datastax.oss.driver.api.core.metadata.Metadata;
import com.datastax.oss.driver.api.core.metadata.Node;
import com.datastax.oss.driver.internal.core.loadbalancing.DcInferringLoadBalancingPolicy;
import com.datastax.oss.driver.internal.core.session.throttling.ConcurrencyLimitingRequestThrottler;
import net.iponweb.disthene.reader.config.StoreConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;


/**
 * @author Andrei Ivanov
 */
public class CassandraService {
    private final static Logger logger = LogManager.getLogger(CassandraService.class);

    private final CqlSession session;

    private final TablesRegistry tablesRegistry;

    public CassandraService(StoreConfiguration storeConfiguration) {

        DriverConfigLoader loader =
                DriverConfigLoader.programmaticBuilder()
                        .withString(DefaultDriverOption.PROTOCOL_COMPRESSION, "lz4")
                        .withStringList(DefaultDriverOption.CONTACT_POINTS, getContactPoints(storeConfiguration))
                        .withDuration(DefaultDriverOption.REQUEST_TIMEOUT, Duration.ofSeconds(storeConfiguration.getReadTimeout()))
                        .withDuration(DefaultDriverOption.CONNECTION_CONNECT_TIMEOUT, Duration.ofSeconds(storeConfiguration.getConnectTimeout()))
                        .withString(DefaultDriverOption.REQUEST_CONSISTENCY, storeConfiguration.getConsistency())
                        .withClass(DefaultDriverOption.LOAD_BALANCING_POLICY_CLASS, DcInferringLoadBalancingPolicy.class)
                        .withInt(DefaultDriverOption.CONNECTION_POOL_LOCAL_SIZE, storeConfiguration.getMaxConnections())
                        .withInt(DefaultDriverOption.CONNECTION_POOL_REMOTE_SIZE, storeConfiguration.getMaxConnections())
                        .withClass(DefaultDriverOption.REQUEST_THROTTLER_CLASS, ConcurrencyLimitingRequestThrottler.class)
                        .withInt(DefaultDriverOption.REQUEST_THROTTLER_MAX_CONCURRENT_REQUESTS, storeConfiguration.getMaxConcurrentRequests())
                        .withInt(DefaultDriverOption.REQUEST_THROTTLER_MAX_QUEUE_SIZE, storeConfiguration.getMaxQueueSize())
                        .build();


        CqlSessionBuilder builder = CqlSession.builder()
                .withConfigLoader(loader);

        if ( storeConfiguration.getUserName() != null && storeConfiguration.getUserPassword() != null ) {
            builder.withAuthCredentials(storeConfiguration.getUserName(), storeConfiguration.getUserPassword());
        }

        session = builder.build();

        Metadata metadata = session.getMetadata();
        logger.debug("Connected to cluster: " + metadata.getClusterName());
        for (Node node : metadata.getNodes().values()) {
            logger.debug(String.format("Datacenter: %s; Host: %s; Rack: %s",
                    node.getDatacenter(),
                    node.getBroadcastAddress().isPresent() ? node.getBroadcastAddress().get().toString() : "unknown", node.getRack()));
        }

        tablesRegistry = new TablesRegistry(session, storeConfiguration);
    }

    public Optional<CompletionStage<AsyncResultSet>> executeAsync(String tenant, String path, int rollup, long from, long to) throws ExecutionException {
        if (tablesRegistry.tenantTableExists(tenant, rollup)) {
            logger.trace("Tenant table exists, adding select from it.");
            return Optional.of(session.executeAsync(tablesRegistry.getStatement(tenant, rollup).bind(path, from, to).setPageSize(Integer.MAX_VALUE)));
        }

        return Optional.empty();
    }

    public void shutdown() {
        logger.info("Closing C* session");
        session.close();
    }

    private List<String> getContactPoints(StoreConfiguration storeConfiguration) {
        return storeConfiguration.getCluster().stream().map(s -> s + ":" + storeConfiguration.getPort()).collect(Collectors.toList());
    }

}
