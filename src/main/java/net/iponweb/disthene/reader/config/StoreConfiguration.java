package net.iponweb.disthene.reader.config;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * @author Andrei Ivanov
 */
@SuppressWarnings("unused")
public class StoreConfiguration {
    private List<String> cluster = new ArrayList<>();
    private String userName;
    private String userPassword;
    private int port;
    private int maxConnections;
    private int readTimeout;
    private int connectTimeout;
    private int maxConcurrentRequests = 1024;
    private int maxQueueSize = 1024*1024;
    private String consistency = "ONE";
    private String keyspace;
    private int cacheExpiration = 180;
    private String tableTemplate = "metric_%s_%d"; //%s - tenant, %d rollup
    private Set<Integer> skipGlobalTableRollups = new HashSet<>();

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

    public int getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public String getConsistency() {
        return consistency;
    }

    public int getMaxConcurrentRequests() {
        return maxConcurrentRequests;
    }

    public void setMaxConcurrentRequests(int maxConcurrentRequests) {
        this.maxConcurrentRequests = maxConcurrentRequests;
    }

    public int getMaxQueueSize() {
        return maxQueueSize;
    }

    public void setMaxQueueSize(int maxQueueSize) {
        this.maxQueueSize = maxQueueSize;
    }

    public void setConsistency(String consistency) {
        this.consistency = consistency;
    }

    public String getKeyspace() {
        return keyspace;
    }

    public void setKeyspace(String keyspace) {
        this.keyspace = keyspace;
    }

    public int getCacheExpiration() {
        return cacheExpiration;
    }

    public void setCacheExpiration(int cacheExpiration) {
        this.cacheExpiration = cacheExpiration;
    }

    public String getTableTemplate() {
        return tableTemplate;
    }

    public void setTableTemplate(String tableTemplate) {
        this.tableTemplate = tableTemplate;
    }

    public Set<Integer> getSkipGlobalTableRollups() {
        return skipGlobalTableRollups;
    }

    public void setSkipGlobalTableRollups(Set<Integer> skipGlobalTableRollups) {
        this.skipGlobalTableRollups = skipGlobalTableRollups;
    }

    @Override
    public String toString() {
        return "StoreConfiguration{" +
                "cluster=" + cluster +
                ", userName='" + userName + '\'' +
                ", userPassword='" + userPassword + '\'' +
                ", port=" + port +
                ", maxConnections=" + maxConnections +
                ", readTimeout=" + readTimeout +
                ", connectTimeout=" + connectTimeout +
                ", maxConcurrentRequests=" + maxConcurrentRequests +
                ", maxQueueSize=" + maxQueueSize +
                ", consistency='" + consistency + '\'' +
                ", keyspace='" + keyspace + '\'' +
                ", cacheExpiration=" + cacheExpiration +
                ", tableTemplate='" + tableTemplate + '\'' +
                ", skipGlobalTableRollups=" + skipGlobalTableRollups +
                '}';
    }
}
