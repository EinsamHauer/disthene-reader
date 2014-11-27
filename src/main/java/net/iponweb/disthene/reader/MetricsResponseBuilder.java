package net.iponweb.disthene.reader;

import com.datastax.driver.core.*;
import com.google.gson.Gson;
import net.iponweb.disthene.reader.utils.ListUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        List<String> paths = PathsService.getInstance().getPaths(tenant, query);
        long end = System.nanoTime();
        logger.debug("Fetched paths from ES in " + (end - start) / 1000000 + "ms");

        Long now = new DateTime().getMillis() * 1000;
        Long effectiveTo = Math.min(to, now);
        int rollup = getRollup(from, Math.min(to, now));
        int period = getPeriod(from, Math.min(to, now));

        // Now let's query C*
        Session session = CassandraService.getInstance().getSession();
        PreparedStatement statement = session.prepare(cassandraQuery);
        Map<String, ResultSetFuture> futures = new HashMap<>();
        for (String path : paths) {
            BoundStatement boundStatement = statement.bind(path, tenant, period, rollup, from, Math.min(to, now));
            futures.put(path, session.executeAsync(boundStatement));
        }

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

        Gson gson = new Gson();
        // Get results from C* and build the response right away
        // Using Gson here - probably additional overhead
        // todo: consider using stream here

        StringBuilder sb = new StringBuilder("{");
        sb.append("\"from\":").append(from)
                .append(",\"to\":").append(effectiveTo)
                .append(",\"step\":").append(rollup)
                .append(",\"series\":{");

        String comma = "";
        for (Map.Entry<String, ResultSetFuture> future : futures.entrySet()) {
            String path = future.getKey();
            sb.append(comma);
            comma = ",";
            sb.append("\"").append(path).append("\":");
            ResultSet resultSet = future.getValue().getUninterruptibly();
            Double values[] = new Double[length];
            for (Row row : resultSet) {
                values[timestampIndices.get(row.getLong("time"))] =
                        isSumMetric(path) ? ListUtils.sum(row.getList("data", Double.class)) : ListUtils.average(row.getList("data", Double.class));
            }
            sb.append(gson.toJson(values));
        }

        sb.append("}}");
        logger.debug("Finished processing query " + query + " for tenant " + tenant);
        return sb.toString();
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
}
