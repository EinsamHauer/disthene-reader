package net.iponweb.disthene.reader.handler;

import com.google.gson.Gson;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import net.iponweb.disthene.reader.exceptions.MissingParameterException;
import net.iponweb.disthene.reader.exceptions.ParameterParsingException;
import net.iponweb.disthene.reader.exceptions.TooMuchDataExpectedException;
import net.iponweb.disthene.reader.exceptions.UnsupportedMethodException;
import net.iponweb.disthene.reader.service.index.IndexService;
import net.iponweb.disthene.reader.service.stats.StatsService;
import org.apache.log4j.Logger;

import java.nio.charset.Charset;

/**
 * @author Andrei Ivanov
 */
public class PathsHandler implements DistheneReaderHandler {

    private final static Logger logger = Logger.getLogger(PathsHandler.class);

    private final IndexService indexService;
    private final StatsService statsService;

    public PathsHandler(IndexService indexService, StatsService statsService) {
        this.indexService = indexService;
        this.statsService = statsService;
    }

    @Override
    public FullHttpResponse handle(HttpRequest request) throws ParameterParsingException, TooMuchDataExpectedException {
        PathsParameters parameters = parse(request);

        statsService.incPathsRequests(parameters.getTenant());

        String pathsAsJsonArray = indexService.getPathsAsJsonArray(parameters.getTenant(), parameters.getQuery());

        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.OK,
                Unpooled.wrappedBuffer(pathsAsJsonArray.getBytes()));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json");
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
        return response;
    }

    private PathsParameters parse(HttpRequest request) throws MissingParameterException, UnsupportedMethodException {
        QueryStringDecoder queryStringDecoder = new QueryStringDecoder(request.uri());

        if (request.method().equals(HttpMethod.GET)) {
            PathsParameters parameters = new PathsParameters();
            if (queryStringDecoder.parameters().get("tenant") != null) {
                parameters.setTenant(queryStringDecoder.parameters().get("tenant").get(0));
            } else {
                // assume tenant "NONE"
                parameters.setTenant("NONE");
                logger.debug("No tenant in request. Assuming value of NONE");
            }
            if (queryStringDecoder.parameters().get("query") != null) {
                parameters.setQuery(queryStringDecoder.parameters().get("query").get(0));
            } else {
                throw new MissingParameterException("Query parameter is missing");
            }

            return parameters;
        } else if (request.method().equals(HttpMethod.POST)) {
            ((HttpContent) request).content().resetReaderIndex();
            PathsParameters parameters = new Gson().fromJson(((HttpContent) request).content().toString(Charset.defaultCharset()), PathsParameters.class);
            if (parameters.getTenant() == null) {
                // assume tenant "NONE"
                parameters.setTenant("NONE");
                logger.debug("No tenant in request. Assuming value of NONE");
            }
            if (parameters.getQuery() == null) {
                throw new MissingParameterException("Query parameter is missing");
            }
            return parameters;
        } else {
            throw new UnsupportedMethodException("Method is not supported: " + request.method().name());
        }
    }

    private static class PathsParameters {
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
