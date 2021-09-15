package net.iponweb.disthene.reader.utils;

import org.apache.commons.lang.StringUtils;

/**
 * @author Andrei Ivanov
 */
public class WildcardUtil {

    public static boolean isPlainPath(String path) {
        char[] noPlainChars = {'*', '?', '{', '(', '['};
        return !(StringUtils.containsAny(path, noPlainChars));
    }

    public static String getPathsRegExFromWildcard(String wildcard) {
        return wildcard.replace(".", "\\.")
                .replace("*", "[^\\.]*")
                .replace("{", "(")
                .replace("}", ")")
                .replace(",", "|")
                .replace("?", "[^\\.]");
    }

}
