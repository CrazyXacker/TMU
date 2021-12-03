package com.crazyxacker.apps.tmu.graalvm.features;

import com.oracle.svm.core.annotate.AutomaticFeature;
import org.graalvm.nativeimage.ImageSingletons;
import org.graalvm.nativeimage.hosted.Feature;
import org.graalvm.nativeimage.hosted.RuntimeClassInitialization;

import java.text.DecimalFormatSymbols;
import java.util.Calendar;
import java.util.Locale;
import java.util.ResourceBundle;

@SuppressWarnings("unused")
@AutomaticFeature
public class LocalizationResourcesFeature implements Feature {

    @Override
    public void beforeAnalysis(Feature.BeforeAnalysisAccess access) {
        // work around for the fact that com.oracle.svm.core.jdk.LocalizationSupport only uses the default locale.
        // replaces --initialize-at-build-time=features.MyLocalizationSupport
        RuntimeClassInitialization.initializeAtBuildTime(LocalizationSupport.class);

        // This code runs at image buildtime and is not reachable at runtime.
        LocalizationSupport bundles = new LocalizationSupport();
        bundles.addResourceBundle(ResourceBundle.getBundle("language", Locale.ENGLISH), Locale.ENGLISH);
        bundles.addResourceBundle(ResourceBundle.getBundle("language", new Locale("ru", "RU")), new Locale("ru", "RU"));

        ImageSingletons.add(LocalizationSupport.class, bundles);
    }

    @SuppressWarnings("unused")
    @Override
    public void afterRegistration(AfterRegistrationAccess access) {
        /* This code runs during image generation => pre-initialize whatever you need */
        // avoid null pointer exception when loading at runtime (Graal issue 1645)
        DecimalFormatSymbols dfsymsEnglish = DecimalFormatSymbols.getInstance(Locale.ENGLISH);
        DecimalFormatSymbols dfsymsRU = DecimalFormatSymbols.getInstance(new Locale("ru", "RU"));

        Calendar calendarEnglish = Calendar.getInstance(Locale.ENGLISH);
        Calendar calendarRU = Calendar.getInstance(new Locale("ru", "RU"));
    }

    @Override
    public void afterCompilation(AfterCompilationAccess access) {
        LocalizationSupport support = ImageSingletons.lookup(LocalizationSupport.class);
        access.registerAsImmutable(support, LocalizationResourcesFeature::isImmutable);
    }

    private static boolean isImmutable(Object object) {
        if (object instanceof java.util.Locale) {
            /* These classes have a mutable hash code field. */
            return false;
        }
        if (object instanceof java.util.Map) {
            /* The maps have lazily initialized cache fields (see JavaUtilSubstitutions). */
            return false;
        }
        return true;
    }
}