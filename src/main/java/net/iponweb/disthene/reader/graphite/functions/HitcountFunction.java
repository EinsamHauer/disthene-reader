package net.iponweb.disthene.reader.graphite.functions;

import net.iponweb.disthene.reader.beans.TimeSeries;
import net.iponweb.disthene.reader.exceptions.EvaluationException;
import net.iponweb.disthene.reader.exceptions.InvalidArgumentException;
import net.iponweb.disthene.reader.exceptions.TimeSeriesNotAlignedException;
import net.iponweb.disthene.reader.graphite.Target;
import net.iponweb.disthene.reader.graphite.evaluation.TargetEvaluator;
import net.iponweb.disthene.reader.utils.DateTimeUtils;
import net.iponweb.disthene.reader.utils.TimeSeriesUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Andrei Ivanov
 */
public class HitcountFunction extends DistheneFunction {

    public HitcountFunction(String text) {
        super(text, "hitcount");
    }

    @Override
    public List<TimeSeries> evaluate(TargetEvaluator evaluator) throws EvaluationException {
        // parse interval
        int interval = (int) Math.abs(DateTimeUtils.parseTimeOffset((String) arguments.get(1)));

        List<TimeSeries> processedArguments = new ArrayList<>();
        processedArguments.addAll(evaluator.eval((Target) arguments.get(0)));

        if (processedArguments.size() == 0) return new ArrayList<>();

        if (!TimeSeriesUtils.checkAlignment(processedArguments)) {
            throw new TimeSeriesNotAlignedException();
        }

        // todo: replicating graphite code below. probably, should be refactored
        for (TimeSeries ts : processedArguments) {
            Double[] values = ts.getValues();
            int bucketCount = (int) Math.ceil((ts.getTo() - ts.getFrom()) / (double) interval);
            Double[] buckets = new Double[bucketCount];
//            long newStart = ts.getTo() - bucketCount * interval;
            long newStart = (ts.getTo() / interval) * interval - (bucketCount - 1) * interval;

            for (int i = 0; i < values.length; i++) {
                Double value = values[i];
                if (value == null) continue;

                long startTime = ts.getFrom() + i * ts.getStep();
                int startBucket = (int) (((startTime - newStart) - (startTime - newStart) % interval) / interval);
                int startMod = (int) ((startTime - newStart) % interval);
                long endTime = startTime + ts.getStep();
                int endBucket = (int) (((endTime - newStart) - (endTime - newStart) % interval) / interval);
                int endMod = (int) ((endTime - newStart) % interval);

                if (endBucket >= bucketCount) {
                    endBucket = bucketCount - 1;
                    endMod = interval;
                }

                if (startBucket == endBucket) {
                    if (startBucket >= 0) {
                        buckets[startBucket] = buckets[startBucket] != null ? buckets[startBucket] + value * (endMod - startMod) : value * (endMod - startMod);
                    }
                } else {
                    if (startBucket >= 0) {
                        buckets[startBucket] = buckets[startBucket] != null ? buckets[startBucket] + value * (interval - startMod) : value * (interval - startMod);
                    }

                    Double hitsPerBucket = value * interval;

                    for (int j = startBucket + 1; j < endBucket; j++) {
                        buckets[j] = buckets[j] != null ? buckets[j] + hitsPerBucket : hitsPerBucket;
                    }

                    if (endMod > 0) {
                        buckets[endBucket] = buckets[endBucket] != null ? buckets[endBucket] + value * endMod : value * endMod;
                    }
                }

            }

            ts.setFrom(newStart);
            ts.setStep(interval);
            ts.setValues(buckets);
            ts.setName("hitcount(" + ts.getName() + ",\"" + arguments.get(1) + "\")");
        }

        return processedArguments;
    }

    @Override
    public void checkArguments() throws InvalidArgumentException {
        if (arguments.size() > 3 || arguments.size() < 2)
            throw new InvalidArgumentException("hitcount: number of arguments is " + arguments.size() + ". Must be two or three.");
        if (!(arguments.get(0) instanceof Target))
            throw new InvalidArgumentException("hitcount: argument is " + arguments.get(0).getClass().getName() + ". Must be series");
        if (!DateTimeUtils.testTimeOffset((String) arguments.get(1)))
            throw new InvalidArgumentException("hitcount: interval cannot be parsed (" + arguments.get(1) + ")");
    }
}
