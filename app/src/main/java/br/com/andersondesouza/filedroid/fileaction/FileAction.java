package br.com.andersondesouza.filedroid.fileaction;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public abstract class FileAction {

    private static final Object loopBlocker = new Object();

    private List<File> files;
    private List<File> failFiles = new ArrayList<>();

    private OnStartListener onStartListener;
    private OnProgressListener onProgressListener;
    private OnFileConflictListener onFileConflictListener;
    private OnEndListener onEndListener;

    private volatile boolean paused;
    private volatile boolean cancelled;

    public FileAction() {
    }

    public FileAction(List<File> files) {
        this.files = files;
    }

    public FileAction(File file) {
        this.files = List.of(file);
    }

    public FileAction(String parent, String child) {
        this.files = List.of(new File(parent, child));
    }

    public FileAction(File parent, String child) {
        this.files = List.of(new File(parent, child));
    }

    protected void execute() {
        postOnStart(files);

        for (int i = 0; i < files.size(); i++) {
            if (cancelled) {
                break;
            }

            File file = files.get(i);
            boolean success = false;

            if (file != null) {
                success = process(file, i);
            }

            if (!success) {
                failFiles.add(file);
            }

            postOnProgress(file, success);
        }
        postOnEnd();
    }

    private void postOnStart(List<File> files) {
        if (onStartListener != null) {
            FileActionManager.runOnMainThread(() -> onStartListener.onStart(files));
        }
    }

    private void postOnProgress(File file, boolean success) {
        if (onProgressListener != null) {
            FileActionManager.runOnMainThread(() -> onProgressListener.onProgress(files, failFiles, file, success));
        }
    }

    protected void postOnFileConflict(File origin, File targetDirectory) {
        if (onFileConflictListener != null) {
            FileActionManager.runOnMainThread(() -> onFileConflictListener.onFileConflict(this, origin, targetDirectory));
        }
    }

    private void postOnEnd() {
        if (onEndListener != null) {
            FileActionManager.runOnMainThread(() -> onEndListener.onEnd(files, failFiles));
        }
    }

    protected abstract boolean process(File file, int index);

    public void cancel() {
        this.cancelled = true;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void pause() {
        synchronized (loopBlocker) {
            paused = true;
            while (paused) {
                try {
                    loopBlocker.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public void resume() {
        synchronized (loopBlocker) {
            paused = false;
            loopBlocker.notifyAll();
        }
    }

    public boolean isPaused() {
        return paused;
    }

    public FileAction setOnStartListener(OnStartListener onStartListener) {
        this.onStartListener = onStartListener;
        return this;
    }

    public OnStartListener getOnStartListener() {
        return onStartListener;
    }

    public FileAction setOnProgressListener(OnProgressListener onProgressListener) {
        this.onProgressListener = onProgressListener;
        return this;
    }

    public OnProgressListener getOnProgressListener() {
        return onProgressListener;
    }

    public FileAction setOnFileConflictListener(OnFileConflictListener onFileConflictListener) {
        this.onFileConflictListener = onFileConflictListener;
        return this;
    }

    public OnFileConflictListener getOnFileConflictListener() {
        return onFileConflictListener;
    }

    public FileAction setOnEndListener(OnEndListener onEndListener) {
        this.onEndListener = onEndListener;
        return this;
    }

    public OnEndListener getOnEndListener() {
        return onEndListener;
    }

    public interface OnStartListener {
        void onStart(List<File> files);
    }

    public interface OnProgressListener {
        void onProgress(List<File> files, List<File> failFiles, File file, boolean success);
    }

    public interface OnFileConflictListener {
        void onFileConflict(FileAction fileAction, File origin, File targetDirectory);
    }

    public interface OnEndListener {
        void onEnd(List<File> files, List<File> failFiles);
    }

}
