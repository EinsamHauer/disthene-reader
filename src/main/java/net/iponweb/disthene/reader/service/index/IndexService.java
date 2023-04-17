package net.iponweb.disthene.reader.service.index;

import com.google.common.base.Joiner;
import com.google.common.base.Stopwatch;
import net.iponweb.disthene.reader.config.IndexConfiguration;
import net.iponweb.disthene.reader.exceptions.TooMuchDataExpectedException;
import net.iponweb.disthene.reader.utils.WildcardUtil;
import org.apache.http.HttpHost;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opensearch.action.ActionListener;
import org.opensearch.action.admin.indices.settings.get.GetSettingsRequest;
import org.opensearch.action.search.*;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestClient;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.client.core.CountRequest;
import org.opensearch.client.core.CountResponse;
import org.opensearch.common.settings.Settings;
import org.opensearch.common.unit.TimeValue;
import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.search.Scroll;
import org.opensearch.search.SearchHit;
import org.opensearch.search.SearchHits;
import org.opensearch.search.builder.SearchSourceBuilder;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import io.github.acm19.aws.interceptor.http.AwsRequestSigningApacheInterceptor;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.signer.Aws4Signer;
import software.amazon.awssdk.regions.Region;

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

        HttpRequestInterceptor interceptor = new AwsRequestSigningApacheInterceptor(
                "es",
                Aws4Signer.create(),
                DefaultCredentialsProvider.create(),
                Region.EU_CENTRAL_1
                );

        String host  = indexConfiguration.getCluster().get(0);
        client = new RestHighLevelClient(
                RestClient.builder(HttpHost.create(host))
                .setHttpClientConfigCallback(hacb -> hacb.addInterceptorLast(interceptor))
                .setCompressionEnabled(true)
                .setChunkedEnabled(false));
        maxResultWindow = 10_000; 
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

            sourceBuilder.size(Math.min(Math.min(limit, maxResultWindow), indexConfiguration.getScroll()));
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

        sourceBuilder.size(Math.min(Math.min(indexConfiguration.getMaxPaths(), maxResultWindow), indexConfiguration.getScroll()));

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

    public String getSearchPathsAsString(String tenant, String regEx) throws IOException {
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery()
                .must(QueryBuilders.regexpQuery("path.keyword", regEx))
                .filter(QueryBuilders.termQuery("tenant.keyword", tenant));

        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder()
                .fetchSource("path", null)
                .query(queryBuilder)
                .size(Math.min(indexConfiguration.getMaxSearchPaths(), maxResultWindow));

        SearchRequest request = new SearchRequest(indexConfiguration.getIndex())
                .source(sourceBuilder)
                .requestCache(true);

        Stopwatch indexTimer = Stopwatch.createStarted();
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        indexTimer.stop();
        logger.debug("Fetching from ES took " + indexTimer.elapsed(TimeUnit.MILLISECONDS) + " milliseconds (" + regEx + ")");

        List<String> paths = new ArrayList<>();
        for (SearchHit hit : response.getHits()) {
            paths.add(String.valueOf(hit.getSourceAsMap().get("path")));
        }

        return Joiner.on(",").skipNulls().join(paths);
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
                            .must(QueryBuilders.termQuery("tenant.keyword", tenant))
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
