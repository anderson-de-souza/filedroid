package br.com.andersondesouza.filedroid.action;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class FileAction {

    private List<File> files;
    private List<File> failFiles = new ArrayList<>();

    private OnStartListener onStartListener;
    private OnProgressListener onProgressListener;
    private OnEndListener onEndListener;

    private ExecutorService executorService;
    private Handler mainHandler;

    private boolean started;
    private volatile boolean cancelled;

    public FileAction() {
        this.files = Collections.EMPTY_LIST;
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

    public synchronized void start() {
        if (started) {
            throw new IllegalStateException("Action already started");
        }

        started = true;

        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());

        executorService.execute(() -> {

            postOnStart(files);

            for (int i = 0; i < files.size(); i++) {

                if (cancelled) {
                    break;
                }

                File file = files.get(i);

                boolean success;

                try {
                    success = execute(file, i);
                } catch (Exception exception) {
                    success = false;
                }

                if (!success) {
                    failFiles.add(file);
                }

                float percent = (i + 1f) / (files.isEmpty() ? 1f : files.size());

                postOnProgress(file, success, percent);

            }

            postOnEnd(files, failFiles);

            executorService.shutdown();

        });

    }

    private void postOnStart(List<File> files) {
        if (onStartListener != null) {
            mainHandler.post(() -> onStartListener.onStart(files));
        }
    }

    private void postOnProgress(File file, boolean success, float percent) {
        if (onProgressListener != null) {
            mainHandler.post(() -> onProgressListener.onProgress(file, success, percent));
        }
    }

    private void postOnEnd(List<File> files, List<File> failFiles) {
        if (onEndListener != null) {
            mainHandler.post(() -> onEndListener.onEnd(files, failFiles));
        }
    }

    public abstract boolean execute(File file, int index);

    public void cancel() {
        this.cancelled = true;
    }

    public void setOnStartListener(OnStartListener onStartListener) {
        this.onStartListener = onStartListener;
    }

    public void setOnProgressListener(OnProgressListener onProgressListener) {
        this.onProgressListener = onProgressListener;
    }

    public void setOnEndListener(OnEndListener onEndListener) {
        this.onEndListener = onEndListener;
    }

    public interface OnStartListener {
        void onStart(List<File> files);
    }

    public interface OnProgressListener {
        void onProgress(File file, boolean success, float percent);
    }

    public interface OnEndListener {
        void onEnd(List<File> files, List<File> failFiles);
    }

}
