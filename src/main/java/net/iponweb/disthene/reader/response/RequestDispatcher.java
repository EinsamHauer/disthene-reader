package net.iponweb.disthene.reader.response;

import com.google.gson.Gson;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import net.iponweb.disthene.reader.exceptions.MissingParameterException;
import net.iponweb.disthene.reader.exceptions.UnsupportedMethodException;
import org.apache.log4j.Logger;

import java.nio.charset.Charset;

/**
 * @author Andrei Ivanov
 */
public class RequestDispatcher {
    final static Logger logger = Logger.getLogger(RequestDispatcher.class);

    private static final String METRICS_PATH = "/metrics";
    private static final String PATHS_PATH = "/paths";

    private Object message;
    private QueryStringDecoder queryStringDecoder;

    public RequestDispatcher(Object message) {
        this.message = message;
        queryStringDecoder = new QueryStringDecoder(((HttpRequest) message).getUri());
    }

    public FullHttpResponse getResponse() {
        // Here we go - first case by path
        switch (getPath()) {
            case METRICS_PATH:
                return getMetricsResponse();
            case PATHS_PATH:
                return getPathsResponse();
            default:
                return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND);
        }
    }

    private HttpMethod getMethod() {
        return ((HttpRequest) message).getMethod();
    }

    private String getPath() {
        return queryStringDecoder.path();
    }

    private FullHttpResponse getPathsResponse() {
        try {
            FullHttpResponse response = new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1,
                    HttpResponseStatus.OK,
                    Unpooled.wrappedBuffer(PathsResponse.getContent(getPathsParameters()).getBytes()));
            response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "application/json");
            response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, response.content().readableBytes());
            return response;
        } catch (UnsupportedMethodException e) {
            logger.debug("Unsupported method used: " + getMethod().name(), e);
            return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.METHOD_NOT_ALLOWED);
        } catch (Exception e) {
            logger.error("An error occurred:", e);
            return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private FullHttpResponse getMetricsResponse() {
        try {
            FullHttpResponse response = new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1,
                    HttpResponseStatus.OK,
                    Unpooled.wrappedBuffer(MetricsResponse.getContent(getMetricsParameters()).getBytes()));
            response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "application/json");
            response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, response.content().readableBytes());
            return response;
        } catch (UnsupportedMethodException e) {
            logger.debug("Unsupported method used: " + getMethod().name(), e);
            return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.METHOD_NOT_ALLOWED);
        } catch (Exception e) {
            logger.error("An error occurred:", e);
            return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private MetricsParameters getMetricsParameters() throws Exception {
        if (getMethod().equals(HttpMethod.GET)) {
            MetricsParameters parameters = new MetricsParameters();
            if (queryStringDecoder.parameters().get("tenant") != null) {
                parameters.setTenant(queryStringDecoder.parameters().get("tenant").get(0));
            } else {
                throw new MissingParameterException("Tenant parameter is missing");
            }
            if (queryStringDecoder.parameters().get("path") != null) {
                for (String path : queryStringDecoder.parameters().get("path")) {
                    parameters.getPath().add(path);
                }
            } else {
                throw new MissingParameterException("Tenant parameter is missing");
            }
            if (queryStringDecoder.parameters().get("from") != null) {
                parameters.setFrom(Long.valueOf(queryStringDecoder.parameters().get("from").get(0)));
            } else {
                throw new MissingParameterException("From parameter is missing");
            }
            if (queryStringDecoder.parameters().get("to") != null) {
                parameters.setTo(Long.valueOf(queryStringDecoder.parameters().get("to").get(0)));
            } else {
                throw new MissingParameterException("To parameter is missing");
            }

            return parameters;
        } else if (getMethod().equals(HttpMethod.POST)) {
            MetricsParameters parameters =
                    new Gson().fromJson(((HttpContent) message).content().toString(Charset.defaultCharset()),
                            MetricsParameters.class);
            if (parameters.getTenant() == null) {
                throw new MissingParameterException("Tenant parameter is missing");
            }
            if (parameters.getPath().size() == 0) {
                throw new MissingParameterException("Path parameter is missing");
            }
            if (parameters.getFrom() == null) {
                throw new MissingParameterException("From parameter is missing");
            }
            if (parameters.getTo() == null) {
                throw new MissingParameterException("To parameter is missing");
            }
            return parameters;
        } else {
            throw new UnsupportedMethodException();
        }
    }

    private PathsParameters getPathsParameters() throws Exception {
        if (getMethod().equals(HttpMethod.GET)) {
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
        } else if (getMethod().equals(HttpMethod.POST)) {
            PathsParameters parameters =
                    new Gson().fromJson(((HttpContent) message).content().toString(Charset.defaultCharset()),
                            PathsParameters.class);
            if (parameters.getTenant() == null) {
                throw new MissingParameterException("Tenant parameter is missing");
            }
            if (parameters.getQuery() == null) {
                throw new MissingParameterException("Query parameter is missing");
            }
            return parameters;
        } else {
            throw new UnsupportedMethodException();
        }
    }

}
