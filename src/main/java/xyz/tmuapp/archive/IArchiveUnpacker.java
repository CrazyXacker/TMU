package xyz.tmuapp.archive;

import javafx.beans.property.BooleanProperty;
import org.jetbrains.annotations.Nullable;
import xyz.tmuapp.comparators.NaturalSortComparator;
import xyz.tmuapp.utils.FileUtils;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.List;
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
