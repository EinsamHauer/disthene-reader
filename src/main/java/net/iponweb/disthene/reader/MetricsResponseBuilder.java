package net.iponweb.disthene.reader;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.gson.Gson;
import net.iponweb.disthene.reader.utils.ListUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Andrei Ivanov
 */
public class MetricsResponseBuilder {
    final static Logger logger = Logger.getLogger(MetricsResponseBuilder.class);

    private static final String cassandraQuery = "SELECT time, data FROM metric.metric " +
            "where path = ? and tenant = ? and period = ? and rollup = ? " +
            "and time >= ? and time <= ? order by time";

    public static String buildResponse(String tenant, String query, long from, long to) throws Exception {
        logger.debug("Processing query " + query + " for tenant " + tenant);
        long start = System.nanoTime();
        // Build paths
        logger.debug("Fetching paths from ES");
        List<String> paths = PathsService.getInstance().getPathPaths(tenant, query);
        long end = System.nanoTime();
        logger.debug("Fetched paths from ES in " + (end - start) / 1000000 + "ms");

        Long now = new DateTime().getMillis() * 1000;
        Long effectiveTo = Math.min(to, now);
        int rollup = getRollup(from, Math.min(to, now));
        int period = getPeriod(from, Math.min(to, now));

        // now build the weird data structures ("in the meanwhile")
        Map<Long, Integer> timestampIndices = new HashMap<>();
        Long timestamp = from;
        int index = 0;
        while (timestamp <= Math.min(to, now)) {
            timestampIndices.put(timestamp, index++);
            timestamp += rollup;
        }

        int length = timestampIndices.size();
        logger.debug("Expected number of data points in series is " + length);
        logger.debug("Expected number of series is " + paths.size());

        // Now let's query C*
        Session session = CassandraService.getInstance().getSession();
        start = System.nanoTime();
        List<ListenableFuture<SinglePathResult>> futures = sendQueriesExEx(session, cassandraQuery, paths, tenant,
                period, rollup, from, to, length, timestampIndices);
        end = System.nanoTime();
        logger.debug("Submitted queries in " + (end - start) / 1000000 + "ms");


        // Get results from C* and build the response right away
        // todo: consider using stream here

        StringBuilder sb = new StringBuilder("{");
        sb.append("\"from\":").append(from)
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
        logger.debug("Finished processing query " + query + " for tenant " + tenant);
        return sb.toString();
    }

    private static List<ListenableFuture<SinglePathResult>> sendQueriesExEx(Session session, String query,
                                                                 List<String> paths, String tenant, int period, int rollup,
                                                                 long from, long to,
                                                                 final int length, final Map<Long, Integer> timestampIndices) {
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
                            session.executeAsync(query, path, tenant, period, rollup, from, to),
                            serializeFunction,
                            executorService
                    )
            );
        }

        return Futures.inCompletionOrder(futures);
    }


    private static boolean isSumMetric(String path) {
        return path.startsWith("sum");

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
