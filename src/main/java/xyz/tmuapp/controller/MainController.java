package xyz.tmuapp.controller;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.graalvm.nativeimage.ImageInfo;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign2.MaterialDesignC;
import org.kordamp.ikonli.materialdesign2.MaterialDesignL;
import org.kordamp.ikonli.materialdesign2.MaterialDesignS;
import xyz.tmuapp.Main;
import xyz.tmuapp.archive.ArchiveUnpackerFactory;
import xyz.tmuapp.archive.IArchiveUnpacker;
import xyz.tmuapp.controller.cards.UploadInfoCardController;
import xyz.tmuapp.controls.DragNDropFilesView;
import xyz.tmuapp.controls.EmptyView;
import xyz.tmuapp.controls.Toast;
import xyz.tmuapp.helpers.ContentDetector;
import xyz.tmuapp.managers.LocaleManager;
import xyz.tmuapp.managers.Settings;
import xyz.tmuapp.models.BookInfo;
import xyz.tmuapp.models.UploadInfo;
import xyz.tmuapp.utils.*;
import xyz.tmuapp.utils.comparator.AlphanumComparator;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class MainController {
    private static MainController INSTANCE;
    private static final String JVM_SUPPORTED_EXTENSIONS = "*.cbz,*.zip,*.cbr,*.rar,*.png,*.jpg,*.jpeg,*.gif,*.webp";
    private static final String NATIVE_IMAGE_SUPPORTED_EXTENSIONS = "*.cbz,*.zip,*.png,*.jpg,*.jpeg,*.gif,*.webp";

    @FXML
    private StackPane root;
    @FXML
    private TextField tfTitle;
    @FXML
    private TextField tfTags;
    @FXML
    private CheckBox cbPublishTags;
    @FXML
    private TextField tfAuthor;
    @FXML
    private TextField tfAuthorLink;
    @FXML
    private CheckBox cbPublishDescription;
    @FXML
    private TextArea taDescription;
    @FXML
    private DragNDropFilesView uploadFiles;
    @FXML
    private Button btnUpload;
    @FXML
    private Button btnUploadWithChapters;
    @FXML
    private FontIcon mdivUpload;
    @FXML
    private FontIcon mdivUploadWithChapters;
    @FXML
    private HBox hbUrl;
    @FXML
    private TextField tfUrl;

    @FXML
    private StackPane spCopyForTelegram;
    @FXML
    private StackPane spCopyLink;
    @FXML
    private StackPane spOpenInBrowser;

    @FXML
    private VBox vbUploads;
    @FXML
    private EmptyView evEmptyView;

    @FXML
    private Label lblError;

    @FXML
    private ImageView ivLogo;
    @FXML
    private FontIcon fiTheme;
    @FXML
    private Button btnLanguage;

    private Toast toast;

    // Previous saved Upload info
    private ObservableList<UploadInfo> uploadInfoList;

    // Define interruption boolean property
    private final BooleanProperty interruptedProperty = new SimpleBooleanProperty(false);

    public static void showToast(String message) {
        INSTANCE.showToastInternal(message);
    }

    @FXML
    private void initialize() {
        INSTANCE = this;

        // Check account
        if (!TelegraphUtils.checkAccount()) {
            showError(LocaleManager.getString("gui.unable_to_create_account"));
            return;
        }

        // Configure publish checkbox
        configurePublishCheckBox();

        // Configure author fields
        configureAuthorFields();

        // Configure Drag'N'Drop view for app tasks
        configureDragNDropView();

        // Configure Upload button on click action
        setUploadButtonUploadAction();

        // Configure upload result url buttons
        configureResultUrlButtons();

        // Load previous user uploads
        loadPreviousUserUploads();

        // Configure EmptyView
        configureEmptyView();

        // Create Toast node for further usage
        createToastNode();

        // Set logo
        setLogo();

        // Set theme button icon
        setThemeButtonIcon();

        // Set language button code
        setLanguageButtonText();

        // Request focus on root node. Required, because Undecorated window steals focus for window buttons
        FXUtils.requestFocus(root);
    }

    private void configurePublishCheckBox() {
        // Publish Tags
        cbPublishTags.setSelected(Settings.getPublishTags());
        cbPublishTags.selectedProperty().addListener((observable, oldValue, newValue) -> Settings.putPublishTags(newValue));
        FXUtils.addTooltipToNode(cbPublishTags, LocaleManager.getString("gui.publish_tags_tooltip"), 14);

        // Publish Description
        cbPublishDescription.setSelected(Settings.getPublishDescription());
        cbPublishDescription.selectedProperty().addListener((observable, oldValue, newValue) -> Settings.putPublishDescription(newValue));
        FXUtils.addTooltipToNode(cbPublishDescription, LocaleManager.getString("gui.publish_description_tooltip"), 14);
    }

    private void configureAuthorFields() {
        tfAuthor.setText(Settings.getAuthor());
        tfAuthorLink.setText(Settings.getAuthorLink());

        tfAuthor.textProperty().addListener((observable, oldValue, newValue) -> Settings.putAuthor(newValue));
        tfAuthorLink.textProperty().addListener((observable, oldValue, newValue) -> Settings.putAuthorLink(newValue));
    }

    private void configureDragNDropView() {
        // Tell Drag'N'Drop view to accept only supported archive/image files
        uploadFiles.setOnlyArchivesAndImages(true);

        // Tell Drag'N'Drop view Extensions Filter for Native file picker
        uploadFiles.setExtensionsFilter(ImageInfo.inImageCode() ? NATIVE_IMAGE_SUPPORTED_EXTENSIONS : JVM_SUPPORTED_EXTENSIONS);

        // Create OnFilesSelected callback
        uploadFiles.setCallback(new DragNDropFilesView.OnFilesSelected() {
            @Override
            public void onFilesSelected(List<File> selectedFiles, List<File> declinedFiles) {
                tfTitle.clear();
                tfTags.clear();

                File firstFile = selectedFiles.get(0);
                String firstFilePath = firstFile.toString();
                tfTitle.setText(FileUtils.getFileName(firstFilePath));

                // Read book_info.json metadata file if present
                parseBookInfo(firstFile);

                // Check if archives has chapters
                boolean hasChapters = Optional.ofNullable(ArchiveUnpackerFactory.create(firstFile))
                        .map(unpacker -> unpacker.isArchiveHasChapters(firstFile))
                        .orElse(false);

                FXUtils.setNodeVisible(btnUpload);
                FXUtils.setNodeVisibleAndManagedFlag(hasChapters, btnUploadWithChapters);
            }

            @Override
            public void onFilesCleared() {
                WorkspaceUtils.clearTempDir();
            }
        });
    }

    private void createToastNode() {
        toast = new Toast(300, 30);
        toast.setInitialPosition(280);
        toast.setTranslateX(0);
        root.getChildren().add(toast);
    }

    private void setUploadButtonUploadAction() {
        // Set default onUpload action for upload button click
        Platform.runLater(() -> {
            FXUtils.setNodeVisible(btnUpload);
            FXUtils.setNodeGone(btnUploadWithChapters);

            btnUpload.setOnAction(event -> {
                onUpload(false);
                FXUtils.setNodeGone(btnUploadWithChapters);
            });
            btnUpload.setText(LocaleManager.getString("gui.upload"));
            mdivUpload.setIconCode(MaterialDesignC.CLOUD_UPLOAD);

            btnUploadWithChapters.setOnAction(event -> {
                onUpload(true);
                FXUtils.setNodeGone(btnUpload);
            });
            btnUploadWithChapters.setText(LocaleManager.getString("gui.upload_with_chapters"));
            mdivUploadWithChapters.setIconCode(MaterialDesignC.CLOUD_UPLOAD);

            // Enable Title/Tags text fields
            disableTextFields(false);
        });
    }

    private void setUploadButtonInterruptAction() {
        // Set default onUpload action for upload button click
        Platform.runLater(() -> {
            btnUpload.setOnAction(event -> {
                interruptedProperty.setValue(true);
                clearDragNDrop();
                setUploadButtonUploadAction();
            });
            btnUpload.setText(LocaleManager.getString("gui.cancel"));
            mdivUpload.setIconCode(MaterialDesignS.STOP_CIRCLE_OUTLINE);

            btnUploadWithChapters.setOnAction(event -> {
                interruptedProperty.setValue(true);
                clearDragNDrop();
                setUploadButtonUploadAction();
            });
            btnUploadWithChapters.setText(LocaleManager.getString("gui.cancel"));
            mdivUploadWithChapters.setIconCode(MaterialDesignS.STOP_CIRCLE_OUTLINE);
        });
    }

    private void configureEmptyView() {
        evEmptyView.visibleProperty().bind(Bindings.createBooleanBinding(() -> uploadInfoList.isEmpty(), uploadInfoList));
    }

    private void configureResultUrlButtons() {
        configureCopyForTelegramMDIV();
        configureCopyLinkMDIV();
        configureOpenInBrowserMDIV();
    }

    private void configureCopyForTelegramMDIV() {
        spCopyForTelegram.setOnMouseClicked(event -> {
            String firstUrl = ArrayUtils.splitString(tfUrl.getText(), ",")
                    .stream()
                    .map(String::trim)
                    .limit(1)
                    .findFirst()
                    .orElse("");

            UploadInfo uploadInfo = Settings.getPreviousUploadInfo()
                    .stream()
                    .filter(info -> info.getFirstLink().equalsIgnoreCase(firstUrl))
                    .findFirst()
                    .orElse(null);

            if (uploadInfo != null) {
                String clip = TelegramUtils.createTelegramPostText(uploadInfo);
                FXUtils.copyToClipboard(clip);
                showToast(LocaleManager.getString("gui.copied_to_clipboard"));
            }
        });
        configureFontIcon(spCopyForTelegram, LocaleManager.getString("gui.copy_to_clipboard_telegram"));
    }

    private void configureCopyLinkMDIV() {
        spCopyLink.setOnMouseClicked(event -> {
            FXUtils.copyToClipboard(tfUrl.getText());
            showToast(LocaleManager.getString("gui.copied_to_clipboard"));
        });
        configureFontIcon(spCopyLink, LocaleManager.getString("gui.copy_to_clipboard"));
    }

    private void configureOpenInBrowserMDIV() {
        spOpenInBrowser.setOnMouseClicked(event -> FXUtils.openUrl(tfUrl.getText()));
        configureFontIcon(spOpenInBrowser, LocaleManager.getString("gui.open_in_browser"));
    }

    private void configureFontIcon(StackPane stackPane, String tooltipText) {
        FXUtils.addTooltipToNode(stackPane, tooltipText, 12);
    }

    private void loadPreviousUserUploads() {
        uploadInfoList = FXCollections.observableList(Settings.getPreviousUploadInfo());

        List<Node> nodes = uploadInfoList.stream()
                .map(this::createUploadInfoController)
                .map(UploadInfoCardController::getRootNode)
                .collect(Collectors.toList());

        vbUploads.getChildren().addAll(nodes);
    }

    private UploadInfoCardController createUploadInfoController(UploadInfo uploadInfo) {
        UploadInfoCardController controller = UploadInfoCardController.createCard(uploadInfo);
        controller.setOnClickItemListener((action, payload) -> {
            if (action.equals(UploadInfoCardController.ACTION_REMOVE)) {
                uploadInfoList.remove(controller.getUploadInfo());
                Settings.putPreviousUploadInfo(uploadInfoList);
                vbUploads.getChildren().remove(controller.getRootNode());
            }
        });
        return controller;
    }

    private void onUpload(boolean withChapters) {
        // Hide Page URL node and Error Label
        FXUtils.setNodeGone(hbUrl, lblError);

        // Check if user selected any supported files in Drag'N'Drop view
        if (tfTitle.getText().isEmpty()) {
            showError(LocaleManager.getString("gui.title_cant_be_empty"));
        } else if (!uploadFiles.hasSelectedFiles()) {
            showError(LocaleManager.getString("gui.no_selected_files"));
        } else {
            // Reset interrupted property
            interruptedProperty.setValue(false);

            // Enable upload mode in Drag'N'Drop view to show progress bar
            uploadFiles.setUploadMode(true);

            // Disable Title/Tags text fields
            disableTextFields(true);

            new Thread(() -> {
                // List all selected image files from Drag'N'Drop view
                List<File> images = uploadFiles.getSelectedFiles()
                        .stream()
                        .filter(ContentDetector::isImageFile)
                        .collect(Collectors.toList());

                // Unpack all selected archives from Drag'N'Drop view. Interrupt execution if unpack error occurred
                if (!unpackArchivesIfPresent()) {
                    // Clear temp directory
                    WorkspaceUtils.clearTempDir();
                    return;
                }

                // Upload all images into server
                listAndUploadAllImages(images, withChapters);

                // Clear temp directory
                WorkspaceUtils.clearTempDir();
            }).start();
        }
    }

    private boolean unpackArchivesIfPresent() {
        // List all selected archive files from Drag'N'Drop view
        List<File> archives = uploadFiles.getSelectedFiles()
                .stream()
                .filter(ContentDetector::isArchiveFile)
                .collect(Collectors.toList());

        // Check if user selected any supported archive files
        if (ArrayUtils.isNotEmpty(archives)) {
            setUploadButtonInterruptAction();

            // Unpack all archives in loop into temp directory using SevenZipJBindings
            for (File archive : archives) {
                boolean success = false;
                String outputDir = WorkspaceUtils.TMP_DIR + FileUtils.addPathSlash(FileUtils.getFileName(archive.getAbsolutePath()));
                IArchiveUnpacker unpacker = ArchiveUnpackerFactory.create(archive);
                if (unpacker != null) {
                    success = unpacker.unpack(
                            archive,
                            outputDir,
                            (fileName) -> uploadFiles.updateTaskNameLater(LocaleManager.getString("gui.unpacking", fileName)),
                            (current, count) -> uploadFiles.updateProgressLater(null, current, count, null),
                            interruptedProperty
                    );
                }

                // If unpacking was unsuccess - interrupt execution
                if (!success) {
                    showError(LocaleManager.getString("gui.unable_to_unpack_archive", archive.getAbsolutePath()));
                    setUploadButtonUploadAction();
                    return false;
                }
            }
        }

        return true;
    }

    private List<String> uploadImages(List<File> images) {
        return TelegraphUtils.uploadImages(
                images,
                (fileName) -> uploadFiles.updateTaskNameLater(LocaleManager.getString("gui.uploading", fileName)),
                (current, count) -> uploadFiles.updateProgressLater(null, current, count, null),
                interruptedProperty
        );
    }

    private void listAndUploadAllImages(List<File> images, boolean withChapters) {
        // List all image files in temp dir after archives unpacking
        Map<String, List<String>> imageUrls = new TreeMap<>(new AlphanumComparator<>());
        if (withChapters) {
            File[] archiveDirectories = new File(WorkspaceUtils.TMP_DIR).listFiles(File::isDirectory);
            for (File archiveDirectory : archiveDirectories) {
                File[] chapterDirectories = archiveDirectory.listFiles(File::isDirectory);
                // For first chapter add all images from root archive directory
                images.addAll(FileUtils.getAllImagesFromDirectory(archiveDirectory.toString(), false));

                for (File chapterDirectory : chapterDirectories) {
                    // List all files in chapter directory
                    images.addAll(FileUtils.getAllImagesFromDirectory(chapterDirectory.toString(), true));

                    // Upload all image files into server
                    imageUrls.put(chapterDirectory.getName(), uploadImages(images));
                    images.clear();
                }
            }
        } else {
            images.addAll(FileUtils.getAllImagesFromDirectory(WorkspaceUtils.TMP_DIR, true));

            // Upload all image files into server
            imageUrls.put(tfTitle.getText(), uploadImages(images));
        }

        if (ArrayUtils.isNotEmpty(imageUrls)) {
            List<String> pageUrls = imageUrls.entrySet()
                    .stream()
                    .map(entry -> TelegraphUtils.createPage(
                            entry.getKey(),
                            cbPublishTags.isSelected() && !withChapters ? tfTags.getText() : null,
                            cbPublishDescription.isSelected() && !withChapters ? taDescription.getText() : null,
                            entry.getValue(),
                            tfAuthor.getText(),
                            tfAuthorLink.getText()
                    ))
                    .collect(Collectors.toList());

            if (ArrayUtils.isNotEmpty(pageUrls)) {
                Platform.runLater(() -> {
                    // Set Page URL into TextField
                    tfUrl.setText(StringUtils.join(", ", pageUrls));

                    // Show Page URL node
                    FXUtils.setNodeVisible(hbUrl);

                    // Clear selected files in Drag'N'Drop and reset upload mode to hide progress bar
                    clearDragNDrop();

                    // Create new UploadInfo, add new Node into list and save Info list into Settings
                    createUploadInfo(
                            tfTitle.getText(),
                            new ArrayList<>(imageUrls.keySet()),
                            pageUrls,
                            ArrayUtils.splitString(tfTags.getText(), ",")
                                    .stream()
                                    .map(String::trim)
                                    .collect(Collectors.toList()),
                            taDescription.getText()
                    );
                });
            }

            setUploadButtonUploadAction();
        } else if (ArrayUtils.isNotEmpty(imageUrls) && !interruptedProperty.get()) {
            showError(LocaleManager.getString("gui.unable_to_upload_files"));
        }
    }

    private void parseBookInfo(File archiveFile) {
        BookInfo bookInfo = BookInfo.findInArchive(archiveFile);
        if (bookInfo != null) {
            // Split user defined tags into set
            Set<String> userTags = ArrayUtils.splitString(tfTags.getText(), ",")
                    .stream()
                    .map(String::trim)
                    .filter(ArrayUtils::isNotEmpty)
                    .collect(Collectors.toSet());

            // Add tags ang genres from BookInfo into set
            userTags.addAll(bookInfo.getGenres());
            userTags.addAll(bookInfo.getTags());

            // Join new tags set and set it into TextField
            tfTags.setText(StringUtils.join(", ", userTags));

            // Set Description if present in BookInfo and not present by user
            if (StringUtils.isNotEmpty(bookInfo.getDescription()) && StringUtils.isEmpty(taDescription.getText())) {
                taDescription.setText(bookInfo.getDescription());
            }
        }
    }

    private void createUploadInfo(String title, List<String> titles, List<String> links, List<String> tags, String description) {
        UploadInfo uploadInfo = new UploadInfo(title, titles, links, tags, description, System.currentTimeMillis());
        UploadInfoCardController controller = createUploadInfoController(uploadInfo);
        vbUploads.getChildren().add(controller.getRootNode());

        uploadInfoList.add(uploadInfo);
        Settings.putPreviousUploadInfo(uploadInfoList);
    }

    private void clearDragNDrop() {
        uploadFiles.setUploadMode(false);
        uploadFiles.clearSelectedFiles();
    }

    public static String convertsTagsIntoHashtags(String tagsString) {
        return ArrayUtils.splitString(tagsString, ",")
                .stream()
                .map(String::trim)
                .filter(StringUtils::isNotEmpty)
                .map(tag -> tag.replace(" ", "_"))
                .map(tag -> tag.replace("-", "_"))
                .map(tag -> tag.replace("/", ""))
                .map(tag -> "#" + tag)
                .collect(Collectors.joining(", "));
    }

    private void disableTextFields(boolean disable) {
        tfTitle.setDisable(disable);
        tfTags.setDisable(disable);
        tfAuthor.setDisable(disable);
        tfAuthorLink.setDisable(disable);
        taDescription.setDisable(disable);
        cbPublishTags.setDisable(disable);
        cbPublishDescription.setDisable(disable);
    }

    private void showError(String errorMessage) {
        Platform.runLater(() -> {
            lblError.setText(errorMessage);
            FXUtils.setNodeVisible(lblError);
        });
    }

    private void showToastInternal(String message) {
        toast.setMessage(message);
        toast.appear();
    }

    private void setLogo() {
        ivLogo.setImage(new Image("/images/logo_" + (Settings.isCurrentThemeDark() ? "dark" : "light") + ".png"));
    }

    private void setThemeButtonIcon() {
        fiTheme.setIconCode(Settings.isCurrentThemeDark() ? MaterialDesignL.LIGHTBULB : MaterialDesignL.LIGHTBULB_OUTLINE);
    }

    private void setLanguageButtonText() {
        btnLanguage.setText(Settings.getDefaultAppLanguageCode());
    }

    public void changeTheme() {
        boolean isSetThemeDark = !Settings.isCurrentThemeDark();
        Settings.putCurrentThemeDark(isSetThemeDark);
        setLogo();
        setThemeButtonIcon();
        FXUtils.reInstallTo(Main.getCurrentStage(), Main.getCurrentScene(), Settings.isCurrentThemeDark());
        Main.manipulateWithNativeWindow();
    }

    public void changeLanguage() {
        String nextLanguageCode = Settings.getDefaultAppLanguageCode().equalsIgnoreCase("EN") ? "RU" : "EN";
        Settings.putDefaultAppLanguageCode(nextLanguageCode);
        setLanguageButtonText();
        MainController.showToast(LocaleManager.getString("gui.need_restart_reversed"));
    }

    public void openGitHub() {
        FXUtils.openUrl(Main.APP_GITHUB_URL);
    }
}
