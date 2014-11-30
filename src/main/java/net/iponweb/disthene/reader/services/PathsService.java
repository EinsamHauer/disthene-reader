package net.iponweb.disthene.reader.services;

import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.JestResult;
import io.searchbox.client.config.HttpClientConfig;
import io.searchbox.core.Search;
import net.iponweb.disthene.reader.Configuration;
import net.iponweb.disthene.reader.utils.WildcardUtil;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.util.*;

/**
 * @author Andrei Ivanov
 */
public class PathsService {

    private static volatile PathsService instance = null;
    private JestClient oldClient;

    private TransportClient client;

    public static PathsService getInstance() {
        if (instance == null) {
            synchronized (PathsService.class) {
                if (instance == null) {
                    instance = new PathsService();
                }
            }
        }

        return instance;
    }

    public PathsService() {
        Set<String> servers = new LinkedHashSet<>();
        servers.add("http://es5.devops.iponweb.net:9200/");
        servers.add("http://es6.devops.iponweb.net:9200/");
        servers.add("http://es7.devops.iponweb.net:9200/");
        servers.add("http://es8.devops.iponweb.net:9200/");

        JestClientFactory factory = new JestClientFactory();
        factory.setHttpClientConfig(new HttpClientConfig
                .Builder(servers)
                .multiThreaded(true)
                .build());
        oldClient = factory.getObject();


        Settings settings = ImmutableSettings.settingsBuilder()
                .put("cluster.name", Configuration.ES_CLUSTER_NAME).build();
        client = new TransportClient(settings);
        for(String node : Configuration.ES_NODES) {
            client.addTransportAddress(new InetSocketTransportAddress(node, Configuration.ES_NATIVE_PORT));
        }
    }

    public List<String> getPathPaths(String tenant, String wildcard) throws Exception {
        List<CyanitePath> resultingPaths = getPaths(tenant, wildcard);

        List<String> resultList = new ArrayList<>();
        for (CyanitePath path : resultingPaths) {
            resultList.add(path.getPath());
        }

        return resultList;
    }

    public List<CyanitePath> getPaths(String tenant, String wildcard) throws Exception {
        String regEx = WildcardUtil.getRegExFromWildcard(wildcard);


        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        SearchSourceBuilder query = searchSourceBuilder.query(QueryBuilders.filteredQuery(QueryBuilders.regexpQuery("path", regEx),
                FilterBuilders.termFilter("tenant", tenant))).size(1).field("path");
        Search search = new Search.Builder(searchSourceBuilder.toString()).addIndex("cyanite_paths").build();
        JestResult result = oldClient.execute(search);
        int totalResults = result.getJsonObject().get("hits").getAsJsonObject().get("total").getAsInt();

        searchSourceBuilder = new SearchSourceBuilder();
        query = searchSourceBuilder.query(QueryBuilders.filteredQuery(QueryBuilders.regexpQuery("path", regEx),
                FilterBuilders.termFilter("tenant", tenant))).size(totalResults);
        search = new Search.Builder(searchSourceBuilder.toString()).addIndex("cyanite_paths").build();
        result = oldClient.execute(search);
        return result.getSourceAsObjectList(CyanitePath.class);
    }

    public Set<String> getPathsSet(String tenant, List<String> wildcards) {
        Set<String> result = new HashSet<>();

        for(String wildcard : wildcards) {
            result.addAll(getPathsSet(tenant, wildcard));
        }

        return result;
    }

    public Set<String> getPathsSet(String tenant, String wildcard) {
        String regEx = WildcardUtil.getRegExFromWildcard(wildcard);
        Set<String> result = new HashSet<>();

        SearchResponse response = client.prepareSearch(Configuration.ES_INDEX)
                .setScroll(new TimeValue(Configuration.ES_TIMEOUT))
                .setSize(Configuration.ES_SCROLL_SIZE)
                .setQuery(QueryBuilders.filteredQuery(QueryBuilders.regexpQuery("path", regEx),
                        FilterBuilders.termFilter("tenant", tenant)))
                .addField("path")
                .execute().actionGet();

        while (response.getHits().getHits().length > 0) {
            for (SearchHit hit : response.getHits()) {
                result.add((String) hit.field("path").getValue());
            }
            response = client.prepareSearchScroll(response.getScrollId())
                    .setScroll(new TimeValue(120000))
                    .execute().actionGet();
        }

        return result;
    }


    public String getPathsAsJsonArray(String tenant, String wildcard) {
        String regEx = WildcardUtil.getRegExFromWildcard(wildcard);

        SearchResponse response = client.prepareSearch(Configuration.ES_INDEX)
                .setScroll(new TimeValue(Configuration.ES_TIMEOUT))
                .setSize(Configuration.ES_SCROLL_SIZE)
                .setQuery(QueryBuilders.filteredQuery(QueryBuilders.regexpQuery("path", regEx),
                        FilterBuilders.termFilter("tenant", tenant)))
                .execute().actionGet();

        StringBuilder sb = new StringBuilder("[");
        String comma = "";
        while (response.getHits().getHits().length > 0) {
            for (SearchHit hit : response.getHits()) {
                sb.append(comma);
                comma = ",";
                sb.append(hit.getSourceAsString());
            }
            response = client.prepareSearchScroll(response.getScrollId())
                    .setScroll(new TimeValue(120000))
                    .execute().actionGet();
        }
        sb.append("]");
        return sb.toString();
    }

    private static class CyanitePath {
        private String path;
        private int depth;
        private String tenant;
        private boolean leaf;

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public int getDepth() {
            return depth;
        }

        public void setDepth(int depth) {
            this.depth = depth;
        }

        public String getTenant() {
            return tenant;
        }

        public void setTenant(String tenant) {
            this.tenant = tenant;
        }

        public boolean isLeaf() {
            return leaf;
        }

        public void setLeaf(boolean leaf) {
            this.leaf = leaf;
        }
    }
}
