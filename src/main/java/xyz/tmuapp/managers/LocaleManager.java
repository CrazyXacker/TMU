package xyz.tmuapp.managers;

import lombok.Getter;
import xyz.tmuapp.utils.StringUtils;

import java.util.Locale;
import java.util.ResourceBundle;

public class LocaleManager {
    @Getter
    private static ResourceBundle resourceBundle;

    public static void loadResourceBundle() {
        resourceBundle = ResourceBundle.getBundle("language", java.util.Locale.getDefault());
    }

    public static void setLocale(String langCode) {
        if (StringUtils.isNotEmpty(langCode)) {
            Locale.setDefault(new Locale(langCode));
        }
    }

    public static String getString(String key) {
        return resourceBundle.getString(key);
    }

    public static String getString(String key, Object... formatArgs) {
        return String.format(getString(key), formatArgs);
    }

    public static boolean isContainsKey(String key) {
        return resourceBundle.containsKey(key);
    }
}
