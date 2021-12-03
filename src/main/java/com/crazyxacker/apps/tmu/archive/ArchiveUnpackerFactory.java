package com.crazyxacker.apps.tmu.archive;

import com.crazyxacker.apps.tmu.helpers.ContentDetector;
import com.crazyxacker.apps.tmu.utils.NativeUtils;

import java.io.File;

public class ArchiveUnpackerFactory {

    public static IArchiveUnpacker create(File file) {
        if (NativeUtils.isNativeImage() && ContentDetector.isZipFile(file)) {
            return Zip.create();
        } else if (ContentDetector.isArchiveFile(file)) {
            return SevenZip.create();
        }
        return null;
    }
}
