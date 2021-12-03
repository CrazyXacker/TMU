package com.crazyxacker.apps.tmu.utils;

import com.crazyxacker.apps.tmu.graalvm.features.LocalizationSupport;
import org.graalvm.nativeimage.ImageSingletons;

public class NativeUtils {

    public static boolean isNativeImage() {
        try {
            Class.forName("org.graalvm.nativeimage.ImageSingletons");
        } catch (ClassNotFoundException e) {
            return false;
        }
        return ImageSingletons.contains(LocalizationSupport.class);
    }
}
