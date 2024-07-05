package xyz.tmuapp.utils;

import xyz.tmuapp.controller.MainController;
import xyz.tmuapp.models.UploadInfo;

import java.util.ArrayList;
import java.util.List;

public class TelegramUtils {

    public static String createTelegramPostText(UploadInfo info) {
        String newDescription = info.getDescription();
        if (StringUtils.isNotEmpty(newDescription) && newDescription.length() > 700) {
            newDescription = String.format("\n\n__%s%s__", info.getDescription().substring(0, 700), "...");
        }

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
