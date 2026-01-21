package br.com.andersondesouza.filedroid.action;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public abstract class FileAction {

    private List<File> files;
    private List<File> failFiles = new ArrayList<>();

    private OnStartListener onStartListener;
    private OnProgressListener onProgressListener;
    private OnEndListener onEndListener;

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

    public void start() {
        postOnStart(files);

        for (int i = 0; i < files.size(); i++) {

            if (cancelled) {
                break;
            }

            File file = files.get(i);
            boolean success = false;

            if (file != null) {
                success = execute(file, i);
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
            FileActionManager.getMainThreadHandler()
                .post(() -> onStartListener.onStart(files));
        }
    }

    private void postOnProgress(File file, boolean success) {
        if (onProgressListener != null) {
            FileActionManager.getMainThreadHandler()
                .post(() -> onProgressListener.onProgress(files, failFiles, file, success));
        }
    }

    private void postOnEnd() {
        if (onEndListener != null) {
            FileActionManager.getMainThreadHandler()
                .post(() -> onEndListener.onEnd(files, failFiles));
        }
    }

    protected abstract boolean execute(File file, int index);

    public void cancel() {
        this.cancelled = true;
    }

    public FileAction setOnStartListener(OnStartListener onStartListener) {
        this.onStartListener = onStartListener;
        return this;
    }

    public FileAction setOnProgressListener(OnProgressListener onProgressListener) {
        this.onProgressListener = onProgressListener;
        return this;
    }

    public FileAction setOnEndListener(OnEndListener onEndListener) {
        this.onEndListener = onEndListener;
        return this;
    }

    public interface OnStartListener {
        void onStart(List<File> files);
    }

    public interface OnProgressListener {
        void onProgress(List<File> files, List<File> failFiles, File file, boolean success);
    }

    public interface OnEndListener {
        void onEnd(List<File> files, List<File> failFiles);
    }

}
