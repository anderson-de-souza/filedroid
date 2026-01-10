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
    public boolean execute(File file, int index) {;

        if (file != null && !file.exists()) {

            File parent = file.getParentFile();

            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }

            try {
                return file.createNewFile();
            } catch (IOException exception) {
                return false;
            }

        }

        return false;

    }

}
