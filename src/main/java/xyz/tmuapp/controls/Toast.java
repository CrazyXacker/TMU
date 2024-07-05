package xyz.tmuapp.controls;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import xyz.tmuapp.utils.FXUtils;

public class Toast extends Label {
    private final float displayTime = 2.f;

    public Toast(int minWidth, int minHeight) {
        super();
        // Starting it with zero because we don't want it to show up upon adding it to the root
        this.setOpacity(0);
        this.setStyle("-fx-background-color: -color-bg-subtle; -fx-font-size: 15;");
        this.setAlignment(Pos.CENTER);
        this.getStyleClass().add("toast");
        this.setMinWidth(minWidth);
        this.setMinHeight(minHeight);
        clipWithRoundedRectangle(minWidth, minHeight);
        FXUtils.setNodeGone(this);
    }

    private void clipWithRoundedRectangle(int minWidth, int minHeight) {
        Rectangle rectangle = new Rectangle(minWidth, minHeight);
        rectangle.setArcHeight(16);
        rectangle.setArcWidth(16);
        this.setClip(rectangle);
    }

    public void setMessage(String msg) {
        this.setText(msg);
    }

    public void appear() {
        FXUtils.setNodeVisible(this);
        Timeline timelineShow = new Timeline(
                new KeyFrame(Duration.seconds(this.displayTime / 6), new KeyValue(this.opacityProperty(), 1.0))
        );
        Timeline timelineStay = new Timeline(
                new KeyFrame(Duration.seconds(this.displayTime))
        );
        Timeline timelineHide = new Timeline(
                new KeyFrame(Duration.seconds(this.displayTime / 6), new KeyValue(this.opacityProperty(), 0))
        );
        Timeline timelineChangeVisibility = new Timeline(
                new KeyFrame(Duration.seconds(0.01), new KeyValue(this.visibleProperty(), false)),
                new KeyFrame(Duration.seconds(0.01), new KeyValue(this.managedProperty(), false))
        );

        new SequentialTransition(timelineShow, timelineStay, timelineHide, timelineChangeVisibility).play();
    }

    public void setInitialPosition(float position) {
        this.setTranslateY(position - (float) this.getMinHeight() - 10);
    }
}