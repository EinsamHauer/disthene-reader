package net.iponweb.disthene.reader;

import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.JestResult;
import io.searchbox.client.config.HttpClientConfig;
import io.searchbox.core.Search;
import net.iponweb.disthene.reader.utils.WildcardUtil;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Andrei Ivanov
 */
public class PathsService {

    private static volatile PathsService instance = null;
    private JestClient client;

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
        client = factory.getObject();
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
        JestResult result = client.execute(search);
        int totalResults = result.getJsonObject().get("hits").getAsJsonObject().get("total").getAsInt();

        searchSourceBuilder = new SearchSourceBuilder();
        query = searchSourceBuilder.query(QueryBuilders.filteredQuery(QueryBuilders.regexpQuery("path", regEx),
                FilterBuilders.termFilter("tenant", tenant))).size(totalResults);
        search = new Search.Builder(searchSourceBuilder.toString()).addIndex("cyanite_paths").build();
        result = client.execute(search);
        return result.getSourceAsObjectList(CyanitePath.class);
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
