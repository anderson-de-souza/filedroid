package br.com.andersondesouza.filedroid.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileOperations {

    public static boolean renameFile(File origin, String newName) {

        if (origin == null || newName == null) {
            return false;
        }

        if (!origin.exists()) {
            return false;
        }

        File destination = null;
        File parent = origin.getParentFile();

        if (parent != null) {
            destination = new File(parent, newName);
        } else {
            destination = new File(newName);
        }

        if (destination.exists()) {
            return false;
        }

        return origin.renameTo(destination);

    }

    public static boolean deleteRegularFile(File origin) {

        if (origin == null) {
            return false;
        }

        if (!origin.isFile()) {
            return  false;
        }

        return origin.delete();

    }

    public static boolean deleteDirectoryTree(File origin) {

        if (origin == null) {
            return false;
        }

        if (!origin.isDirectory()) {
            return  false;
        }

        File[] children = origin.listFiles();

        if (children != null) {

            for (File child : children) {

                if (child.isDirectory()) {
                    if (!deleteDirectoryTree(child)) {
                        return false;
                    }
                } else if (child.isFile()) {
                    if (!deleteRegularFile(child)) {
                        return false;
                    }
                }

            }
        }

        return origin.delete();

    }

    public static boolean copyFile(File origin, File destination, boolean overwrite) {

        if (origin == null || destination == null) {
            return false;
        }

        if (!origin.isFile()) {
            return false;
        }

        if (destination.isDirectory()) {
            return false;
        }

        if (!overwrite && destination.exists()) {
            return false;
        }

        File parent = destination.getParentFile();

        if (parent != null && !parent.exists()) {
            if (!parent.mkdirs()) {
                return false;
            }
        }

        try (FileInputStream inputStream = new FileInputStream(origin);
             FileOutputStream outputStream = new FileOutputStream(destination, false)) {

            byte[] buffer = new byte[8192];
            int readBytes;

            while ((readBytes = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, readBytes);
            }

        } catch (IOException e) {
            return false;
        }

        return true;

    }

    public static boolean copyDirectoryTree(File origin, File destination) {

        if (origin == null || destination == null) {
            return false;
        }

        if (!origin.isDirectory()) {
            return false;
        }

        if (destination.exists()) {
            if (!destination.isDirectory()) {
                return false;
            }
        } else {
            if (!destination.mkdirs()) {
                return false;
            }
        }

        File[] children = origin.listFiles();

        if (children != null) {

            for (File child : children) {

                if (child.isDirectory()) {

                    File targetChild = new File(destination, child.getName());

                    if (!targetChild.exists() && !targetChild.mkdirs()) {
                        return false;
                    }

                    if (!copyDirectoryTree(child, targetChild)) {
                        return false;
                    }

                } else if (child.isFile()) {

                    File targetChild = new File(destination, child.getName());

                    if (!copyFile(child, targetChild, false)) {
                        return false;
                    }

                }
            }
        }

        return true;

    }

    public static boolean moveFile(File origin, File destination) {
        return copyFile(origin, destination, false) && deleteRegularFile(origin);
    }

    public static boolean moveDirectoryTree(File origin, File destination) {
        return copyDirectoryTree(origin, destination) && deleteDirectoryTree(origin);
    }

}
