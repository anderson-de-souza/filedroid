package br.com.andersondesouza.filedroid;

import android.app.Application;
import android.os.Environment;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import java.io.File;

public class ExternalStorageViewModel extends AndroidViewModel {

    private final File externalStorage = Environment.getExternalStorageDirectory();
    private MutableLiveData<File> currentDirectoryLiveData = new MutableLiveData<>(externalStorage);

    public ExternalStorageViewModel(Application app) {
        super(app);
    }

    public File getExternalStorage() {
        return externalStorage;
    }

    public File getCurrentDirectory() {
        return currentDirectoryLiveData.getValue();
    }

    public void updateCurrentDirectory(File currentDirectory) {
        if (currentDirectory != null && currentDirectory.isDirectory() &&
                !currentDirectory.equals(externalStorage.getParentFile())) {
            currentDirectoryLiveData.postValue(currentDirectory);
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
        currentDirectoryLiveData.observe(owner, observer);
    }

}
