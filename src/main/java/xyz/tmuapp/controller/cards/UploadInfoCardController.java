package xyz.tmuapp.controller.cards;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import lombok.Getter;
import lombok.Setter;
import xyz.tmuapp.controller.MainController;
import xyz.tmuapp.listeners.OnItemClickListener;
import xyz.tmuapp.managers.LocaleManager;
import xyz.tmuapp.models.UploadInfo;
import xyz.tmuapp.utils.FXUtils;
import xyz.tmuapp.utils.TelegramUtils;

public class UploadInfoCardController {
    public static final String ACTION_REMOVE = "remove";

    @FXML
    private Node root;
    @FXML
    private Pane pane;

    @FXML
    private Label lblTitle;
    @FXML
    private Label lblLink;
    @FXML
    private Label lblDate;

    @FXML
    private Button btnCopyForTelegram;
    @FXML
    private Button btnCopyLink;
    @FXML
    private Button btnOpenInBrowser;
    @FXML
    private Button btnRemove;

    @Getter
    @Setter
    private UploadInfo uploadInfo;
    @Setter
    private OnItemClickListener onClickItemListener;

    public static UploadInfoCardController createCard(UploadInfo uploadInfo) {
        FXMLLoader fxmlLoader = FXUtils.loadFXMLAndGetLoader("/fxml/cards/UploadInfoCard.fxml");
        UploadInfoCardController controller = fxmlLoader.getController();
        controller.setUploadInfo(uploadInfo);
        return controller;
    }

    @FXML
    public void initialize() {
        // Hide Remove button on initialize
        FXUtils.setNodeGone(btnCopyForTelegram, btnCopyLink, btnOpenInBrowser, btnRemove);

        // Create Show/Hide Node border effect for Main Node and Remove Button
        FXUtils.createOnMouseEnterExitedBorderEffect(pane, btnCopyForTelegram, btnCopyLink, btnOpenInBrowser, btnRemove);

        // Change Remove button visibility/managed flags on Root Node mouse enter/exit
        root.setOnMouseEntered(event -> FXUtils.setNodeVisible(btnCopyForTelegram, btnCopyLink, btnOpenInBrowser, btnRemove));
        root.setOnMouseExited(event -> FXUtils.setNodeGone(btnCopyForTelegram, btnCopyLink, btnOpenInBrowser, btnRemove));

        // Copy UploadInfo link into clipboard and show Toast on Root Node click
        root.setOnMouseClicked(event -> {
            FXUtils.copyToClipboard(uploadInfo.getLinksCommaSeparated());
            MainController.showToast(LocaleManager.getString("gui.copied_to_clipboard"));
        });

        // Copy post for Telegram
        btnCopyForTelegram.setOnMouseClicked(event -> {
            String clip = TelegramUtils.createTelegramPostText(uploadInfo);
            FXUtils.copyToClipboard(clip);
            MainController.showToast(LocaleManager.getString("gui.copied_to_clipboard"));
        });

        // Copy link into clipboard
        btnCopyLink.setOnMouseClicked(event -> {
            FXUtils.copyToClipboard(uploadInfo.getLinksCommaSeparated());
            MainController.showToast(LocaleManager.getString("gui.copied_to_clipboard"));
        });

        // Open link in browser
        btnOpenInBrowser.setOnMouseClicked(event -> FXUtils.openUrl(uploadInfo.getFirstLink()));

        // Remove Card from Settings and UI on Remove button click
        btnRemove.setOnMouseClicked(event -> onClickItemListener.onClick(ACTION_REMOVE, uploadInfo));

        // Update UI with UpdateInfo data
        update();
    }

    private void update() {
        Platform.runLater(() -> {
            lblTitle.setText(uploadInfo.getTitle());
            lblLink.setText(uploadInfo.getLinksCommaSeparated());
            lblDate.setText(uploadInfo.getReadableUploadedAt());
        });
    }

    public Node getRootNode() {
        return root;
    }
}
