package net.iponweb.disthene.reader.graphite.evaluation;

import io.netty.util.internal.StringUtil;
import net.iponweb.disthene.reader.exceptions.InvalidFunctionException;
import net.iponweb.disthene.reader.graphite.PathTarget;
import net.iponweb.disthene.reader.graphite.Target;
import net.iponweb.disthene.reader.graphite.functions.DistheneFunction;
import net.iponweb.disthene.reader.graphite.functions.registry.FunctionRegistry;
import net.iponweb.disthene.reader.service.index.IndexService;
import net.iponweb.disthene.reader.service.metric.MetricService;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Andrei Ivanov
 */
public class DollarPreProcessor {
    final static Logger logger = Logger.getLogger(DollarPreProcessor.class);

    private IndexService indexService;

    public DollarPreProcessor(IndexService indexService) {
        this.indexService = indexService;
    }

    public List<Target> preProcess(Target target) throws InvalidFunctionException {
        if (!target.getText().contains("$1")) return Collections.singletonList(target);
        if (StringUtils.countMatches(target.getText(), "*") != 1) return Collections.singletonList(target);

        PathTarget basePathTarget = getBasePathTarget(target);
        logger.debug(basePathTarget != null ? basePathTarget.getText() : "null!!!!");

        if (basePathTarget == null || StringUtils.countMatches(basePathTarget.getPath(), "*") > 1) return Collections.singletonList(target);

        Pattern pattern = Pattern.compile(basePathTarget.getPath()
                .replace(".", "\\.").replace("}", ")").replace(",", "|").replace("?", "[^\\.]").replace("{", "(")
                .replace("*", "(?<base>[^\\.]+)"
                ));

        // resolve base path target
        List<String> paths = indexService.getPaths(basePathTarget.getTenant(), Collections.singletonList(basePathTarget.getPath()));

        logger.debug(paths);

        List<Target> targets = new ArrayList<>();
        for (String path : paths) {
            Matcher matcher = pattern.matcher(path);
            if (matcher.matches()) {
                logger.debug(matcher.group("base"));
                targets.add(explodeTarget(matcher.group("base"), target));
            }
        }

        for (Target target1 : targets) {
            logger.debug(target1);
        }

        return targets;

    }

    private PathTarget getBasePathTarget(Target target) {
        // go dfs until we hit the first path target with *
        if (target instanceof PathTarget && ((PathTarget) target).getPath().contains("*")) return (PathTarget) target;
        if (target instanceof PathTarget && !((PathTarget) target).getPath().contains("*")) return null;

        if (target instanceof DistheneFunction) {
            for (Object argument : ((DistheneFunction) target).getArguments()) {

                if (argument instanceof Target) {
                    PathTarget pathTarget = getBasePathTarget((Target) argument);
                    if (pathTarget != null) return pathTarget;
                }
            }
        }

        return null;

    }

    private Target explodeTarget(String base, Target original) throws InvalidFunctionException {

        if (original instanceof PathTarget) {
            return new PathTarget(
                    original.getText().replaceAll("\\*", base).replaceAll("$1", base),
                    ((PathTarget) original).getPath().replaceAll("\\*", base).replaceAll("\\$1", base),
                    ((PathTarget) original).getTenant(),
                    ((PathTarget) original).getFrom(),
                    ((PathTarget) original).getTo()
            );
        }

        if (original instanceof DistheneFunction) {
            DistheneFunction function = FunctionRegistry.getFunction(((DistheneFunction) original).getName(), ((DistheneFunction) original).getFrom(), ((DistheneFunction) original).getTo());

            for (Object argument : ((DistheneFunction) original).getArguments()) {
                if (argument instanceof Target) {
                    function.addArg(explodeTarget(base, (Target) argument));
                } else {
                    function.addArg(argument);
                }
            }

            return function;
        }

        return original;

    }



}
