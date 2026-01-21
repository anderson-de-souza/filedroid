package br.com.andersondesouza.filedroid.action;

import java.io.File;
import java.util.List;

public class CreateDirectoryAction extends FileAction {

    public CreateDirectoryAction() {
        super();
    }

    public CreateDirectoryAction(List<File> files) {
        super(files);
    }

    public CreateDirectoryAction(File file) {
        super(file);
    }

    public CreateDirectoryAction(String parent, String child) {
        super(parent, child);
    }

    public CreateDirectoryAction(File parent, String child) {
        super(parent, child);
    }

    @Override
    protected boolean execute(File file, int index) {
        if (!file.isDirectory()) {
            return file.mkdirs();
        }
        return false;
    }
}
