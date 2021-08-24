package net.iponweb.disthene.reader.service.metric;

import com.datastax.oss.driver.api.core.cql.AsyncResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import com.google.common.base.Joiner;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.gson.Gson;
import com.spotify.futures.CompletableFutures;
import net.iponweb.disthene.reader.beans.TimeSeries;
import net.iponweb.disthene.reader.config.DistheneReaderConfiguration;
import net.iponweb.disthene.reader.config.Rollup;
import net.iponweb.disthene.reader.exceptions.TooMuchDataExpectedException;
import net.iponweb.disthene.reader.graphite.evaluation.TargetEvaluator;
import net.iponweb.disthene.reader.service.index.IndexService;
import net.iponweb.disthene.reader.service.stats.StatsService;
import net.iponweb.disthene.reader.service.store.CassandraService;
import net.iponweb.disthene.reader.utils.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;

/**
 * @author Andrei Ivanov
 */
@SuppressWarnings("UnstableApiUsage")
public class MetricService {
    private final static Logger logger = LogManager.getLogger(MetricService.class);

    private final IndexService indexService;
    private final CassandraService cassandraService;
    private final StatsService statsService;

    private final DistheneReaderConfiguration distheneReaderConfiguration;

    private final ExecutorService executorService = MoreExecutors.listeningDecorator(Executors.newCachedThreadPool());

    public MetricService(IndexService indexService, CassandraService cassandraService, StatsService statsService, DistheneReaderConfiguration distheneReaderConfiguration) {
        this.indexService = indexService;
        this.cassandraService = cassandraService;
        this.distheneReaderConfiguration = distheneReaderConfiguration;
        this.statsService = statsService;
    }

    public List<String> getPaths(String tenant, String wildcard) throws TooMuchDataExpectedException, IOException {
        return indexService.getPaths(tenant, Collections.singletonList(wildcard));
    }

    public String getMetricsAsJson(String tenant, List<String> wildcards, long from, long to) throws ExecutionException, InterruptedException, TooMuchDataExpectedException, IOException {
        List<String> paths = indexService.getPaths(tenant, wildcards);
        Collections.sort(paths);

        // Calculate rollup etc
        long effectiveTo = Math.min(to, Instant.now().getEpochSecond());
        Rollup bestRollup = getRollup(from);
        long effectiveFrom = (from % bestRollup.getRollup()) == 0 ? from : from + bestRollup.getRollup() - (from % bestRollup.getRollup());
        effectiveTo = effectiveTo - (effectiveTo % bestRollup.getRollup());
        logger.debug("Effective from: " + effectiveFrom);
        logger.debug("Effective to: " + effectiveTo);

        // now build the weird data structures ("in the meanwhile")
        final Map<Long, Integer> timestampIndices = new HashMap<>();
        long timestamp = effectiveFrom;
        int index = 0;
        while (timestamp <= effectiveTo) {
            timestampIndices.put(timestamp, index++);
            timestamp += bestRollup.getRollup();
        }

        final int length = timestampIndices.size();
        logger.debug("Expected number of data points in series is " + length);
        logger.debug("Expected number of series is " + paths.size());


        // Now let's query C*
        List<CompletionStage<SinglePathResult>> completionStages = Lists.newArrayListWithExpectedSize(paths.size());
        for (final String path : paths) {
            Function<AsyncResultSet, SinglePathResult> serializeFunction = resultSet -> {
                SinglePathResult result = new SinglePathResult(path);
                result.makeJson(resultSet, length, timestampIndices);
                return result;
            };

            cassandraService
                    .executeAsync(tenant, path, bestRollup.getRollup(), effectiveFrom, effectiveTo)
                    .ifPresent(asyncResultSetCompletionStage -> completionStages.add(asyncResultSetCompletionStage.thenApplyAsync(serializeFunction, executorService)));
        }

        // Build response content JSON
        List<String> singlePathJsons = new ArrayList<>();
        for (SinglePathResult singlePathResult : CompletableFutures.allAsList(completionStages).get()) {
            if (!singlePathResult.isAllNulls()) {
                singlePathJsons.add("\"" + singlePathResult.getPath() + "\":" + singlePathResult.getJson());
            }
        }

        return "{\"from\":" + effectiveFrom + ",\"to\":" + effectiveTo + ",\"step\":" + bestRollup.getRollup() +
                ",\"series\":{" + Joiner.on(",").skipNulls().join(singlePathJsons) + "}}";

    }

