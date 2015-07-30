package net.iponweb.disthene.reader.utils;

/**
 * @author Andrei Ivanov
 */
public class WildcardUtil {

    public static String getPathsRegExFromWildcard(String wildcard) {
        return wildcard.replace(".", "\\.").replace("*", "[^\\.]*").replace("{", "(")
                .replace("}", ")").replace(",", "|").replace("?", "[^\\.]");
    }

}
