package net.iponweb.disthene.reader.handler;

import com.google.common.base.Joiner;
import com.google.common.base.Stopwatch;
import com.google.common.util.concurrent.SimpleTimeLimiter;
import com.google.common.util.concurrent.TimeLimiter;
import com.google.common.util.concurrent.UncheckedTimeoutException;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import net.iponweb.disthene.reader.config.ReaderConfiguration;
import net.iponweb.disthene.reader.exceptions.MissingParameterException;
import net.iponweb.disthene.reader.exceptions.ParameterParsingException;
import net.iponweb.disthene.reader.exceptions.TooMuchDataExpectedException;
import net.iponweb.disthene.reader.service.index.IndexService;
import net.iponweb.disthene.reader.service.stats.StatsService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.concurrent.*;

/**
 * @author Andrei Ivanov
 */
@SuppressWarnings("UnstableApiUsage")
public class SearchHandler implements DistheneReaderHandler {
    private final static Logger logger = LogManager.getLogger(SearchHandler.class);

    private final IndexService indexService;
    private final StatsService statsService;

    private final ReaderConfiguration readerConfiguration;

    private static final ExecutorService executor = Executors.newCachedThreadPool();
    private final TimeLimiter timeLimiter = SimpleTimeLimiter.create(executor);

    public SearchHandler(IndexService indexService, StatsService statsService, ReaderConfiguration readerConfiguration) {
        this.indexService = indexService;
        this.statsService = statsService;
        this.readerConfiguration = readerConfiguration;
    }

    @Override
    public FullHttpResponse handle(HttpRequest request) throws ParameterParsingException, IOException, TooMuchDataExpectedException {
        SearchParameters parameters = parse(request);
        logger.debug("Got request: " + parameters);

        Stopwatch timer = Stopwatch.createStarted();

        statsService.incPathsRequests(parameters.getTenant());

        FullHttpResponse response;
        try {
            response = timeLimiter.callWithTimeout(() -> handleInternal(parameters), readerConfiguration.getRequestTimeout(), TimeUnit.SECONDS);
        } catch (UncheckedTimeoutException e) {
            logger.debug("Request timed out: " + parameters);
            statsService.incTimedOutRequests(parameters.getTenant());
            response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.REQUEST_ENTITY_TOO_LARGE);
        } catch (Exception e) {
            response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.wrappedBuffer(("Ohoho.. We have a weird problem: " + e.getCause().getMessage()).getBytes()));
        }

        timer.stop();
        logger.debug("Request took " + timer.elapsed(TimeUnit.MILLISECONDS) + " milliseconds (" + parameters + ")");

        return response;
    }

    private FullHttpResponse handleInternal(SearchParameters parameters) throws IOException {
        String pathsAsString = indexService.getSearchPathsAsString(parameters.getTenant(), parameters.getQuery());

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

        @Override
        public String toString() {
            return "SearchParameters{" +
                    "tenant='" + tenant + '\'' +
                    ", query='" + query + '\'' +
                    '}';
        }
    }
}
