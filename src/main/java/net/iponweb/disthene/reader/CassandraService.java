package net.iponweb.disthene.reader;

import com.datastax.driver.core.*;
import com.datastax.driver.core.policies.DCAwareRoundRobinPolicy;
import com.datastax.driver.core.policies.TokenAwarePolicy;
import org.apache.log4j.Logger;

/**
 * @author Andrei Ivanov
 */
public class CassandraService {

    private static final String[] CASSANDRA_CPS = {
            "cassandra11.devops.iponweb.net",
            "cassandra12.devops.iponweb.net",
            "cassandra17.devops.iponweb.net",
            "cassandra18.devops.iponweb.net"
    };
    private final static Logger logger = Logger.getLogger(Main.class);


    private static volatile CassandraService instance = null;

    private Session session;

    public static CassandraService getInstance() {
        if (instance == null) {
            synchronized (PathsService.class) {
                if (instance == null) {
                    instance = new CassandraService();
                }
            }
        }

        return instance;
    }

    public CassandraService() {
        SocketOptions socketOptions = new SocketOptions();
        socketOptions.setReceiveBufferSize(8388608);
        socketOptions.setSendBufferSize(1048576);
        socketOptions.setTcpNoDelay(false);
        socketOptions.setReadTimeoutMillis(1000000);
        socketOptions.setReadTimeoutMillis(1000000);

        Cluster.Builder builder = Cluster.builder()
                .withSocketOptions(socketOptions)
                .withCompression(ProtocolOptions.Compression.LZ4)
                .withLoadBalancingPolicy(new TokenAwarePolicy(new DCAwareRoundRobinPolicy()))
                .withPort(9042);
        for(String cp : CASSANDRA_CPS) {
            builder.addContactPoint(cp);
        }
        Cluster cluster = builder.build();
        Metadata metadata = cluster.getMetadata();
        logger.debug("Connected to cluster: " + metadata.getClusterName());
        for (Host host : metadata.getAllHosts()) {
            logger.debug(String.format("Datacenter: %s; Host: %s; Rack: %s", host.getDatacenter(), host.getAddress(), host.getRack()));
        }

        session = cluster.connect();
    }

    public Session getSession() {
        return session;
    }
}
