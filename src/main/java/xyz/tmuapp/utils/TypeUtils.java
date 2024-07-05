package xyz.tmuapp.utils;

public class TypeUtils {

    public static int getIntDef(String value, int def) {
        if (value == null) {
            return def;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            return def;
        }
    }

    public static boolean getBoolDef(String value, boolean def) {
        if (value == null) {
            return def;
        }
        try {
            return Boolean.parseBoolean(value);
        } catch (NumberFormatException ex) {
            return def;
        }
    }
}
