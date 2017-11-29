package net.iponweb.disthene.reader.handler;

import com.google.gson.Gson;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import net.iponweb.disthene.reader.exceptions.MissingParameterException;
import net.iponweb.disthene.reader.exceptions.ParameterParsingException;
import net.iponweb.disthene.reader.exceptions.TooMuchDataExpectedException;
import net.iponweb.disthene.reader.exceptions.UnsupportedMethodException;
import net.iponweb.disthene.reader.service.index.IndexService;
import net.iponweb.disthene.reader.service.stats.StatsService;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 * @author Andrei Ivanov
 */
public class PathStatsHandler implements DistheneReaderHandler {

    final static Logger logger = Logger.getLogger(PathStatsHandler.class);

    private IndexService indexService;
    private StatsService statsService;

    public PathStatsHandler(IndexService indexService, StatsService statsService) {
        this.indexService = indexService;
        this.statsService = statsService;
    }

    @Override
    public FullHttpResponse handle(HttpRequest request) throws ParameterParsingException, TooMuchDataExpectedException {
        PathStatsParameters parameters = parse(request);

        statsService.incPathsRequests(parameters.getTenant());

        String pathsAsJsonArray = indexService.getPathsWithStats(parameters.getTenant(), parameters.getQuery());

        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.OK,
                Unpooled.wrappedBuffer(pathsAsJsonArray.getBytes()));
        response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "application/json");
        response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, response.content().readableBytes());

        return response;
    }

    private PathStatsParameters parse(HttpRequest request) throws MissingParameterException, UnsupportedMethodException {
        if (request.getMethod().equals(HttpMethod.GET)) {
            QueryStringDecoder queryStringDecoder = new QueryStringDecoder(request.getUri());

            PathStatsParameters parameters = new PathStatsParameters();
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
        } else if (request.getMethod().equals(HttpMethod.POST)) {
            PathStatsParameters parameters = new PathStatsParameters();
            ((HttpContent) request).content().resetReaderIndex();
            HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(new DefaultHttpDataFactory(false), request);

            try {
                parameters.setTenant(((Attribute) decoder.getBodyHttpData("tenant")).getValue());
                parameters.setQuery(((Attribute) decoder.getBodyHttpData("query")).getValue());
            } catch (IOException e) {
                throw new MissingParameterException("Some of the parameters are missing: " + request.getMethod().name());
            }
            return parameters;
        } else {
            throw new UnsupportedMethodException("Method is not supported: " + request.getMethod().name());
        }
    }

    private class PathStatsParameters {
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
