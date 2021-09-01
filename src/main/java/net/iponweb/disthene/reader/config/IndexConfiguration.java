package net.iponweb.disthene.reader.config;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Andrei Ivanov
 */
public class IndexConfiguration {
    private String index;
    private List<String> cluster = new ArrayList<>();
    private int port;
    private int scroll;
    private int timeout;
    private int maxPaths;

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
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

    public int getScroll() {
        return scroll;
    }

    public void setScroll(int scroll) {
        this.scroll = scroll;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public int getMaxPaths() {
        return maxPaths;
    }

    public void setMaxPaths(int maxPaths) {
        this.maxPaths = maxPaths;
    }

    @Override
    public String toString() {
        return "IndexConfiguration{" +
                "index='" + index + '\'' +
                ", cluster=" + cluster +
                ", port=" + port +
                ", scroll=" + scroll +
                ", timeout=" + timeout +
                ", maxPaths=" + maxPaths +
                '}';
    }
}
