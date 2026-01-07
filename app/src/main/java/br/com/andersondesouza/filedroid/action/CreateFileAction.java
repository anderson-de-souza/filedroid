package br.com.andersondesouza.filedroid.action;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class CreateFileAction extends FileAction {

    public CreateFileAction() {
        super();
    }

    public CreateFileAction(List<File> files) {
        super(files);
    }

    public CreateFileAction(File file) {
        super(file);
    }

    public CreateFileAction(String parent, String child) {
        super(parent, child);
    }

    public CreateFileAction(File parent, String child) {
        super(parent, child);
    }

    @Override
    public boolean execute(File file) {;

        File parentDir = new File(file.getParent());

        if (!parentDir.exists()) {
            parentDir.mkdirs();
        }

        if (!file.exists()) {
            try {
                return file.createNewFile();
            } catch (IOException exception) {
                return false;
            }
        }

        return false;

    }

}
