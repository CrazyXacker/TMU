package xyz.tmuapp.archive;

import xyz.tmuapp.helpers.ContentDetector;

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
