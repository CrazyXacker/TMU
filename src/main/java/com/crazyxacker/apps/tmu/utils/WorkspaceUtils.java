package com.crazyxacker.apps.tmu.utils;

import java.io.File;

public class WorkspaceUtils {
    public static final String WORKING_DIR = getWorkingDir();
    public static final String TMP_DIR = WORKING_DIR + "tmp" + File.separator;

    private static final String[] FOLDERS = new String[]{"tmp"};

    public static void configureWorkspace() {
        createFolders();
    }

    private static void createFolders() {
        for (String folder : FOLDERS) {
            new File(WORKING_DIR + folder).mkdirs();
        }
    }

    public static void clearTempDirAsync() {
        new Thread(WorkspaceUtils::clearTempDir);
    }

    public static void clearTempDir() {
        FileUtils.deleteDirectory(new File(TMP_DIR));
        createFolders();
    }

    private static String getWorkingDir() {
        File workDir = new File(System.getProperty("user.dir"));
        String workDirString = workDir.toString();
        if (workDir.isFile()) {
            workDirString = workDirString.substring(0, workDirString.lastIndexOf(File.separator));
        }
        return workDirString + File.separator;
    }
}
