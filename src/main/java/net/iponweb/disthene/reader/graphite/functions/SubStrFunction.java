package net.iponweb.disthene.reader.graphite.functions;

import net.iponweb.disthene.reader.beans.TimeSeries;
import net.iponweb.disthene.reader.exceptions.EvaluationException;
import net.iponweb.disthene.reader.exceptions.InvalidArgumentException;
import net.iponweb.disthene.reader.exceptions.TimeSeriesNotAlignedException;
import net.iponweb.disthene.reader.graphite.Target;
import net.iponweb.disthene.reader.graphite.evaluation.TargetEvaluator;
import net.iponweb.disthene.reader.utils.TimeSeriesUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author swhors@coupang.com, 12.12.2019
 */
public class SubStrFunction extends DistheneFunction {

    public SubStrFunction(String text) {
        super(text, "substr");
    }
    
    private String substrName(String name, int start, int stop) {
        int left = name.indexOf("(");
        int right = name.lastIndexOf(")");
        
        left  = Math.max(left, 0);
        right = (right < 0 ? name.length() - 1 : right);
        
        String [] cleanName = name.substring(left + 1, right).split("\\.");
        
        int newStart = (start < 0 ? (cleanName.length + start) : start);
        
        int newStop = (stop <= 0 ? (cleanName.length + stop) : stop);
        newStop = Math.min(cleanName.length, newStop);
        
        int diffStopAndStart = newStop - newStart;
        
        if ((diffStopAndStart > 0) && (diffStopAndStart <= cleanName.length)) {
            String [] newCleanName = Arrays.copyOfRange(cleanName, newStart, newStop);
            return String.join(".", newCleanName).replaceAll(",.+", "");
        }
        return "";
    }

    @Override
    public List<TimeSeries> evaluate(TargetEvaluator evaluator) throws EvaluationException {
        List <TimeSeries> processedArguments = new ArrayList <>(evaluator.eval((Target) arguments.get(0)));
    
        int start = ((Double) arguments.get(1)).intValue();
        int stop = ((Double) arguments.get(2)).intValue();
    
        if (processedArguments.size() == 0) return new ArrayList<>();

        if (!TimeSeriesUtils.checkAlignment(processedArguments)) {
            throw new TimeSeriesNotAlignedException();
        }
    
        processedArguments.forEach(ts -> ts.setName(substrName(ts.getName(), start, stop)));
    
        return processedArguments;
    }

    @Override
    public void checkArguments() throws InvalidArgumentException {
        if (arguments.size() != 3) {
            throw new InvalidArgumentException("alias: number of arguments is " + arguments.size() + ". Must be two.");
        }
        if (!(arguments.get(0) instanceof Target)) {
            throw new InvalidArgumentException("substr: argument is " + arguments.get(0).getClass().getName() + ". Must be series");
        }
        if (!(arguments.get(1) instanceof Double)) {
            throw new InvalidArgumentException("substr: argument is " + arguments.get(1).getClass().getName() + ". Must be a Double");
        }
        if (!(arguments.get(2) instanceof Double)) {
            throw new InvalidArgumentException("substr: argument is " + arguments.get(2).getClass().getName() + ". Must be a Double");
        }
    }
}
