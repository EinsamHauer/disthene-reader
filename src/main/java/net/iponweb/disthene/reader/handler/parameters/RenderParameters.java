package net.iponweb.disthene.reader.handler.parameters;

import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import net.iponweb.disthene.reader.exceptions.InvalidParameterValueException;
import net.iponweb.disthene.reader.exceptions.MissingParameterException;
import net.iponweb.disthene.reader.exceptions.ParameterParsingException;
import net.iponweb.disthene.reader.format.Format;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Andrei Ivanov
 */
// For now we absolutely need:
// tz, target, tenant, height, width, from, until (in timestamp format)
// let's stick to this bare minimum

// todo: support full set of render API parameters
public class RenderParameters {
    final static Logger logger = Logger.getLogger(RenderParameters.class);

    private String tenant;
    private List<String> targets = new ArrayList<>();;
    private Long from;
    private Long until;
    private Format format;
    private DateTimeZone tz;

    private ImageParameters imageParameters = new ImageParameters();


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

    public Long getUntil() {
        return until;
    }

    public void setUntil(Long until) {
        this.until = until;
    }

    public Format getFormat() {
        return format;
    }

    public void setFormat(Format format) {
        this.format = format;
    }

    public DateTimeZone getTz() {
        return tz;
    }

    public void setTz(DateTimeZone tz) {
        this.tz = tz;
    }

    public ImageParameters getImageParameters() {
        return imageParameters;
    }

    public void setImageParameters(ImageParameters imageParameters) {
        this.imageParameters = imageParameters;
    }


    @Override
    public String toString() {
        return "RenderParameters{" +
                "tenant='" + tenant + '\'' +
                ", targets=" + targets +
                ", from=" + from +
                ", until=" + until +
                ", format=" + format +
                ", tz=" + tz +
                ", imageParameters=" + imageParameters +
                '}';
    }

    public static RenderParameters parse(HttpRequest request) throws ParameterParsingException {
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
        logger.debug(parameterString);
        QueryStringDecoder queryStringDecoder = new QueryStringDecoder(parameterString);

        RenderParameters parameters = new RenderParameters();

        if (queryStringDecoder.parameters().get("tenant") != null) {
            parameters.setTenant(queryStringDecoder.parameters().get("tenant").get(0));
        } else {
            // assume tenant "NONE"
            parameters.setTenant("NONE");
            logger.debug("No tenant in request. Assuming NONE");
        }

        if (queryStringDecoder.parameters().get("target") != null) {
            for (String path : queryStringDecoder.parameters().get("target")) {
                parameters.getTargets().add(path);
            }
        }

        // First decode tz and default to UTC
        if (queryStringDecoder.parameters().get("tz") != null) {
            try {
                parameters.setTz(DateTimeZone.forID(queryStringDecoder.parameters().get("tz").get(0)));
            } catch (Exception e) {
                throw new InvalidParameterValueException("Timezone not found: " + queryStringDecoder.parameters().get("tz").get(0));
            }
        } else {
            parameters.setTz(DateTimeZone.UTC);
        }

        // parse from defaulting to -1d
        if (queryStringDecoder.parameters().get("from") != null) {
            try {
                parameters.setFrom(new DateTime(Long.valueOf(queryStringDecoder.parameters().get("from").get(0)) * 1000, parameters.getTz()).getMillis() / 1000L);
            } catch (NumberFormatException e) {
                throw new InvalidParameterValueException("DateTime format not recognized (from): " + queryStringDecoder.parameters().get("from").get(0));
            }
        } else {
            // default to -1d
            parameters.setFrom((System.currentTimeMillis() / 1000L) - 86400);
        }

        // parse until defaulting to -1d
        if (queryStringDecoder.parameters().get("until") != null) {
            try {
                parameters.setUntil(new DateTime(Long.valueOf(queryStringDecoder.parameters().get("until").get(0)) * 1000, parameters.getTz()).getMillis() / 1000L);
            } catch (NumberFormatException e) {
                throw new InvalidParameterValueException("DateTime format not recognized (until): " + queryStringDecoder.parameters().get("until").get(0));
            }
        } else {
            // default to now
            parameters.setUntil(System.currentTimeMillis() / 1000L);
        }

        // parse format defaulting to PNG
        if (queryStringDecoder.parameters().get("format") != null) {
            try {
                parameters.setFormat(Format.valueOf(queryStringDecoder.parameters().get("format").get(0).toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new InvalidParameterValueException("Format not supported: " + queryStringDecoder.parameters().get("format").get(0));
            }
        } else {
            // default to now
            parameters.setFormat(Format.PNG);
        }

        //*************************************************************************************************************
        // Image parameters
        //*************************************************************************************************************
        if (queryStringDecoder.parameters().get("width") != null) {
            try {
                parameters.getImageParameters().setWidth(Integer.valueOf(queryStringDecoder.parameters().get("width").get(0)));
            } catch (NumberFormatException e) {
                throw new InvalidParameterValueException("Width format : " + queryStringDecoder.parameters().get("width").get(0));
            }
        }

        if (queryStringDecoder.parameters().get("height") != null) {
            try {
                parameters.getImageParameters().setHeight(Integer.valueOf(queryStringDecoder.parameters().get("height").get(0)));
            } catch (NumberFormatException e) {
                throw new InvalidParameterValueException("Height format : " + queryStringDecoder.parameters().get("height").get(0));
            }
        }

        if (queryStringDecoder.parameters().get("lineMode") != null) {
            try {
                parameters.getImageParameters().setLineMode(ImageParameters.LineMode.valueOf(queryStringDecoder.parameters().get("lineMode").get(0).toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new InvalidParameterValueException("Unknown line mode : " + queryStringDecoder.parameters().get("lineMode").get(0).toUpperCase());
            }
        }

        if (queryStringDecoder.parameters().get("connectedLimit") != null) {
            try {
                parameters.getImageParameters().setConnectedLimit(Integer.valueOf(queryStringDecoder.parameters().get("connectedLimit").get(0)));
            } catch (NumberFormatException e) {
                throw new InvalidParameterValueException("ConnectedLimit format : " + queryStringDecoder.parameters().get("connectedLimit").get(0));
            }
        }

        return parameters;
    }

}
