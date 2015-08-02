package net.iponweb.disthene.reader.graphite.functions;

import net.iponweb.disthene.reader.beans.TimeSeries;
import net.iponweb.disthene.reader.exceptions.EvaluationException;
import net.iponweb.disthene.reader.exceptions.InvalidArgumentException;
import net.iponweb.disthene.reader.exceptions.TimeSeriesNotAlignedException;
import net.iponweb.disthene.reader.graphite.Target;
import net.iponweb.disthene.reader.graphite.evaluation.TargetEvaluator;
import net.iponweb.disthene.reader.utils.CollectionUtils;
import net.iponweb.disthene.reader.utils.DateTimeUtils;
import net.iponweb.disthene.reader.utils.TimeSeriesUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Andrei Ivanov
 */
//todo: this implementation work as in vanilla graphite. But isn't it better to move right end by one step left??
public class SummarizeFunction extends DistheneFunction {

    public SummarizeFunction(String text) {
        super(text, "summarize");
    }

    @Override
    public List<TimeSeries> evaluate(TargetEvaluator evaluator) throws EvaluationException {
        // parse interval
        int step = (int) Math.abs(DateTimeUtils.parseTimeOffset((String) arguments.get(1)));

        String aggregation = arguments.size() > 2 ? ((String) arguments.get(2)).toLowerCase().replaceAll("[\"\']", "") : "sum";

        List<TimeSeries> processedArguments = new ArrayList<>();
        processedArguments.addAll(evaluator.eval((Target) arguments.get(0)));

        if (processedArguments.size() == 0) return new ArrayList<>();

        if (!TimeSeriesUtils.checkAlignment(processedArguments)) {
            throw new TimeSeriesNotAlignedException();
        }


        long from = processedArguments.get(0).getFrom() % step == 0 ?
                processedArguments.get(0).getFrom() : processedArguments.get(0).getFrom() - processedArguments.get(0).getFrom() % step;
        long to = processedArguments.get(0).getTo() % step == 0 ?
                processedArguments.get(0).getTo() : processedArguments.get(0).getTo() - processedArguments.get(0).getTo() % step;

        for (TimeSeries ts : processedArguments) {
            List<Double> consolidated = new ArrayList<>();
            List<Double> buffer = new ArrayList<>();

            int index = 0;
            while (ts.getFrom() + index * ts.getStep() <= ts.getTo()) {
                if ((ts.getFrom() + index * ts.getStep()) % step == 0 && (ts.getFrom() + index * ts.getStep()) != from) {
                    switch (aggregation) {
                        case "last": {
                            consolidated.add(CollectionUtils.last(buffer));
                            break;
                        }
                        case "avg": {
                            consolidated.add(CollectionUtils.average(buffer));
                            break;
                        }
                        case "sum": {
                            consolidated.add(CollectionUtils.sum(buffer));
                            break;
                        }
                        case "min": {
                            consolidated.add(CollectionUtils.min(buffer));
                            break;
                        }
                        case "max": {
                            consolidated.add(CollectionUtils.max(buffer));
                            break;
                        }
                    }
                    buffer.clear();
                }
                buffer.add(ts.getValues()[index]);

                index++;
            }

            switch (aggregation) {
                case "last": {
                    consolidated.add(CollectionUtils.last(buffer));
                    break;
                }
                case "avg": {
                    consolidated.add(CollectionUtils.average(buffer));
                    break;
                }
                case "sum": {
                    consolidated.add(CollectionUtils.sum(buffer));
                    break;
                }
                case "min": {
                    consolidated.add(CollectionUtils.min(buffer));
                    break;
                }
                case "max": {
                    consolidated.add(CollectionUtils.max(buffer));
                    break;
                }
            }

            ts.setFrom(from);
            ts.setTo(to);
            ts.setStep(step);
            ts.setValues(consolidated.toArray(new Double[1]));
            ts.setName("summarize(" + ts.getName() + ",\"" + arguments.get(1) + "\",\"" + aggregation + "\")");
        }

        return processedArguments;
    }

    @Override
    public void checkArguments() throws InvalidArgumentException {
        if (arguments.size() > 4 || arguments.size() < 2) throw new InvalidArgumentException("summarize: number of arguments is " + arguments.size() + ". Must be two or three.");
        if (!(arguments.get(0) instanceof Target)) throw new InvalidArgumentException("summarize: argument is " + arguments.get(0).getClass().getName() + ". Must be series");
        if (!DateTimeUtils.testTimeOffset((String) arguments.get(1))) throw new InvalidArgumentException("summarize: interval cannot be parsed (" + arguments.get(1) + ")");

        if (arguments.size() > 2) {
            if (!(arguments.get(2) instanceof String)) throw new InvalidArgumentException("summarize: argument is " + arguments.get(2).getClass().getName() + ". Must be a string");
            String argument = ((String) arguments.get(2)).toLowerCase().replaceAll("[\"\']", "");
            if (!argument.equals("last") && !argument.equals("avg") && !argument.equals("sum") && !argument.equals("min") && !argument.equals("max")) {
                throw new InvalidArgumentException("summarize: must be aggregation.");
            }
        }
    }
}
