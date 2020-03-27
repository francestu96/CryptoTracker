package com.example.cryptotracker.utils;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class PurchaseViewModel extends AndroidViewModel
{
    private PurchaseRepository mRepository;
    private LiveData<List<Purchase>> mAllPurchases;
    public PurchaseViewModel(Application application) {
        super(application);
        mRepository = new PurchaseRepository(application);
        mAllPurchases = mRepository.getAllPurchases();
    }
    public void insert(Purchase word) {

        mRepository.insert(word);
    }

    public LiveData<List<Purchase>> getAllPurchases() {
        return mAllPurchases;
    }
}
