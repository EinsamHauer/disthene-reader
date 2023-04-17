package net.iponweb.disthene.reader.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * @author Andrei Ivanov
 */
public class WildcardUtil {

    public static boolean isPlainPath(String path) {
        char[] noPlainChars = {'*', '?', '{', '(', '['};
        return !(StringUtils.containsAny(path, noPlainChars));
    }

    public static String getPathsRegExFromWildcard(String wildcard) {
        return wildcard.replace(".", "\\.").replace("*", "[^\\.]*").replace("{", "(")
                .replace("}", ")").replace(",", "|").replace("?", "[^\\.]");
    }

    public static boolean regexIsValid(String regex) {
        try {
            Pattern.compile(regex);
            return true;
        } catch (PatternSyntaxException exception) {
            return false;
        }
    }

}
