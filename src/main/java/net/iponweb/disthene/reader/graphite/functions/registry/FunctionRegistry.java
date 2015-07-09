package net.iponweb.disthene.reader.graphite.functions.registry;

import net.iponweb.disthene.reader.exceptions.InvalidFunctionException;
import net.iponweb.disthene.reader.graphite.functions.TimeShiftFunction;
import net.iponweb.disthene.reader.graphite.functions.*;

import java.awt.*;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Andrei Ivanov
 */
public class FunctionRegistry {

    private static final Map<String, Class<? extends DistheneFunction>> registry = new HashMap<>();

    static {
        registry.put("absolute", AbsoluteFunction.class);
        registry.put("alias", AliasFunction.class);
        registry.put("aliasByNode", AliasByNodeFunction.class);
        registry.put("aliasByMetric", AliasByMetricFunction.class);
        registry.put("alpha", AlphaFunction.class);
        registry.put("asPercent", AsPercentFunction.class);
        registry.put("averageOutsidePercentile", AverageOutsidePercentileFunction.class);
        registry.put("pct", AsPercentFunction.class);
        registry.put("averageAbove", AverageAboveFunction.class);
        registry.put("averageBelow", AverageBelowFunction.class);
        registry.put("averageSeries", AverageSeriesFunction.class);
        registry.put("avg", AverageSeriesFunction.class);
        registry.put("color", ColorFunction.class);
        registry.put("countSeries", CountSeriesFunction.class);
        registry.put("currentAbove", CurrentAboveFunction.class);
        registry.put("currentBelow", CurrentBelowFunction.class);
        registry.put("dashed", DashedFunction.class);
        registry.put("derivative", DerivativeFunction.class);
        registry.put("diffSeries", DiffSeriesFunction.class);
        registry.put("divideSeries", DivideSeriesFunction.class);
        registry.put("highestAverage", HighestAverageFunction.class);
        registry.put("highestCurrent", HighestCurrentFunction.class);
        registry.put("highestMax", HighestMaxFunction.class);
        registry.put("integral", IntegralFunction.class);
        registry.put("invert", InvertFunction.class);
        registry.put("limit", LimitFunction.class);
        registry.put("logarithm", LogarithmFunction.class);
        registry.put("log", LogarithmFunction.class);
        registry.put("lowestAverage", LowestAverageFunction.class);
        registry.put("lowestCurrent", LowestCurrentFunction.class);
        registry.put("maximumAbove", MaximumAboveFunction.class);
        registry.put("maximumBelow", MaximumBelowFunction.class);
        registry.put("minimumAbove", MinimumAboveFunction.class);
        registry.put("minimumBelow", MinimumBelowFunction.class);
        registry.put("maxSeries", MaxSeriesFunction.class);
        registry.put("minSeries", MinSeriesFunction.class);
        registry.put("mostDeviant", MostDeviantFunction.class);
        registry.put("movingAverage", MovingAverageFunction.class);
        registry.put("movingMedian", MovingMedianFunction.class);
        registry.put("multiplySeries", MultiplySeriesFunction.class);
        registry.put("nonNegativeDerivative", NonNegativeDerivativeFunction.class);
        registry.put("nPercentile", NPercentileFunction.class);
        registry.put("offset", OffsetFunction.class);
        registry.put("offsetToZero", OffsetToZeroFunction.class);
        registry.put("percentileOfSeries", PercentileOfSeriesFunction.class);
        registry.put("perSecond", PerSecondFunction.class);
        registry.put("pow", PowFunction.class);
        registry.put("rangeOfSeries", RangeOfSeriesFunction.class);
        registry.put("removeAbovePercentile", RemoveAbovePercentileFunction.class);
        registry.put("removeAboveValue", RemoveAboveValueFunction.class);
        registry.put("removeBelowPercentile", RemoveBelowPercentileFunction.class);
        registry.put("removeBelowValue", RemoveBelowValueFunction.class);
        registry.put("scale", ScaleFunction.class);
        registry.put("scaleToSeconds", ScaleToSecondsFunction.class);
        registry.put("secondYAxis", SecondYAxisFunction.class);
        registry.put("sortByMaxima", SortByMaximaFunction.class);
        registry.put("sortByMinima", SortByMinimaFunction.class);
        registry.put("sortByName", SortByNameFunction.class);
        registry.put("sortByTotal", SortByTotalFunction.class);
        registry.put("squareRoot", SquareRootFunction.class);
        registry.put("stacked", StackedFunction.class);
        registry.put("stdev", StdevFunction.class);
        registry.put("sumSeries", SumSeriesFunction.class);
        registry.put("sum", SumSeriesFunction.class);
        registry.put("timeShift", TimeShiftFunction.class);
    }

    public static DistheneFunction getFunction(String name) throws InvalidFunctionException {
        if (registry.get(name) == null) {
            throw new InvalidFunctionException();
        }

        try {
            Constructor<DistheneFunction> constructor = (Constructor<DistheneFunction>) registry.get(name).getConstructor(String.class);
            return constructor.newInstance(name);
        } catch (Exception e) {
            throw new InvalidFunctionException();
        }
    }
}
