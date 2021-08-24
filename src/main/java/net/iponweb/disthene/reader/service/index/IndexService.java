package net.iponweb.disthene.reader.service.index;

import com.google.common.base.Joiner;
import net.iponweb.disthene.reader.config.IndexConfiguration;
import net.iponweb.disthene.reader.exceptions.TooMuchDataExpectedException;
import net.iponweb.disthene.reader.utils.WildcardUtil;
import org.apache.http.HttpHost;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.client.core.CountResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Andrei Ivanov
 */
public class IndexService {
    private final static Logger logger = LogManager.getLogger(IndexService.class);

    private final IndexConfiguration indexConfiguration;
    private final RestHighLevelClient client;
    private final Joiner joiner = Joiner.on(",").skipNulls();

    public IndexService(IndexConfiguration indexConfiguration) {
        this.indexConfiguration = indexConfiguration;

        client = new RestHighLevelClient(
                RestClient.builder(
                        indexConfiguration.getCluster().stream().map(node -> new HttpHost(node, indexConfiguration.getPort())).toArray(HttpHost[]::new)));
    }

    private List<String> getPathsFromRegExs(String tenant, List<String> regExs, int limit) throws TooMuchDataExpectedException, IOException {
        List<String> result = new ArrayList<>();

        if (regExs.size() > 0) {
            String regEx = Joiner.on("|").skipNulls().join(regExs);

            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder()
                    .size(limit)
                    .fetchSource("path", null)
                    .query(QueryBuilders.boolQuery()
                            .must(QueryBuilders.regexpQuery("path", regEx))
                            .must(QueryBuilders.termQuery("tenant", tenant)));


            SearchRequest request = new SearchRequest(indexConfiguration.getIndex())
                    .source(sourceBuilder);

            SearchResponse response = client.search(request, RequestOptions.DEFAULT);

            // if total hits exceeds maximum - abort right away returning empty array
            if (response.getHits().getTotalHits().value > indexConfiguration.getMaxPaths()) {
                logger.debug("Total number of paths exceeds the limit: " + response.getHits().getTotalHits().value);
                throw new TooMuchDataExpectedException("Total number of paths exceeds the limit: " + response.getHits().getTotalHits().value + " (the limit is " + indexConfiguration.getMaxPaths() + ")");
            }

            for (SearchHit hit : response.getHits()) {
                result.add(hit.field("path").getValue());
            }
        }

        return result;
    }

    private List<String> getPathsFromRegExs(String tenant, List<String> regExs) throws TooMuchDataExpectedException, IOException {
        return getPathsFromRegExs(tenant, regExs, indexConfiguration.getMaxPaths());
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

        result.addAll(getPathsFromRegExs(tenant, regExs));

        return result;
    }

    public String getPathsAsJsonArray(String tenant, String wildcard) throws TooMuchDataExpectedException, IOException {
        String regEx = WildcardUtil.getPathsRegExFromWildcard(wildcard);

        if (!WildcardUtil.regexIsValid(regEx)) {
            return "[]";
        }

        return "[" + joiner.join(getPathsFromRegExs(tenant, List.of(regEx))) + "]";
    }

    public String getSearchPathsAsString(String tenant, String regEx, int limit) throws IOException, TooMuchDataExpectedException {
        return Joiner.on(",").skipNulls().join(getPathsFromRegExs(tenant, List.of(regEx), limit));
    }

    public String getPathsWithStats(String tenant, String wildcard) throws TooMuchDataExpectedException, IOException {
        String regEx = WildcardUtil.getPathsRegExFromWildcard(wildcard);

        List<String> paths = getPathsFromRegExs(tenant, List.of(regEx));
        Collections.sort(paths);

        // we got the paths. Now let's get the counts
        List<String> result = new ArrayList<>();
        for (String path : paths) {

            CountRequest countRequest = new CountRequest(indexConfiguration.getIndex())
                    .query(QueryBuilders.boolQuery()
                            .must(QueryBuilders.regexpQuery("path", path + "\\..*"))
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
}
