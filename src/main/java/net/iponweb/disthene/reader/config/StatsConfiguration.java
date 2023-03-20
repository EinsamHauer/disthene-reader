package net.iponweb.disthene.reader.config;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author Andrei Ivanov
 */
public class StatsConfiguration {
    private int interval;
    private String tenant;
    private String hostname;
    private String pathPrefix;
    private String carbonHost;
    private int carbonPort;


    public StatsConfiguration() {
        try {
            hostname = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            hostname = "unknown";
        }
        pathPrefix = "";
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public String getTenant() {
        return tenant;
    }

    public void setTenant(String tenant) {
        this.tenant = tenant;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getPathPrefix() {
        return pathPrefix;
    }

    public void setPathPrefix(String pathPrefix) {
        // remove right-trailing dot
        this.pathPrefix = pathPrefix.endsWith(".") ? pathPrefix.replaceAll("\\.+\\z", "") : pathPrefix;
    }

    public String getCarbonHost() {
        return carbonHost;
    }

    public void setCarbonHost(String carbonHost) {
        this.carbonHost = carbonHost;
    }

    public int getCarbonPort() {
        return carbonPort;
    }

    public void setCarbonPort(int carbonPort) {
        this.carbonPort = carbonPort;
    }

    public String getPath() {
        if (pathPrefix.isEmpty()) {
            return hostname;
        } else {
            return pathPrefix + "." + hostname;
        }
    }

    @Override
    public String toString() {
        return "StatsConfiguration{" +
                "interval=" + interval +
                ", tenant='" + tenant + '\'' +
                ", hostname='" + hostname + '\'' +
                ", pathPrefix='" + pathPrefix + '\'' +
                ", carbonHost='" + carbonHost + '\'' +
                ", carbonPort=" + carbonPort +
                '}';
    }
}
