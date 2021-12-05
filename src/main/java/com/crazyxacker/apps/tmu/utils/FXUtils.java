package com.crazyxacker.apps.tmu.utils;

import com.crazyxacker.apps.tmu.Main;
import com.crazyxacker.apps.tmu.controller.MainController;
import com.crazyxacker.apps.tmu.controls.JFXCustomDecorator;
import com.crazyxacker.apps.tmu.controls.JFXRoundedRippler;
import com.crazyxacker.apps.tmu.managers.LocaleManager;
import com.crazyxacker.apps.tmu.managers.Settings;
import com.jfoenix.controls.base.IFXLabelFloatControl;
import com.jfoenix.validation.RequiredFieldValidator;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIconView;
import de.jensd.fx.glyphs.materialdesignicons.utils.MaterialDesignIconFactory;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Pair;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class FXUtils {
    public static final String DEFAULT_BORDER_CSS = "border-transparent";
    public static final String HOVER_BORDER_CSS = "border-colored";

    public static void requestFocus(Node node) {
        Platform.runLater(node::requestFocus);
    }

    public static void openURL(String url) {
        Runtime rt = Runtime.getRuntime();
        try {
            // Try execute command on Windows
            rt.exec("rundll32 url.dll,FileProtocolHandler " + url).waitFor();
        } catch (IOException | InterruptedException e) {
            // Maybe running on Linux?
            try {
                String[] cmd = {"xdg-open", url};
                rt.exec(cmd).waitFor();
            } catch (IOException | InterruptedException e2) {
                e.printStackTrace();
                e2.printStackTrace();
            }
        }
    }

    public static void copyToClipboard(String clip) {
        final Clipboard clipboard = Clipboard.getSystemClipboard();
        final ClipboardContent content = new ClipboardContent();
        content.putString(clip);
        clipboard.setContent(content);
    }

    public static void copyToClipboard(Image image) {
        final Clipboard clipboard = Clipboard.getSystemClipboard();
        final ClipboardContent content = new ClipboardContent();
        content.putImage(image);
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
        primaryStage.getIcons().add(appIconImage);
    }

    public static Scene createDecorator(Stage stage, Node root, int width, int height, boolean withIcon, boolean isDarkTheme, Runnable onCloseButtonAction) {
        JFXCustomDecorator decorator = new JFXCustomDecorator(stage, root, false, false, true, isDarkTheme);
        decorator.setCustomMaximize(false);
        decorator.setLanguageText(Settings.getDefaultAppLanguageCode());
        decorator.setOnLanguageActionRunnable(() -> {
            String nextLanguageCode = Settings.getDefaultAppLanguageCode().equalsIgnoreCase("EN") ? "RU" : "EN";
            decorator.setLanguageText(nextLanguageCode);
            Settings.putDefaultAppLanguageCode(nextLanguageCode);
            MainController.showToast(LocaleManager.getString("gui.need_restart_reversed"));
        });

        decorator.setOnThemeChangeActionRunnable(() -> {
            boolean isSetThemeDark = !Settings.isCurrentThemeDark();
            Settings.putCurrentThemeDark(isSetThemeDark);
            MainController.showToast(LocaleManager.getString("gui.need_restart"));
        });

        decorator.setOnGithubActionRunnable(() -> FXUtils.openURL(Main.APP_GITHUB_URL));

        if (withIcon) {
            decorator.setGraphic(createAppIconImageView(isDarkTheme));
        }

        if (onCloseButtonAction != null) {
            decorator.setOnCloseButtonAction(onCloseButtonAction);
        }

        Scene scene = new Scene(decorator, width, height);
        addStylesheetToScene(scene, isDarkTheme);
        stage.initStyle(StageStyle.TRANSPARENT);
        scene.setFill(Color.TRANSPARENT);

        // Set Taskbar icon for app
        FXUtils.setTaskbarAppIcon(stage, FXUtils.createAppIconImage(128, isDarkTheme));

        return scene;
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

    public static void addStylesheetToScene(Scene scene, boolean isDarkTheme) {
        final ObservableList<String> stylesheets = scene.getStylesheets();
        stylesheets.remove(FXUtils.class.getResource(isDarkTheme ? "/css/theme.css" : "/css/theme_dark.css").toExternalForm());
        stylesheets.addAll(FXUtils.class.getResource(isDarkTheme ? "/css/theme_dark.css" : "/css/theme.css").toExternalForm());
    }

    public static void addTooltipToNode(Node layout, String tooltipText, int fontSize) {
        Tooltip nodeTooltip = new Tooltip(tooltipText);
        nodeTooltip.setFont(Font.font(fontSize));
        Tooltip.install(layout, nodeTooltip);
    }

    public static MaterialDesignIconView createMaterialDesignIconView(MaterialDesignIcon icon, int iconSize, String iconColor) {
        return createMaterialDesignIconView(icon, iconSize, iconColor, "");
    }

    public static MaterialDesignIconView createMaterialDesignIconView(MaterialDesignIcon icon, int iconSize, String iconColor, @NotNull String style) {
        MaterialDesignIconView mdiv = new MaterialDesignIconView(icon);
        mdiv.setStyle(String.format("-fx-font-family: 'Material Design Icons'; -fx-font-size: %s; -fx-fill: %s; %s;", iconSize, iconColor, style));
        return mdiv;
    }

    public static void createRoundedRippler(Pane ripplerPane, Pane root, int maskArcHeight, int maskArcWidth) {
        JFXRoundedRippler rippler = new JFXRoundedRippler(ripplerPane, maskArcHeight, maskArcWidth);
        rippler.setRipplerFill(Color.GRAY);
        root.getChildren().add(rippler);
    }

    public static void addTextFieldValidator(IFXLabelFloatControl textField, RequiredFieldValidator validator, boolean withValidationOnFocusChange) {
        textField.getValidators().add(validator);
        if (withValidationOnFocusChange) {
            ((TextField) textField).focusedProperty().addListener((o, oldVal, newVal) -> {
                if (!newVal) {
                    textField.validate();
                }
            });
        }
    }

    public static RequiredFieldValidator createNotEmptyValidator() {
        RequiredFieldValidator validator = new RequiredFieldValidator();
        validator.setMessage(LocaleManager.getString("gui.cant_be_empty"));
        validator.setIcon(MaterialDesignIconFactory.get().createIcon(MaterialDesignIcon.EXCLAMATION));
        return validator;
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
