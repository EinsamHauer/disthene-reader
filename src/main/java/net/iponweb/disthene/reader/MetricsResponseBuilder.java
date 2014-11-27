package net.iponweb.disthene.reader;

import com.datastax.driver.core.*;
import com.google.gson.Gson;
import net.iponweb.disthene.reader.utils.ListUtils;
import org.joda.time.DateTime;

import java.util.*;

/**
 * @author Andrei Ivanov
 */
public class MetricsResponseBuilder {

    private static final String cassandraQuery = "SELECT time, data FROM metric.metric " +
            "where path = ? and tenant = ? and period = ? and rollup = ? " +
            "and time >= ? and time <= ? ";


    public static String buildResponse(String tenant, String query, long from, long to) throws Exception {
        // Build paths
        List<String> paths = PathsService.getInstance().getPaths(tenant, query);

        Long now = new DateTime().getMillis() * 1000;
        int rollup = getRollup(from, Math.min(to, now));
        int period = getPeriod(from, Math.min(to, now));

        // Now let's query C*
        Session session = CassandraService.getInstance().getSession();
        PreparedStatement statement = session.prepare(cassandraQuery);
        Map<String, ResultSetFuture> futures = new HashMap<>();
        for(String path : paths) {
            BoundStatement boundStatement = statement.bind(path, tenant, period, rollup, from, Math.min(to, now));
            futures.put(path, session.executeAsync(boundStatement));
        }

        // now build the weird data structure ("in the meanwhile")
        Map<String, Map<Long, Double>> data = new HashMap<>();

        for(String path : paths) {
            Map<Long, Double> pathData = new TreeMap<>();
            data.put(path, pathData);

            Long currentTimestamp = from;

            while (currentTimestamp <= Math.min(to, now)) {
                pathData.put(currentTimestamp, null);
                currentTimestamp += rollup;
            }

        }

        // Get results from C*
        for(Map.Entry<String, ResultSetFuture> future : futures.entrySet()) {
            String path = future.getKey();
            ResultSet resultSet = future.getValue().getUninterruptibly();
            for(Row row : resultSet) {
                Long point = row.getLong("time");
                Double value = isSumMetric(path) ?  ListUtils.sum(row.getList("data", Double.class)) : ListUtils.average(row.getList("data", Double.class));
                data.get(path).put(point, value);
            }
        }


        //debug output:
/*
        StringBuilder sb = new StringBuilder();
        sb.append(tenant).append("/").append(query).append("/").append(from).append("/").append(to).append(":");
        for (String path : paths) {
            sb.append(path).append("\n");
        }

        sb.append("================================================================\n");


        return sb.toString();
*/

        MetricsResponse metricsResponse = new MetricsResponse(from, Math.min(to, now), rollup, data);
        Gson gson = new Gson();
        return gson.toJson(metricsResponse);

    }

    private static boolean isSumMetric(String path) {
        return path.startsWith("sum");

    }

    private static int getRollup(Long from, Long to) {
        if ((from-to) / 900 > 1600) {
            return 900;
        } else {
            return 60;
        }
    }

    private static int getPeriod(Long from, Long to) {
        if ((from-to) / 900 > 1600) {
            return 69120;
        } else {
            return 89280;
        }
    }

    private static class MetricsResponse {

        private Long from;
        private Long to;
        private int step;
        private Map<String, Collection<Double>> series;

        private MetricsResponse(Long from, Long to, int step, Map<String, Map<Long, Double>> data) {
            this.from = from;
            this.to = to;
            this.step = step;
            this.series = new HashMap<>();

            for(Map.Entry<String, Map<Long, Double>> entry : data.entrySet()) {
                series.put(entry.getKey(), entry.getValue().values());
            }
        }

        public Long getFrom() {
            return from;
        }

        public void setFrom(Long from) {
            this.from = from;
        }

        public Long getTo() {
            return to;
        }

        public void setTo(Long to) {
            this.to = to;
        }

        public int getStep() {
            return step;
        }

        public void setStep(int step) {
            this.step = step;
        }

        public Map<String, Collection<Double>> getSeries() {
            return series;
        }

        public void setSeries(Map<String, Collection<Double>> series) {
            this.series = series;
        }
    }
}
