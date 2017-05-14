package net.iponweb.disthene.reader.config;

import net.iponweb.disthene.reader.utils.CassandraLoadBalancingPolicies;
import java.util.ArrayList;
import java.util.List;


/**
 * @author Andrei Ivanov
 */
public class StoreConfiguration {
    private List<String> cluster = new ArrayList<>();
    private String keyspace;
    private String columnFamily;
    private String userName;
    private String userPassword;
    private int port;
    private int maxConnections;
    private double readTimeout;
    private double connectTimeout;
    private int maxRequests;
    private String consistency = "ONE";
    private String loadBalancingPolicyName = CassandraLoadBalancingPolicies.tokenDcAwareRoundRobinPolicy;
    private String protocolVersion = "V2";

    public String getUserName() {
        return userName;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }

    public List<String> getCluster() {
        return cluster;
    }

    public void setCluster(List<String> cluster) {
        this.cluster = cluster;
    }

    public String getKeyspace() {
        return keyspace;
    }

    public void setKeyspace(String keyspace) {
        this.keyspace = keyspace;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getMaxConnections() {
        return maxConnections;
    }

    public void setMaxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
    }

    public double getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(double readTimeout) {
        this.readTimeout = readTimeout;
    }

    public double getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(double connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public int getMaxRequests() {
        return maxRequests;
    }

    public void setMaxRequests(int maxRequests) {
        this.maxRequests = maxRequests;
    }

    public String getColumnFamily() {
        return columnFamily;
    }

    public void setColumnFamily(String columnFamily) {
        this.columnFamily = columnFamily;
    }

    public String getConsistency() {
        return consistency;
    }

    public void setConsistency(String consistency) {
        this.consistency = consistency;
    }

    public String getLoadBalancingPolicyName() {
        return loadBalancingPolicyName;
    }

    public void setLoadBalancingPolicyName(String policy) {
        this.loadBalancingPolicyName = policy;
    }

    public String getProtocolVersion() {
        return protocolVersion;
    }

    public void setProtocolVersion(String protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    @Override
    public String toString() {
        return "StoreConfiguration{" +
                "cluster=" + cluster +
                ", keyspace='" + keyspace + '\'' +
                ", columnFamily='" + columnFamily + '\'' +
                ", userName='" + userName + '\'' +
                ", userPassword='" + userPassword + '\'' +
                ", port=" + port +
                ", maxConnections=" + maxConnections +
                ", readTimeout=" + readTimeout +
                ", connectTimeout=" + connectTimeout +
                ", maxRequests=" + maxRequests +
                ", consistency='" + consistency + '\'' +
                ", loadBalancingPolicyName='" + loadBalancingPolicyName + '\'' +
                ", protocolVersion='" + protocolVersion + '\'' +
                '}';
    }
}
