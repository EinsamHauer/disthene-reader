package net.iponweb.disthene.reader.graphite.functions;

import net.iponweb.disthene.reader.beans.TimeSeries;
import net.iponweb.disthene.reader.exceptions.EvaluationException;
import net.iponweb.disthene.reader.exceptions.InvalidArgumentException;
import net.iponweb.disthene.reader.graphite.Target;
import net.iponweb.disthene.reader.graphite.evaluation.TargetEvaluator;
import net.iponweb.disthene.reader.utils.DateTimeUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author swhors@coupang.com fintech platform
 */
//todo: this implementation work as in vanilla graphite. But isn't it better to move right end by one step left??
public class SmartSummarizeFunction<aggregationFnc> extends DistheneFunction {

    public SmartSummarizeFunction(String text) {
        super(text, "smartSummarize");
    }
    
    final private long TIME_DELAY = 1860;
    
    abstract class AggregationFnc {
        abstract Double getValue(Double [] values);
        
        Double findLastUsingStream(Double[] array) {
            List<Double> filtered =  Arrays.stream(array)
                .filter(Objects :: nonNull)
                .collect(Collectors.toList());
            if (filtered.size() <= 0) return 0.0;
            return filtered.get(filtered.size() - 1);
        }
    
        Double findSumUsingStream(Double[] array) {
            return Arrays.stream(array)
                .filter(Objects :: nonNull)
                .mapToDouble(Double::doubleValue).sum();
        }
    
        OptionalDouble findAvgUsingStream(Double[] array) {
            return Arrays.stream(array)
                .filter(Objects :: nonNull)
                .mapToDouble(Double::doubleValue).average();
        }
        
        OptionalDouble findMinUsingStream(Double[] array) {
            return Arrays.stream(array)
                .filter(Objects :: nonNull)
                .mapToDouble(Double::doubleValue).min();
        }
        
        OptionalDouble findMaxUsingStream(Double[] array) {
            return Arrays.stream(array)
                .filter(Objects :: nonNull)
                .mapToDouble(Double::doubleValue).max();
        }
    }
    
    private class GetMin extends SmartSummarizeFunction <AggregationFnc>.AggregationFnc {
        @Override
        public Double getValue(Double [] values) {
            OptionalDouble optionalMin = findMinUsingStream(values);
            if (optionalMin.isPresent()) {
                return optionalMin.getAsDouble();
            }
            return 0.0;
        }
    }
    
    private class GetMax extends SmartSummarizeFunction <AggregationFnc>.AggregationFnc {
        @Override
        public Double getValue(Double [] values) {
            OptionalDouble optionalMin = findMaxUsingStream(values);
            if (optionalMin.isPresent()) {
                return optionalMin.getAsDouble();
            }
            return 0.0;
        }
    }
    
    private class GetSum extends SmartSummarizeFunction <AggregationFnc>.AggregationFnc {
        @Override
        public Double getValue(Double [] values) {
            return findSumUsingStream(values);
        }
    }
    
    private class GetAvg extends SmartSummarizeFunction <AggregationFnc>.AggregationFnc {
        @Override
        public Double getValue(Double [] values) {
            OptionalDouble avgValue = findAvgUsingStream(values);
            if (avgValue.isPresent()) return avgValue.getAsDouble();
            return 0.0;
        }
    }
    
    private class GetLast extends SmartSummarizeFunction <AggregationFnc>.AggregationFnc {
        @Override
        public Double getValue(Double [] values) {
            return findLastUsingStream(values);
        }
    }
    
    private final Map<String,SmartSummarizeFunction <AggregationFnc>.AggregationFnc> aggregationFncHashMap =
        new HashMap<String,SmartSummarizeFunction <AggregationFnc>.AggregationFnc>() {{
            put("avg", new GetAvg());
            put("last", new GetLast());
            put("sum", new GetSum());
            put("min", new GetMin());
            put("max", new GetMax());
        }};
    
