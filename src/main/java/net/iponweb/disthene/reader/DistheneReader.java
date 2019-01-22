package net.iponweb.disthene.reader;

import net.iponweb.disthene.reader.config.DistheneReaderConfiguration;
import net.iponweb.disthene.reader.config.ThrottlingConfiguration;
import net.iponweb.disthene.reader.handler.*;
import net.iponweb.disthene.reader.server.ReaderServer;
import net.iponweb.disthene.reader.service.index.IndexService;
import net.iponweb.disthene.reader.service.metric.MetricService;
import net.iponweb.disthene.reader.service.stats.StatsService;
import net.iponweb.disthene.reader.service.store.CassandraService;
import net.iponweb.disthene.reader.service.throttling.ThrottlingService;
import org.apache.commons.cli.*;
import org.apache.log4j.Logger;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.representer.Representer;
import sun.misc.Signal;
import sun.misc.SignalHandler;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * @author Andrei Ivanov
 */
public class DistheneReader {

    private static final String DEFAULT_CONFIG_LOCATION = "/etc/disthene-reader/disthene-reader.yaml";
    private static final String DEFAULT_LOG_CONFIG_LOCATION = "/etc/disthene-reader/disthene-reader-log4j.xml";
    private static final String DEFAULT_THROTTLING_CONFIG_LOCATION = "/etc/disthene-reader/throttling.yaml";

    private static final String METRICS_PATH = "^/metrics\\/?$";
    private static final String PATHS_PATH = "^/paths\\/?$";
    private static final String PING_PATH = "^/ping\\/?$";
    private static final String RENDER_PATH = "^/render\\/?$";
    private static final String SEARCH_PATH = "^/search\\/?$";
    private static final String PATHS_STATS_PATH = "^/path_stats\\/?$";

    private static Logger logger;

    private String configLocation;
    private String throttlingConfigLocation;
    private ReaderServer readerServer;
    private IndexService indexService;
    private CassandraService cassandraService;
    private MetricService metricService;
    private StatsService statsService;
    private ThrottlingService throttlingService;

    private static Yaml yaml;
    static {
        Representer representer = new Representer();
        representer.getPropertyUtils().setSkipMissingProperties(true);
        yaml = new Yaml(representer);
    }


    private DistheneReader(String configLocation, String throttlingConfigLocation) {
        this.configLocation = configLocation;
        this.throttlingConfigLocation = throttlingConfigLocation;
    }

    private void run() {
        try {
            InputStream in = Files.newInputStream(Paths.get(configLocation));
            DistheneReaderConfiguration distheneReaderConfiguration = yaml.loadAs(in, DistheneReaderConfiguration.class);
            in.close();
            logger.info("Running with the following config: " + distheneReaderConfiguration.toString());

            ThrottlingConfiguration throttlingConfiguration;
            File file = new File(throttlingConfigLocation);
            if(file.exists() && !file.isDirectory()) {
                logger.info("Loading throttling rules");
                in = Files.newInputStream(Paths.get(throttlingConfigLocation));
                throttlingConfiguration = yaml.loadAs(in, ThrottlingConfiguration.class);
                in.close();
            } else {
                throttlingConfiguration = new ThrottlingConfiguration();
            }

            logger.debug("Running with the following throttling configuration: " + throttlingConfiguration.toString());
            logger.info("Creating throttling");
            throttlingService = new ThrottlingService(throttlingConfiguration);

            logger.info("Creating stats");
            statsService = new StatsService(distheneReaderConfiguration.getStats());

            logger.info("Creating reader");
            readerServer = new ReaderServer(distheneReaderConfiguration.getReader());

            logger.info("Creating index service");
            indexService = new IndexService(distheneReaderConfiguration.getIndex());

            logger.info("Creating C* service");
            cassandraService = new CassandraService(distheneReaderConfiguration.getStore());

            logger.info("Creating metric service");
            metricService = new MetricService(indexService, cassandraService, statsService, distheneReaderConfiguration);

            logger.info("Creating paths handler");
            PathsHandler pathsHandler = new PathsHandler(indexService, statsService);
            readerServer.registerHandler(PATHS_PATH, pathsHandler);

            logger.info("Creating metrics handler");
            MetricsHandler metricsHandler = new MetricsHandler(metricService);
            readerServer.registerHandler(METRICS_PATH, metricsHandler);

            logger.info("Creating ping handler");
            PingHandler pingHandler = new PingHandler();
            readerServer.registerHandler(PING_PATH, pingHandler);

            logger.info("Creating render handler");
            RenderHandler renderHandler = new RenderHandler(metricService, statsService, throttlingService, distheneReaderConfiguration.getReader());
            readerServer.registerHandler(RENDER_PATH, renderHandler);

            logger.info("Creating search handler");
            SearchHandler searchHandler = new SearchHandler(indexService, statsService);
            readerServer.registerHandler(SEARCH_PATH, searchHandler);

            logger.info("Creating path stats handler");
            PathStatsHandler pathStatsHandler = new PathStatsHandler(indexService, statsService);
            readerServer.registerHandler(PATHS_STATS_PATH, pathStatsHandler);

            logger.info("Starting reader");
            readerServer.run();

            Signal.handle(new Signal("TERM"), new SigtermSignalHandler());
            Signal.handle(new Signal("HUP"), new SighupSignalHandler());
        } catch (IOException e) {
            logger.error(e);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Options options = new Options();
        options.addOption("c", "config", true, "config location");
        options.addOption("l", "log-config", true, "log config location");
        options.addOption("t", "throttling-config", true, "throttling config location");

        CommandLineParser parser = new GnuParser();

        try {
            CommandLine commandLine = parser.parse(options, args);
            System.getProperties().setProperty("log4j.configuration", "file:" + commandLine.getOptionValue("l", DEFAULT_LOG_CONFIG_LOCATION));
            logger = Logger.getLogger(DistheneReader.class);

            new DistheneReader(commandLine.getOptionValue("c", DEFAULT_CONFIG_LOCATION), commandLine.getOptionValue("t", DEFAULT_THROTTLING_CONFIG_LOCATION)).run();

        } catch (ParseException e) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("Disthene", options);
        } catch (Exception e) {
            System.out.println("Start failed");
            e.printStackTrace();
        }

    }

    private class SigtermSignalHandler implements SignalHandler {

        @Override
        public void handle(Signal signal) {
            logger.info("Shutting down carbon server");
            readerServer.shutdown();

            logger.info("Shutting down index service");
            indexService.shutdown();

            logger.info("Shutting down C* service");
            cassandraService.shutdown();

            logger.info("Shutting down stats service");
            statsService.shutdown();

            logger.info("Shutdown complete");

            System.exit(0);
        }
    }

    private class SighupSignalHandler implements SignalHandler {

        @Override
        public void handle(Signal signal) {
            logger.info("Received sighup");

            logger.info("Reloading throttling configuration");
            try {
                ThrottlingConfiguration throttlingConfiguration;
                File file = new File(throttlingConfigLocation);
                if(file.exists() && !file.isDirectory()) {
                    logger.info("Loading throttling configuration");
                    InputStream in = Files.newInputStream(Paths.get(throttlingConfigLocation));
                    throttlingConfiguration = yaml.loadAs(in, ThrottlingConfiguration.class);
                    in.close();
                } else {
                    throttlingConfiguration = new ThrottlingConfiguration();
                }

                throttlingService.reload(throttlingConfiguration);

                logger.debug("Running with the following throttling configuration: " + throttlingConfiguration.toString());
            } catch (Exception e) {
                logger.error("Reloading throttling configuration failed");
                logger.error(e);
            }
        }
    }


}
