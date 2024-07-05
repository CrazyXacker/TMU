package xyz.tmuapp.controls;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.VBox;
import org.jetbrains.annotations.Nullable;
import xyz.tmuapp.helpers.Kaomoji;
import xyz.tmuapp.utils.ArrayUtils;
import xyz.tmuapp.utils.FXUtils;

import java.util.List;

@SuppressWarnings("unused")
public class EmptyView extends VBox {
    @FXML
    private VBox vbEmptyView;
    @FXML
    private Label lblKaomoji;
    @FXML
    private Label lblNothingFound;

    public EmptyView() {
        FXUtils.loadComponent(this, "/fxml/controls/EmptyView.fxml");
    }

    public boolean checkEmptyItems(List<?> list, @Nullable Node showNodeIfNotEmpty, @Nullable ProgressIndicator loadingPane) {
        FXUtils.setNodeGone(loadingPane);
        if (ArrayUtils.isNotEmpty(list)) {
            hideEmptyView();
            if (showNodeIfNotEmpty != null) {
                FXUtils.setNodeVisible(showNodeIfNotEmpty);
            }
            return false;
        } else {
            showEmptyView();
            return true;
        }
    }

    public void hideEmptyView() {
        FXUtils.setNodeGone(vbEmptyView);
    }

    public void showEmptyView() {
        Platform.runLater(() -> {
            lblKaomoji.setText(Kaomoji.getRandomKaomoji(Kaomoji.KaomojiType.SADNESS));
            FXUtils.setNodeVisible(vbEmptyView);
        });
    }

    public void setEmptyText(String text) {
        lblNothingFound.setText(text);
    }
}
