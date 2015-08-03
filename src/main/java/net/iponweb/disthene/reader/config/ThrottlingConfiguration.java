package net.iponweb.disthene.reader.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Andrei Ivanov
 */
public class ThrottlingConfiguration {

    private boolean throttlingEnabled = false;
    private int totalQPS = 100;
    private int defaultQPS = 100;

    private Map<String, Integer> tenants = new HashMap<>();
    private List<String> exceptions = new ArrayList<>();

    public boolean isThrottlingEnabled() {
        return throttlingEnabled;
    }

    public void setThrottlingEnabled(boolean throttlingEnabled) {
        this.throttlingEnabled = throttlingEnabled;
    }

    public int getTotalQPS() {
        return totalQPS;
    }

    public void setTotalQPS(int totalQPS) {
        this.totalQPS = totalQPS;
    }

    public int getDefaultQPS() {
        return defaultQPS;
    }

    public void setDefaultQPS(int defaultQPS) {
        this.defaultQPS = defaultQPS;
    }

    public Map<String, Integer> getTenants() {
        return tenants;
    }

    public void setTenants(Map<String, Integer> tenants) {
        this.tenants = tenants;
    }

    public List<String> getExceptions() {
        return exceptions;
    }

    public void setExceptions(List<String> exceptions) {
        this.exceptions = exceptions;
    }

    @Override
    public String toString() {
        return "ThrottlingConfiguration{" +
                "throttlingEnabled=" + throttlingEnabled +
                ", totalQPS=" + totalQPS +
                ", defaultQPS=" + defaultQPS +
                ", tenants=" + tenants +
                ", exceptions=" + exceptions +
                '}';
    }
}
