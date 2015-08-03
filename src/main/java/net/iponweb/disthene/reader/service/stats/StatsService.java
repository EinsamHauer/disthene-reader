package net.iponweb.disthene.reader.service.stats;

import com.google.common.util.concurrent.AtomicDouble;
import net.iponweb.disthene.reader.config.StatsConfiguration;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Andrei Ivanov
 */
public class StatsService {
    private static final String SCHEDULER_NAME = "distheneReaderStatsFlusher";

    private Logger logger = Logger.getLogger(StatsService.class);

    private StatsConfiguration statsConfiguration;

    private ConcurrentMap<String, StatsRecord> stats = new ConcurrentHashMap<>();

    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public StatsService(StatsConfiguration statsConfiguration) {
        this.statsConfiguration = statsConfiguration;

        scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                flush();
            }
        }, 60 - ((System.currentTimeMillis() / 1000L) % 60), statsConfiguration.getInterval(), TimeUnit.SECONDS);
    }

    private StatsRecord getStatsRecord(String tenant) {
        StatsRecord statsRecord = stats.get(tenant);
        if (statsRecord == null) {
            StatsRecord newStatsRecord = new StatsRecord();
            statsRecord = stats.putIfAbsent(tenant, newStatsRecord);
            if (statsRecord == null) {
                statsRecord = newStatsRecord;
            }
        }

        return statsRecord;
    }

    public void incRenderRequests(String tenant) {
        getStatsRecord(tenant).incRenderRequests();
    }

    public void incRenderPointsRead(String tenant, int inc) {
        getStatsRecord(tenant).incRenderPointsRead(inc);
    }

    public void incRenderPathsRead(String tenant, int inc) {
        getStatsRecord(tenant).incRenderPathsRead(inc);
    }

    public void incPathsRequests(String tenant) {
        getStatsRecord(tenant).incPathsRequests();
    }

    public void incThrottleTime(String tenant, double value) {
        getStatsRecord(tenant).incThrottled(value);
    }


    private synchronized void flush() {
        Map<String, StatsRecord> statsToFlush = new HashMap<>();

        for (ConcurrentMap.Entry<String, StatsRecord> entry : stats.entrySet()) {
            statsToFlush.put(entry.getKey(), entry.getValue().reset());
        }



        long timestamp = DateTime.now(DateTimeZone.UTC).withSecondOfMinute(0).withMillisOfSecond(0).getMillis() / 1000L;

        try {
            Socket connection = new Socket(statsConfiguration.getCarbonHost(), statsConfiguration.getCarbonPort());
            DataOutputStream dos = new DataOutputStream(connection.getOutputStream());

            long totalRenderRequests = 0;
            long totalRenderPathsRead = 0;
            long totalRenderPointsRead = 0;
            long totalPathsRequests = 0;
            double totalThrottled = 0;

            for (Map.Entry<String, StatsRecord> entry : statsToFlush.entrySet()) {
                String tenant = entry.getKey();
                StatsRecord statsRecord = entry.getValue();

                totalRenderRequests += statsRecord.getRenderRequests();
                totalRenderPathsRead += statsRecord.getRenderPathsRead();
                totalRenderPointsRead += statsRecord.getRenderPointsRead();
                totalPathsRequests += statsRecord.getPathsRequests();
                totalThrottled += statsRecord.getThrottled();

                dos.writeBytes(statsConfiguration.getHostname() + ".disthene-reader.tenants." + tenant + ".render_requests " + statsRecord.getRenderRequests() + " " + timestamp + " " + statsConfiguration.getTenant() + "\n");
                dos.writeBytes(statsConfiguration.getHostname() + ".disthene-reader.tenants." + tenant + ".render_paths_read " + statsRecord.getRenderPathsRead() + " " + timestamp + " " + statsConfiguration.getTenant() + "\n");
                dos.writeBytes(statsConfiguration.getHostname() + ".disthene-reader.tenants." + tenant + ".render_points_read " + statsRecord.getRenderPointsRead() + " " + timestamp + " " + statsConfiguration.getTenant() + "\n");
                dos.writeBytes(statsConfiguration.getHostname() + ".disthene-reader.tenants." + tenant + ".paths_requests " + statsRecord.getPathsRequests() + " " + timestamp + " " + statsConfiguration.getTenant() + "\n");
                dos.writeBytes(statsConfiguration.getHostname() + ".disthene-reader.tenants." + tenant + ".throttled " + statsRecord.getThrottled() + " " + timestamp + " " + statsConfiguration.getTenant() + "\n");
            }

            dos.writeBytes(statsConfiguration.getHostname() + ".disthene-reader.render_requests " + totalRenderRequests + " " + timestamp + " " + statsConfiguration.getTenant());
            dos.writeBytes(statsConfiguration.getHostname() + ".disthene-reader.render_paths_read " + totalRenderPathsRead + " " + timestamp + " " + statsConfiguration.getTenant());
            dos.writeBytes(statsConfiguration.getHostname() + ".disthene-reader.render_points_read " + totalRenderPointsRead + " " + timestamp + " " + statsConfiguration.getTenant());
            dos.writeBytes(statsConfiguration.getHostname() + ".disthene-reader.paths_requests " + totalPathsRequests + " " + timestamp + " " + statsConfiguration.getTenant());
            dos.writeBytes(statsConfiguration.getHostname() + ".disthene-reader.throttled " + totalThrottled + " " + timestamp + " " + statsConfiguration.getTenant());

            dos.flush();
            connection.close();
        } catch (Exception e) {
            logger.error("Failed to send stats", e);
        }
    }

    public synchronized void shutdown() {
        scheduler.shutdown();
    }

    private class StatsRecord {
        private AtomicLong renderRequests = new AtomicLong(0);
        private AtomicLong renderPathsRead = new AtomicLong(0);
        private AtomicLong renderPointsRead = new AtomicLong(0);
        private AtomicLong pathsRequests = new AtomicLong(0);
        private AtomicDouble throttled = new AtomicDouble(0);

        public StatsRecord() {
        }



        public StatsRecord(long renderRequests, long renderPathsRead, long renderPointsRead, long pathsRequests, double throttled) {
            this.renderRequests = new AtomicLong(renderRequests);
            this.renderPathsRead = new AtomicLong(renderPathsRead);
            this.renderPointsRead = new AtomicLong(renderPointsRead);
            this.pathsRequests = new AtomicLong(pathsRequests);
            this.throttled = new AtomicDouble(throttled);
        }

        /**
         * Resets the stats to zeroes and returns a snapshot of the record
         * @return snapshot of the record
         */
        public StatsRecord reset() {
            return new StatsRecord(renderRequests.getAndSet(0), renderPathsRead.getAndSet(0), renderPointsRead.getAndSet(0), pathsRequests.getAndSet(0), throttled.getAndSet(0));
        }

        public void incRenderRequests() {
            renderRequests.addAndGet(1);
        }

        public void incRenderPathsRead(int inc) {
            renderPathsRead.addAndGet(inc);
        }

        public void incRenderPointsRead(int inc) {
            renderPointsRead.addAndGet(inc);
        }

        public void incPathsRequests() {
            pathsRequests.addAndGet(1);
        }

        public void incThrottled(double value) {
            throttled.addAndGet(value);
        }

        public long getRenderRequests() {
            return renderRequests.get();
        }

        public long getRenderPathsRead() {
            return renderPathsRead.get();
        }

        public long getRenderPointsRead() {
            return renderPointsRead.get();
        }

        public long getPathsRequests() {
            return pathsRequests.get();
        }

        public double getThrottled() {
            return throttled.get();
        }
    }

}
