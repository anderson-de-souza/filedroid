package br.com.andersondesouza.filedroid.action;

import android.os.Handler;
import android.os.Looper;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FileActionManager {

    private static FileActionManager instance;

    private static final Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    private ExecutorService executorService;
    private Set<FileAction> fileActions;

    private FileActionManager() {
        fileActions = Collections.synchronizedSet(new LinkedHashSet<>());
        executorService = Executors.newSingleThreadExecutor();
    }

    public void add(FileAction action) {
        if (fileActions.add(action)) {
            executorService.execute(() -> {
                action.start();
                fileActions.remove(action);
            });
        }
    }

    public void cancel(FileAction action) {
        if (fileActions.remove(action)) {
            action.cancel();
        }
    }

    public void shutdown() {
        executorService.shutdown();
        instance = null;
    }

    public static void addToQueue(FileAction action) {
        getInstance().add(action);
    }
    public static void cancelAction(FileAction action) {
        getInstance().cancel(action);
    }

    public static void instanceShutdown() {
        getInstance().shutdown();
    }

    public static FileActionManager getInstance() {
        if (instance == null) {
            instance = new FileActionManager();
        }
        return instance;
    }

    public static Handler getMainThreadHandler() {
        return mainThreadHandler;
    }
}
