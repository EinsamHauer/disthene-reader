package net.iponweb.disthene.reader.handler.parameters;

import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import net.iponweb.disthene.reader.exceptions.InvalidParameterValueException;
import net.iponweb.disthene.reader.exceptions.ParameterParsingException;
import net.iponweb.disthene.reader.format.Format;
import net.iponweb.disthene.reader.graph.ColorTable;
import net.iponweb.disthene.reader.graph.Graph;
import net.iponweb.disthene.reader.graphite.utils.UnitSystem;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.awt.*;
import java.lang.String;
import java.lang.Integer;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Andrei Ivanov
 */
public class RenderParameters {
    final static Logger logger = Logger.getLogger(RenderParameters.class);

    private String tenant;
    private List<String> targets = new ArrayList<>();
    private Long from;
    private Long until;
    private Format format;
    private DateTimeZone tz;
    private int maxDataPoints = Integer.MAX_VALUE;

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

    public int getMaxDataPoints() {
        return maxDataPoints;
    }

    public void setMaxDataPoints(int maxDataPoints) {
        this.maxDataPoints = maxDataPoints;
    }

    public void setImageParameters(ImageParameters imageParameters) {
        this.imageParameters = imageParameters;
    }

    public ImageParameters getImageParameters() {
        return imageParameters;
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
                String passedFromValue = queryStringDecoder.parameters().get("from").get(0);

                if (passedFromValue.matches("^.*[a-zA-Z]")) {
                    // a relative time character was found
                    // split into number, unit

                    passedFromValue = passedFromValue.replace("-", ""); // remove signed value if present

                    String fromValue = "";
                    String fromUnit = "";
                    String v = "";
                    Long unitValue = 0L;

                    for (int i = 0 ; i < passedFromValue.length() - 1 ; i++) {
                        v = String.valueOf(passedFromValue.charAt(i));

                        if (v.matches("[a-zA-Z]")) {
                            fromUnit = fromUnit.concat(v);
                        } else {
                            fromValue = fromValue.concat(v);
                        }
                    }
                    // calc unit value
                    if (fromUnit.startsWith("s")) {
                        unitValue = 1L;
                    } else if (fromUnit.startsWith("min")) {
                        unitValue = 60L;
                    } else if (fromUnit.startsWith("h")) {
                        unitValue = 3600L;
                    } else if (fromUnit.startsWith("d")) {
                        unitValue = 86400L;
                    } else if (fromUnit.startsWith("w")) {
                        unitValue = 604800L;
                    } else if (fromUnit.startsWith("mon")) {
                        unitValue = 18144000L;
                    } else if (fromUnit.startsWith("y")) {
                        unitValue = 31536000L;
                    } else {
                        unitValue = 60L;
                    }
                    // calc offset as (now) - (number * unit value)
                    parameters.setFrom((System.currentTimeMillis() / 1000L) - (Long.valueOf(fromValue) * unitValue));
                } else {
                    // no only numeric time
                    parameters.setFrom(new DateTime(Long.valueOf(queryStringDecoder.parameters().get("from").get(0)) * 1000, parameters.getTz()).getMillis() / 1000L);
                }
            } catch (NumberFormatException e) {
                throw new InvalidParameterValueException("DateTime format not recognized (from): " + queryStringDecoder.parameters().get("from").get(0));
            }
        } else {
            // default to -1d
            parameters.setFrom((System.currentTimeMillis() / 1000L) - 86400);
        }

        // parse until
        if (queryStringDecoder.parameters().get("until") != null) {
            try {
               String passedUntilValue = queryStringDecoder.parameters().get("until").get(0);

                if (passedUntilValue.matches("^.*[a-zA-Z]")) {
                    // remove signed value if present
                    passedUntilValue = passedUntilValue.replace("-", "");

                    String untilValue = "";
                    String untilUnit = "";
                    String v = "";
                    Long unitValue = 0L;

                    for (int i = 0 ; i < passedUntilValue.length() - 1 ; i++) {
                        v = String.valueOf(passedUntilValue.charAt(i));

                        if (v.matches("[a-zA-Z]")) {
                            untilUnit = untilUnit.concat(v);
                        } else {
                            untilValue = untilValue.concat(v);
                        }
                    }
                    // calc unit value
                    if (untilUnit.startsWith("s")) {
                        unitValue = 1L;
                    } else if (untilUnit.startsWith("min")) {
                        unitValue = 60L;
                    } else if (untilUnit.startsWith("h")) {
                        unitValue = 3600L;
                    } else if (untilUnit.startsWith("d")) {
                        unitValue = 86400L;
                    } else if (untilUnit.startsWith("w")) {
                        unitValue = 604800L;
                    } else if (untilUnit.startsWith("mon")) {
                        unitValue = 18144000L;
                    } else if (untilUnit.startsWith("y")) {
                        unitValue = 31536000L;
                    } else {
                        unitValue = 60L;
                    }
                    // calc offset as (now) - (number * unit value)
                    parameters.setUntil((System.currentTimeMillis() / 1000L) - (Long.valueOf(untilValue) * unitValue));
                } else {
                    parameters.setUntil(new DateTime(Long.valueOf(queryStringDecoder.parameters().get("until").get(0)) * 1000, parameters.getTz()).getMillis() / 1000L);
                }
            } catch (NumberFormatException e) {
                throw new InvalidParameterValueException("DateTime format not recognized (until): " + queryStringDecoder.parameters().get("until").get(0));
            }
        } else {
            // default to now
            parameters.setUntil(System.currentTimeMillis() / 1000L);
        }

        // Prohibiting "from greater than until"
        if (parameters.getFrom() > parameters.getUntil()) {
            parameters.setFrom(parameters.getUntil());
        }

        // Prohibiting "until in the future"
        if (parameters.getUntil() > (System.currentTimeMillis() / 1000L)) {
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

        if (queryStringDecoder.parameters().get("maxDataPoints") != null) {
            try {
                parameters.setMaxDataPoints(Integer.valueOf(queryStringDecoder.parameters().get("maxDataPoints").get(0)));
            } catch (NumberFormatException e) {
                throw new InvalidParameterValueException("fontSize format : " + queryStringDecoder.parameters().get("fontSize").get(0));
            }
        }


        //*************************************************************************************************************
        // Image parameters
        //*************************************************************************************************************
        if (queryStringDecoder.parameters().get("areaAlpha") != null) {
            try {
                Float areaAlpha = Float.valueOf(queryStringDecoder.parameters().get("areaAlpha").get(0));
                if (areaAlpha < 0.) areaAlpha = 0f;
                if (areaAlpha > 1.) areaAlpha = 1f;
                parameters.getImageParameters().setAreaAlpha(areaAlpha);
            } catch (NumberFormatException e) {
                throw new InvalidParameterValueException("AreaAlpha format : " + queryStringDecoder.parameters().get("areaAlpha").get(0));
            }
        }

        if (queryStringDecoder.parameters().get("areaMode") != null) {
            try {
                parameters.getImageParameters().setAreaMode(ImageParameters.AreaMode.valueOf(queryStringDecoder.parameters().get("areaMode").get(0).toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new InvalidParameterValueException("Unknown area mode : " + queryStringDecoder.parameters().get("areaMode").get(0).toUpperCase());
            }
        }

        if (queryStringDecoder.parameters().get("bgcolor") != null) {
            Color color = ColorTable.getColorByName(queryStringDecoder.parameters().get("bgcolor").get(0).replaceAll("^\"|\"$", ""));
            if (color != null) parameters.getImageParameters().setBackgroundColor(color);
        }

        if (queryStringDecoder.parameters().get("colorList") != null) {
            String colorsString = queryStringDecoder.parameters().get("colorList").get(0).replaceAll("^\"|\"$", "");
            String[] split = colorsString.split(",");
            List<Color> colors = new ArrayList<>();
            for (String c : split) {
                Color color = ColorTable.getColorByName(c);
                if (color != null) colors.add(color);
            }

            if (colors.size() > 0) {
                parameters.getImageParameters().setColorList(colors);
            }
        }

        if (queryStringDecoder.parameters().get("drawNullAsZero") != null) {
            parameters.getImageParameters().setDrawNullAsZero(Boolean.parseBoolean(queryStringDecoder.parameters().get("drawNullAsZero").get(0)));
        }

        if (queryStringDecoder.parameters().get("fgcolor") != null) {
            Color color = ColorTable.getColorByName(queryStringDecoder.parameters().get("fgcolor").get(0).replaceAll("^\"|\"$", ""));
            if (color != null) parameters.getImageParameters().setForegroundColor(color);
        }

        if (queryStringDecoder.parameters().get("fontBold") != null) {
            parameters.getImageParameters().setFontBold(Boolean.parseBoolean(queryStringDecoder.parameters().get("fontBold").get(0)));
        }

        if (queryStringDecoder.parameters().get("fontItalic") != null) {
            parameters.getImageParameters().setFontItalic(Boolean.parseBoolean(queryStringDecoder.parameters().get("fontItalic").get(0)));
        }

        if (queryStringDecoder.parameters().get("fontName") != null) {
            parameters.getImageParameters().setFontName(queryStringDecoder.parameters().get("fontName").get(0).toLowerCase());
        }

        if (queryStringDecoder.parameters().get("fontSize") != null) {
            try {
                parameters.getImageParameters().setFontSize(Float.valueOf(queryStringDecoder.parameters().get("fontSize").get(0)));
            } catch (NumberFormatException e) {
                throw new InvalidParameterValueException("fontSize format : " + queryStringDecoder.parameters().get("fontSize").get(0));
            }
        }

        if (queryStringDecoder.parameters().get("graphOnly") != null) {
            parameters.getImageParameters().setGraphOnly(Boolean.parseBoolean(queryStringDecoder.parameters().get("graphOnly").get(0)));
        }

        if (queryStringDecoder.parameters().get("graphType") != null) {
            try {
                parameters.getImageParameters().setGraphType(Graph.GraphType.valueOf(queryStringDecoder.parameters().get("graphType").get(0).toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new InvalidParameterValueException("Unknown graphType : " + queryStringDecoder.parameters().get("graphType").get(0).toUpperCase());
            }
        }

        if (queryStringDecoder.parameters().get("hideLegend") != null) {
            boolean hideLegend = Boolean.parseBoolean(queryStringDecoder.parameters().get("hideLegend").get(0));
            if (hideLegend) {
                parameters.getImageParameters().setHideLegendCompletely(true);
            } else {
                parameters.getImageParameters().setHideLegend(false);
            }
        }

        if (queryStringDecoder.parameters().get("hideAxes") != null) {
            parameters.getImageParameters().setHideAxes(Boolean.parseBoolean(queryStringDecoder.parameters().get("hideAxes").get(0)));
        }

        if (queryStringDecoder.parameters().get("hideYAxes") != null) {
            parameters.getImageParameters().setHideYAxis(Boolean.parseBoolean(queryStringDecoder.parameters().get("hideYAxes").get(0)));
        }

        if (queryStringDecoder.parameters().get("hideGrid") != null) {
            parameters.getImageParameters().setHideGrid(Boolean.parseBoolean(queryStringDecoder.parameters().get("hideGrid").get(0)));
        }

        if (queryStringDecoder.parameters().get("height") != null) {
            try {
                parameters.getImageParameters().setHeight(Integer.valueOf(queryStringDecoder.parameters().get("height").get(0)));
            } catch (NumberFormatException e) {
                throw new InvalidParameterValueException("Height format : " + queryStringDecoder.parameters().get("height").get(0));
            }
        }

        if (queryStringDecoder.parameters().get("leftColor") != null) {
            Color color = ColorTable.getColorByName(queryStringDecoder.parameters().get("leftColor").get(0).replaceAll("^\"|\"$", ""));
            if (color != null) parameters.getImageParameters().setLeftColor(color);
        }

        if (queryStringDecoder.parameters().get("leftDashed") != null) {
            parameters.getImageParameters().setLeftDashed(Boolean.parseBoolean(queryStringDecoder.parameters().get("leftDashed").get(0)));
        }

        if (queryStringDecoder.parameters().get("leftWidth") != null) {
            try {
                parameters.getImageParameters().setLeftWidth(Double.valueOf(queryStringDecoder.parameters().get("leftWidth").get(0)));
            } catch (NumberFormatException e) {
                throw new InvalidParameterValueException("leftWidth format : " + queryStringDecoder.parameters().get("leftWidth").get(0));
            }
        }

        if (queryStringDecoder.parameters().get("lineMode") != null) {
            try {
                parameters.getImageParameters().setLineMode(ImageParameters.LineMode.valueOf(queryStringDecoder.parameters().get("lineMode").get(0).toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new InvalidParameterValueException("Unknown line mode : " + queryStringDecoder.parameters().get("lineMode").get(0).toUpperCase());
            }
        }

        if (queryStringDecoder.parameters().get("lineWidth") != null) {
            try {
                parameters.getImageParameters().setLineWidth(Double.valueOf(queryStringDecoder.parameters().get("lineWidth").get(0)));
            } catch (NumberFormatException e) {
                throw new InvalidParameterValueException("lineWidth format : " + queryStringDecoder.parameters().get("lineWidth").get(0));
            }
        }

        if (queryStringDecoder.parameters().get("logBase") != null) {
            try {
                Double logBase = Double.valueOf(queryStringDecoder.parameters().get("logBase").get(0));
                if (logBase > 0 && logBase != 1) {
                    parameters.getImageParameters().setLogBase(logBase);
                }
            } catch (NumberFormatException e) {
                throw new InvalidParameterValueException("lineWidth format : " + queryStringDecoder.parameters().get("lineWidth").get(0));
            }
        }

        if (queryStringDecoder.parameters().get("majorGridLineColor") != null) {
            Color color = ColorTable.getColorByName(queryStringDecoder.parameters().get("majorGridLineColor").get(0).replaceAll("^\"|\"$", ""));
            if (color != null) parameters.getImageParameters().setMajorGridLineColor(color);
        }

        if (queryStringDecoder.parameters().get("margin") != null) {
            try {
                Double margin = Double.valueOf(queryStringDecoder.parameters().get("margin").get(0));
                if (margin > 0) {
                    parameters.getImageParameters().setMargin(margin.intValue());
                }
            } catch (NumberFormatException e) {
                throw new InvalidParameterValueException("margin format : " + queryStringDecoder.parameters().get("margin").get(0));
            }
        }

        if (queryStringDecoder.parameters().get("minorGridLineColor") != null) {
            Color color = ColorTable.getColorByName(queryStringDecoder.parameters().get("minorGridLineColor").get(0).replaceAll("^\"|\"$", ""));
            if (color != null) parameters.getImageParameters().setMinorGridLineColor(color);
        }

        if (queryStringDecoder.parameters().get("minorY") != null) {
            try {
                Double minorY = Double.valueOf(queryStringDecoder.parameters().get("minorY").get(0));
                if (minorY >= 0) {
                    parameters.getImageParameters().setMinorY(minorY.intValue());
                }
            } catch (NumberFormatException e) {
                throw new InvalidParameterValueException("minorY format : " + queryStringDecoder.parameters().get("minorY").get(0));
            }
        }

        if (queryStringDecoder.parameters().get("minXStep") != null) {
            try {
                Double minXStep = Double.valueOf(queryStringDecoder.parameters().get("minXStep").get(0));
                if (minXStep >= 0) {
                    parameters.getImageParameters().setMinXStep(minXStep.intValue());
                }
            } catch (NumberFormatException e) {
                throw new InvalidParameterValueException("minXStep format : " + queryStringDecoder.parameters().get("minXStep").get(0));
            }
        }

        if (queryStringDecoder.parameters().get("pieMode") != null) {
            try {
                parameters.getImageParameters().setPieMode(Graph.PieMode.valueOf(queryStringDecoder.parameters().get("pieMode").get(0).toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new InvalidParameterValueException("Unknown pieMode : " + queryStringDecoder.parameters().get("pieMode").get(0).toUpperCase());
            }
        }

        if (queryStringDecoder.parameters().get("pieLabels") != null) {
            try {
                parameters.getImageParameters().setPieLabelsOrientation(Graph.PieLabelsOrientation.valueOf(queryStringDecoder.parameters().get("pieLabels").get(0).toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new InvalidParameterValueException("Unknown pieLabels : " + queryStringDecoder.parameters().get("pieLabels").get(0).toUpperCase());
            }
        }

        if (queryStringDecoder.parameters().get("rightColor") != null) {
            Color color = ColorTable.getColorByName(queryStringDecoder.parameters().get("rightColor").get(0).replaceAll("^\"|\"$", ""));
            if (color != null) parameters.getImageParameters().setRightColor(color);
        }

        if (queryStringDecoder.parameters().get("rightDashed") != null) {
            parameters.getImageParameters().setRightDashed(Boolean.parseBoolean(queryStringDecoder.parameters().get("rightDashed").get(0)));
        }

        if (queryStringDecoder.parameters().get("rightWidth") != null) {
            try {
                parameters.getImageParameters().setRightWidth(Double.valueOf(queryStringDecoder.parameters().get("rightWidth").get(0)));
            } catch (NumberFormatException e) {
                throw new InvalidParameterValueException("rightWidth format : " + queryStringDecoder.parameters().get("rightWidth").get(0));
            }
        }

        if (queryStringDecoder.parameters().get("title") != null) {
            parameters.getImageParameters().setTitle(queryStringDecoder.parameters().get("title").get(0).replaceAll("^\"|\"$", ""));
        }

        if (queryStringDecoder.parameters().get("uniqueLegend") != null) {
            parameters.getImageParameters().setUniqueLegend(Boolean.parseBoolean(queryStringDecoder.parameters().get("uniqueLegend").get(0)));
        }

        if (queryStringDecoder.parameters().get("valueLabels") != null) {
            try {
                parameters.getImageParameters().setPieLabelsStyle(Graph.PieLabelsStyle.valueOf(queryStringDecoder.parameters().get("valueLabels").get(0).toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new InvalidParameterValueException("Unknown valueLabels : " + queryStringDecoder.parameters().get("valueLabels").get(0).toUpperCase());
            }
        }

        if (queryStringDecoder.parameters().get("valueLabelsMin") != null) {
            try {
                parameters.getImageParameters().setPieLabelsMin(Double.valueOf(queryStringDecoder.parameters().get("valueLabelsMin").get(0)));
            } catch (NumberFormatException e) {
                throw new InvalidParameterValueException("valueLabelsMin format : " + queryStringDecoder.parameters().get("valueLabelsMin").get(0));
            }
        }

        if (queryStringDecoder.parameters().get("vtitle") != null) {
            parameters.getImageParameters().setVerticalTitle(queryStringDecoder.parameters().get("vtitle").get(0).replaceAll("^\"|\"$", ""));
        }

        if (queryStringDecoder.parameters().get("vtitleRight") != null) {
            parameters.getImageParameters().setVerticalTitleRight(queryStringDecoder.parameters().get("vtitleRight").get(0).replaceAll("^\"|\"$", ""));
        }

        if (queryStringDecoder.parameters().get("width") != null) {
            try {
                parameters.getImageParameters().setWidth(Integer.valueOf(queryStringDecoder.parameters().get("width").get(0)));
            } catch (NumberFormatException e) {
                throw new InvalidParameterValueException("Width format : " + queryStringDecoder.parameters().get("width").get(0));
            }
        }

        if (queryStringDecoder.parameters().get("yAxisSide") != null) {
            try {
                parameters.getImageParameters().setyAxisSide(ImageParameters.Side.valueOf(queryStringDecoder.parameters().get("yAxisSide").get(0).toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new InvalidParameterValueException("Unknown y axis side : " + queryStringDecoder.parameters().get("yAxisSide").get(0).toUpperCase());
            }
        }

        if (queryStringDecoder.parameters().get("yMax") != null) {
            try {
                parameters.getImageParameters().setyMax(Double.valueOf(queryStringDecoder.parameters().get("yMax").get(0)));
            } catch (NumberFormatException e) {
                throw new InvalidParameterValueException("yMax format : " + queryStringDecoder.parameters().get("yMax").get(0));
            }
        }

        if (queryStringDecoder.parameters().get("yMaxLeft") != null) {
            try {
                parameters.getImageParameters().setyMaxLeft(Double.valueOf(queryStringDecoder.parameters().get("yMaxLeft").get(0)));
            } catch (NumberFormatException e) {
                throw new InvalidParameterValueException("yMaxLeft format : " + queryStringDecoder.parameters().get("yMaxLeft").get(0));
            }
        }

        if (queryStringDecoder.parameters().get("yMaxRight") != null) {
            try {
                parameters.getImageParameters().setyMaxRight(Double.valueOf(queryStringDecoder.parameters().get("yMaxRight").get(0)));
            } catch (NumberFormatException e) {
                throw new InvalidParameterValueException("yMaxRight format : " + queryStringDecoder.parameters().get("yMaxRight").get(0));
            }
        }

        if (queryStringDecoder.parameters().get("yMin") != null) {
            try {
                parameters.getImageParameters().setyMin(Double.valueOf(queryStringDecoder.parameters().get("yMin").get(0)));
            } catch (NumberFormatException e) {
                throw new InvalidParameterValueException("yMin format : " + queryStringDecoder.parameters().get("yMin").get(0));
            }
        }

        if (queryStringDecoder.parameters().get("yMinLeft") != null) {
            try {
                parameters.getImageParameters().setyMinLeft(Double.valueOf(queryStringDecoder.parameters().get("yMinLeft").get(0)));
            } catch (NumberFormatException e) {
                throw new InvalidParameterValueException("yMinLeft format : " + queryStringDecoder.parameters().get("yMinLeft").get(0));
            }
        }

        if (queryStringDecoder.parameters().get("yMinRight") != null) {
            try {
                parameters.getImageParameters().setyMinRight(Double.valueOf(queryStringDecoder.parameters().get("yMinRight").get(0)));
            } catch (NumberFormatException e) {
                throw new InvalidParameterValueException("yMinRight format : " + queryStringDecoder.parameters().get("yMinRight").get(0));
            }
        }

        if (queryStringDecoder.parameters().get("yStep") != null) {
            try {
                parameters.getImageParameters().setyStep(Double.valueOf(queryStringDecoder.parameters().get("yStep").get(0)));
            } catch (NumberFormatException e) {
                throw new InvalidParameterValueException("yStep format : " + queryStringDecoder.parameters().get("yStep").get(0));
            }
        }

        if (queryStringDecoder.parameters().get("yStepLeft") != null) {
            try {
                parameters.getImageParameters().setyStepLeft(Double.valueOf(queryStringDecoder.parameters().get("yStepLeft").get(0)));
            } catch (NumberFormatException e) {
                throw new InvalidParameterValueException("yStepLeft format : " + queryStringDecoder.parameters().get("yStepLeft").get(0));
            }
        }

        if (queryStringDecoder.parameters().get("yStepRight") != null) {
            try {
                parameters.getImageParameters().setyStepRight(Double.valueOf(queryStringDecoder.parameters().get("yStepRight").get(0)));
            } catch (NumberFormatException e) {
                throw new InvalidParameterValueException("yStepRight format : " + queryStringDecoder.parameters().get("yStepRight").get(0));
            }
        }

        if (queryStringDecoder.parameters().get("yUnitSystem") != null) {
            try {
                parameters.getImageParameters().setyUnitSystem(UnitSystem.valueOf(queryStringDecoder.parameters().get("yUnitSystem").get(0).toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new InvalidParameterValueException("Unknown yUnitSystem : " + queryStringDecoder.parameters().get("yUnitSystem").get(0).toUpperCase());
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
