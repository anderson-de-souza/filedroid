package br.com.andersondesouza.filedroid;

import android.app.Application;
import android.os.Environment;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExternalStorageViewModel extends AndroidViewModel {

    private final File externalStorage = Environment.getExternalStorageDirectory();
    private MutableLiveData<File> currentDirLiveData = new MutableLiveData<>(externalStorage);

    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    public ExternalStorageViewModel(Application app) {
        super(app);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }

    public File getExternalStorage() {
        return externalStorage;
    }

    public File getCurrentDir() {
        return currentDirLiveData.getValue();
    }

    public void updateCurrentDir(File currentDir) {
        if (currentDir != null && currentDir.isDirectory() && !currentDir.equals(externalStorage.getParentFile())) {
            currentDirLiveData.postValue(currentDir);
        }
    }

    public void createNewFile(String fileName) {
        if (fileName != null) {

            File newFile = new File(getCurrentDir(), fileName);

            File parentDir = new File(newFile.getParent());

            if (!parentDir.exists()) {
                parentDir.mkdirs();
            }

            if (!newFile.exists()) {
                try {
                    if (newFile.createNewFile()) {
                        updateCurrentDir(getCurrentDir());
                    }
                } catch (IOException exception) {
                    throw new RuntimeException(exception);
                }
            }

        }
    }

    public void asyncCreateNewFile(String fileName) {
        executorService.execute(() -> createNewFile(fileName));
    }

    public void createNewDir(String dirName) {
        if (dirName != null) {

            File newDir = new File(getCurrentDir(), dirName);

            if (!newDir.exists()) {
                if (newDir.mkdirs()) {
                    updateCurrentDir(newDir);
                }
            }

        }
    }

    public void asyncCreateNewDir(String dirName) {
        executorService.execute(() -> createNewDir(dirName));
    }

    public void observeCurrentDir(LifecycleOwner owner, Observer<File> observer) {
        currentDirLiveData.observe(owner, observer);
    }

}
