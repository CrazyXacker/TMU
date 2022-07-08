package com.crazyxacker.apps.tmu.archive;

import com.crazyxacker.apps.tmu.comparators.NaturalSortComparator;
import com.crazyxacker.apps.tmu.models.BookInfo;
import com.crazyxacker.apps.tmu.utils.FileUtils;
import javafx.beans.property.BooleanProperty;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface IArchiveUnpacker extends Closeable {
    Comparator<String> NAT_SORT_COMPARATOR = new NaturalSortComparator();

    List<String> getMediaTypes();

    IArchiveUnpacker createInstance();
    void open(String archivePath) throws IOException;
    void reset() throws IOException;
    boolean next();
    long getEntrySize() throws IOException;
    int getDirectoriesCount();
    String getEntryName() throws IOException;
    InputStream getEntryInputStream() throws IOException;
    InputStream getEntryInputStreamByName(String entryName) throws IOException;
    boolean unpack(File inputFile, String outputDir, @Nullable Consumer<String> fileNameConsumer,
                   @Nullable BiConsumer<Integer, Integer> progressConsumer, @Nullable BooleanProperty interrupted);
    void close();

    default boolean isArchiveHasChapters(File archiveFile) {
        try {
            open(archiveFile.toString());
            return getDirectoriesCount() > 1;
        } catch (Exception ignored) {
        } finally {
            FileUtils.closeQuietly(this);
        }
        return false;
    }
}
