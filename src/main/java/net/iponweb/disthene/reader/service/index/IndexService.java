package net.iponweb.disthene.reader.service.index;

import com.google.common.base.Joiner;
import net.iponweb.disthene.reader.config.IndexConfiguration;
import net.iponweb.disthene.reader.exceptions.TooMuchDataExpectedException;
import net.iponweb.disthene.reader.utils.WildcardUtil;
import org.apache.http.HttpHost;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.indices.settings.get.GetSettingsRequest;
import org.elasticsearch.action.search.*;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.client.core.CountResponse;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.Scroll;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.io.IOException;
import java.util.*;

/**
 * @author Andrei Ivanov
 */
public class IndexService {
    private final static Logger logger = LogManager.getLogger(IndexService.class);

    private final IndexConfiguration indexConfiguration;
    private final RestHighLevelClient client;
    private final Joiner joiner = Joiner.on(",").skipNulls();
    private final int maxResultWindow;
    private final Scroll scroll = new Scroll(TimeValue.timeValueMinutes(1L));

    public IndexService(IndexConfiguration indexConfiguration) throws IOException {
        this.indexConfiguration = indexConfiguration;

        client = new RestHighLevelClient(
                RestClient.builder(
                        indexConfiguration.getCluster().stream().map(node -> new HttpHost(node, indexConfiguration.getPort())).toArray(HttpHost[]::new)));

        GetSettingsRequest request = new GetSettingsRequest().indices(indexConfiguration.getIndex())
                .names("index.max_result_window")
                .includeDefaults(true);

        Settings settings = client.indices().getSettings(request, RequestOptions.DEFAULT).getIndexToSettings().get(indexConfiguration.getIndex());
        maxResultWindow = settings.getAsInt("index.max_result_window", 10_000);
    }

    private List<String> getPathsFromRegExs(String tenant, List<String> regExs, boolean leaf, int limit) throws TooMuchDataExpectedException, IOException {
        List<String> result = new ArrayList<>();

        if (regExs.size() > 0) {
            String regEx = Joiner.on("|").skipNulls().join(regExs);

            BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery()
                    .must(QueryBuilders.regexpQuery("path.keyword", regEx))
                    .filter(QueryBuilders.termQuery("tenant.keyword", tenant));

            if (leaf) queryBuilder.filter(QueryBuilders.termQuery("leaf", true));

            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder()
                    .fetchSource("path", null)
                    .query(queryBuilder);

            if (limit < maxResultWindow) sourceBuilder.size(limit);

            SearchRequest request = new SearchRequest(indexConfiguration.getIndex())
                    .source(sourceBuilder)
                    .scroll(scroll);

            final Set<String> scrollIds = new HashSet<>();

            SearchResponse response = client.search(request, RequestOptions.DEFAULT);
            String scrollId = response.getScrollId();
            scrollIds.add(scrollId);

            // if total hits exceeds maximum - abort right away throwing an exception
            if (response.getHits().getTotalHits().value > indexConfiguration.getMaxPaths()) {
                logger.debug("Total number of paths exceeds the limit: " + response.getHits().getTotalHits().value);
                throw new TooMuchDataExpectedException("Total number of paths exceeds the limit: " + response.getHits().getTotalHits().value + " (the limit is " + indexConfiguration.getMaxPaths() + ")");
            }

            SearchHits hits = response.getHits();

            while (hits.getHits().length > 0) {
                for (SearchHit hit : hits) {
                    result.add(String.valueOf(hit.getSourceAsMap().get("path")));
                }

                SearchScrollRequest scrollRequest = new SearchScrollRequest(scrollId).scroll(scroll);
                response = client.scroll(scrollRequest, RequestOptions.DEFAULT);
                scrollId = response.getScrollId();
                scrollIds.add(scrollId);

                hits = response.getHits();

            }

            clearScrolls(scrollIds);
        }

        return result;
    }

    private List<String> getPathsFromRegExs(String tenant, List<String> regExs, boolean leaf) throws TooMuchDataExpectedException, IOException {
        return getPathsFromRegExs(tenant, regExs, leaf, indexConfiguration.getMaxPaths());
    }

