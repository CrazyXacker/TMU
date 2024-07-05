package xyz.tmuapp.helpers;

import org.apache.tika.config.TikaConfig;
import org.apache.tika.exception.TikaException;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import xyz.tmuapp.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ContentDetector {
    private static TikaConfig tika;
    private static final List<String> ARCHIVE_MEDIA_TYPES = new ArrayList<>();
    private static final List<String> IMAGE_MEDIA_TYPES = new ArrayList<>();

    public static String detectMediaType(Path path) {
        Metadata metadata = new Metadata();
        metadata.add(Metadata.RESOURCE_NAME_KEY, path.getFileName().toString());

        TikaInputStream tikaInputStream = null;
        try {
            return tika.getDetector().detect(tikaInputStream = TikaInputStream.get(path), metadata).toString();
        } catch (Exception ex) {
            return "unknown";
        } finally {
            FileUtils.closeQuietly(tikaInputStream);
        }
    }

    public static boolean isArchiveFile(File file) {
        String mediaType = detectMediaType(file.toPath());
        return ARCHIVE_MEDIA_TYPES.contains(mediaType);
    }

    public static boolean isSevenZipOrRarFile(File file) {
        String mediaType = detectMediaType(file.toPath());
        return !"application/zip".equalsIgnoreCase(mediaType) && ARCHIVE_MEDIA_TYPES.contains(mediaType);
    }

    public static boolean isZipFile(File file) {
        String mediaType = detectMediaType(file.toPath());
        return "application/zip".equalsIgnoreCase(mediaType);
    }

    public static boolean isImageFile(File file) {
        String mediaType = detectMediaType(file.toPath());
        return IMAGE_MEDIA_TYPES.contains(mediaType);
    }

    static {
        ARCHIVE_MEDIA_TYPES.add("application/zip");
        ARCHIVE_MEDIA_TYPES.add("application/x-rar-compressed");
        ARCHIVE_MEDIA_TYPES.add("application/x-rar-compressed; version=4");
        ARCHIVE_MEDIA_TYPES.add("application/x-7z-compressed");

        IMAGE_MEDIA_TYPES.add("image/png");
        IMAGE_MEDIA_TYPES.add("image/jpeg");
        IMAGE_MEDIA_TYPES.add("image/jpg");
        IMAGE_MEDIA_TYPES.add("image/gif");
        IMAGE_MEDIA_TYPES.add("image/webp");

        try {
            tika = new TikaConfig();
        } catch (TikaException | IOException e) {
            e.printStackTrace();
        }
    }
}