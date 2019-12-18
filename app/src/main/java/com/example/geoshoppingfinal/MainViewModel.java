package com.example.geoshoppingfinal;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
/**
 * Created by Luke Shaw 17072613
 */
//View model for Main fragment
//Automatically generated when creating this project
public class MainViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public MainViewModel() {
        mText = new MutableLiveData<>();
    }

    public void addTitle(String title) {
        mText.postValue(title);
    }

    public LiveData<String> getText() {
        return mText;
    }
}