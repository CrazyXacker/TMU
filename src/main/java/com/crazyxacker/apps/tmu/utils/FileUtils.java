package com.crazyxacker.apps.tmu.utils;

import com.crazyxacker.apps.tmu.archive.IArchiveUnpacker;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {
    public static final String[] ALLOWED_IMAGE_EXTENSIONS = new String[]{"gif", "jpeg", "jpg", "png", "webp"};

    public static boolean isDirectory(File file) {
        return isFileExist(file) && file.isDirectory();
    }

    public static boolean isFileExist(File file) {
        return file != null && file.exists();
    }

    public static String addPathSlash(String str) {
        if (StringUtils.isNotEmpty(str) && !str.endsWith("/") && !str.endsWith(File.separator)) {
            return str + File.separator;
        }
        return str;
    }

    public static String getPath(String path) {
        int indexSlash = path.lastIndexOf("\\");
        int indexBackSlash = path.lastIndexOf("/");

        if (indexSlash >= 0) {
            return path.substring(0, indexSlash);
        } else if (indexBackSlash >= 0) {
            return path.substring(0, indexBackSlash);
        }

        return "";
    }

    public static String getFileName(String path) {
        int indexSlash = path.lastIndexOf(File.separator);
        int indexDot = path.lastIndexOf(".");

        if (indexSlash >= 0 && indexDot > indexSlash) {
            return path.substring(indexSlash + 1, indexDot);
        }
        if (indexSlash >= 0) {
            return path.substring(indexSlash + 1);
        }
        if (indexDot >= 0) {
            return path.substring(0, indexDot);
        }

        return path;
    }

    public static String getFileNameWithExt(String path) {
        int indexSlash = path.lastIndexOf(File.separator);
        return indexSlash >= 0 ? path.substring(indexSlash + 1) : path;
    }

    public static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException ignored) {
                // ignore
            }
        }
    }

    public static boolean deleteDirectory(File directory) {
        boolean result = false;
        if (directory != null && directory.exists()) {
            result = true;
            File[] listFiles = directory.listFiles();
            if (ArrayUtils.isNotEmpty(listFiles)) {
                int i = 0;
                while (i < listFiles.length) {
                    File file = listFiles[i];
                    result = file.isDirectory() ? result && deleteDirectory(file) : result && file.delete();
                    i = i + 1;
                }
            }
            result = result && directory.delete();
        }
        return result;
    }

    public static List<File> getAllImagesFromDirectory(String path, boolean recursive) {
        return getAllFilesFromDirectory(path, ALLOWED_IMAGE_EXTENSIONS, recursive);
    }

    public static List<File> getAllFilesFromDirectory(String path, String[] allowedExtensions, boolean recursive) {
        File file = new File(path);
        if (file.isDirectory()) {
            List<File> files = (List<File>) org.apache.commons.io.FileUtils.listFiles(file, allowedExtensions, recursive);
            files.sort((file1, file2) -> IArchiveUnpacker.NAT_SORT_COMPARATOR.compare(file1.getPath().replace(path, ""), file2.getPath().replace(path, "")));
            return files;
        }

        return new ArrayList<>();
    }
}
