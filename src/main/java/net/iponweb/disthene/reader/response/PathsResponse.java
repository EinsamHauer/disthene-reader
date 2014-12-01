package net.iponweb.disthene.reader.response;

import net.iponweb.disthene.reader.services.PathsService;
import org.apache.log4j.Logger;

/**
 * @author Andrei Ivanov
 */
public class PathsResponse {
    final static Logger logger = Logger.getLogger(PathsResponse.class);


    public static String getContent(PathsParameters parameters) {
        logger.debug("Processing query: " + parameters);
        return PathsService.getInstance().getPathsAsJsonArray(parameters.getTenant(),
                parameters.getQuery().equals("*") ? "[^\\.]*" : parameters.getQuery());
    }
}
