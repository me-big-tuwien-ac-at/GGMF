package com.modcmga.backendservice.util;

/**
 * Provides String functionalities methods.
 */
public final class StringUtil {
    private final static String CAMEL_CASE_REGEX = "(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])";

    /**
     * Splits the string {@code s} in camel or title case into separate words.
     * @param s the string to be altered
     * @return the string in separate words.
     */
    public static String separateCamelOrTitleCase(String s) {
        var words = s.split(CAMEL_CASE_REGEX);

        return String.join(" ", words);
    }
}
