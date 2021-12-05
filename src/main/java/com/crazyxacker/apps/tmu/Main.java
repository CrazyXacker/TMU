package com.crazyxacker.apps.tmu;

import com.crazyxacker.apps.tmu.managers.LocaleManager;
import com.crazyxacker.apps.tmu.managers.Settings;
import com.crazyxacker.apps.tmu.utils.FXUtils;
import com.crazyxacker.apps.tmu.utils.WorkspaceUtils;
import com.google.gson.Gson;
import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.Getter;
import org.telegram.telegraph.TelegraphLogger;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;

public class Main extends Application {
    public static final String APP_NAME = "TelegraphMangaUploader";
    public static final String APP_GITHUB_URL = "https://github.com/CrazyXacker/TMU";
    public static final Gson GSON;
    @Getter
    private static Stage currentStage;

    public static void main(String[] args) {
        // Load settings file
        Settings.init();

        // Configure workspace. Create necessary folders
        WorkspaceUtils.configureWorkspace();

        // Set AppLocale and load Locale Resource Bundle
        LocaleManager.setLocale(Settings.getDefaultAppLanguageCode());
        LocaleManager.loadResourceBundle();

        // Set up Telegraph logger
        setUpTelegraphLogger();

        // Launch JavaFX App
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        // Load main FXML layout
        Node rootNode = FXUtils.loadFXML("/fxml/Main.fxml").getKey();

        // Create Undecorated Scene window with Custom Decorator
        Scene scene = FXUtils.createDecorator(stage, rootNode, 1100, 850, true, Settings.isCurrentThemeDark(), null);

        // Configuring Stage with title, min width/height
        stage.setTitle(APP_NAME);
        stage.setMinWidth(800);
        stage.setMinHeight(600);

        // Disallow Stage resizing
        stage.setResizable(false);

        // Set created Scene to Stage
        stage.setScene(scene);

        // Save Stage in static field for further access
        currentStage = stage;

        // Show Stage
        stage.show();
    }

    private static void setUpTelegraphLogger() {
        TelegraphLogger.setLevel(Level.ALL);
        TelegraphLogger.registerLogger(new ConsoleHandler());
    }

    static {
        GSON = new Gson();
    }
}
