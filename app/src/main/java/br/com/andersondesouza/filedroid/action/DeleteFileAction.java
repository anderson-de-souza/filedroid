package br.com.andersondesouza.filedroid.action;

import java.io.File;
import java.util.List;

public class DeleteFileAction extends FileAction {

    public DeleteFileAction() {
        super();
    }

    public DeleteFileAction(List<File> files) {
        super(files);
    }

    public DeleteFileAction(File file) {
        super(file);
    }

    public DeleteFileAction(String parent, String child) {
        super(parent, child);
    }

    public DeleteFileAction(File parent, String child) {
        super(parent, child);
    }

    @Override
    public boolean execute(File file, int index) {

        if (file != null && file.isDirectory()) {

            File[] children = file.listFiles();

            if (children != null) {
                for (File child : children) {
                    execute(child, index);
                }
            }

        }

        return file.delete();
    }

}
