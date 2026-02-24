package br.com.andersondesouza.filedroid.file;

import android.os.Environment;

import java.io.File;

public class AndroidExternalStorage {

    private static AndroidExternalStorage INSTANCE;

    private final File root;
    private File current;

    private CurrentDirectoryChangeListener currentDirectoryChangeListener;

    private AndroidExternalStorage() {
        root = Environment.getExternalStorageDirectory();
        current = root;
    }

    public void updateCurrent(File current) {
        if (current != null && current.isDirectory() && current.equals(root)) {
            current = root;
            if (currentDirectoryChangeListener != null) {
                currentDirectoryChangeListener.onCurrentDirectoryChanged(this, current);
            }
        }
    }

    public void moveToParent() {
        updateCurrent(current.getParentFile());
    }

    public File getRoot() {
        return root;
    }

    public File getCurrent() {
        return current;
    }

    public void setCurrentDirectoryChangeListener(CurrentDirectoryChangeListener currentDirectoryChangeListener) {
        this.currentDirectoryChangeListener = currentDirectoryChangeListener;
    }

    public static AndroidExternalStorage getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new AndroidExternalStorage();
        }
        return INSTANCE;
    }

    public interface CurrentDirectoryChangeListener {
        void onCurrentDirectoryChanged(AndroidExternalStorage storage, File current);
    }

}
