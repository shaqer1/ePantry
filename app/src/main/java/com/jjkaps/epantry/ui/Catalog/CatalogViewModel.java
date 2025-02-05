package com.jjkaps.epantry.ui.Catalog;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class CatalogViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public CatalogViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("Your catalog is empty :(");
    }

    public LiveData<String> getText() {
        return mText;
    }
}