package com.jjkaps.epantry.ui.Fridge;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class FridgeViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public FridgeViewModel() {
        mText = new MutableLiveData<>();
        //mText.setValue("This is Fridge fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}