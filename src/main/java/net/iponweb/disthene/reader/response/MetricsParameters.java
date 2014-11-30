package net.iponweb.disthene.reader.response;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Andrei Ivanov
 */
public class MetricsParameters {

    private String tenant;
    private List<String> path = new ArrayList<>();
    private Long from;
    private Long to;

    public String getTenant() {
        return tenant;
    }

    public void setTenant(String tenant) {
        this.tenant = tenant;
    }

    public List<String> getPath() {
        return path;
    }

    public void setPath(List<String> path) {
        this.path = path;
    }

    public Long getFrom() {
        return from;
    }

    public void setFrom(Long from) {
        this.from = from;
    }

    public Long getTo() {
        return to;
    }

    public void setTo(Long to) {
        this.to = to;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
