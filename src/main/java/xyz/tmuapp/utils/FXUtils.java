package xyz.tmuapp.utils;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Pair;
import lombok.SneakyThrows;
import org.jetbrains.annotations.Nullable;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.javafx.FontIcon;
import xyz.tmuapp.Main;
import xyz.tmuapp.managers.LocaleManager;

import java.io.IOException;
import java.net.URL;
import java.util.List;

public class FXUtils {
    public static final String DEFAULT_BORDER_CSS = "border-transparent";
    public static final String HOVER_BORDER_CSS = "border-colored";

    public static void requestFocus(Node node) {
        Platform.runLater(node::requestFocus);
    }

    public static void openUrl(String url) {
        Main.getAppHostServices().showDocument(url);
    }

    public static void copyToClipboard(String clip) {
        final Clipboard clipboard = Clipboard.getSystemClipboard();
        final ClipboardContent content = new ClipboardContent();
        content.putString(clip);
        clipboard.setContent(content);
    }

    public static void loadComponent(Object rootAndController, String fxmlPath) {
        FXMLLoader fxmlLoader = new FXMLLoader(FXUtils.class.getResource(fxmlPath), LocaleManager.getResourceBundle());
        fxmlLoader.setRoot(rootAndController);
        fxmlLoader.setController(rootAndController);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    @SneakyThrows
    public static <T> Pair<Node, T> loadFXML(String fxmlPath) {
        FXMLLoader loader = getLoader(fxmlPath);
        return new Pair<>(loader.load(), loader.getController());
    }

    @SneakyThrows
    public static FXMLLoader getLoader(String fxmlPath) {
        return new FXMLLoader(FXUtils.class.getResource(fxmlPath), LocaleManager.getResourceBundle());
    }

    @SneakyThrows
    public static FXMLLoader loadFXMLAndGetLoader(String fxmlPath) {
        FXMLLoader loader = getLoader(fxmlPath);
        loader.load();
        return loader;
    }

    public static void setTaskbarAppIcon(Stage primaryStage, Image appIconImage) {
        primaryStage.getIcons().clear();
        primaryStage.getIcons().add(appIconImage);
    }

    public static Scene createScene(Stage stage, Parent root, int width, int height, boolean isDarkTheme) {
        Scene scene = new Scene(root, width, height);

        // Install Theme
        reInstallTo(stage, scene, isDarkTheme);

        return scene;
    }

    public static void reInstallTo(Stage stage, Scene scene, boolean isDarkTheme) {
        // Set Taskbar icon for app
        FXUtils.setTaskbarAppIcon(stage, FXUtils.createAppIconImage(128, isDarkTheme));

        scene.getStylesheets().clear();
        installTo(scene, List.of(
                FXUtils.class.getResource(isDarkTheme ? "/css/cupertino-dark.css" : "/css/cupertino-light.css"),
                FXUtils.class.getResource("/css/customs.css")
        ));
    }

    private static void installTo(@Nullable Scene scene, List<URL> stylesheetUrls) {
        if (scene != null && ArrayUtils.isNotEmpty(stylesheetUrls)) {
            scene.getStylesheets().addAll(
                    stylesheetUrls.stream()
                            .map(URL::toExternalForm)
                            .toList()
            );
        }
    }

    public static ImageView createAppIconImageView(boolean isDarkTheme) {
        ImageView ivAppIcon = new ImageView(createAppIconImage(28, isDarkTheme));
        ivAppIcon.setFitWidth(28);
        ivAppIcon.setFitHeight(28);
        return ivAppIcon;
    }

    public static Image createAppIconImage(int widthHeight, boolean isDarkTheme) {
        return new Image(isDarkTheme ? "/images/app_icon_dark.png" : "/images/app_icon.png", widthHeight, widthHeight, true, true);
    }

    public static void addTooltipToNode(Node layout, String tooltipText, int fontSize) {
        Tooltip nodeTooltip = new Tooltip(tooltipText);
        nodeTooltip.setFont(Font.font(fontSize));
        Tooltip.install(layout, nodeTooltip);
    }

    public static FontIcon createIkonliIconView(Ikon ikon, int iconSize, String color) {
        return FontIcon.of(ikon, iconSize, Color.web(color));
    }

    public static void setNodeVisible(@Nullable Node... nodes) {
        setNodeVisibleAndManagedFlag(true, true, nodes);
    }

    public static void setNodeInvisible(@Nullable Node... nodes) {
        setNodeVisibleAndManagedFlag(false, true, nodes);
    }

    public static void setNodeUnmanaged(@Nullable Node... nodes) {
        setNodeVisibleAndManagedFlag(true, false, nodes);
    }

    public static void setNodeGone(@Nullable Node... nodes) {
        setNodeVisibleAndManagedFlag(false, false, nodes);
    }

    public static void setNodeVisibleAndManagedFlag(boolean isVisibleAndManaged, @Nullable Node... nodes) {
        setNodeVisibleAndManagedFlag(isVisibleAndManaged, isVisibleAndManaged, nodes);
    }

    public static void setNodeVisibleAndManagedFlag(boolean isVisible, boolean isManaged, Node... nodes) {
        if (ArrayUtils.isNotEmpty(nodes)) {
            for (Node node : nodes) {
                node.setVisible(isVisible);
                node.setManaged(isManaged);
            }
        }
    }

    public static void createOnMouseEnterExitedBorderEffect(Node... nodes) {
        createOnMouseEnterBorderEffect(nodes);
        createOnMouseExitedBorderEffect(nodes);
    }

    public static void createOnMouseEnterBorderEffect(Node... nodes) {
        createOnMouseEnterBorderEffect(DEFAULT_BORDER_CSS, HOVER_BORDER_CSS, nodes);
    }

    public static void createOnMouseExitedBorderEffect(Node... nodes) {
        createOnMouseExitedBorderEffect(DEFAULT_BORDER_CSS, HOVER_BORDER_CSS, nodes);
    }

    public static void createOnMouseEnterBorderEffect(String defaultStyleName, String hoverBorderEffect, Node... nodes) {
        for (Node node : nodes) {
            node.setOnMouseEntered(event -> {
                ObservableList<String> styleClass = node.getStyleClass();
                styleClass.remove(defaultStyleName);
                styleClass.add(hoverBorderEffect);
            });
        }
    }

    public static void createOnMouseExitedBorderEffect(String defaultStyleName, String hoverBorderEffect, Node... nodes) {
        for (Node node : nodes) {
            node.setOnMouseExited(event -> {
                ObservableList<String> styleClass = node.getStyleClass();
                styleClass.remove(hoverBorderEffect);
                styleClass.add(defaultStyleName);
            });
        }
    }
}
