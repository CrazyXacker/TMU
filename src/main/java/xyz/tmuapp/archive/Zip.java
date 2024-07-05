package xyz.tmuapp.archive;

import javafx.beans.property.BooleanProperty;
import net.sf.sevenzipjbinding.SevenZipException;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.Nullable;
import xyz.tmuapp.utils.FileUtils;
import xyz.tmuapp.utils.StringUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

class Zip implements IArchiveUnpacker {
    private String archivePath;
    private ZipFile zipFile;
    private List<? extends ZipEntry> entries;
    private ListIterator<? extends ZipEntry> iterator;
    private ZipEntry entry;
    private int countFiles;

    public static Zip create() {
        return new Zip();
    }

    private Zip() {
    }

    @Override
    public List<String> getMediaTypes() {
        return Collections.singletonList("application/zip");
    }

    @Override
    public IArchiveUnpacker createInstance() {
        return create();
    }

    @Override
    public boolean unpack(File inputFile, String outputDir, @Nullable Consumer<String> fileNameConsumer,
                          @Nullable BiConsumer<Integer, Integer> progressConsumer, @Nullable BooleanProperty interrupted) {
        return unpack(inputFile.getAbsolutePath(), outputDir, fileNameConsumer, progressConsumer, interrupted);
    }

    public boolean unpack(String inputFilePath, String outputDir, @Nullable Consumer<String> fileNameConsumer,
                          @Nullable BiConsumer<Integer, Integer> progressConsumer, @Nullable BooleanProperty interrupted) {
        try {
            open(inputFilePath);

            int currentFile = 1;
            while (next()) {
                if (interrupted != null && interrupted.getValue()) {
                    return false;
                }

                String entryPath = getEntryName();
                InputStream in = getEntryInputStream();

                if (fileNameConsumer != null) {
                    fileNameConsumer.accept(entryPath);
                }

                String entryPathWithoutFile = FileUtils.getPath(entryPath);
                File outFile = new File(outputDir + entryPathWithoutFile);
                outFile.mkdirs();

                OutputStream out = new FileOutputStream(new File(outputDir, FileUtils.getFileNameWithExt(entryPath)));
                IOUtils.copy(in, out);

                if (progressConsumer != null) {
                    progressConsumer.accept(currentFile, countFiles);
                }

                FileUtils.closeQuietly(in);
                FileUtils.closeQuietly(out);

                currentFile++;
            }

            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.err.println("Unable to create temp dir");
            return false;
        } catch (SevenZipException e) {
            e.printStackTrace();
            System.err.println("Unable to read archive stream");
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Unable to open archive or unpack file");
            return false;
        } finally {
            FileUtils.closeQuietly(this);
        }
    }

    @Override
    public void open(String archivePath) throws IOException {
        // Открытие архива для чтения
        this.archivePath = archivePath;
        System.out.println("[ZIP] Reading archive: " + archivePath);
        openWithCharset(Charset.forName("CP866"));
        reset();
    }

    private void openWithCharset(Charset charset) throws IOException {
        zipFile = new ZipFile(archivePath, charset);
    }

    @Override
    public void reset() {
        entries = Collections.list(zipFile.entries());
        countFiles = entries.size();

        iterator = entries.stream()
                .filter(it -> !it.isDirectory())
                .sorted((entry1, entry2) -> NAT_SORT_COMPARATOR.compare(entry1.getName().toLowerCase(), entry2.getName().toLowerCase()))
                .collect(Collectors.toList())
                .listIterator();
    }

    @Override
    public boolean next() {
        if (iterator.hasNext()) {
            entry = iterator.next();
            return true;
        }
        return false;
    }

    @Override
    public long getEntrySize() {
        return entry.getSize();
    }

    @Override
    public int getDirectoriesCount() {
        return entries.stream()
                .map(ZipEntry::getName)
                .map(FileUtils::getPath)
                .filter(StringUtils::isNotEmpty)
                .collect(Collectors.toSet())
                .size();
    }

    @Override
    public String getEntryName() {
        return entry.getName();
    }

    @Override
    public InputStream getEntryInputStream() throws IOException {
        return zipFile.getInputStream(entry);
    }

    @Override
    public InputStream getEntryInputStreamByName(String entryName) throws IOException {
        entry = zipFile.getEntry(entryName);
        // Maybe archive packed with in UTF-8 charset and entry has Cyrillic symbols?
        if (entry == null) {
            close();
            openWithCharset(StandardCharsets.UTF_8);
            entry = zipFile.getEntry(entryName);
        }
        if (entry == null) {
            throw new IOException();
        }
        return zipFile.getInputStream(entry);
    }

    @Override
    public void close() {
        iterator = null;
        entry = null;
        FileUtils.closeQuietly(zipFile);
    }
}