    public List<String> getPaths(String tenant, List<String> wildcards) throws TooMuchDataExpectedException, IOException {
        List<String> regExs = new ArrayList<>();
        List<String> result = new ArrayList<>();

        for (String wildcard : wildcards) {
            if (WildcardUtil.isPlainPath(wildcard)) {
                result.add(wildcard);
            } else {
                regExs.add(WildcardUtil.getPathsRegExFromWildcard(wildcard));
            }
        }

        logger.debug("getPaths plain paths: " + result.size() + ", wildcard paths: " + regExs.size());

        result.addAll(getPathsFromRegExs(tenant, regExs, true));

        return result;
    }

    public String getPathsAsJsonArray(String tenant, String wildcard) throws TooMuchDataExpectedException, IOException {
        String regEx = WildcardUtil.getPathsRegExFromWildcard(wildcard);

        if (!WildcardUtil.regexIsValid(regEx)) {
            return "[]";
        }

        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery()
                .must(QueryBuilders.regexpQuery("path.keyword", regEx))
                .filter(QueryBuilders.termQuery("tenant.keyword", tenant));

        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder()
                .query(queryBuilder);

        if (indexConfiguration.getMaxPaths() < maxResultWindow) sourceBuilder.size(indexConfiguration.getMaxPaths());

        SearchRequest request = new SearchRequest(indexConfiguration.getIndex())
                .source(sourceBuilder)
                .scroll(scroll);

        final Set<String> scrollIds = new HashSet<>();

        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        String scrollId = response.getScrollId();
        scrollIds.add(scrollId);

        // if total hits exceeds maximum - abort right away throwing an exception
        if (response.getHits().getTotalHits().value > indexConfiguration.getMaxPaths()) {
            logger.debug("Total number of paths exceeds the limit: " + response.getHits().getTotalHits().value);
            throw new TooMuchDataExpectedException("Total number of paths exceeds the limit: " + response.getHits().getTotalHits().value + " (the limit is " + indexConfiguration.getMaxPaths() + ")");
        }

        SearchHits hits = response.getHits();
        List<String> paths = new ArrayList<>();

        while (hits.getHits().length > 0) {
            for (SearchHit hit : hits) {
                paths.add(hit.getSourceAsString());
            }

            SearchScrollRequest scrollRequest = new SearchScrollRequest(scrollId).scroll(scroll);
            response = client.scroll(scrollRequest, RequestOptions.DEFAULT);
            scrollId = response.getScrollId();
            scrollIds.add(scrollId);

            hits = response.getHits();
        }

        clearScrolls(scrollIds);

        return "[" + joiner.join(paths) + "]";
    }

    public String getSearchPathsAsString(String tenant, String regEx, int limit) throws IOException, TooMuchDataExpectedException {
        return Joiner.on(",").skipNulls().join(getPathsFromRegExs(tenant, List.of(regEx), false, limit));
    }

    public String getPathsWithStats(String tenant, String wildcard) throws TooMuchDataExpectedException, IOException {
        String regEx = WildcardUtil.getPathsRegExFromWildcard(wildcard);

        List<String> paths = getPathsFromRegExs(tenant, List.of(regEx), false);
        Collections.sort(paths);

        // we got the paths. Now let's get the counts
        List<String> result = new ArrayList<>();
        for (String path : paths) {

            CountRequest countRequest = new CountRequest(indexConfiguration.getIndex())
                    .query(QueryBuilders.boolQuery()
                            .must(QueryBuilders.regexpQuery("path.keyword", path + "\\..*"))
                            .must(QueryBuilders.termQuery("tenant", tenant))
                            .must(QueryBuilders.termQuery("leaf", true)));

            CountResponse countResponse = client.count(countRequest, RequestOptions.DEFAULT);

            result.add("{\"path\": \"" + path + "\",\"count\":" + countResponse.getCount() + "}");
        }

        return "[" + joiner.join(result) + "]";
    }

    public void shutdown() {
        try {
            client.close();
        } catch (IOException e) {
            logger.error("Failed to close ES client: ", e);
        }
    }

    private void clearScrolls(Iterable<String> scrollIds) {
        ClearScrollRequest request = new ClearScrollRequest();
        for (var scrollId : scrollIds) {
            request.addScrollId(scrollId);
        }

        client.clearScrollAsync(request, RequestOptions.DEFAULT, new ActionListener<>() {
            @Override
            public void onResponse(ClearScrollResponse clearScrollResponse) {
                // do nothing. We don't care
            }

            @Override
            public void onFailure(Exception e) {
                logger.warn("Failed to clear scroll with ids " + scrollIds, e);
            }
        });

    }

}
