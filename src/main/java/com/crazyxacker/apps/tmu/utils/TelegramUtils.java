package com.crazyxacker.apps.tmu.utils;

import com.crazyxacker.apps.tmu.controller.MainController;
import com.crazyxacker.apps.tmu.models.UploadInfo;

import java.util.ArrayList;
import java.util.List;

public class TelegramUtils {

    public static String createTelegramPostText(UploadInfo info) {
        boolean addEllipsize = info.getDescription().length() > 700;
        String newDescription = StringUtils.isNotEmpty(info.getDescription())
                ? String.format("\n\n__%s%s__", info.getDescription().substring(0, 700), (addEllipsize ? "..." : ""))
                : "";

        String tagsLine = StringUtils.join(",", info.getTags());

        List<String> links = new ArrayList<>();
        for (int i = 0; i < info.getLinks().size(); i++) {
            links.add(String.format("%s %s", info.getTitles().get(i), info.getLinks().get(i)));
        }

        return StringUtils.isNotEmpty(tagsLine)
                ? String.format("**%s**\n\n%s%s\n\n%s", info.getTitle(), MainController.convertsTagsIntoHashtags(tagsLine), newDescription, StringUtils.join("\n", links))
                : String.format("**%s**\n\n%s%s", info.getTitle(), newDescription, StringUtils.join("\n", links));
    }
}
