package net.iponweb.disthene.reader.handler;

import com.google.common.base.Stopwatch;
import com.google.common.util.concurrent.SimpleTimeLimiter;
import com.google.common.util.concurrent.TimeLimiter;
import com.google.common.util.concurrent.UncheckedTimeoutException;
import io.netty.handler.codec.http.*;
import net.iponweb.disthene.reader.beans.TimeSeries;
import net.iponweb.disthene.reader.config.ReaderConfiguration;
import net.iponweb.disthene.reader.exceptions.*;
import net.iponweb.disthene.reader.format.ResponseFormatter;
import net.iponweb.disthene.reader.graphite.Target;
import net.iponweb.disthene.reader.graphite.TargetVisitor;
import net.iponweb.disthene.reader.graphite.evaluation.EvaluationContext;
import net.iponweb.disthene.reader.graphite.evaluation.TargetEvaluator;
import net.iponweb.disthene.reader.graphite.grammar.GraphiteLexer;
import net.iponweb.disthene.reader.graphite.grammar.GraphiteParser;
import net.iponweb.disthene.reader.graphite.utils.ValueFormatter;
import net.iponweb.disthene.reader.handler.parameters.RenderParameters;
import net.iponweb.disthene.reader.service.metric.MetricService;
import net.iponweb.disthene.reader.service.stats.StatsService;
import net.iponweb.disthene.reader.service.throttling.ThrottlingService;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author Andrei Ivanov
 */
@SuppressWarnings("UnstableApiUsage")
public class RenderHandler implements DistheneReaderHandler {
    private final static Logger logger = LogManager.getLogger(RenderHandler.class);

    private final TargetEvaluator evaluator;
    private final StatsService statsService;
    private final ThrottlingService throttlingService;
    private final ReaderConfiguration readerConfiguration;

    private static final ExecutorService executor = Executors.newCachedThreadPool();
    private final TimeLimiter timeLimiter = SimpleTimeLimiter.create(executor);


    public RenderHandler(MetricService metricService, StatsService statsService, ThrottlingService throttlingService, ReaderConfiguration readerConfiguration) {
        this.evaluator = new TargetEvaluator(metricService);
        this.statsService = statsService;
        this.throttlingService = throttlingService;
        this.readerConfiguration = readerConfiguration;
    }

    @Override
    public FullHttpResponse handle(final HttpRequest request) throws ParameterParsingException, EvaluationException, LogarithmicScaleNotAllowed {
        final RenderParameters parameters = RenderParameters.parse(request);

        logger.debug("Got request: " + parameters);
        Stopwatch timer = Stopwatch.createStarted();

        double throttled = throttlingService.throttle(parameters.getTenant());

        statsService.incRenderRequests(parameters.getTenant());

        if (throttled > 0) {
            statsService.incThrottleTime(parameters.getTenant(), throttled);
        }

        final List<Target> targets = new ArrayList<>();

        EvaluationContext context = new EvaluationContext(
                readerConfiguration.isHumanReadableNumbers() ? ValueFormatter.getInstance(parameters.getFormat()) : ValueFormatter.getInstance(ValueFormatter.ValueFormatterType.MACHINE)
        );

        // Let's parse the targets
        for(String targetString : parameters.getTargets()) {
            GraphiteLexer lexer = new GraphiteLexer(new ANTLRInputStream(targetString));
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            GraphiteParser parser = new GraphiteParser(tokens);
            ParseTree tree = parser.expression();
            try {
                targets.add(new TargetVisitor(parameters.getTenant(), parameters.getFrom(), parameters.getUntil(), context).visit(tree));
            } catch (ParseCancellationException e) {
                String additionalInfo = null;
                if (e.getMessage() != null) additionalInfo = e.getMessage();
                if (e.getCause() != null) additionalInfo = e.getCause().getMessage();
                throw new InvalidParameterValueException("Could not parse target: " + targetString + " (" + additionalInfo + ")");
            }
        }

        FullHttpResponse response;
        try {
            response = timeLimiter.callWithTimeout(() -> handleInternal(targets, parameters), readerConfiguration.getRequestTimeout(), TimeUnit.SECONDS);
        } catch (UncheckedTimeoutException e) {
            logger.debug("Request timed out: " + parameters);
            statsService.incTimedOutRequests(parameters.getTenant());
            response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.REQUEST_ENTITY_TOO_LARGE);
        } catch (ExecutionException e) {
            if (e.getCause() instanceof EvaluationException) {
                throw (EvaluationException) e.getCause();
            } else if (e.getCause() instanceof LogarithmicScaleNotAllowed) {
                throw (LogarithmicScaleNotAllowed) e.getCause();
            } else {
                logger.error("Unexpected error:", e);
                response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (Exception e) {
            logger.error("Unexpected error:", e);
            response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR);
        }

        timer.stop();
        statsService.addResponseTime(parameters.getTenant(), timer.elapsed(TimeUnit.MILLISECONDS));

        logger.debug("Request took " + timer.elapsed(TimeUnit.MILLISECONDS) + " milliseconds (" + parameters + ")");


        return response;
    }
    private FullHttpResponse handleInternal(List<Target> targets, RenderParameters parameters) throws EvaluationException, LogarithmicScaleNotAllowed {
        // now evaluate each target producing list of TimeSeries
        List<TimeSeries> results = new ArrayList<>();

        for(Target target : targets) {
            results.addAll(evaluator.eval(target));
        }

        return ResponseFormatter.formatResponse(results, parameters);
    }

}
