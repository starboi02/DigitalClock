package com.example.digitalclock.ui.stopwatch;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class StopwatchViewModel extends ViewModel {
    private MutableLiveData<String> mText;

    public StopwatchViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is Stopwatch fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}