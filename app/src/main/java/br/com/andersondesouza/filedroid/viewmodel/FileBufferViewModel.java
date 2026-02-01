package br.com.andersondesouza.filedroid.viewmodel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileBufferViewModel extends AndroidViewModel {

    private MutableLiveData<List<File>> fileBufferLiveData = new MutableLiveData<>(new ArrayList<>());

    public FileBufferViewModel(Application app) {
        super(app);
    }

    public void updateFileBuffer(List<File> fileBuffer) {
        fileBufferLiveData.postValue(new ArrayList<>(fileBuffer));
    }

    public void clearFileBuffer() {
        fileBufferLiveData.postValue(new ArrayList<>());
    }

    public MutableLiveData<List<File>> getFileBufferLiveData() {
        return fileBufferLiveData;
    }

    public List<File> getFileBuffer() {
        return fileBufferLiveData.getValue();
    }

    public void observeFileBuffer(LifecycleOwner owner, Observer<List<File>> observer) {
        fileBufferLiveData.observe(owner, observer);
    }

    public boolean isEmptyFileBuffer() {
        return getFileBuffer().isEmpty();
    }

    public int getFileBufferCount() {
        return getFileBuffer().size();
    }

}
