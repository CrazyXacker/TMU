package com.crazyxacker.apps.tmu.helpers;

import com.crazyxacker.apps.tmu.Main;
import com.crazyxacker.apps.tmu.controls.DragNDropFilesView;
import com.crazyxacker.apps.tmu.utils.ArrayUtils;
import com.crazyxacker.apps.tmu.utils.FileUtils;
import com.crazyxacker.apps.tmu.utils.StringUtils;
import com.jpro.webapi.WebAPI;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Collection;
import java.util.List;

public class FileDirChooserHelper {
    private static final DirectoryChooser directoryChooser = new DirectoryChooser();
    private static final FileChooser fileChooser = new FileChooser();

    public static WebAPI.FileUploader fileHandler;

    private static void initWebApiFileHandler(DragNDropFilesView dragNDropFilesView) {
        if (fileHandler == null) {
            fileHandler = WebAPI.makeFileUploadNodeStatic(dragNDropFilesView);

            fileHandler.setSelectFileOnClick(true);
            fileHandler.setSelectFileOnDrop(true);
            fileHandler.fileDragOverProperty().addListener((o, oldV, newV) -> {
                // TODO: 08.07.2022
            });
            fileHandler.setOnFileSelected((file) -> fileHandler.uploadFile());
            fileHandler.uploadedFileProperty().addListener((p, oldV, newV) -> dragNDropFilesView.onUploadedFiles(newV));
            fileHandler.progressProperty().addListener((obs, oldV, newV) -> {
                // TODO: 08.07.2022
            });
        }
    }

    public static File chooseDirectory(File initialDirectory) {
        if (FileUtils.isDirectory(initialDirectory)) {
            directoryChooser.setInitialDirectory(initialDirectory);
        }
        return directoryChooser.showDialog(Main.getCurrentStage());
    }

    public static File chooseDirectory() {
        return directoryChooser.showDialog(Main.getCurrentStage());
    }

    public static void chooseDirectoryAndSetPathIntoTextField(TextField textField) {
        File dir = chooseDirectory();
        if (dir != null) {
            textField.setText(dir.toString());
        }
    }

    public static File chooseFile() {
        return chooseFile(null, "");
    }

    public static File chooseFile(String extensionDescription, String... extensions) {
        configureExtensionFilters(extensionDescription, extensions);
        return fileChooser.showOpenDialog(Main.getCurrentStage());
    }

    public static List<File> chooseFiles(@Nullable String extensionDescription, @Nullable String... extensions) {
        configureExtensionFilters(extensionDescription, extensions);
        return fileChooser.showOpenMultipleDialog(Main.getCurrentStage());
    }

    public static List<File> chooseFiles(DragNDropFilesView dragNDropFilesView, @Nullable String extensionDescription, @Nullable Collection<String> extensions) {
        if (WebAPI.isBrowser()) {
            initWebApiFileHandler(dragNDropFilesView);
            return null;
        } else {
            configureExtensionFilters(extensionDescription, ArrayUtils.isNotEmpty(extensions) ? extensions.toArray(new String[0]) : null);
            return fileChooser.showOpenMultipleDialog(Main.getCurrentStage());
        }
    }

    private static void configureExtensionFilters(@Nullable String extensionDescription, @Nullable String... extensions) {
        if (StringUtils.isNotEmpty(extensionDescription) && ArrayUtils.isNotEmpty(extensions)) {
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(extensionDescription, extensions));
        } else {
            fileChooser.getExtensionFilters().clear();
        }
    }
}

