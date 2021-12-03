package com.crazyxacker.apps.tmu.managers;

import com.crazyxacker.apps.tmu.graalvm.features.LocalizationSupport;
import com.crazyxacker.apps.tmu.utils.NativeUtils;
import com.crazyxacker.apps.tmu.utils.StringUtils;
import lombok.Getter;
import org.graalvm.nativeimage.ImageSingletons;

import java.util.Locale;
import java.util.ResourceBundle;

public class LocaleManager {
    @Getter
    private static ResourceBundle resourceBundle;

    public static void loadResourceBundle() {
        if (NativeUtils.isNativeImage()) {
            // Only when compiled to native image
            LocalizationSupport bundles = ImageSingletons.lookup(LocalizationSupport.class);
            resourceBundle = bundles.getResourceBundle("language", java.util.Locale.getDefault());
        } else {
            // Normal JVM run
            resourceBundle = ResourceBundle.getBundle("language", java.util.Locale.getDefault());
        }
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