    private List<List<Double>> getReOganizedMetrics(int smartSummarizeStep, Double [] metrics, Long from, int firstPos, int step) {
        List<List<Double>> dataLists = new ArrayList <>();
    
        Long timeStamp = from;
        for(int pos = firstPos; pos < metrics.length;pos++)
        {
            int newPos = (int)(timeStamp - from) / smartSummarizeStep;
        
            if (dataLists.size() <= newPos) {
                dataLists.add(new ArrayList <>());
            }
        
            if (metrics[pos] != null) {
                dataLists.get(newPos).add(metrics[pos]);
            }
            timeStamp += step;
        }
        return dataLists;
    }
    
    private List<Double> getSmartSummarizedMetic(List<List<Double>> dataLists, String aggregation) {
        List<Double> newValues = new ArrayList<>();
        dataLists.forEach( datas -> {
            if (datas.size() > 0){
                newValues.add(aggregationFncHashMap.get(aggregation).getValue(datas.toArray(new Double[0])));
            } else {
                newValues.add(null);
            }
        });
        return newValues;
    }
    
    private List<TimeSeries> smartSummarize(TargetEvaluator evaluator,
                                            int smartSummarizeStep,
                                            Target nextTarget,
                                            String aggregation) throws EvaluationException {
        if ((this.to - this.from) <= TIME_DELAY) {
            throw new EvaluationException();
        }
    
        List <TimeSeries> newTimeSeries = new ArrayList <>(evaluator.eval((Target) arguments.get(0)));
    
        int firstPos = (int) (TIME_DELAY / newTimeSeries.get(0).getStep());
    
        newTimeSeries.forEach( ts -> {
            Long newFrom = ts.getFrom() + TIME_DELAY;
            List<List<Double>> dataLists = getReOganizedMetrics(smartSummarizeStep, ts.getValues(), newFrom, firstPos, ts.getStep());
            List<Double> newValues = getSmartSummarizedMetic(dataLists, aggregation);
            ts.setFrom(ts.getFrom() + TIME_DELAY);
            ts.setStep(smartSummarizeStep);
            ts.setName(this.getName() + "(" + ts.getName() + ",\"" + arguments.get(1) + "\",\"" + arguments.get(2) + "\")");
            ts.setValues(newValues.toArray(new Double[0]));
        });
        
        return newTimeSeries;
    }
    
    @Override
    public List<TimeSeries> evaluate(TargetEvaluator evaluator) throws EvaluationException {
        int smartSummarizeStep = (int) Math.abs(DateTimeUtils.parseTimeOffset((String) arguments.get(1)));
        String aggregation = arguments.size() > 2 ? ((String) arguments.get(2)).toLowerCase().replaceAll("[\"']", "") : "sum";
        Target nextTarget = (Target) arguments.get(0);
        return smartSummarize(evaluator, smartSummarizeStep, nextTarget, aggregation);
        
    }

    @Override
    public void checkArguments() throws InvalidArgumentException {
        if (arguments.size() != 3) throw new InvalidArgumentException("smartSummarize: number of arguments is " + arguments.size() + ". Must be two or two.");
        if (!(arguments.get(0) instanceof Target)) throw new InvalidArgumentException("smartSummarize: argument is " + arguments.get(0).getClass().getName() + ". Must be series");
        if (!DateTimeUtils.testTimeOffset((String) arguments.get(1))) throw new InvalidArgumentException("smartSummarize: interval cannot be parsed (" + arguments.get(1) + ")");

        if (arguments.size() > 2) {
            if (!(arguments.get(2) instanceof String)) throw new InvalidArgumentException("smartSummarize: argument is " + arguments.get(2).getClass().getName() + ". Must be a string");
            String argument = ((String) arguments.get(2)).toLowerCase().replaceAll("[\"']", "");
            if (!argument.equals("last") && !argument.equals("avg") && !argument.equals("sum") && !argument.equals("min") && !argument.equals("max")) {
                throw new InvalidArgumentException("smartSummarize: must be aggregation.");
            }
        }
    }
}
