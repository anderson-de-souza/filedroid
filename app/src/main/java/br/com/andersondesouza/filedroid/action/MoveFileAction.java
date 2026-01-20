package br.com.andersondesouza.filedroid.action;

import java.io.File;
import java.util.List;

public class MoveFileAction extends FileAction {

    private CopyFileAction copyFileAction = new CopyFileAction();
    private DeleteFileAction deleteFileAction = new DeleteFileAction();

    private File targetDirectory;

    public MoveFileAction() {
        super();
    }

    public MoveFileAction(List<File> files, File targetDirectory) {
        super(files);
        copyFileAction.setTargetDirectory(targetDirectory);
    }

    public MoveFileAction(File file, File targetDirectory) {
        super(file);
        copyFileAction.setTargetDirectory(targetDirectory);
    }

    public MoveFileAction(String parent, String child, File targetDirectory) {
        super(parent, child);
        copyFileAction.setTargetDirectory(targetDirectory);
    }

    public MoveFileAction(File parent, String child, File targetDirectory) {
        super(parent, child);
        copyFileAction.setTargetDirectory(targetDirectory);
    }

    @Override
    public boolean execute(File file, int index) {
        if (copyFileAction.execute(file, index)) {
            if (deleteFileAction.execute(file, index)) {
                return true;
            }
        }
        return false;
    }

}
