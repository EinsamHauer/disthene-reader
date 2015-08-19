package net.iponweb.disthene.reader.config;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Andrei Ivanov
 */
public class ReaderConfiguration {
    private String bind;
    private int port;
    private int threads = 32;
    private int requestTimeout = 30;
    private int maxPoints = 60_000_000;
    private List<Rollup> rollups = new ArrayList<>();

    public String getBind() {
        return bind;
    }

    public void setBind(String bind) {
        this.bind = bind;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public List<Rollup> getRollups() {
        return rollups;
    }

    public void setRollups(List<Rollup> rollups) {
        this.rollups = rollups;
    }

    public int getThreads() {
        return threads;
    }

    public void setThreads(int threads) {
        this.threads = threads;
    }

    public int getRequestTimeout() {
        return requestTimeout;
    }

    public void setRequestTimeout(int requestTimeout) {
        this.requestTimeout = requestTimeout;
    }

    public int getMaxPoints() {
        return maxPoints;
    }

    public void setMaxPoints(int maxPoints) {
        this.maxPoints = maxPoints;
    }

    @Override
    public String toString() {
        return "ReaderConfiguration{" +
                "bind='" + bind + '\'' +
                ", port=" + port +
                ", threads=" + threads +
                ", requestTimeout=" + requestTimeout +
                ", maxPoints=" + maxPoints +
                ", rollups=" + rollups +
                '}';
    }
}
