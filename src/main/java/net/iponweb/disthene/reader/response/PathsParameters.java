package net.iponweb.disthene.reader.response;

import com.google.gson.Gson;

/**
 * @author Andrei Ivanov
 */
public class PathsParameters {

    private String tenant;
    private String query;

    public String getTenant() {
        return tenant;
    }

    public void setTenant(String tenant) {
        this.tenant = tenant;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
