package net.iponweb.disthene.reader.service.metric;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.gson.Gson;
import net.iponweb.disthene.reader.beans.TimeSeries;
import net.iponweb.disthene.reader.config.DistheneReaderConfiguration;
import net.iponweb.disthene.reader.config.Rollup;
import net.iponweb.disthene.reader.service.index.IndexService;
import net.iponweb.disthene.reader.service.stats.StatsService;
import net.iponweb.disthene.reader.service.store.CassandraService;
import net.iponweb.disthene.reader.utils.CollectionUtils;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author Andrei Ivanov
 */
public class MetricService {
    final static Logger logger = Logger.getLogger(MetricService.class);

    private IndexService indexService;
    private CassandraService cassandraService;
    private StatsService statsService;

    private DistheneReaderConfiguration distheneReaderConfiguration;

    private ExecutorService executorService = MoreExecutors.listeningDecorator(Executors.newCachedThreadPool());

    public MetricService(IndexService indexService, CassandraService cassandraService, StatsService statsService, DistheneReaderConfiguration distheneReaderConfiguration) {
        this.indexService = indexService;
        this.cassandraService = cassandraService;
        this.distheneReaderConfiguration = distheneReaderConfiguration;
        this.statsService = statsService;
    }

    public String getMetricsAsJson(String tenant, List<String> wildcards, long from, long to) throws ExecutionException, InterruptedException {
        List<String> paths = indexService.getPaths(tenant, wildcards);
        Collections.sort(paths);

        // Calculate rollup etc
        Long now = System.currentTimeMillis() * 1000;
        Long effectiveTo = Math.min(to, now);
        Rollup bestRollup = getRollup(from);
        Long effectiveFrom = (from % bestRollup.getRollup()) == 0 ? from : from + bestRollup.getRollup() - (from % bestRollup.getRollup());
        effectiveTo = effectiveTo - (effectiveTo % bestRollup.getRollup());
        logger.debug("Effective from: " + effectiveFrom);
        logger.debug("Effective to: " + effectiveTo);

        // now build the weird data structures ("in the meanwhile")
        final Map<Long, Integer> timestampIndices = new HashMap<>();
        Long timestamp = effectiveFrom;
        int index = 0;
        while (timestamp <= effectiveTo) {
            timestampIndices.put(timestamp, index++);
            timestamp += bestRollup.getRollup();
        }

        final int length = timestampIndices.size();
        logger.debug("Expected number of data points in series is " + length);
        logger.debug("Expected number of series is " + paths.size());


        // Now let's query C*
        List<ListenableFuture<SinglePathResult>> futures = Lists.newArrayListWithExpectedSize(paths.size());
        for (final String path : paths) {
            Function<ResultSet, SinglePathResult> serializeFunction =
                    new Function<ResultSet, SinglePathResult>() {
                        public SinglePathResult apply(ResultSet resultSet) {
                            SinglePathResult result = new SinglePathResult(path);
                            result.makeJson(resultSet, length, timestampIndices);
                            return result;
                        }
                    };


            futures.add(
                    Futures.transform(
                            cassandraService.executeAsync(tenant, path, bestRollup.getPeriod(), bestRollup.getRollup(), effectiveFrom, effectiveTo),
                            serializeFunction,
                            executorService
                    )
            );
        }

        futures = Futures.inCompletionOrder(futures);

        // Build response content JSON
        List<String> singlePathJsons = new ArrayList<>();

        for (ListenableFuture<SinglePathResult> future : futures) {
            SinglePathResult singlePathResult = future.get();
            if (!singlePathResult.isAllNulls()) {
                singlePathJsons.add("\"" + singlePathResult.getPath() + "\":" + singlePathResult.getJson());
            }
        }


        return "{\"from\":" + effectiveFrom + ",\"to\":" + effectiveTo + ",\"step\":" + bestRollup.getRollup() +
                ",\"series\":{" + Joiner.on(",").skipNulls().join(singlePathJsons) + "}}";

    }

    public List<TimeSeries> getMetricsAsList(String tenant, List<String> wildcards, long from, long to) throws ExecutionException, InterruptedException {
        List<String> paths = indexService.getPaths(tenant, wildcards);

        statsService.incRenderPathsRead(tenant, paths.size());

        // Calculate rollup etc
        Long now = System.currentTimeMillis() * 1000;
        Long effectiveTo = Math.min(to, now);
        Rollup bestRollup = getRollup(from);
        Long effectiveFrom = (from % bestRollup.getRollup()) == 0 ? from : from + bestRollup.getRollup() - (from % bestRollup.getRollup());
        effectiveTo = effectiveTo - (effectiveTo % bestRollup.getRollup());
        logger.debug("Effective from: " + effectiveFrom);
        logger.debug("Effective to: " + effectiveTo);

        // now build the weird data structures ("in the meanwhile")
        final Map<Long, Integer> timestampIndices = new HashMap<>();
        Long timestamp = effectiveFrom;
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
            return Collections.emptyList();
        }

