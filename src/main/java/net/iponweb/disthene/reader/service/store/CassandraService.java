package net.iponweb.disthene.reader.service.store;

import com.datastax.driver.core.*;
import com.datastax.driver.core.policies.DCAwareRoundRobinPolicy;
import com.datastax.driver.core.policies.TokenAwarePolicy;
import com.datastax.driver.core.policies.WhiteListPolicy;
import net.iponweb.disthene.reader.config.StoreConfiguration;
import org.apache.log4j.Logger;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Collections;

/**
 * @author Andrei Ivanov
 */
public class CassandraService {

    private Logger logger = Logger.getLogger(CassandraService.class);

    private Cluster cluster;
    private Session session;
    private final PreparedStatement statement;

    public CassandraService(StoreConfiguration storeConfiguration) {
        String query = "SELECT time, data FROM " +
                            storeConfiguration.getKeyspace() + "." + storeConfiguration.getColumnFamily() +
                            " where path = ? and tenant = ? and period = ? and rollup = ? and time >= ? and time <= ? order by time";

        SocketOptions socketOptions = new SocketOptions()
                .setReceiveBufferSize(1024 * 1024)
                .setSendBufferSize(1024 * 1024)
                .setTcpNoDelay(false)
                .setReadTimeoutMillis((int) (storeConfiguration.getReadTimeout() * 1000))
                .setConnectTimeoutMillis((int) (storeConfiguration.getConnectTimeout() * 1000));

        PoolingOptions poolingOptions = new PoolingOptions();
        poolingOptions.setMaxConnectionsPerHost(HostDistance.LOCAL, storeConfiguration.getMaxConnections());
        poolingOptions.setMaxConnectionsPerHost(HostDistance.REMOTE, storeConfiguration.getMaxConnections());
        poolingOptions.setMaxSimultaneousRequestsPerConnectionThreshold(HostDistance.REMOTE, storeConfiguration.getMaxRequests());
        poolingOptions.setMaxSimultaneousRequestsPerConnectionThreshold(HostDistance.LOCAL, storeConfiguration.getMaxRequests());

        Cluster.Builder builder = Cluster.builder()
                .withSocketOptions(socketOptions)
                .withCompression(ProtocolOptions.Compression.LZ4)
                .withLoadBalancingPolicy(new TokenAwarePolicy(new DCAwareRoundRobinPolicy()))
                .withPoolingOptions(poolingOptions)
                .withQueryOptions(new QueryOptions().setConsistencyLevel(ConsistencyLevel.valueOf(storeConfiguration.getConsistency())))
                .withProtocolVersion(ProtocolVersion.V2)
                .withPort(storeConfiguration.getPort());

        if ( storeConfiguration.getUserName() != null && storeConfiguration.getUserPassword() != null) {
            builder = builder.withCredentials(storeConfiguration.getUserName(), storeConfiguration.getUserPassword());
        }

        for (String cp : storeConfiguration.getCluster()) {
            builder.addContactPoint(cp);
        }

        cluster = builder.build();
        Metadata metadata = cluster.getMetadata();
        logger.debug("Connected to cluster: " + metadata.getClusterName());
        for (Host host : metadata.getAllHosts()) {
            logger.debug(String.format("Datacenter: %s; Host: %s; Rack: %s", host.getDatacenter(), host.getAddress(), host.getRack()));
        }

        session = cluster.connect();

        statement = session.prepare(query);
    }

    public ResultSetFuture executeAsync(String tenant, String path, int period, int rollup, long from, long to) {
        return session.executeAsync(statement.bind(path, tenant, period, rollup, from, to));
    }

    public void shutdown() {
        logger.info("Closing C* session");
        logger.info("Waiting for C* queries to be completed");
        while (getInFlightQueries(session.getState()) > 0) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignored) {
            }
        }
        session.close();
        logger.info("Closing C* cluster");
        cluster.close();
    }

    private int getInFlightQueries(Session.State state) {
        int result = 0;
        Collection<Host> hosts = state.getConnectedHosts();
        for(Host host : hosts) {
            result += state.getInFlightQueries(host);
        }

        return result;
    }

}
