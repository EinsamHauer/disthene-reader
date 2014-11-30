package net.iponweb.disthene.reader;


import com.google.gson.Gson;
import net.iponweb.disthene.reader.services.PathsService;
import org.apache.log4j.Logger;

/**
 * @author Andrei Ivanov
 */
public class PathsResponseBuilder {

    final static Logger logger = Logger.getLogger(MetricsResponseBuilder.class);

    public static String buildResponse(String tenant, String query) throws Exception {
        return new Gson().toJson(PathsService.getInstance().getPaths(tenant, query));
    }

}
