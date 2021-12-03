package com.crazyxacker.apps.tmu.graalvm.features;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

public class LocalizationSupport {
    protected final Map<String, ResourceBundle> bundles = new HashMap<>();

    public LocalizationSupport() {
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    private String getBundleLocalizedName(String bundleName, Locale locale) {
        String localizedName = bundleName + "_" + locale.getLanguage();
        return localizedName;
    }

    public ResourceBundle getResourceBundle(String bundleName, Locale locale) {
        return bundles.get(getBundleLocalizedName(bundleName, locale));
    }

    @SuppressWarnings("UnusedReturnValue")
    public LocalizationSupport addResourceBundle(ResourceBundle rb, Locale locale) {
        bundles.put(getBundleLocalizedName(rb.getBaseBundleName(), locale), rb);
        return this;
    }
}
