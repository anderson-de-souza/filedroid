package br.com.andersondesouza.filedroid.fileaction;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class CopyFileAction extends FileAction {

    private File targetDirectory;

    public CopyFileAction() {
        super();
    }

    public CopyFileAction(List<File> files, File targetDirectory) {
        super(files);
        setTargetDirectory(targetDirectory);
    }

    public CopyFileAction(File file, File targetDirectory) {
        super(file);
        setTargetDirectory(targetDirectory);
    }

    public CopyFileAction(String parent, String child, File targetDirectory) {
        super(parent, child);
        setTargetDirectory(targetDirectory);
    }

    public CopyFileAction(File parent, String child, File targetDirectory) {
        super(parent, child);
        setTargetDirectory(targetDirectory);
    }

    @Override
    protected boolean process(File file, int index) {
        if (file.isDirectory()) {
            return copyDirectoryTree(file, targetDirectory);
        } else {
            return copy(file, new File(targetDirectory, file.getName()));
        }
    }

    private boolean copyDirectoryTree(File origin, File targetDirectory) {
        if (targetDirectory.isDirectory()) {
            postOnFileConflict(origin, targetDirectory);
            if (getOnFileConflictListener() != null) {
                pause();
            }
        }

        File target = new File(targetDirectory, origin.getName());

        if (!target.isDirectory()) {
            target.mkdirs();
        }

        File[] children = origin.listFiles();

        if (children != null) {
            for (File child: children) {

                boolean success;

                if (child.isDirectory()) {
                    success = copyDirectoryTree(child, target);
                } else {
                    success = copy(child, new File(target, child.getName()));
                }

                if (!success) {
                    return false;
                }

            }
        }
        return true;
    }

    private boolean copy(File origin, File target) {
        if (target.isFile()) {
            postOnFileConflict(origin, targetDirectory);
            if (getOnFileConflictListener() != null) {
                pause();
            }
        }

        FileInputStream inputStream = null;
        FileOutputStream outputStream = null;

        try {
            inputStream = new FileInputStream(origin);
            outputStream = new FileOutputStream(target);

            byte[] buffer = new byte[8192]; // 8KB
            int readedBytes;

            while ((readedBytes = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, readedBytes);
            }

        } catch (IOException exception) {
            return false;

        } finally {
            try {

                if (inputStream != null) {
                    inputStream.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }

            } catch (IOException e) {
                return false;
            }

        }
        return true;
    }

    private void checkTargetDirectory() {
        if (targetDirectory != null && !targetDirectory.isDirectory()) {
            throw new RuntimeException("Invalid Target Directory");
        }
    }

    public void setTargetDirectory(File targetDirectory) {
        this.targetDirectory = targetDirectory;
        checkTargetDirectory();
    }

}
