package net.iponweb.disthene.reader;

/**
 * @author Andrei Ivanov
 */
public class Configuration {

    public static int PORT = 9080;

    //ES
    public static String ES_CLUSTER_NAME = "cyanite";
    public static String ES_INDEX = "cyanite_paths";
    public static int ES_SCROLL_SIZE = 100;
    public static int ES_TIMEOUT = 120000;
    public static String[] ES_NODES = {
            "es5.devops.iponweb.net",
            "es6.devops.iponweb.net",
            "es7.devops.iponweb.net",
            "es8.devops.iponweb.net"
    };
    public static int ES_NATIVE_PORT = 9300;

}
