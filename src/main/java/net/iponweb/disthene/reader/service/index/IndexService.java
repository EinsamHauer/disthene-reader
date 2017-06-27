package net.iponweb.disthene.reader.service.index;

import com.google.common.base.Joiner;
import net.iponweb.disthene.reader.config.IndexConfiguration;
import net.iponweb.disthene.reader.utils.WildcardUtil;
import org.apache.log4j.Logger;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

/**
 * @author Andrei Ivanov
 */
public class IndexService {
    final static Logger logger = Logger.getLogger(IndexService.class);

    private IndexConfiguration indexConfiguration;
    private TransportClient client;
    private Joiner joiner = Joiner.on(",").skipNulls();

    public IndexService(IndexConfiguration indexConfiguration) {
        this.indexConfiguration = indexConfiguration;

        Settings settings = ImmutableSettings.settingsBuilder()
                .put("cluster.name", indexConfiguration.getName())
                .build();
        client = new TransportClient(settings);

        for (String node : indexConfiguration.getCluster()) {
            client.addTransportAddress(new InetSocketTransportAddress(node, indexConfiguration.getPort()));
        }
    }

    public Map<String, String> getPaths(String tenant, List<String> wildcards) {
        List<String> regExs = new ArrayList<>();
        for(String wildcard : wildcards) {
            regExs.add(WildcardUtil.getPathsRegExFromWildcard(wildcard));
        }
        String regEx = Joiner.on("|").skipNulls().join(regExs);

        Map<String, String> result = new HashMap<>();

        SearchResponse response = client.prepareSearch(indexConfiguration.getIndex())
                .setScroll(new TimeValue(indexConfiguration.getTimeout()))
                .setSize(indexConfiguration.getScroll())
                .setQuery(QueryBuilders.filteredQuery(QueryBuilders.regexpQuery("path", regEx),
                        FilterBuilders.termFilter("tenant", tenant)))
                .addField("path")
                .addField("origin")
                .execute().actionGet();

        // if total hits exceeds maximum - abort right away returning empty array
        if (response.getHits().totalHits() > indexConfiguration.getMaxPaths()) {
            return result;
        }

        while (response.getHits().getHits().length > 0) {
            for (SearchHit hit : response.getHits()) {
                String path = (String) hit.field("path").getValue();

                if (hit.getFields().containsKey("origin")) {
                    result.put(path, (String) hit.field("origin").getValue());
                } else {
                    result.put(path, path);
                }
            }
            response = client.prepareSearchScroll(response.getScrollId())
                    .setScroll(new TimeValue(indexConfiguration.getTimeout()))
                    .execute().actionGet();
        }

        return result;
    }

    public String getPathsAsJsonArray(String tenant, String wildcard) {
        String regEx = WildcardUtil.getPathsRegExFromWildcard(wildcard);

        SearchResponse response = client.prepareSearch(indexConfiguration.getIndex())
                .setScroll(new TimeValue(indexConfiguration.getTimeout()))
                .setSize(indexConfiguration.getScroll())
                .setQuery(QueryBuilders.filteredQuery(
                        QueryBuilders.regexpQuery("path", regEx),
                        FilterBuilders.termFilter("tenant", tenant)))
                .execute().actionGet();

        // if total hits exceeds maximum - abort right away returning empty array
        if (response.getHits().totalHits() > indexConfiguration.getMaxPaths()) {
            return "[]";
        }

        List<String> paths = new ArrayList<>();
        while (response.getHits().getHits().length > 0) {
            for (SearchHit hit : response.getHits()) {
                paths.add(hit.getSourceAsString());
            }
            response = client.prepareSearchScroll(response.getScrollId())
                    .setScroll(new TimeValue(indexConfiguration.getTimeout()))
                    .execute().actionGet();
        }

        return "[" + joiner.join(paths) + "]";
    }

    public String getSearchPathsAsString(String tenant, String regEx, int limit) {
        SearchResponse response = client.prepareSearch(indexConfiguration.getIndex())
                .setScroll(new TimeValue(indexConfiguration.getTimeout()))
                .setSize(limit)
                .setQuery(QueryBuilders.filteredQuery(
                        QueryBuilders.regexpQuery("path", regEx),
                        FilterBuilders.termFilter("tenant", tenant)))
                .addField("path")
                .execute().actionGet();

        List<String> paths = new ArrayList<>();
        for (SearchHit hit : response.getHits()) {
            paths.add((String) hit.field("path").getValue());
        }

        return Joiner.on(",").skipNulls().join(paths);
    }

    public void shutdown() {
        client.close();
    }
}
