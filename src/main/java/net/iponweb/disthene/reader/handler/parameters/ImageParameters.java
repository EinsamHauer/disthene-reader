package net.iponweb.disthene.reader.handler.parameters;

import net.iponweb.disthene.reader.graph.ColorTable;
import net.iponweb.disthene.reader.graph.FontTable;
import net.iponweb.disthene.reader.graph.Graph;
import net.iponweb.disthene.reader.graphite.utils.UnitSystem;

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
    private Color backgroundColor = ColorTable.BLACK;
    private Color foregroundColor = Color.WHITE;

    private Font font = new Font(Font.SANS_SERIF, Font.PLAIN, 10);
    private String fontName = "sans";
    private boolean fontItalic = false;
    private boolean fontBold = false;
    private float fontSize = 10;

    private boolean graphOnly = false;

    private boolean hideLegend = true;
    private boolean hideGrid = false;
    private boolean hideAxes = false;
    private boolean hideYAxis = false;
    private Side yAxisSide = Side.LEFT;
    private String title = "";
    private String verticalTitle = "";
    private String verticalTitleRight = "";
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

    private Color leftColor = null;
    private Color rightColor = null;

    private Double rightWidth = null;
    private Boolean rightDashed = false;
    private Double leftWidth = null;
    private Boolean leftDashed = false;

    private boolean uniqueLegend = false;
    private int minXStep = 1;
    private Double lineWidth = 1.2;

    private boolean drawNullAsZero = false;

    private List<Color> colorList = ColorTable.getColorRotationList();

    private List<Integer> yDivisors = new ArrayList<>();

    private double logBase = 0;

//    private Color majorGridLineColor = new Color(96, 79, 96);
    private Color majorGridLineColor = new Color(114,114,114);
    private Color minorGridLineColor = new Color(52, 52, 52);
    private int minorY = 1;

    private double areaAlpha = 1;

    private Graph.GraphType graphType = Graph.GraphType.LINE;
    private Graph.PieMode pieMode = Graph.PieMode.AVERAGE;
    private Graph.PieLabelsStyle pieLabelsStyle = Graph.PieLabelsStyle.PERCENT;
    private Graph.PieLabelsOrientation pieLabelsOrientation = Graph.PieLabelsOrientation.HORIZONTAL;
    private Double pieLabelsMin = 5.;

    public ImageParameters() {
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

    public String getVerticalTitleRight() {
        return verticalTitleRight;
    }

    public void setVerticalTitleRight(String verticalTitleRight) {
        this.verticalTitleRight = verticalTitleRight;
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

    public Double getRightWidth() {
        return rightWidth;
    }

    public void setRightWidth(Double rightWidth) {
        this.rightWidth = rightWidth;
    }

    public Boolean getRightDashed() {
        return rightDashed;
    }

    public void setRightDashed(Boolean rightDashed) {
        this.rightDashed = rightDashed;
    }

    public Double getLeftWidth() {
        return leftWidth;
    }

    public void setLeftWidth(Double leftWidth) {
        this.leftWidth = leftWidth;
    }

    public Boolean getLeftDashed() {
        return leftDashed;
    }

    public void setLeftDashed(Boolean leftDashed) {
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

    public Double getLineWidth() {
        return lineWidth;
    }

    public void setLineWidth(Double lineWidth) {
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

    public Color getLeftColor() {
        return leftColor;
    }

    public void setLeftColor(Color leftColor) {
        this.leftColor = leftColor;
    }

    public Color getRightColor() {
        return rightColor;
    }

    public void setRightColor(Color rightColor) {
        this.rightColor = rightColor;
    }

    public void setFontName(String fontName) {
        this.fontName = fontName;

        font = FontTable.getFont(fontName, getFontStyle(), fontSize);
    }

    public void setFontItalic(boolean fontItalic) {
        this.fontItalic = fontItalic;

        font = FontTable.getFont(fontName, getFontStyle(), fontSize);
    }

    public void setFontBold(boolean fontBold) {
        this.fontBold = fontBold;

        font = FontTable.getFont(fontName, getFontStyle(), fontSize);
    }

    public void setFontSize(float fontSize) {
        this.fontSize = fontSize;

        font = FontTable.getFont(fontName, getFontStyle(), fontSize);
    }

    private int getFontStyle() {
        int fontStyle = 0;
        if (fontBold) {
            fontStyle = fontStyle | Font.BOLD;
        }
        if (fontItalic) {
            fontStyle = fontStyle | Font.ITALIC;
        }

        return fontStyle;
    }

    public Graph.GraphType getGraphType() {
        return graphType;
    }

    public void setGraphType(Graph.GraphType graphType) {
        this.graphType = graphType;
    }

    public Graph.PieMode getPieMode() {
        return pieMode;
    }

    public void setPieMode(Graph.PieMode pieMode) {
        this.pieMode = pieMode;
    }

    public Graph.PieLabelsStyle getPieLabelsStyle() {
        return pieLabelsStyle;
    }

    public void setPieLabelsStyle(Graph.PieLabelsStyle pieLabelsStyle) {
        this.pieLabelsStyle = pieLabelsStyle;
    }

    public Graph.PieLabelsOrientation getPieLabelsOrientation() {
        return pieLabelsOrientation;
    }

    public void setPieLabelsOrientation(Graph.PieLabelsOrientation pieLabelsOrientation) {
        this.pieLabelsOrientation = pieLabelsOrientation;
    }

    public Double getPieLabelsMin() {
        return pieLabelsMin;
    }

    public void setPieLabelsMin(Double pieLabelsMin) {
        this.pieLabelsMin = pieLabelsMin;
    }

    public enum Side {
        LEFT, RIGHT
    }

    public enum LineMode {
        STAIRCASE, SLOPE, CONNECTED
    }

    public enum AreaMode {
        NONE, FIRST, ALL, STACKED
    }

}
