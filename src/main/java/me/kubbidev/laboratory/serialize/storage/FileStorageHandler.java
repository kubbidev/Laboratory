package me.kubbidev.laboratory.serialize.storage;

import me.kubbidev.laboratory.util.MoreFiles;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

/**
 * Utility class for handling storage file i/o.
 * Saves backups of the data files on each save.
 *
 * @param <T> the type being stored
 */
public abstract class FileStorageHandler<T> {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd-HH-mm");

    private final String fileName;
    private final String fileExtension;
    private final Path dataFolder;

    public FileStorageHandler(String fileName, String fileExtension, Path dataFolder) {
        this.fileName = fileName;
        this.fileExtension = fileExtension;

        this.dataFolder = dataFolder;
        try {
            MoreFiles.createDirectoriesIfNotExists(this.dataFolder);
        } catch (IOException e) {
            // ignore
        }
    }

    protected abstract T readFromFile(Path path);

    protected abstract void saveToFile(Path path, T t);

    private Path resolveFile() {
        return this.dataFolder.resolve(this.fileName + this.fileExtension);
    }

    public Optional<T> load() {
        Path file = resolveFile();
        return Files.exists(file)
                ? Optional.ofNullable(readFromFile(file))
                : Optional.empty();
    }

    public void save(T data) throws IOException {
        Path file = resolveFile();
        if (Files.exists(file)) {
            Files.delete(file);
        }

        MoreFiles.createFileIfNotExists(file);
        saveToFile(file, data);
    }

    public void saveAndBackup(T data) throws IOException {
        Path file = resolveFile();
        if (Files.exists(file)) {
            Path backupFile = createBackupDirectory().resolve(this.fileName + "-" + buildBackupDate() + this.fileExtension);
            try {
                Files.move(file, backupFile, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        MoreFiles.createFileIfNotExists(file);
        saveToFile(file, data);
    }

    private Path createBackupDirectory() {
        // create the backup directory if dont already exist
        Path backupDirectory = this.dataFolder.resolve("backups");
        try {
            MoreFiles.createDirectoriesIfNotExists(backupDirectory);
        } catch (IOException e) {
            // ignore
        }
        return backupDirectory;
    }

    private static String buildBackupDate() {
        return DATE_FORMAT.format(new Date(System.currentTimeMillis()));
    }
}