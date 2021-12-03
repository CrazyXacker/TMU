package com.crazyxacker.apps.tmu.controls;

import com.crazyxacker.apps.tmu.helpers.ContentDetector;
import com.crazyxacker.apps.tmu.helpers.FileDirChooserHelper;
import com.crazyxacker.apps.tmu.managers.LocaleManager;
import com.crazyxacker.apps.tmu.utils.*;
import com.crazyxacker.apps.tmu.utils.comparator.AlphanumComparator;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIconView;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.VBox;
import lombok.Getter;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class DragNDropFilesView extends VBox {
    private static final String STYLE_ACTIVE_UPLOAD_BOX = "active";
    private static final String STYLE_INACTIVE_UPLOAD_BOX = "inactive";

    @FXML
    private VBox root;
    @FXML
    private Label lblFilesToUpload;

    @FXML
    private VBox containerNodes;
    @FXML
    private MaterialDesignIconView mdivIcon;
    @FXML
    private ProgressBar pbUploading;
    @FXML
    private Label lblLog;

    @Getter private List<File> selectedFiles;
    @Getter private List<File> declinedFiles;

    private OnFilesSelected callback;

    private final BooleanProperty uploadMode = new SimpleBooleanProperty(false);
    private final BooleanProperty onlyArchives = new SimpleBooleanProperty(false);
    private final BooleanProperty onlyImages = new SimpleBooleanProperty(false);
    private final BooleanProperty onlyArchivesAndImages = new SimpleBooleanProperty(false);
    private final BooleanProperty folderSelectMode = new SimpleBooleanProperty(false);

    private final StringProperty chooserExtensionsFilterDescription = new SimpleStringProperty(null);
    private final StringProperty extensionsFilter = new SimpleStringProperty("");

    public DragNDropFilesView() {
        FXUtils.loadComponent(this, "/fxml/controls/DragNDropFilesView.fxml");
    }

    public void setCallback(OnFilesSelected callback) {
        this.callback = callback;
    }

    public void setIcon(MaterialDesignIcon icon) {
        mdivIcon.setGlyphName(icon.name());
    }

    @FXML
    private void initialize() {
        containerNodes.setOnMouseClicked(event -> selectFiles());
        containerNodes.setOnMouseEntered(event -> setUploadBoxState(true));
        containerNodes.setOnMouseExited(event -> setUploadBoxState(false));

        FXUtils.setNodeVisibleAndManagedFlag(uploadMode.get(), uploadMode.get(), pbUploading);
        configureDragAndDropNode();
        configureIcon();

        uploadMode.addListener((observable, oldValue, newValue) -> FXUtils.setNodeVisibleAndManagedFlag(newValue, newValue, pbUploading));
        folderSelectMode.addListener((observable, oldValue, newValue) -> {
            configureIcon();
            clearSelectedFiles();
        });
    }

    @FXML
    private void selectFiles() {
        if (folderSelectMode.get()) {
            File dir = FileDirChooserHelper.chooseDirectory();
            if (dir != null) {
                selectedFiles = Collections.singletonList(dir);
            }
        } else {
            selectedFiles = FileDirChooserHelper.chooseFiles(chooserExtensionsFilterDescription.get(), ArrayUtils.splitString(extensionsFilter.get(), ","));
        }

        onFilesSelected();
        FXUtils.requestFocus(root);
    }

    public void updateErrorsLater(List<String> errorMessages) {
        Platform.runLater(() -> updateErrors(errorMessages));
    }

    public void updateErrors(List<String> errorMessages) {
        lblLog.setText(StringUtils.join("\n", errorMessages));
    }

    public void updateTaskNameLater(String taskName) {
        Platform.runLater(() -> updateTaskName(taskName));
    }

    public void updateTaskName(String taskName) {
        lblFilesToUpload.setText(taskName);
    }

    public void updateProgressLater(int progress, int total, List<String> errorMessages) {
        Platform.runLater(() -> updateProgress(progress, total, errorMessages));
    }

    public void updateProgress(int progress, int total, List<String> errorMessages) {
        updateProgress(
                LocaleManager.getString("gui.uploading", progress < selectedFiles.size() ? selectedFiles.get(progress).getName() : ""),
                progress,
                total,
                errorMessages
        );
    }

    public void updateProgressLater(String taskName, int progress, int total, List<String> errorMessages) {
        Platform.runLater(() -> updateProgress(taskName, progress, total, errorMessages));
    }

    public void updateProgress(String taskName, int progress, int total, List<String> errorMessages) {
        Platform.runLater(() -> {
            pbUploading.setProgress((double) progress / total);
            if (StringUtils.isNotEmpty(taskName)) {
                lblFilesToUpload.setText(taskName);
            }
            if (ArrayUtils.isNotEmpty(errorMessages)) {
                lblLog.setText(StringUtils.join("\n", errorMessages));
            }
        });
    }

    public void selectFiles(List<File> files) {
        clearSelectedFiles();
        selectedFiles = files;
        onFilesSelected();
    }

    public boolean hasSelectedFiles() {
        return ArrayUtils.isNotEmpty(selectedFiles);
    }

    public boolean hasDeclinedFiles() {
        return ArrayUtils.isNotEmpty(declinedFiles);
    }

    public void clearSelectedFiles() {
        if (ArrayUtils.isNotEmpty(selectedFiles)) {
            selectedFiles.clear();
        }
        if (ArrayUtils.isNotEmpty(declinedFiles)) {
            declinedFiles.clear();
        }
        onFilesSelected();
    }

    public void showUploadProgress() {
        FXUtils.setNodeVisible(lblLog, pbUploading);
        FXUtils.requestFocus(root);
    }

    public void hideUploadProgress() {
        FXUtils.setNodeGone(lblLog, pbUploading);
        FXUtils.requestFocus(root);
    }

    private void onFilesSelected() {
        filterSelectedFiles();

        if (ArrayUtils.isNotEmpty(selectedFiles)) {
            lblFilesToUpload.setText(
                    folderSelectMode.get()
                            ? LocaleManager.getString("gui.selected_folders", selectedFiles.size())
                            : LocaleManager.getString("gui.selected_files", selectedFiles.size())
            );
            if (callback != null) {
                callback.onFilesSelected(selectedFiles, declinedFiles);
            }

            FXUtils.addTooltipToNode(this, StringUtils.join("\n", selectedFiles.stream().map(File::getAbsolutePath).collect(Collectors.toList())), 12);
        } else {
            lblFilesToUpload.setText(LocaleManager.getString(
                    folderSelectMode.get()
                            ? "gui.drag_n_drop_select_folders"
                            : "gui.drag_n_drop_select_files"
            ));
            if (callback != null) {
                callback.onFilesCleared();
            }
        }
    }

    private void filterSelectedFiles() {
        if (ArrayUtils.isNotEmpty(selectedFiles)) {
            boolean isNativeImage = NativeUtils.isNativeImage();
            Map<Boolean, List<File>> groups = selectedFiles.stream().collect(Collectors.partitioningBy(file -> {
                if (folderSelectMode.get()) {
                    return file.isDirectory();
                } else if (file.isFile()) {
                    if (onlyArchives.get()) {
                        return isNativeImage ? ContentDetector.isZipFile(file) : ContentDetector.isArchiveFile(file);
                    } else if (onlyImages.get()) {
                        return ContentDetector.isImageFile(file);
                    } else if (onlyArchivesAndImages.get()) {
                        return isNativeImage ? ContentDetector.isZipFile(file) : ContentDetector.isArchiveFile(file) || ContentDetector.isImageFile(file);
                    }
                }
                return false;
            }));

            selectedFiles = groups.get(true);
            declinedFiles = groups.get(false);
        }
    }

    private void configureDragAndDropNode() {
        setUploadBoxState(false);

        containerNodes.setOnDragOver(event -> {
            if (event.getGestureSource() != containerNodes && event.getDragboard().hasFiles()) {
                setUploadBoxState(true);
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }
            event.consume();
        });

        containerNodes.setOnDragExited(event -> setUploadBoxState(false));

        containerNodes.setOnDragDropped(event -> {
            Dragboard dragboard = event.getDragboard();
            boolean success = false;
            if (dragboard.hasFiles()) {
                selectedFiles = new ArrayList<>();
                List<File> files = dragboard.getFiles();
                files.forEach(file -> {
                    if (!getFolderSelectMode() && file.isDirectory()) {
                        selectedFiles.addAll(FileUtils.getAllFilesFromDirectory(file.getAbsolutePath(), null, true));
                    } else {
                        selectedFiles.add(file);
                    }
                });
                selectedFiles.sort((file1, file2) -> AlphanumComparator.compareStrings(file1.getAbsolutePath(), file2.getAbsolutePath()));
                onFilesSelected();
                success = true;
            }
            event.setDropCompleted(success);
            event.consume();
        });
    }

    private void configureIcon() {
        if (folderSelectMode.get()) {
            setIcon(MaterialDesignIcon.FOLDER);
        }
    }

    private void setUploadBoxState(boolean isActive) {
        ObservableList<String> styleClass = containerNodes.getStyleClass();
        if (isActive) {
            styleClass.removeAll(STYLE_INACTIVE_UPLOAD_BOX);
            styleClass.add(STYLE_ACTIVE_UPLOAD_BOX);
        } else {
            styleClass.removeAll(STYLE_ACTIVE_UPLOAD_BOX);
            styleClass.add(STYLE_INACTIVE_UPLOAD_BOX);
        }
    }

    public final void setUploadMode(boolean value) {
        uploadMode.set(value);
    }

    public final boolean getUploadMode() {
        return uploadMode.get();
    }

    public final void setOnlyArchives(boolean value) {
        onlyArchives.set(value);
    }

    public final boolean getOnlyArchives() {
        return onlyArchives.get();
    }

    public final void setOnlyImages(boolean value) {
        onlyImages.set(value);
    }

    public final boolean getOnlyImages() {
        return onlyImages.get();
    }

    public final void setOnlyArchivesAndImages(boolean value) {
        onlyArchivesAndImages.set(value);
    }

    public final boolean getOnlyArchivesAndImages() {
        return onlyArchivesAndImages.get();
    }

    public final void setFolderSelectMode(boolean value) {
        folderSelectMode.set(value);
    }

    public final boolean getFolderSelectMode() {
        return folderSelectMode.get();
    }

    public final void setChooserExtensionsFilterDescription(String value) {
        chooserExtensionsFilterDescription.set(value);
    }

    public final String getChooserExtensionsFilterDescription() {
        return chooserExtensionsFilterDescription.get();
    }

    public final void setExtensionsFilter(String value) {
        extensionsFilter.set(value);
    }

    public final String getExtensionsFilter() {
        return extensionsFilter.get();
    }

    public interface OnFilesSelected {
        void onFilesSelected(List<File> selectedFiles, List<File> declinedFiles);
        void onFilesCleared();
    }
}
