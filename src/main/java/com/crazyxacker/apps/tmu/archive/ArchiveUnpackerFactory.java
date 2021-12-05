package com.crazyxacker.apps.tmu.archive;

import com.crazyxacker.apps.tmu.helpers.ContentDetector;

import java.io.File;

public class ArchiveUnpackerFactory {

    public static IArchiveUnpacker create(File file) {
        if (ContentDetector.isZipFile(file)) {
            return Zip.create();
        } else if (ContentDetector.isSevenZipOrRarFile(file)) {
            return SevenZip.create();
        }
        return null;
    }
}
