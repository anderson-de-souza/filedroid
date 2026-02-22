package br.com.andersondesouza.filedroid.file;

import android.os.Handler;
import android.os.Looper;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FileActionManager {

    private static final Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    private static FileActionManager instance;

    private ExecutorService executorService;
    private Set<FileAction> fileActions;

    private FileActionManager() {
        fileActions = Collections.synchronizedSet(new LinkedHashSet<>());
        executorService = Executors.newSingleThreadExecutor();
    }

    public void add(FileAction action) {
        if (fileActions.add(action)) {
            executorService.execute(() -> {
                action.execute();
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
        if (executorService != null) {
            executorService.shutdown();
        }
        instance = null;
    }

    public static void runOnMainThread(Runnable task) {
        getMainThreadHandler().post(task);
    }

    public static Handler getMainThreadHandler() {
        return mainThreadHandler;
    }

    public static FileActionManager getInstance() {
        if (instance == null) {
            instance = new FileActionManager();
        }
        return instance;
    }

}
