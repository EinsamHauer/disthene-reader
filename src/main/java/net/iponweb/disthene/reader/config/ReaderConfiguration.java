package net.iponweb.disthene.reader.config;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Andrei Ivanov
 */
public class ReaderConfiguration {
    private String bind;
    private int port;
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

    @Override
    public String toString() {
        return "ReaderConfiguration{" +
                "bind='" + bind + '\'' +
                ", port=" + port +
                ", rollups=" + rollups +
                '}';
    }
}
