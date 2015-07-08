package net.iponweb.disthene.reader.handler;

import com.google.gson.Gson;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import net.iponweb.disthene.reader.exceptions.MissingParameterException;
import net.iponweb.disthene.reader.exceptions.ParameterParsingException;
import net.iponweb.disthene.reader.exceptions.UnsupportedMethodException;
import net.iponweb.disthene.reader.service.index.IndexService;
import org.apache.log4j.Logger;

import java.nio.charset.Charset;

/**
 * @author Andrei Ivanov
 */
public class PathsHandler implements DistheneReaderHandler {

    final static Logger logger = Logger.getLogger(PathsHandler.class);

    private IndexService indexService;

    public PathsHandler(IndexService indexService) {
        this.indexService = indexService;
    }

    @Override
    public FullHttpResponse handle(HttpRequest request) throws ParameterParsingException {
        PathsParameters parameters = parse(request);
        String pathsAsJsonArray = indexService.getPathsAsJsonArray(parameters.getTenant(), parameters.getQuery());

        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.OK,
                Unpooled.wrappedBuffer(pathsAsJsonArray.getBytes()));
        response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "application/json");
        response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, response.content().readableBytes());
        return response;
    }

    private PathsParameters parse(HttpRequest request) throws MissingParameterException, UnsupportedMethodException {
        QueryStringDecoder queryStringDecoder = new QueryStringDecoder(request.getUri());

        if (request.getMethod().equals(HttpMethod.GET)) {
            PathsParameters parameters = new PathsParameters();
            if (queryStringDecoder.parameters().get("tenant") != null) {
                parameters.setTenant(queryStringDecoder.parameters().get("tenant").get(0));
            } else {
                throw new MissingParameterException("Tenant parameter is missing");
            }
            if (queryStringDecoder.parameters().get("query") != null) {
                parameters.setQuery(queryStringDecoder.parameters().get("query").get(0));
            } else {
                throw new MissingParameterException("Query parameter is missing");
            }

            return parameters;
        } else if (request.getMethod().equals(HttpMethod.POST)) {
            ((HttpContent) request).content().resetReaderIndex();
            PathsParameters parameters = new Gson().fromJson(((HttpContent) request).content().toString(Charset.defaultCharset()), PathsParameters.class);
            if (parameters.getTenant() == null) {
                throw new MissingParameterException("Tenant parameter is missing");
            }
            if (parameters.getQuery() == null) {
                throw new MissingParameterException("Query parameter is missing");
            }
            return parameters;
        } else {
            throw new UnsupportedMethodException("Method is not supported: " + request.getMethod().name());
        }
    }

    private class PathsParameters {
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
