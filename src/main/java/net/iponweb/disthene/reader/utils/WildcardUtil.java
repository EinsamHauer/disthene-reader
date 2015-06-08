package net.iponweb.disthene.reader.utils;

import org.apache.commons.lang.StringUtils;

/**
 * @author Andrei Ivanov
 */
public class WildcardUtil {

    public static String getRegExFromWildcard(String wildcard) {
        return wildcard.replace(".", "\\.").replace("*", ".*").replace("{", "(")
                .replace("}", ")").replace(",", "|");
    }

    public static String getPathsRegExFromWildcard(String wildcard) {
        return wildcard.replace(".", "\\.").replace("*", "[^\\.]*").replace("{", "(")
                .replace("}", ")").replace(",", "|");
    }

    public static int getPathDepth(String wildcard) {
        return StringUtils.countMatches(wildcard, ".") + 1;
    }
}
