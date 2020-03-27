package com.example.cryptotracker.utils;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface PurchaseDao
{
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Purchase word);

    @Query("SELECT * from purchaseTable ORDER BY dateTime ASC")
    LiveData<List<Purchase>> getAllPurchases();

    @Query("DELETE FROM purchaseTable")
    void deleteAll();
}
