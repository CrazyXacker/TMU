package xyz.tmuapp.archive;

import javafx.beans.property.BooleanProperty;
import net.sf.sevenzipjbinding.IInArchive;
import net.sf.sevenzipjbinding.SevenZipException;
import net.sf.sevenzipjbinding.SevenZipNativeInitializationException;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;
import net.sf.sevenzipjbinding.simple.ISimpleInArchiveItem;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.Nullable;
import xyz.tmuapp.utils.FileUtils;

import java.io.*;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

class SevenZip implements IArchiveUnpacker {
    private RandomAccessFile randomAccessFile;
    private IInArchive inArchive;

    private ListIterator<ISimpleInArchiveItem> iterator;
    private ISimpleInArchiveItem entry;

    public static SevenZip create() {
        return new SevenZip();
    }

    private SevenZip() {
        try {
            net.sf.sevenzipjbinding.SevenZip.initSevenZipFromPlatformJAR();
        } catch (SevenZipNativeInitializationException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<String> getMediaTypes() {
        return Arrays.asList("application/x-rar-compressed", "application/x-rar-compressed; version=4", "application/x-7z-compressed");
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
            int countFiles = countFiles();

            int currentFile = 1;
            while (next()) {
                if (interrupted != null && interrupted.getValue()) {
                    return false;
                }

                String entryPath = getEntryPath();
                InputStream in = getEntryInputStream();

                if (fileNameConsumer != null) {
                    fileNameConsumer.accept(entryPath);
                }

                String entryPathWithoutFile = FileUtils.getPath(entryPath);
                File outFile = new File(outputDir + entryPathWithoutFile);
                outFile.mkdirs();

                OutputStream out = new FileOutputStream(new File(outFile, FileUtils.getFileNameWithExt(entryPath)));
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

    public void open(String archivePath) throws IOException {
        System.out.println("[SevenZip] Reading archive: " + archivePath);
        // Открытие архива для чтения

        randomAccessFile = new RandomAccessFile(archivePath, "r");
        inArchive = net.sf.sevenzipjbinding.SevenZip.openInArchive(null, new RandomAccessFileInStream(randomAccessFile));

        reset();
    }

    public void reset() throws IOException {
        iterator = Arrays.stream(inArchive.getSimpleInterface().getArchiveItems())
                .filter(it -> {
                    try {
                        return !it.isFolder();
                    } catch (SevenZipException e) {
                        return false;
                    }
                })
                .sorted((entry1, entry2) -> {
                    try {
                        return NAT_SORT_COMPARATOR.compare(entry1.getPath().toLowerCase(), entry2.getPath().toLowerCase());
                    } catch (SevenZipException e) {
                        return 0;
                    }
                })
                .collect(Collectors.toList())
                .listIterator();
    }

    public boolean next() {
        if (iterator.hasNext()) {
            entry = iterator.next();
            return true;
        }
        return false;
    }

    public long getEntrySize() throws IOException {
        return entry.getSize();
    }

    public int getDirectoriesCount() {
        try {
            return (int) Arrays.stream(inArchive.getSimpleInterface().getArchiveItems())
                    .filter(it -> {
                        try {
                            return it.isFolder();
                        } catch (SevenZipException e) {
                            return false;
                        }
                    })
                    .count();
        } catch (SevenZipException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public String getEntryName() {
        try {
            return entry.getPath();
        } catch (SevenZipException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getEntryPath() {
        try {
            return entry.getPath();
        } catch (SevenZipException e) {
            e.printStackTrace();
        }
        return null;
    }

    public InputStream getEntryInputStream() throws SevenZipException {
        return getInputStream(entry);
    }

    private InputStream getInputStream(ISimpleInArchiveItem item) throws SevenZipException {
        List<ByteArrayInputStream> arrayInputStreams = new ArrayList<>();
        item.extractSlow(data -> {
            arrayInputStreams.add(new ByteArrayInputStream(data));
            return data.length;
        });

        return new SequenceInputStream(Collections.enumeration(arrayInputStreams));
    }

    public InputStream getEntryInputStreamByName(String entryName) throws SevenZipException {
        while (next()) {
            if (getEntryName().equals(entryName)) {
                break;
            }
        }
        return getEntryName().equals(entryName) ? getEntryInputStream() : null;
    }

    public int countFiles() throws SevenZipException {
        return inArchive.getNumberOfItems();
    }

    @Override
    public void close() {
        iterator = null;
        entry = null;
        FileUtils.closeQuietly(randomAccessFile);
        FileUtils.closeQuietly(inArchive);
    }
}