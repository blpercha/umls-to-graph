package utils;

public class TextFormatUtils {
    public static String insertPeriods(String code) {
        if (code.matches("^[A-Z][0-9]{2}$") || code.matches("^[0-9]{3}$")) {
            return code;
        }
        if (code.matches("^[0-9]{4,5}$") || code.matches("^[A-Z][0-9]{3,5}$") ||
                code.matches("^[A-Z][0-9][A-Z][A-Z0-9]{1,4}$") || code.matches("^[A-Z][0-9]{2}[A-Z0-9]{1,4}$")) {
            return code.substring(0, 3) + "." + code.substring(3);
        }
        throw new IllegalArgumentException("Can't parse code: " + code);
    }
}
