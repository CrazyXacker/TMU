package com.crazyxacker.apps.tmu.controller.cards;

import com.crazyxacker.apps.tmu.controller.MainController;
import com.crazyxacker.apps.tmu.controls.Toast;
import com.crazyxacker.apps.tmu.listeners.OnItemClickListener;
import com.crazyxacker.apps.tmu.managers.LocaleManager;
import com.crazyxacker.apps.tmu.models.UploadInfo;
import com.crazyxacker.apps.tmu.utils.FXUtils;
import com.jfoenix.controls.JFXButton;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import lombok.Getter;
import lombok.Setter;

public class UploadInfoCardController {
    public static final String ACTION_REMOVE = "remove";

    @FXML
    private Node root;
    @FXML
    private Pane pane;
    @FXML
    private Pane pRippler;

    @FXML
    private Label lblTitle;
    @FXML
    private Label lblLink;
    @FXML
    private Label lblDate;
    @FXML
    private JFXButton btnRemove;

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
        FXUtils.setNodeGone(btnRemove);

        // Create Ripple click effect for Node
        FXUtils.createRoundedRippler(pRippler, pane, 12, 12);

        // Create Show/Hide Node border effect for Main Node and Remove Button
        FXUtils.createOnMouseEnterExitedBorderEffect(pane, btnRemove);

        // Change Remove button visibility/managed flags on Root Node mouse enter/exit
        root.setOnMouseEntered(event -> FXUtils.setNodeVisible(btnRemove));
        root.setOnMouseExited(event -> FXUtils.setNodeGone(btnRemove));

        // Copy UploadInfo link into clipboard and show Toast on Root Node click
        root.setOnMouseClicked(event -> {
            FXUtils.copyToClipboard(uploadInfo.getLink());
            MainController.showToast(LocaleManager.getString("gui.copied_to_clipboard"));
        });

        // Remove Card from Settings and UI on Remove button click
        btnRemove.setOnMouseClicked(event -> onClickItemListener.onClick(ACTION_REMOVE, uploadInfo));

        // Update UI with UpdateInfo data
        update();
    }

    private void update() {
        Platform.runLater(() -> {
            lblTitle.setText(uploadInfo.getTitle());
            lblLink.setText(uploadInfo.getLink());
            lblDate.setText(uploadInfo.getReadableUploadedAt());
        });
    }

    public Node getRootNode() {
        return root;
    }
}