        // Now let's query C*
        List<ListenableFuture<SinglePathResult>> futures = Lists.newArrayListWithExpectedSize(paths.size());
        for (final String path : paths) {
            Function<ResultSet, SinglePathResult> serializeFunction =
                    new Function<ResultSet, SinglePathResult>() {
                        public SinglePathResult apply(ResultSet resultSet) {
                            SinglePathResult result = new SinglePathResult(path);
                            result.makeArray(resultSet, length, timestampIndices);
                            return result;
                        }
                    };


            futures.add(
                    Futures.transform(
                            cassandraService.executeAsync(tenant, path, bestRollup.getPeriod(), bestRollup.getRollup(), effectiveFrom, effectiveTo),
                            serializeFunction,
                            executorService
                    )
            );
        }

        Stopwatch timer = Stopwatch.createStarted();

        futures = Futures.inCompletionOrder(futures);

        List<TimeSeries> timeSeries = new ArrayList<>();

        for (ListenableFuture<SinglePathResult> future : futures) {
            SinglePathResult singlePathResult = future.get();
            if (singlePathResult.getValues() != null) {
                TimeSeries ts = new TimeSeries(singlePathResult.getPath(), effectiveFrom, effectiveTo, bestRollup.getRollup());
                ts.setValues(singlePathResult.getValues());
                timeSeries.add(ts);
            }
        }

        logger.debug("Number of series fetched: " + timeSeries.size());
        timer.stop();
        logger.debug("Fetching from Cassandra took " + timer.elapsed(TimeUnit.MILLISECONDS) + " milliseconds (" + wildcards + ")");

        // sort it by path
        Collections.sort(timeSeries, new Comparator<TimeSeries>() {
            @Override
            public int compare(TimeSeries ts1, TimeSeries ts2) {
                return ts1.getName().compareTo(ts2.getName());
            }
        });

        int totalPoints = 0;

        for (TimeSeries ts : timeSeries) {
            totalPoints += ts.getValues().length;
        }

        statsService.incRenderPointsRead(tenant, totalPoints);

        return timeSeries;
    }

    public Rollup getRollup(long from) {
        long now = System.currentTimeMillis() / 1000L ;

        // Let's find a rollup that potentially can have all the data taking retention in account
        List<Rollup> survivals = new ArrayList<>();
        for (Rollup rollup : distheneReaderConfiguration.getReader().getRollups()) {
            if (now - rollup.getPeriod() * rollup.getRollup() <= from) {
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
        String path;
        String json;
        Double[] values = null;
        boolean allNulls = true;

        private SinglePathResult(String path) {
            this.path = path;
        }

        public String getPath() {
            return path;
        }

        public String getJson() {
            return json;
        }

        public Double[] getValues() {
            return values;
        }

        public boolean isAllNulls() {
            return allNulls;
        }

        public void makeJson(ResultSet resultSet, int length, Map<Long, Integer> timestampIndices) {
            Double values[] = new Double[length];
            for (Row row : resultSet) {
                values[timestampIndices.get(row.getLong("time"))] =
                        isSumMetric(path) ? CollectionUtils.unsafeSum(row.getList("data", Double.class)) : CollectionUtils.unsafeAverage(row.getList("data", Double.class));
            }

            json = new Gson().toJson(values);
        }

        public void makeArray(ResultSet resultSet, int length, Map<Long, Integer> timestampIndices) {
            if (resultSet.getAvailableWithoutFetching() > 0) {
                allNulls = false;
                values = new Double[length];
                for (Row row : resultSet) {
                    values[timestampIndices.get(row.getLong("time"))] =
                            isSumMetric(path) ? CollectionUtils.unsafeSum(row.getList("data", Double.class)) : CollectionUtils.unsafeAverage(row.getList("data", Double.class));
                }
            } else {
                values = new Double[length];
                for (Map.Entry<Long, Integer> entry : timestampIndices.entrySet()) {
                    values[entry.getValue()] = null;
                }
            }
        }

        private static boolean isSumMetric(String path) {
            return path.startsWith("sum");

        }
    }
}
