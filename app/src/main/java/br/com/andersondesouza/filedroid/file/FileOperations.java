package br.com.andersondesouza.filedroid.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileOperations {

    public static Results rename(File origin, String newName) {

        if (origin == null || newName == null) {
            return Results.NULL_ARGUMENT;
        }

        if (!origin.exists()) {
            return Results.NOT_FOUND;
        }

        File destination = null;
        File parent = origin.getParentFile();

        if (parent != null) {
            destination = new File(parent, newName);
        } else {
            destination = new File(newName);
        }

        if (destination.exists()) {
            return Results.ALREADY_EXISTS;
        }

        if (origin.renameTo(destination)) {
            return Results.OK;
        }

        return Results.IO_ERROR;

    }

    public static Results deleteRegularFile(File origin) {

        if (origin == null) {
            return Results.NULL_ARGUMENT;
        }

        if (!origin.exists()) {
            return Results.NOT_FOUND;
        }

        if (!origin.isFile()) {
            return  Results.NOT_A_FILE;
        }

        if (origin.delete()) {
            return Results.OK;
        }

        return Results.IO_ERROR;

    }

    public static Results deleteDirectoryTree(File origin) {

        if (origin == null) {
            return Results.NULL_ARGUMENT;
        }

        if (!origin.isDirectory()) {
            return Results.NOT_A_DIRECTORY;
        }

        File[] children = origin.listFiles();

        if (children != null) {

            for (File child : children) {

                if (child.isDirectory()) {

                    Results result = deleteDirectoryTree(child);

                    if (result != Results.OK) {
                        return result;
                    }

                } else if (child.isFile()) {

                    Results result = deleteRegularFile(child);

                    if (result != Results.OK) {
                        return result;
                    }

                }

            }
        }

        if (origin.delete()) {
            return Results.OK;
        }

        return Results.IO_ERROR;

    }

    public static Results copyFile(File origin, File destination, boolean overwrite) {

        if (origin == null || destination == null) {
            return Results.NULL_ARGUMENT;
        }

        if (!origin.isFile()) {
            return Results.NOT_A_FILE;
        }

        if (destination.isDirectory()) {
            return Results.NOT_A_FILE;
        }

        if (!overwrite && destination.exists()) {
            return Results.ALREADY_EXISTS;
        }

        File parent = destination.getParentFile();

        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            return Results.IO_ERROR;
        }

        try (FileInputStream inputStream = new FileInputStream(origin);
             FileOutputStream outputStream = new FileOutputStream(destination, false)) {

            byte[] buffer = new byte[8192];
            int readBytes;

            while ((readBytes = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, readBytes);
            }

        } catch (IOException e) {
            return Results.IO_ERROR;
        }

        return Results.OK;

    }

    public static Results copyDirectoryTree(File origin, File destination) {

        if (origin == null || destination == null) {
            return Results.NULL_ARGUMENT;
        }

        if (!origin.isDirectory()) {
            return Results.NOT_A_DIRECTORY;
        }

        if (destination.exists() && !destination.isDirectory()) {
            return Results.ALREADY_EXISTS;
        } else if (!destination.mkdirs()) {
            return Results.IO_ERROR;
        }

        File[] children = origin.listFiles();

        if (children != null) {

            for (File child : children) {

                if (child.isDirectory()) {

                    File targetChild = new File(destination, child.getName());

                    if (!targetChild.exists() && !targetChild.mkdirs()) {
                        return Results.IO_ERROR;
                    }

                    Results result = copyDirectoryTree(child, targetChild);

                    if (result != Results.OK) {
                        return result;
                    }

                } else if (child.isFile()) {

                    File targetChild = new File(destination, child.getName());
                    Results result = copyFile(child, targetChild, false);

                    if (result != Results.OK) {
                        return result;
                    }

                }
            }
        }

        return Results.OK;

    }

    public static Results moveFile(File origin, File destination) {
        if (copyFile(origin, destination, false) != Results.OK) {
            return Results.COPY_ERROR;
        }
        if (deleteRegularFile(origin) != Results.OK) {
            return Results.DELETE_ERROR;
        }
        return Results.OK;
    }

    public static Results moveDirectoryTree(File origin, File destination) {
        if (copyDirectoryTree(origin, destination) != Results.OK) {
            return Results.COPY_ERROR;
        }
        if (deleteDirectoryTree(origin) != Results.OK) {
            return Results.DELETE_ERROR;
        }
        return Results.OK;
    }

    public static enum Results {
        OK,
        NULL_ARGUMENT,
        NOT_FOUND,
        ALREADY_EXISTS,
        IO_ERROR,
        NOT_A_FILE,
        NOT_A_DIRECTORY,
        COPY_ERROR,
        DELETE_ERROR
    }

}
