package net.iponweb.disthene.reader.handler;

import com.google.common.base.Joiner;
import com.google.gson.Gson;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import net.iponweb.disthene.reader.exceptions.MissingParameterException;
import net.iponweb.disthene.reader.exceptions.ParameterParsingException;
import net.iponweb.disthene.reader.exceptions.UnsupportedMethodException;
import net.iponweb.disthene.reader.service.index.IndexService;
import net.iponweb.disthene.reader.service.stats.StatsService;
import org.apache.log4j.Logger;

import java.nio.charset.Charset;

/**
 * @author Andrei Ivanov
 */
public class SearchHandler implements DistheneReaderHandler {

    private final static int SEARCH_LIMIT = 100;

    final static Logger logger = Logger.getLogger(SearchHandler.class);

    private IndexService indexService;
    private StatsService statsService;

    public SearchHandler(IndexService indexService, StatsService statsService) {
        this.indexService = indexService;
        this.statsService = statsService;
    }

    @Override
    public FullHttpResponse handle(HttpRequest request) throws ParameterParsingException {
        SearchParameters parameters = parse(request);

        statsService.incPathsRequests(parameters.getTenant());

        String pathsAsString = indexService.getSearchPathsAsString(parameters.getTenant(), parameters.getQuery(), SEARCH_LIMIT);

        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.OK,
                Unpooled.wrappedBuffer(pathsAsString.getBytes()));
        response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/plain");
        response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, response.content().readableBytes());
        return response;
    }

    private SearchParameters parse(HttpRequest request) throws MissingParameterException, UnsupportedMethodException {
        //todo: do it in some beautiful way
        String parameterString;
        if (request.getMethod().equals(HttpMethod.POST)) {
            ((HttpContent) request).content().resetReaderIndex();
            byte[] bytes = new byte[((HttpContent) request).content().readableBytes()];
            ((HttpContent) request).content().readBytes(bytes);
            parameterString = "/render/?" + new String(bytes);
        } else {
            parameterString = request.getUri();
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

    private class SearchParameters {
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
    }
}
