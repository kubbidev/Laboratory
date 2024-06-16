package me.kubbidev.laboratory.util;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Utility class for common file and directory operations.
 */
public final class MoreFiles {
    private MoreFiles() {
    }

    /**
     * Creates a file at the specified path if it does not already exist.
     *
     * @param path The path to the file.
     * @return The path to the created or existing file.
     * @throws IOException if an I/O error occurs during file creation.
     */
    public static Path createFileIfNotExists(Path path) throws IOException {
        if (!Files.exists(path)) {
            Files.createFile(path);
        }
        return path;
    }

    /**
     * Creates a directory at the specified path if it does not already exist.
     *
     * @param path The path to the directory.
     * @return The path to the created or existing directory.
     * @throws IOException if an I/O error occurs during directory creation.
     */
    public static Path createDirectoryIfNotExists(Path path) throws IOException {
        if (Files.exists(path) && (Files.isDirectory(path) || Files.isSymbolicLink(path))) {
            return path;
        }

        try {
            Files.createDirectory(path);
        } catch (FileAlreadyExistsException e) {
            // ignore
        }

        return path;
    }

    /**
     * Creates directories at the specified path if they do not already exist.
     *
     * @param path The path to the directories.
     * @return The path to the created or existing directories.
     * @throws IOException if an I/O error occurs during directory creation.
     */
    public static Path createDirectoriesIfNotExists(Path path) throws IOException {
        if (Files.exists(path) && (Files.isDirectory(path) || Files.isSymbolicLink(path))) {
            return path;
        }

        try {
            Files.createDirectories(path);
        } catch (FileAlreadyExistsException e) {
            // ignore
        }

        return path;
    }

    /**
     * Deletes a directory and its contents recursively.
     *
     * @param path The path to the directory.
     * @throws IOException if an I/O error occurs during directory deletion.
     */
    public static void deleteDirectory(Path path) throws IOException {
        if (!Files.exists(path) || !Files.isDirectory(path)) {
            return;
        }

        try (DirectoryStream<Path> contents = Files.newDirectoryStream(path)) {
            for (Path file : contents) {
                if (Files.isDirectory(file)) {
                    deleteDirectory(file);
                } else {
                    Files.delete(file);
                }
            }
        }

        Files.delete(path);
    }

}