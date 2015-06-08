package net.iponweb.disthene.reader;

import net.iponweb.disthene.reader.config.DistheneReaderConfiguration;
import net.iponweb.disthene.reader.handler.MetricsHandler;
import net.iponweb.disthene.reader.handler.PathsHandler;
import net.iponweb.disthene.reader.server.ReaderServer;
import net.iponweb.disthene.reader.service.index.IndexService;
import net.iponweb.disthene.reader.service.metric.MetricService;
import net.iponweb.disthene.reader.service.store.CassandraService;
import org.apache.commons.cli.*;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.yaml.snakeyaml.Yaml;
import sun.misc.Signal;
import sun.misc.SignalHandler;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * @author Andrei Ivanov
 */
public class DistheneReader {

    private static final String DEFAULT_CONFIG_LOCATION = "/etc/disthene/disthene-reader.yaml";
    private static final String DEFAULT_LOG_CONFIG_LOCATION = "/etc/disthene/disthene-reader-log4j.xml";

    private static final String METRICS_PATH = "/metrics";
    private static final String PATHS_PATH = "/paths";
    private static final String PING_PATH = "/ping";

    private static Logger logger;

    private String configLocation;
    private ReaderServer readerServer;
    private IndexService indexService;
    private CassandraService cassandraService;
    private MetricService metricService;


    public DistheneReader(String configLocation) {
        this.configLocation = configLocation;
    }

    private void run() {
        try {
            Yaml yaml = new Yaml();
            InputStream in = Files.newInputStream(Paths.get(configLocation));
            DistheneReaderConfiguration distheneReaderConfiguration = yaml.loadAs(in, DistheneReaderConfiguration.class);
            in.close();
            logger.info("Running with the following config: " + distheneReaderConfiguration.toString());

            logger.info("Creating reader");
            readerServer = new ReaderServer(distheneReaderConfiguration.getReader());

            logger.info("Creating index service");
            indexService = new IndexService(distheneReaderConfiguration.getIndex());

            logger.info("Creating C* service");
            cassandraService = new CassandraService(distheneReaderConfiguration.getStore());

            logger.info("Creating metric service");
            metricService = new MetricService(indexService, cassandraService, distheneReaderConfiguration);

            logger.info("Creating paths handler");
            PathsHandler pathsHandler = new PathsHandler(indexService);
            readerServer.registerHandler(PATHS_PATH, pathsHandler);

            logger.info("Creating metrics handler");
            MetricsHandler metricsHandler = new MetricsHandler(metricService);
            readerServer.registerHandler(METRICS_PATH, metricsHandler);

            logger.info("Starting reader");
            readerServer.run();

            Signal.handle(new Signal("TERM"), new SigtermSignalHandler());
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

        CommandLineParser parser = new GnuParser();

        try {
            CommandLine commandLine = parser.parse(options, args);
            System.getProperties().setProperty("log4j.configuration", "file:" + commandLine.getOptionValue("l", DEFAULT_LOG_CONFIG_LOCATION));
            logger = Logger.getLogger(DistheneReader.class);

            new DistheneReader(commandLine.getOptionValue("c", DEFAULT_CONFIG_LOCATION)).run();

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

            logger.info("Shutdown complete");

            System.exit(0);
        }
    }
}
