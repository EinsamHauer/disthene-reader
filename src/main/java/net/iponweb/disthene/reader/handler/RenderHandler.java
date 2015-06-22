package net.iponweb.disthene.reader.handler;

import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import net.iponweb.disthene.reader.exceptions.MissingParameterException;
import net.iponweb.disthene.reader.exceptions.UnsupportedMethodException;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * @author Andrei Ivanov
 */
public class RenderHandler implements DistheneReaderHandler {

    final static Logger logger = Logger.getLogger(RenderHandler.class);



    @Override
    public FullHttpResponse handle(HttpRequest request) throws UnsupportedMethodException, MissingParameterException, ExecutionException, InterruptedException {
        return null;
    }

    private RenderParameters parse(HttpRequest request) throws MissingParameterException {
        QueryStringDecoder queryStringDecoder = new QueryStringDecoder(request.getUri());

        if (request.getMethod().equals(HttpMethod.GET)) {
            RenderParameters parameters = new RenderParameters();

            if (queryStringDecoder.parameters().get("tenant") != null) {
                parameters.setTenant(queryStringDecoder.parameters().get("tenant").get(0));
            } else {
                // assume tenant "NONE"
                parameters.setTenant("NONE");
                logger.debug("No tenant in request. Assuming NONE");
            }

            if (queryStringDecoder.parameters().get("target") != null) {
                for (String path : queryStringDecoder.parameters().get("path")) {
                    parameters.getTargets().add(path);
                }
            } else {
                throw new MissingParameterException("Target parameter is missing");
            }

            if (queryStringDecoder.parameters().get("from") != null) {
                parameters.setFrom(Long.valueOf(queryStringDecoder.parameters().get("from").get(0)));
            } else {
                // default to -1d
                parameters.setFrom((System.currentTimeMillis() / 1000L) - 86400);
            }



            return parameters;
        } else {
            RenderParameters parameters = new RenderParameters();
            return parameters;
        }

    }

    private enum Format {
        PNG, RAW, CSV, JSON, SVG, PICKLE,
    }

    // For now we absolutely need:
    // tz, target, tenant, height, width, from, until (in timestamp format)

    // todo: support full set of render API parameters
    private class RenderParameters {

        private String tenant;
        private List<String> targets = new ArrayList<>();;
        private Long from;
        private Long to;
        private Format format;


        public String getTenant() {
            return tenant;
        }

        public void setTenant(String tenant) {
            this.tenant = tenant;
        }

        public List<String> getTargets() {
            return targets;
        }

        public void setTargets(List<String> targets) {
            this.targets = targets;
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

        public Format getFormat() {
            return format;
        }

        public void setFormat(Format format) {
            this.format = format;
        }
    }

}
