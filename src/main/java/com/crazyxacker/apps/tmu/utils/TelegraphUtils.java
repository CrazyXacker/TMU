package com.crazyxacker.apps.tmu.utils;

import com.crazyxacker.apps.tmu.Main;
import com.crazyxacker.apps.tmu.managers.LocaleManager;
import com.crazyxacker.apps.tmu.managers.Settings;
import com.crazyxacker.apps.tmu.models.TelegraphUpload;
import com.google.gson.reflect.TypeToken;
import javafx.beans.property.BooleanProperty;
import org.telegram.telegraph.api.methods.CreateAccount;
import org.telegram.telegraph.api.methods.CreatePage;
import org.telegram.telegraph.api.methods.EditAccountInfo;
import org.telegram.telegraph.api.objects.*;
import org.telegram.telegraph.exceptions.TelegraphException;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class TelegraphUtils {
    private static final Type LIST_TYPE;

    public static boolean checkAccount() {
        if (StringUtils.isEmpty(Settings.getAccessToken())) {
            // Create Telegraph Account
            try {
                Account account = new CreateAccount("TMU")
                        .setAuthorName("TMU")
                        .execute();

                // Save access token into Settings
                Settings.putAccessToken(account.getAccessToken());
            } catch (TelegraphException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    public static boolean editAccount(String authorName, String authorLink) {
        if (!StringUtils.isEmpty(Settings.getAccessToken())) {
            // Edit Telegraph Account
            try {
                new EditAccountInfo(Settings.getAccessToken())
                        .setAuthorName(StringUtils.isNotEmpty(authorName) ? authorName : "TMU")
                        .setAuthorUrl(StringUtils.isNotEmpty(authorLink) ? authorLink : "")
                        .execute();
            } catch (TelegraphException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    public static String createPage(String title, String tags, List<String> images, String authorName, String authorLink) {
        List<Node> content = new ArrayList<>();
        if (StringUtils.isNotEmpty(tags)) {
            Node tagsParagraph = new NodeElement("p", new HashMap<>(), new ArrayList<>() {{
                add(new NodeElement("b", new HashMap<>(), Collections.singletonList(new NodeText(LocaleManager.getString("gui.tags_node")))));
                add(new NodeText(tags));
            }});
            content.add(tagsParagraph);
        }

        for (String image : images) {
            Map<String, String> attrs = new HashMap<>();
            attrs.put("src", image);

            Node imageNode = new NodeElement("img", attrs, new ArrayList<>());
            content.add(imageNode);
        }

        try {
            Page page = new CreatePage(Settings.getAccessToken(), title, content)
                    .setAuthorName(StringUtils.isNotEmpty(authorName) ? authorName : "TMU")
                    .setAuthorUrl(StringUtils.isNotEmpty(authorLink) ? authorLink : "")
                    .setReturnContent(true)
                    .execute();

            return page.getUrl();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static List<String> uploadImages(List<File> images, Consumer<String> fileNameConsumer,
                                            BiConsumer<Integer, Integer> progressConsumer,
                                            BooleanProperty interruptedProperty) {
        try {
            List<String> imageUrls = new ArrayList<>();
            if (ArrayUtils.isNotEmpty(images)) {
                for (int i = 0; i < images.size(); i++) {
                    if (interruptedProperty.get()) {
                        System.err.println("Uploading interrupted by user...");
                        return new ArrayList<>();
                    }

                    File image = images.get(i);
                    fileNameConsumer.accept(image.getName());
                    progressConsumer.accept(i, images.size());

                    System.out.println("Uploading image: " + image.getAbsolutePath());
                    MultipartUtil multipartUtil = new MultipartUtil(TelegraphConstants.UPLOAD_URL, "UTF-8");
                    multipartUtil.addFilePart(image.getName(), image);
                    String response = multipartUtil.finish();

                    TelegraphUtils.extractSrcUrlsFromResponse(response, imageUrls);

                    Thread.sleep(700);
                }
            }

            progressConsumer.accept(images.size(), images.size());
            return imageUrls;
        } catch (IOException | InterruptedException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static void extractSrcUrlsFromResponse(String response, List<String> imageUrls) {
        List<TelegraphUpload> upload = Main.GSON.fromJson(response, LIST_TYPE);
        for (TelegraphUpload telegraphUpload : upload) {
            System.out.println("Uploaded image url: " + telegraphUpload.getSrcUrl());
            imageUrls.add(telegraphUpload.getSrcUrl());
        }
    }

    static {
        LIST_TYPE = new TypeToken<ArrayList<TelegraphUpload>>() {
        }.getType();
    }
}
