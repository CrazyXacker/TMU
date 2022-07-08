package com.crazyxacker.apps.tmu.utils;

import com.crazyxacker.apps.tmu.controller.MainController;

public class TelegramUtils {

    public static String createTelegramPostText(String title, String tags, String description, String url) {
        boolean addEllipsize = description.length() > 700;
        String newDescription = StringUtils.isNotEmpty(description)
                ? String.format("\n\n__%s%s__", description.substring(0, 700), (addEllipsize ? "..." : ""))
                : "";

        String linksNewLineSeparated = StringUtils.join("\n", ArrayUtils.splitString(url, ", "));
        return StringUtils.isNotEmpty(tags)
                ? String.format("**%s**\n\n%s%s\n\n%s", title, MainController.convertsTagsIntoHashtags(tags), newDescription, linksNewLineSeparated)
                : String.format("**%s**\n\n%s%s", title, newDescription, url);
    }
}