    public List<TimeSeries> getMetricsAsList(String tenant, List<String> wildcards, long from, long to) throws ExecutionException, InterruptedException, TooMuchDataExpectedException, IOException {
        Stopwatch indexTimer = Stopwatch.createStarted();
        List<String> paths = indexService.getPaths(tenant, wildcards);
        indexTimer.stop();
        statsService.addIndexResponseTime(tenant, indexTimer.elapsed(TimeUnit.MILLISECONDS));

        statsService.incRenderPathsRead(tenant, paths.size());

        // Calculate rollup etc
        long effectiveTo = Math.min(to, Instant.now().getEpochSecond());
        Rollup bestRollup = getRollup(from);
        long effectiveFrom = (from % bestRollup.getRollup()) == 0 ? from : from + bestRollup.getRollup() - (from % bestRollup.getRollup());
        effectiveTo = effectiveTo - (effectiveTo % bestRollup.getRollup());
        logger.debug("Effective from: " + effectiveFrom);
        logger.debug("Effective to: " + effectiveTo);

        // now build the weird data structures ("in the meanwhile")
        final Map<Long, Integer> timestampIndices = new HashMap<>();
        long timestamp = effectiveFrom;
        int index = 0;
        while (timestamp <= effectiveTo) {
            timestampIndices.put(timestamp, index++);
            timestamp += bestRollup.getRollup();
        }

        final int length = timestampIndices.size();
        logger.debug("Expected number of data points in series is " + length);
        logger.debug("Expected number of series is " + paths.size());

        // Fail (return empty list) right away if we exceed maximum number of points
        if (paths.size() * length > distheneReaderConfiguration.getReader().getMaxPoints()) {
            logger.debug("Expected total number of data points exceeds the limit: " + paths.size() * length);
            throw new TooMuchDataExpectedException("Expected total number of data points exceeds the limit: " + paths.size() * length + " (the limit is " + distheneReaderConfiguration.getReader().getMaxPoints() + ")");
        }

        // Now let's query C*
        List<CompletionStage<SinglePathResult>> completionStages = Lists.newArrayListWithExpectedSize(paths.size());
        for (final String path : paths) {
            Function<AsyncResultSet, SinglePathResult> serializeFunction = resultSets -> {
                SinglePathResult result = new SinglePathResult(path);
                result.makeArray(resultSets, length, timestampIndices);
                return result;
            };

            cassandraService
                    .executeAsync(tenant, path, bestRollup.getRollup(), effectiveFrom, effectiveTo)
                    .ifPresent(asyncResultSetCompletionStage -> completionStages.add(asyncResultSetCompletionStage.thenApplyAsync(serializeFunction, executorService)));
        }

        Stopwatch cassandraTimer = Stopwatch.createStarted();

        List<TimeSeries> timeSeries = new ArrayList<>();

        for (SinglePathResult singlePathResult : CompletableFutures.allAsList(completionStages).get()) {
            if (singlePathResult.getValues() != null) {
                TimeSeries ts = new TimeSeries(singlePathResult.getPath(), effectiveFrom, effectiveTo, bestRollup.getRollup());
                ts.setValues(singlePathResult.getValues());
                timeSeries.add(ts);
            }
        }

        logger.debug("Number of series fetched: " + timeSeries.size());
        cassandraTimer.stop();

        statsService.addStoreResponseTime(tenant, cassandraTimer.elapsed(TimeUnit.MILLISECONDS));

        logger.debug("Fetching from Cassandra took " + cassandraTimer.elapsed(TimeUnit.MILLISECONDS) + " milliseconds (" + wildcards + ")");

        // sort it by path
        timeSeries.sort(Comparator.comparing(TimeSeries::getName));

        int totalPoints = 0;

        for (TimeSeries ts : timeSeries) {
            totalPoints += ts.getValues().length;
        }

        statsService.incRenderPointsRead(tenant, totalPoints);

        return timeSeries;
    }

    public Rollup getRollup(long from) {
        long now = System.currentTimeMillis() / 1000L;

        // Let's find a rollup that potentially can have all the data taking retention in account
        List<Rollup> survivals = new ArrayList<>();
        for (Rollup rollup : distheneReaderConfiguration.getReader().getRollups()) {
            if (now - (long) rollup.getPeriod() * rollup.getRollup() <= from) {
                survivals.add(rollup);
            }
        }

        // no survivals found - take the last rollup (may be there is something there)
        if (survivals.size() == 0) {
            return distheneReaderConfiguration.getReader().getRollups().get(distheneReaderConfiguration.getReader().getRollups().size() - 1);
        }

        return survivals.get(0);
    }

    private static class SinglePathResult {
        final String path;
        String json;
        Double[] values = null;
        boolean allNulls = true;
        final boolean isSumMetric;

        private SinglePathResult(String path) {
            this.path = path;
            // trying to optimize something that JIT should do. Probably redundant
            this.isSumMetric = isSumMetric(path);
        }

        public String getPath() {
            return path;
        }

        String getJson() {
            return json;
        }

        public Double[] getValues() {
            return values;
        }

        boolean isAllNulls() {
            return allNulls;
        }

        private void makeJson(AsyncResultSet resultSet, int length, Map<Long, Integer> timestampIndices) {
            makeArray(resultSet, length, timestampIndices);
            json = new Gson().toJson(values);
        }

        private void makeArray(AsyncResultSet resultSet, int length, Map<Long, Integer> timestampIndices) {
            values = new Double[length];

            for (Row row : resultSet.currentPage()) {
                allNulls = false;
                int index = timestampIndices.get(row.getLong("time"));
                if (isSumMetric) {
                    values[index] = (values[index] != null ? values[index] : 0) + CollectionUtils.unsafeSum(row.getList("data", Double.class));
                } else {
                    values[index] = CollectionUtils.unsafeAverage(row.getList("data", Double.class));
                }
            }
        }

        private static boolean isSumMetric(String path) {
            return path.startsWith("sum");

        }
    }
}
