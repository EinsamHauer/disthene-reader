package net.iponweb.disthene.reader.handler;

import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import net.iponweb.disthene.reader.beans.TimeSeries;
import net.iponweb.disthene.reader.exceptions.*;
import net.iponweb.disthene.reader.format.Format;
import net.iponweb.disthene.reader.format.ResponseFormatter;
import net.iponweb.disthene.reader.graphite.Target;
import net.iponweb.disthene.reader.graphite.TargetVisitor;
import net.iponweb.disthene.reader.graphite.evaluation.TargetEvaluator;
import net.iponweb.disthene.reader.graphite.grammar.GraphiteLexer;
import net.iponweb.disthene.reader.graphite.grammar.GraphiteParser;
import net.iponweb.disthene.reader.handler.parameters.RenderParameters;
import net.iponweb.disthene.reader.service.metric.MetricService;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * @author Andrei Ivanov
 */
public class RenderHandler implements DistheneReaderHandler {

    final static Logger logger = Logger.getLogger(RenderHandler.class);

    private TargetEvaluator evaluator;

    public RenderHandler(MetricService metricService) {
        this.evaluator = new TargetEvaluator(metricService);
    }

    @Override
    public FullHttpResponse handle(HttpRequest request) throws ParameterParsingException, ExecutionException, InterruptedException, EvaluationException, LogarithmicScaleNotAllowed {
        RenderParameters parameters = RenderParameters.parse(request);

        logger.debug("Got request: " + parameters);

        List<Target> targets = new ArrayList<>();

        // Let's parse the targets
        for(String targetString : parameters.getTargets()) {
            GraphiteLexer lexer = new GraphiteLexer(new ANTLRInputStream(targetString));
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            GraphiteParser parser = new GraphiteParser(tokens);
            ParseTree tree = parser.expression();
            try {
                targets.add(new TargetVisitor(parameters.getTenant(), parameters.getFrom(), parameters.getUntil()).visit(tree));
            } catch (ParseCancellationException e) {
                String additionalInfo = null;
                if (e.getMessage() != null) additionalInfo = e.getMessage();
                if (e.getCause() != null) additionalInfo = e.getCause().getMessage();
                throw new InvalidParameterValueException("Could not parse target: " + targetString + " (" + additionalInfo + ")");
            }
        }
//        logger.debug(targets);

        // now evaluate each target producing list of TimeSeries
        List<TimeSeries> results = new ArrayList<>();

        for(Target target : targets) {
            results.addAll(evaluator.eval(target));
        }

/*
        for(TimeSeries ts : results) {
            logger.debug(ts);
        }
*/

        return ResponseFormatter.formatResponse(results, parameters);
    }

}
