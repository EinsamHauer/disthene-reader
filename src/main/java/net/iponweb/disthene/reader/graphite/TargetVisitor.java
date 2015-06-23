package net.iponweb.disthene.reader.graphite;

import net.iponweb.disthene.reader.exceptions.InvalidArgumentException;
import net.iponweb.disthene.reader.exceptions.InvalidFunctionException;
import net.iponweb.disthene.reader.graphite.functions.DistheneFunction;
import net.iponweb.disthene.reader.graphite.functions.factory.FunctionFactory;
import net.iponweb.disthene.reader.graphite.grammar.GraphiteBaseVisitor;
import net.iponweb.disthene.reader.graphite.grammar.GraphiteParser;
import org.antlr.v4.runtime.misc.ParseCancellationException;

/**
 * @author Andrei Ivanov
 */
public class TargetVisitor extends GraphiteBaseVisitor<Target> {

    private String tenant;
    private Long from;
    private Long to;

    public TargetVisitor(String tenant, Long from, Long to) {
        this.tenant = tenant;
        this.from = from;
        this.to = to;
    }

    @Override
    public Target visitExpressionPathExpression(GraphiteParser.ExpressionPathExpressionContext ctx) {
        return new PathTarget(ctx.getText(), ctx.pathExpression().getText(), tenant, from, to);
    }

    @Override
    public Target visitExpressionCall(GraphiteParser.ExpressionCallContext ctx) {
        GraphiteParser.CallContext call = ctx.call();
        try {
            DistheneFunction function = FunctionFactory.getFunction(call.FunctionName().getText());
            function.setText(ctx.getText());

            for(GraphiteParser.ArgContext arg : call.args().arg()) {
                if (arg instanceof GraphiteParser.ArgExpressionContext) {
                    function.addArg(visit(arg));
                } else if (arg instanceof GraphiteParser.ArgBooleanContext) {
                    function.addArg(Boolean.parseBoolean(arg.getText()));
                } else if (arg instanceof GraphiteParser.ArgNumberContext) {
                    function.addArg(Double.parseDouble(arg.getText()));
                } else if (arg instanceof GraphiteParser.ArgStringContext) {
                    function.addArg(arg.getText());
                }
            }

            return function;
        } catch (InvalidFunctionException | InvalidArgumentException e) {
            e.printStackTrace();
            throw new ParseCancellationException(e);
        }


    }


}
