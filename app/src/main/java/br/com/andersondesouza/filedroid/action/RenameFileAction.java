package br.com.andersondesouza.filedroid.action;

import java.io.File;
import java.util.List;

public class RenameFileAction extends FileAction {

    private String pattern;
    private String placeholder;

    public RenameFileAction() {
        super();
    }

    public RenameFileAction(List<File> files, String pattern, String placeholder) {
        super(files);
        this.pattern = pattern;
        this.placeholder = placeholder;
    }

    public RenameFileAction(File file, String pattern, String placeholder) {
        super(file);
        this.pattern = pattern;
        this.placeholder = placeholder;
    }

    public RenameFileAction(String parent, String child, String pattern, String placeholder) {
        super(parent, child);
        this.pattern = pattern;
        this.placeholder = placeholder;
    }

    public RenameFileAction(File parent, String child, String pattern, String placeholder) {
        super(parent, child);
        this.pattern = pattern;
        this.placeholder = placeholder;
    }

    @Override
    protected boolean execute(File file, int index) {
        File patternFile = new File(pattern.replace(placeholder, Integer.toString(index)));
        File parent = file.getParentFile();

        File target;

        if (parent == null) {
            target = new File(patternFile.getName());
        } else {
            target = new File(parent, patternFile.getName());
        }

        return file.renameTo(target);
    }

}
