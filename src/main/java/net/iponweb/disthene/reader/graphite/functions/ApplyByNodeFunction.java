package net.iponweb.disthene.reader.graphite.functions;

import com.google.common.base.Joiner;
import net.iponweb.disthene.reader.beans.TimeSeries;
import net.iponweb.disthene.reader.exceptions.EvaluationException;
import net.iponweb.disthene.reader.exceptions.InvalidArgumentException;
import net.iponweb.disthene.reader.graphite.PathTarget;
import net.iponweb.disthene.reader.graphite.TargetVisitor;
import net.iponweb.disthene.reader.graphite.evaluation.TargetEvaluator;
import net.iponweb.disthene.reader.graphite.grammar.GraphiteLexer;
import net.iponweb.disthene.reader.graphite.grammar.GraphiteParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Andrei Ivanov
 */
public class ApplyByNodeFunction extends DistheneFunction {

    public ApplyByNodeFunction(String text) {
        super(text, "applyByNode");
    }

    @Override
    public List<TimeSeries> evaluate(TargetEvaluator evaluator) throws EvaluationException {
        int position = ((Double) arguments.get(1)).intValue();
        String function = (String) arguments.get(2);
        String name = arguments.size() > 3 ? (String) arguments.get(3) : null;

        String tenant = ((PathTarget) arguments.get(0)).getTenant();
        long from = ((PathTarget) arguments.get(0)).getFrom();
        long to = ((PathTarget) arguments.get(0)).getTo();

        List<TimeSeries> result = new ArrayList<>();

        List<String> paths = getKeys(evaluator.getPaths((PathTarget) arguments.get(0)), position);

        for (String path : paths) {
            GraphiteLexer lexer = new GraphiteLexer(CharStreams.fromString(function.replaceAll("%", path)));
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            GraphiteParser parser = new GraphiteParser(tokens);
            ParseTree tree = parser.expression();

            List<TimeSeries> evaluated = evaluator.eval(new TargetVisitor(tenant, from, to, getContext()).visit(tree));

            if (name != null) {
                String newName = name.replaceAll("%", path);
                for (TimeSeries ts : evaluated) {
                    ts.setName(newName);
                }
            }

            result.addAll(evaluated);
        }

        return result;
    }

    private List<String> getKeys(List<String> paths, int position) {
        List<String> result = new ArrayList<>();
        for(String path : paths) {
            String[] parts = Arrays.copyOfRange(path.split("\\."), 0, position + 1);
            result.add(Joiner.on(".").skipNulls().join(parts));
        }
        return result;
    }

    @Override
    public void checkArguments() throws InvalidArgumentException {
        if (arguments.size() > 4 || arguments.size() < 3) throw new InvalidArgumentException("applyByNode: number of arguments is " + arguments.size() + ". Must be three or four.");
        if (!(arguments.get(0) instanceof PathTarget)) throw new InvalidArgumentException("applyByNode: argument is " + arguments.get(0).getClass().getName() + ". Must be series");

        if (!(arguments.get(1) instanceof Double)) throw new InvalidArgumentException("applyByNode: argument is " + arguments.get(1).getClass().getName() + ". Must be a number");
        if (!(arguments.get(2) instanceof String)) throw new InvalidArgumentException("applyByNode: argument is " + arguments.get(2).getClass().getName() + ". Must be a string");

        if (arguments.size() > 3 && !(arguments.get(3) instanceof String)) throw new InvalidArgumentException("applyByNode: argument is " + arguments.get(3).getClass().getName() + ". Must be a string");
    }
}
