package net.iponweb.disthene.reader.handler.parameters;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * @author Andrei Ivanov
 */
public class ImageParameters {

    private int width = 600;
    private int height = 300;
    private int margin = 10;
    private Color backgroundColor = Color.BLACK;
    private Color foregroundColor = Color.WHITE;

    private Font font = new Font(Font.SANS_SERIF, Font.PLAIN, 10);
    private boolean graphOnly = false;

    private boolean hideLegend = false;
    private boolean hideGrid = false;
    private boolean hideAxes = false;
    private boolean hideYAxis = false;
    private Side yAxisSide = Side.LEFT;
    private String title = "";
    private String verticalTitle = "";
    private double yMin = Double.NEGATIVE_INFINITY;
    private double yMax = Double.POSITIVE_INFINITY;
    private double yStep = Double.POSITIVE_INFINITY;
    private double yMinLeft = Double.NEGATIVE_INFINITY;
    private double yMaxLeft = Double.POSITIVE_INFINITY;
    private double yMinRight = Double.NEGATIVE_INFINITY;
    private double yMaxRight = Double.POSITIVE_INFINITY;
    private double yStepLeft = Double.POSITIVE_INFINITY;
    private double yStepRight = Double.POSITIVE_INFINITY;
    private UnitSystem yUnitSystem = UnitSystem.SI;
    private LineMode lineMode = LineMode.SLOPE;
    private int connectedLimit = Integer.MAX_VALUE;
    private AreaMode areaMode = AreaMode.NONE;

    private int rightWidth = 1;
    private int rightDashed = 0;
    private int leftWidth = 1;
    private int leftDashed = 0;

    private boolean uniqueLegend = false;
    private int minXStep = 1;
    private double lineWidth = 1.2;

    private boolean drawNullAsZero = false;

    private List<Color> colorList = new ArrayList<>();

    private List<Integer> yDivisors = new ArrayList<>();

    private double logBase = 0;

    private Color majorGridLineColor = new Color(96, 79, 96);
    private Color minorGridLineColor = new Color(52, 52, 52);
    private int minorY = 1;

    private double areaAlpha = -1;

