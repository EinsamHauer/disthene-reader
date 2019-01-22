package net.iponweb.disthene.reader.graphite.utils;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import net.iponweb.disthene.reader.beans.TimeSeries;
import net.iponweb.disthene.reader.utils.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Grouper {

    interface AggregationMethod {
        Double apply(List<Double> points);
    }
    
    private static ImmutableMap<String, AggregationMethod> aggregationMap;
    static {
        AggregationMethod avg = new AggregationMethod() { public Double apply(List<Double> points) { return CollectionUtils.average(points);}};
        AggregationMethod sum = new AggregationMethod() { public Double apply(List<Double> points) { return CollectionUtils.sum(points);}};
        AggregationMethod min = new AggregationMethod() { public Double apply(List<Double> points) { return CollectionUtils.min(points);}};
        AggregationMethod max = new AggregationMethod() { public Double apply(List<Double> points) { return CollectionUtils.max(points);}};

        aggregationMap = ImmutableMap.<String, AggregationMethod>builder()
            .put("sum", sum)
            .put("sumSeries", sum)
            .put("avg", avg)
            .put("average", avg)
            .put("min", min)
            .put("minSeries", min)
            .put("max", max)
            .put("maxSeries", max)
        .build();
    }

    private List<TimeSeries> timeSeries;
    private String aggregator;

    public Grouper(List<TimeSeries> ts, String aggregatorName) {
        timeSeries = ts;
        aggregator = aggregatorName;
    }

    public List<TimeSeries> byNodesIndex(int[] indexes) {
        Map<String, List<TimeSeries>> buckets = new HashMap<>();

        for (TimeSeries ts : timeSeries) {
            String bucketName = getBucketName(ts.getName(), indexes);
            if (!buckets.containsKey(bucketName)) buckets.put(bucketName, new ArrayList<TimeSeries>());
            buckets.get(bucketName).add(ts);
        }
        
        long from = timeSeries.get(0).getFrom();
        long to = timeSeries.get(0).getTo();
        int step = timeSeries.get(0).getStep();
        int length = timeSeries.get(0).getValues().length;
  
        List<TimeSeries> resultTimeSeries = new ArrayList<>();

        for (Map.Entry<String, List<TimeSeries>> bucket : buckets.entrySet()) {
            TimeSeries timeSeries = new TimeSeries(bucket.getKey(), from, to, step);
            Double[] values = new Double[length];

            for (int i = 0; i < length; i++) {
                List<Double> points = new ArrayList<>();
                for (TimeSeries ts : bucket.getValue()) {
                    points.add(ts.getValues()[i]);
                }
                values[i] = aggregationMap.get(aggregator).apply(points);
            }
            timeSeries.setValues(values);
            timeSeries.setName(bucket.getKey());
            resultTimeSeries.add(timeSeries);
        }

        return resultTimeSeries;
    }

    public static boolean hasAggregationMethod(String name) {
        return aggregationMap.containsKey(name);
    }

    public static String[] getAvailableAggregationMethods() {
        return aggregationMap.keySet().toArray(new String[0]);
    }

    private String getBucketName(String name, int[] positions) {
        String[] split = name.split("\\.");
        List<String> parts = new ArrayList<>();
        for (int position : positions) {
            parts.add(split[position]);
        }
        return Joiner.on(".").skipNulls().join(parts);
    }
}
