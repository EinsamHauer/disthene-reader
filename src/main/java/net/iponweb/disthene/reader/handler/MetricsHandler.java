package net.iponweb.disthene.reader.handler;

import com.google.gson.Gson;
import io.netty.handler.codec.http.*;
import net.iponweb.disthene.reader.exceptions.MissingParameterException;
import net.iponweb.disthene.reader.exceptions.UnsupportedMethodException;
import net.iponweb.disthene.reader.service.index.IndexService;
import net.iponweb.disthene.reader.service.store.CassandraService;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Andrei Ivanov
 */
public class MetricsHandler implements DistheneReaderHandler {

    final static Logger logger = Logger.getLogger(MetricsHandler.class);

    private IndexService indexService;
    private CassandraService cassandraService;

    public MetricsHandler(IndexService indexService, CassandraService cassandraService) {
        this.indexService = indexService;
        this.cassandraService = cassandraService;
    }

    @Override
    public FullHttpResponse handle(HttpRequest request) throws UnsupportedMethodException, MissingParameterException {
        MetricsParameters parameters = parse(request);
        List<String> paths = indexService.getPaths(parameters.getTenant(), parameters.getPaths());


        // Calculate rollup etc
/*
        Long now = new DateTime().getMillis() * 1000;
        Long effectiveTo = Math.min(parameters.getTo(), now);
        int rollup = getRollup(parameters.getFrom(), effectiveTo);
        int period = getPeriod(parameters.getFrom(), effectiveTo);
        Long effectiveFrom = (parameters.getFrom() % rollup) == 0 ? parameters.getFrom() : parameters.getFrom() + rollup - (parameters.getFrom() % rollup);
        effectiveTo = effectiveTo - (effectiveTo % rollup);

        logger.debug("Effective from: " + effectiveFrom);
        logger.debug("Effective to: " + effectiveTo);

        // now build the weird data structures ("in the meanwhile")
        final Map<Long, Integer> timestampIndices = new HashMap<>();
        Long timestamp = effectiveFrom;
        int index = 0;
        while (timestamp <= effectiveTo) {
            timestampIndices.put(timestamp, index++);
            timestamp += rollup;
        }

*/

        return null;
    }

    private MetricsParameters parse(HttpRequest request) throws MissingParameterException, UnsupportedMethodException {
        QueryStringDecoder queryStringDecoder = new QueryStringDecoder(request.getUri());

        if (request.getMethod().equals(HttpMethod.GET)) {
            MetricsParameters parameters = new MetricsParameters();
            if (queryStringDecoder.parameters().get("tenant") != null) {
                parameters.setTenant(queryStringDecoder.parameters().get("tenant").get(0));
            } else {
                throw new MissingParameterException("Tenant parameter is missing");
            }
            if (queryStringDecoder.parameters().get("path") != null) {
                for (String path : queryStringDecoder.parameters().get("path")) {
                    parameters.getPaths().add(path);
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
        } else if (request.getMethod().equals(HttpMethod.POST)) {
            MetricsParameters parameters =
                    new Gson().fromJson(((HttpContent) request).content().toString(Charset.defaultCharset()), MetricsParameters.class);
            if (parameters.getTenant() == null) {
                throw new MissingParameterException("Tenant parameter is missing");
            }
            if (parameters.getPaths().size() == 0) {
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

    private class MetricsParameters {

        private String tenant;
        private List<String> paths = new ArrayList<>();
        private Long from;
        private Long to;

        public String getTenant() {
            return tenant;
        }

        public void setTenant(String tenant) {
            this.tenant = tenant;
        }

        public List<String> getPaths() {
            return paths;
        }

        public void setPath(List<String> paths) {
            this.paths = paths;
        }

        public Long getFrom() {
            return from;
        }

        public void setFrom(Long from) {
            this.from = from;
        }

        public Long getTo() {
            return to;
        }

        public void setTo(Long to) {
            this.to = to;
        }
    }

}