    public ImageParameters() {
        // init colors
//        colorList.add(Color.BLUE);
        colorList.add(new Color(100, 100, 255)); // blue
//        colorList.add(Color.GREEN);
        colorList.add(new Color(0, 200, 0)); // green
        colorList.add(Color.RED);
        colorList.add(new Color(200, 100, 255)); // purple
        colorList.add(new Color(150, 100, 50)); // brown
        colorList.add(Color.YELLOW);
        colorList.add(new Color(0, 150, 150)); // aqua
        colorList.add(new Color(175, 175, 175)); // grey
        colorList.add(new Color(255, 0, 255)); // magenta
        colorList.add(new Color(255, 100, 100)); // pink
        colorList.add(new Color(200, 200, 0)); // gold
        colorList.add(new Color(200, 150, 200)); // rose


        yDivisors.add(4);
        yDivisors.add(5);
        yDivisors.add(6);
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getMargin() {
        return margin;
    }

    public void setMargin(int margin) {
        this.margin = margin;
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public Font getFont() {
        return font;
    }

    public void setFont(Font font) {
        this.font = font;
    }

    public boolean isGraphOnly() {
        return graphOnly;
    }

    public void setGraphOnly(boolean graphOnly) {
        this.graphOnly = graphOnly;
    }

    public boolean isHideLegend() {
        return hideLegend;
    }

    public void setHideLegend(boolean hideLegend) {
        this.hideLegend = hideLegend;
    }

    public boolean isHideGrid() {
        return hideGrid;
    }

    public void setHideGrid(boolean hideGrid) {
        this.hideGrid = hideGrid;
    }

    public boolean isHideAxes() {
        return hideAxes;
    }

    public void setHideAxes(boolean hideAxes) {
        this.hideAxes = hideAxes;
    }

    public boolean isHideYAxis() {
        return hideYAxis;
    }

    public void setHideYAxis(boolean hideYAxis) {
        this.hideYAxis = hideYAxis;
    }

    public Side getyAxisSide() {
        return yAxisSide;
    }

    public void setyAxisSide(Side yAxisSide) {
        this.yAxisSide = yAxisSide;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getVerticalTitle() {
        return verticalTitle;
    }

    public void setVerticalTitle(String verticalTitle) {
        this.verticalTitle = verticalTitle;
    }

    public double getyMin() {
        return yMin;
    }

    public void setyMin(double yMin) {
        this.yMin = yMin;
    }

    public double getyMax() {
        return yMax;
    }

    public void setyMax(double yMax) {
        this.yMax = yMax;
    }

    public UnitSystem getyUnitSystem() {
        return yUnitSystem;
    }

    public void setyUnitSystem(UnitSystem yUnitSystem) {
        this.yUnitSystem = yUnitSystem;
    }

    public LineMode getLineMode() {
        return lineMode;
    }

    public void setLineMode(LineMode lineMode) {
        this.lineMode = lineMode;
    }

    public int getConnectedLimit() {
        return connectedLimit;
    }

    public void setConnectedLimit(int connectedLimit) {
        this.connectedLimit = connectedLimit;
    }

    public AreaMode getAreaMode() {
        return areaMode;
    }

    public void setAreaMode(AreaMode areaMode) {
        this.areaMode = areaMode;
    }

    public int getRightWidth() {
        return rightWidth;
    }

    public void setRightWidth(int rightWidth) {
        this.rightWidth = rightWidth;
    }

    public int getRightDashed() {
        return rightDashed;
    }

    public void setRightDashed(int rightDashed) {
        this.rightDashed = rightDashed;
    }

    public int getLeftWidth() {
        return leftWidth;
    }

    public void setLeftWidth(int leftWidth) {
        this.leftWidth = leftWidth;
    }

    public int getLeftDashed() {
        return leftDashed;
    }

    public void setLeftDashed(int leftDashed) {
        this.leftDashed = leftDashed;
    }

    public Color getForegroundColor() {
        return foregroundColor;
    }

    public void setForegroundColor(Color foregroundColor) {
        this.foregroundColor = foregroundColor;
    }

    public List<Color> getColorList() {
        return colorList;
    }

    public void setColorList(List<Color> colorList) {
        this.colorList = colorList;
    }

    public boolean isUniqueLegend() {
        return uniqueLegend;
    }

    public void setUniqueLegend(boolean uniqueLegend) {
        this.uniqueLegend = uniqueLegend;
    }

    public int getMinXStep() {
        return minXStep;
    }

    public void setMinXStep(int minXStep) {
        this.minXStep = minXStep;
    }

    public double getLineWidth() {
        return lineWidth;
    }

    public void setLineWidth(double lineWidth) {
        this.lineWidth = lineWidth;
    }

    public boolean isDrawNullAsZero() {
        return drawNullAsZero;
    }

    public void setDrawNullAsZero(boolean drawNullAsZero) {
        this.drawNullAsZero = drawNullAsZero;
    }

    public double getLogBase() {
        return logBase;
    }

    public void setLogBase(double logBase) {
        this.logBase = logBase;
    }

    public double getyStep() {
        return yStep;
    }

    public void setyStep(double yStep) {
        this.yStep = yStep;
    }

    public List<Integer> getyDivisors() {
        return yDivisors;
    }

    public void setyDivisors(List<Integer> yDivisors) {
        this.yDivisors = yDivisors;
    }

    public double getyMinLeft() {
        return yMinLeft;
    }

    public void setyMinLeft(double yMinLeft) {
        this.yMinLeft = yMinLeft;
    }

    public double getyMaxLeft() {
        return yMaxLeft;
    }

    public void setyMaxLeft(double yMaxLeft) {
        this.yMaxLeft = yMaxLeft;
    }

    public double getyMinRight() {
        return yMinRight;
    }

    public void setyMinRight(double yMinRight) {
        this.yMinRight = yMinRight;
    }

    public double getyMaxRight() {
        return yMaxRight;
    }

    public void setyMaxRight(double yMaxRight) {
        this.yMaxRight = yMaxRight;
    }

    public double getyStepLeft() {
        return yStepLeft;
    }

    public void setyStepLeft(double yStepLeft) {
        this.yStepLeft = yStepLeft;
    }

    public double getyStepRight() {
        return yStepRight;
    }

    public void setyStepRight(double yStepRight) {
        this.yStepRight = yStepRight;
    }

    public Color getMajorGridLineColor() {
        return majorGridLineColor;
    }

    public void setMajorGridLineColor(Color majorGridLineColor) {
        this.majorGridLineColor = majorGridLineColor;
    }

    public Color getMinorGridLineColor() {
        return minorGridLineColor;
    }

    public void setMinorGridLineColor(Color minorGridLineColor) {
        this.minorGridLineColor = minorGridLineColor;
    }

    public int getMinorY() {
        return minorY;
    }

    public void setMinorY(int minorY) {
        this.minorY = minorY;
    }

    public double getAreaAlpha() {
        return areaAlpha;
    }

    public void setAreaAlpha(double areaAlpha) {
        this.areaAlpha = areaAlpha;
    }



    public enum Side {
        LEFT, RIGHT;
    }

    public static class Unit {
        private String prefix;
        private Double value;

        public Unit(String prefix, Double value) {
            this.prefix = prefix;
            this.value = value;
        }

        public String getPrefix() {
            return prefix;
        }

        public Double getValue() {
            return value;
        }
    }

    public enum UnitSystem {
        BINARY("binary"),
        SI("si"),
        NONE("");

        private List<Unit> prefixes = new ArrayList<>();



        UnitSystem(String system) {
            switch (system) {
                case "binary":
                    prefixes.add(new Unit("Pi", Math.pow(1024.0, 5)));
                    prefixes.add(new Unit("Ti", Math.pow(1024.0, 4)));
                    prefixes.add(new Unit("Gi", Math.pow(1024.0, 3)));
                    prefixes.add(new Unit("Mi", Math.pow(1024.0, 2)));
                    prefixes.add(new Unit("Ki", 1024.0));
                    break;
                case "si":
                    prefixes.add(new Unit("P", Math.pow(1000.0, 5)));
                    prefixes.add(new Unit("T", Math.pow(1000.0, 4)));
                    prefixes.add(new Unit("G", Math.pow(1000.0, 3)));
                    prefixes.add(new Unit("M", Math.pow(1000.0, 2)));
                    prefixes.add(new Unit("K", 1000.0));
                    break;
                default:
                    break;
            }
        }

        public List<Unit> getPrefixes() {
            return prefixes;
        }
    }

    public enum LineMode {
        STAIRCASE, SLOPE, CONNECTED;
    }

    public enum AreaMode {
        NONE, FIRST, ALL, STACKED;
    }

}
