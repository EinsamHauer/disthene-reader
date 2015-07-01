package net.iponweb.disthene.reader.handler.parameters;

import java.awt.*;
import java.util.ArrayList;
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
    private double yMin;
    private double yMax;
    private UnitSystem yUnitSystem = UnitSystem.SI;
    private LineMode lineMode = LineMode.SLOPE;
    private int connectedLimit = Integer.MAX_VALUE;
    private AreaMode areaMode = AreaMode.STACKED;

    private int rightWidth = 1;
    private int rightDashed = 0;
    private int leftWidth = 1;
    private int leftDashed = 0;

    private boolean uniqueLegend = false;
    private int minXStep = 1;
    private double lineWidth = 1.2;

    private boolean drawNullAsZero = false;

    private List<Color> colorList = new ArrayList<>();

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

    public enum Side {
        LEFT, RIGHT;
    }

    public enum UnitSystem {
        BINARY, SI, NONE;

    }

    public enum LineMode {
        STAIRCASE, SLOPE, CONNECTED;
    }

    public enum AreaMode {
        NONE, FIRST, ALL, STACKED;
    }

}
