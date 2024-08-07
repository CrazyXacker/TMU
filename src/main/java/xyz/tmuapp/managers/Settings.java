package xyz.tmuapp.managers;

import com.google.gson.reflect.TypeToken;
import xyz.tmuapp.Main;
import xyz.tmuapp.models.UploadInfo;
import xyz.tmuapp.utils.ArrayUtils;
import xyz.tmuapp.utils.FileUtils;
import xyz.tmuapp.utils.TypeUtils;
import xyz.tmuapp.utils.WorkspaceUtils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

public class Settings {
    private static Properties properties;
    private static final String PROPERTIES_FILENAME = "app.properties";

    private static final Type UPLOAD_INFO_LIST_TYPE;

    public static void init() {
        properties = new Properties();
        try (FileInputStream fis = new FileInputStream(WorkspaceUtils.WORKING_DIR + PROPERTIES_FILENAME);
             InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8)) {
            properties.load(isr);
        } catch (IOException e) {
            System.err.println("Unable to load " + PROPERTIES_FILENAME);
        }
    }

    private static void setProperty(String propertyName, String propertyValue) {
        properties.setProperty(propertyName, propertyValue);
        saveProperties();
    }

    private static void saveProperties() {
        FileOutputStream fout = null;
        try {
            fout = new FileOutputStream(WorkspaceUtils.WORKING_DIR + PROPERTIES_FILENAME);
            properties.store(fout, "Auto save properties: " + new Date());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            FileUtils.closeQuietly(fout);
        }
    }

    private static final String KEY_APP_LANGUAGE = "app_language";
    private static final String DEFAULT_APP_LANGUAGE = "EN";

    public static String getDefaultAppLanguageCode() {
        return properties.getProperty(KEY_APP_LANGUAGE, DEFAULT_APP_LANGUAGE);
    }

    public static void putDefaultAppLanguageCode(String value) {
        setProperty(KEY_APP_LANGUAGE, value);
    }

    private static final String KEY_IS_CURRENT_THEME_DARK = "is_current_theme_dark";
    private static final boolean DEFAULT_IS_CURRENT_THEME_DARK = true;

    public static boolean isCurrentThemeDark() {
        return TypeUtils.getBoolDef(properties.getProperty(KEY_IS_CURRENT_THEME_DARK, null), DEFAULT_IS_CURRENT_THEME_DARK);
    }

    public static void putCurrentThemeDark(boolean value) {
        setProperty(KEY_IS_CURRENT_THEME_DARK, String.valueOf(value));
    }

    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String DEFAULT_ACCESS_TOKEN = "";

    public static String getAccessToken() {
        return properties.getProperty(KEY_ACCESS_TOKEN, DEFAULT_ACCESS_TOKEN);
    }

    public static void putAccessToken(String value) {
        setProperty(KEY_ACCESS_TOKEN, value);
    }

    private static final String KEY_AUTHOR = "author";
    private static final String DEFAULT_AUTHOR = "";

    public static String getAuthor() {
        return properties.getProperty(KEY_AUTHOR, DEFAULT_AUTHOR);
    }

    public static void putAuthor(String value) {
        setProperty(KEY_AUTHOR, value);
    }

    private static final String KEY_AUTHOR_LINK = "author_link";
    private static final String DEFAULT_AUTHOR_LINK = "";

    public static String getAuthorLink() {
        return properties.getProperty(KEY_AUTHOR_LINK, DEFAULT_AUTHOR_LINK);
    }

    public static void putAuthorLink(String value) {
        setProperty(KEY_AUTHOR_LINK, value);
    }

    private static final String KEY_PUBLISH_TAGS = "publish_tags";
    private static final boolean DEFAULT_PUBLISH_TAGS = true;

    public static boolean getPublishTags() {
        return TypeUtils.getBoolDef(properties.getProperty(KEY_PUBLISH_TAGS, null), DEFAULT_PUBLISH_TAGS);
    }

    public static void putPublishTags(boolean value) {
        setProperty(KEY_PUBLISH_TAGS, String.valueOf(value));
    }

    private static final String KEY_PUBLISH_DESCRIPTION = "publish_description";
    private static final boolean DEFAULT_PUBLISH_DESCRIPTION = true;

    public static boolean getPublishDescription() {
        return TypeUtils.getBoolDef(properties.getProperty(KEY_PUBLISH_DESCRIPTION, null), DEFAULT_PUBLISH_DESCRIPTION);
    }

    public static void putPublishDescription(boolean value) {
        setProperty(KEY_PUBLISH_DESCRIPTION, String.valueOf(value));
    }

    private static final String KEY_UPLOAD_INFO = "upload_info";
    private static final String DEFAULT_UPLOAD_INFO = null;

    public static List<UploadInfo> getPreviousUploadInfo() {
        try {
            List<UploadInfo> list = Main.GSON.fromJson(properties.getProperty(KEY_UPLOAD_INFO, DEFAULT_UPLOAD_INFO), UPLOAD_INFO_LIST_TYPE);
            return !ArrayUtils.isNull(list) ? list : new ArrayList<>();
        } catch (Exception ex) {
            return new ArrayList<>();
        }
    }

    public static void putPreviousUploadInfo(List<UploadInfo> uploadInfo) {
        setProperty(KEY_UPLOAD_INFO, Main.GSON.toJson(uploadInfo));
    }

    static {
        UPLOAD_INFO_LIST_TYPE = new TypeToken<ArrayList<UploadInfo>>() {
        }.getType();
    }
}
