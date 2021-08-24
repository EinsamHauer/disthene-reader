package net.iponweb.disthene.reader.handler;

import com.google.common.base.Joiner;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import net.iponweb.disthene.reader.exceptions.MissingParameterException;
import net.iponweb.disthene.reader.exceptions.ParameterParsingException;
import net.iponweb.disthene.reader.exceptions.TooMuchDataExpectedException;
import net.iponweb.disthene.reader.service.index.IndexService;
import net.iponweb.disthene.reader.service.stats.StatsService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

/**
 * @author Andrei Ivanov
 */
public class SearchHandler implements DistheneReaderHandler {
    private final static Logger logger = LogManager.getLogger(SearchHandler.class);

    private final static int SEARCH_LIMIT = 100;

    private final IndexService indexService;
    private final StatsService statsService;

    public SearchHandler(IndexService indexService, StatsService statsService) {
        this.indexService = indexService;
        this.statsService = statsService;
    }

    @Override
    public FullHttpResponse handle(HttpRequest request) throws ParameterParsingException, IOException, TooMuchDataExpectedException {
        SearchParameters parameters = parse(request);

        statsService.incPathsRequests(parameters.getTenant());

        String pathsAsString = indexService.getSearchPathsAsString(parameters.getTenant(), parameters.getQuery(), SEARCH_LIMIT);

        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.OK,
                Unpooled.wrappedBuffer(pathsAsString.getBytes()));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain");
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
        return response;
    }

    private SearchParameters parse(HttpRequest request) throws MissingParameterException {
        //todo: do it in some beautiful way
        String parameterString;
        if (request.method().equals(HttpMethod.POST)) {
            ((HttpContent) request).content().resetReaderIndex();
            byte[] bytes = new byte[((HttpContent) request).content().readableBytes()];
            ((HttpContent) request).content().readBytes(bytes);
            parameterString = "/render/?" + new String(bytes);
        } else {
            parameterString = request.uri();
        }

        QueryStringDecoder queryStringDecoder = new QueryStringDecoder(parameterString);

        SearchParameters parameters = new SearchParameters();
        if (queryStringDecoder.parameters().get("tenant") != null) {
            parameters.setTenant(queryStringDecoder.parameters().get("tenant").get(0));
        } else {
            // assume tenant "NONE"
            parameters.setTenant("NONE");
            logger.debug("No tenant in request. Assuming value of NONE");
        }
        if (queryStringDecoder.parameters().get("query") != null) {
            parameters.setQuery(Joiner.on("|").skipNulls().join(queryStringDecoder.parameters().get("query")));
        } else {
            throw new MissingParameterException("Query parameter is missing");
        }

        return parameters;
    }

    private static class SearchParameters {
        private String tenant;
        private String query;

        public String getTenant() {
            return tenant;
        }

        public void setTenant(String tenant) {
            this.tenant = tenant;
        }

        String getQuery() {
            return query;
        }

        void setQuery(String query) {
            this.query = query;
        }
    }
}
