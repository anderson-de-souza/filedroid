package br.com.andersondesouza.filedroid;

import android.app.Application;
import android.os.Environment;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import java.io.File;
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

    public File getCurrentDirectory() {
        return currentDirLiveData.getValue();
    }

    public void updateCurrentDirectory(File currentDir) {
        if (currentDir != null && currentDir.isDirectory() &&
                !currentDir.equals(externalStorage.getParentFile())) {
            currentDirLiveData.postValue(currentDir);
        }
    }

    public void updateCurrentDirectory() {
        updateCurrentDirectory(getCurrentDirectory());
    }

    public boolean isRoot() {
        return getCurrentDirectory().equals(externalStorage);
    }

    public void backToParent() {
        updateCurrentDirectory(getCurrentDirectory().getParentFile());
    }

    public void observeCurrentDirectory(LifecycleOwner owner, Observer<File> observer) {
        currentDirLiveData.observe(owner, observer);
    }

}
