package net.iponweb.disthene.reader;

/**
 * @author Andrei Ivanov
 */
public class Configuration {

    public static int PORT = 9080;

    //C*
    public static String[] CASSANDRA_CPS = {
            "cassandra11.devops.iponweb.net",
            "cassandra12.devops.iponweb.net",
            "cassandra17.devops.iponweb.net",
            "cassandra18.devops.iponweb.net"
    };

    //ES
    public static String ES_CLUSTER_NAME = "cyanite";
    public static String ES_INDEX = "cyanite_paths";
    public static int ES_SCROLL_SIZE = 50000;
    public static int ES_TIMEOUT = 120000;
    public static String[] ES_NODES = {
            "es5.devops.iponweb.net",
            "es6.devops.iponweb.net",
            "es7.devops.iponweb.net",
            "es8.devops.iponweb.net"
    };
    public static int ES_NATIVE_PORT = 9300;

}
