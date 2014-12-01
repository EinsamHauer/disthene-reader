package net.iponweb.disthene.reader.services;

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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Andrei Ivanov
 */
public class PathsService {

    private static volatile PathsService instance = null;
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
        Settings settings = ImmutableSettings.settingsBuilder()
                .put("cluster.name", Configuration.ES_CLUSTER_NAME).build();
        client = new TransportClient(settings);
        for(String node : Configuration.ES_NODES) {
            client.addTransportAddress(new InetSocketTransportAddress(node, Configuration.ES_NATIVE_PORT));
        }
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
        String regEx = WildcardUtil.getPathsRegExFromWildcard(wildcard);
        int depth = WildcardUtil.getPathDepth(wildcard);

        SearchResponse response = client.prepareSearch(Configuration.ES_INDEX)
                .setScroll(new TimeValue(Configuration.ES_TIMEOUT))
                .setSize(Configuration.ES_SCROLL_SIZE)
                .setQuery(QueryBuilders.filteredQuery(
                        QueryBuilders.regexpQuery("path", regEx),
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
}
