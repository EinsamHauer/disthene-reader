package net.iponweb.disthene.reader.response;

/**
 * @author Andrei Ivanov
 */
public class PathsResponse {


    public static String getContent(PathsParameters parameters) {
        StringBuilder sb = new StringBuilder();
        sb.append("tenant:").append(parameters.getTenant()).append("\n");
        sb.append("query:").append(parameters.getQuery()).append("\n");
        return sb.toString();
    }
}
