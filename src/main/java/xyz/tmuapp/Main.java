package xyz.tmuapp;

import com.google.gson.Gson;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.Getter;
import org.telegram.telegraph.TelegraphLogger;
import xyz.tmuapp.jna.DwmAttribute;
import xyz.tmuapp.jna.StageOps;
import xyz.tmuapp.managers.LocaleManager;
import xyz.tmuapp.managers.Settings;
import xyz.tmuapp.utils.FXUtils;
import xyz.tmuapp.utils.WorkspaceUtils;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;

public class Main extends Application {
    public static final String APP_VERSION = "v2.0";
    public static final String APP_NAME = "TelegraphMangaUploader";
    public static final String APP_GITHUB_URL = "https://github.com/CrazyXacker/TMU";
    public static final Gson GSON;

    @Getter
    private static Stage currentStage;
    @Getter
    private static Scene currentScene;
    @Getter
    private static HostServices appHostServices;

    public static String getAppName() {
        return String.format("%s (%s)", APP_NAME, APP_VERSION);
    }

    public static void main(String[] args) {
        // Launch JavaFX App
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        // Load settings file
        Settings.init();

        // Configure workspace. Create necessary folders
        WorkspaceUtils.configureWorkspace();

        // Set AppLocale and load Locale Resource Bundle
        LocaleManager.setLocale(Settings.getDefaultAppLanguageCode());
        LocaleManager.loadResourceBundle();

        // Set up Telegraph logger
        setUpTelegraphLogger();

        // Load main FXML layout
        Parent rootNode = (Parent) FXUtils.loadFXML("/fxml/Main.fxml").getKey();

        // Create Undecorated Scene window with Custom Decorator
        currentScene = FXUtils.createScene(stage, rootNode, 1100, 850, Settings.isCurrentThemeDark());

        // Save HostServices instance for URL opening
        appHostServices = getHostServices();

        // Configuring Stage with title, min width/height
        stage.setTitle(getAppName());
        stage.setMinWidth(800);
        stage.setMinHeight(600);

        // Disallow Stage resizing
        stage.setResizable(false);

        // Set created Scene to Stage
        stage.setScene(currentScene);

        // Save Stage in static field for further access
        currentStage = stage;

        // Show Stage
        stage.show();

        // Enable dark window style and Mica material on Windows
        manipulateWithNativeWindow();
    }

    public static void manipulateWithNativeWindow() {
        try {
            // Enable Mica material and dark mode
            StageOps.WindowHandle handle = new StageOps.WindowHandle(getAppName());

            // Enable the dark mode
            StageOps.dwmSetBooleanValue(handle, DwmAttribute.DWMWA_USE_IMMERSIVE_DARK_MODE, Settings.isCurrentThemeDark());

            // Enable the Mica material
            if (!StageOps.dwmSetIntValue(handle, DwmAttribute.DWMWA_SYSTEMBACKDROP_TYPE, DwmAttribute.DWMSBT_MAINWINDOW.value)) {
                StageOps.dwmSetBooleanValue(handle, DwmAttribute.DWMWA_MICA_EFFECT, Settings.isCurrentThemeDark()); // This is the "old" way
            }
        } catch (Exception ignored) {
            // Maybe launching on Linux/macOS
        }
    }

    private static void setUpTelegraphLogger() {
        TelegraphLogger.setLevel(Level.ALL);
        TelegraphLogger.registerLogger(new ConsoleHandler());
    }

    static {
        GSON = new Gson();
    }
}
