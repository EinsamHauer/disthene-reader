package net.iponweb.disthene.reader.response;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.gson.Gson;
import net.iponweb.disthene.reader.services.CassandraService;
import net.iponweb.disthene.reader.services.PathsService;
import net.iponweb.disthene.reader.utils.ListUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Andrei Ivanov
 */
public class MetricsResponse {
    final static Logger logger = Logger.getLogger(MetricsResponse.class);

    private static final String query = "SELECT time, data FROM metric.metric " +
            "where path = ? and tenant = ? and period = ? and rollup = ? " +
            "and time >= ? and time <= ? order by time";

    public static String getContent(MetricsParameters parameters) throws Exception {
        logger.debug("Processing query: " + parameters);

        // Build paths
        logger.debug("Fetching paths from ES");
        long start = System.nanoTime();
        Set<String> paths;
        if (parameters.getPath().size() > 1) {
            paths = new HashSet<>();
            paths.addAll(parameters.getPath());
        } else {
            paths = PathsService.getInstance().getPathsSet(parameters.getTenant(), parameters.getPath());
        }
        long end = System.nanoTime();
        logger.debug("Fetched paths from ES in " + (end - start) / 1000000 + "ms");

        // Calculate rollup etc
        Long now = new DateTime().getMillis() * 1000;
        Long effectiveTo = Math.min(parameters.getTo(), now);
        int rollup = getRollup(parameters.getFrom(), effectiveTo);
        int period = getPeriod(parameters.getFrom(), effectiveTo);
        Long effectiveFrom = (parameters.getFrom() % rollup) == 0 ? parameters.getFrom() : parameters.getFrom() + rollup - (parameters.getFrom() % rollup);
        effectiveTo = effectiveTo - (effectiveTo % rollup);

        // now build the weird data structures ("in the meanwhile")
        final Map<Long, Integer> timestampIndices = new HashMap<>();
        Long timestamp = effectiveFrom;
        int index = 0;
        while (timestamp <= effectiveTo) {
            timestampIndices.put(timestamp, index++);
            timestamp += rollup;
        }

        final int length = timestampIndices.size();
        logger.debug("Expected number of data points in series is " + length);
        logger.debug("Expected number of series is " + paths.size());

        // Now let's query C*
        Session session = CassandraService.getInstance().getSession();
        ExecutorService executorService = MoreExecutors.listeningDecorator(Executors.newCachedThreadPool());
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
                            session.executeAsync(query, path, parameters.getTenant(), period, rollup, effectiveFrom, effectiveTo),
                            serializeFunction,
                            executorService
                    )
            );
        }
        futures = Futures.inCompletionOrder(futures);

        // Build response content JSON
        StringBuilder sb = new StringBuilder("{");
        sb.append("\"from\":").append(effectiveFrom)
                .append(",\"to\":").append(effectiveTo)
                .append(",\"step\":").append(rollup)
                .append(",\"series\":{");
        String comma = "";
        for (ListenableFuture<SinglePathResult> future : futures) {
            String path = future.get().path;
            sb.append(comma);
            comma = ",";
            sb.append("\"").append(path).append("\":");
            sb.append(future.get().json);
        }

        sb.append("}}");
        logger.debug("Finished processing query: " + parameters);
        return sb.toString();
    }

    private static int getRollup(Long from, Long to) {
        if ((to - from) / 900 > 1600) {
            return 900;
        } else {
            return 60;
        }
    }

    private static int getPeriod(Long from, Long to) {
        if ((to - from) / 900 > 1600) {
            return 69120;
        } else {
            return 89280;
        }
    }

    private static boolean isSumMetric(String path) {
        return path.startsWith("sum");

    }

    private static class SinglePathResult {
        String path;
        String json;

        private SinglePathResult(String path) {
            this.path = path;
        }

        public void makeJson(ResultSet resultSet, int length, Map<Long, Integer> timestampIndices) {
            Double values[] = new Double[length];
            for (Row row : resultSet) {
                values[timestampIndices.get(row.getLong("time"))] =
                        isSumMetric(path) ? ListUtils.sum(row.getList("data", Double.class)) : ListUtils.average(row.getList("data", Double.class));
            }

            json = new Gson().toJson(values);
        }
    }
}
