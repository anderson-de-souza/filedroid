package br.com.andersondesouza.filedroid.fileaction;

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
    protected boolean process(File file, int index) {
        if (file.isDirectory()) {

            File[] children = file.listFiles();

            if (children != null) {
                for (File child : children) {
                    process(child, index);
                }
            }

        }
        return file.delete();
    }

}
