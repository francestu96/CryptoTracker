package com.example.cryptotracker.utils;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.room.Database;
import androidx.room.Entity;

import androidx.room.PrimaryKey;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Entity(tableName = "purchaseTable")
public class Purchase {

    @PrimaryKey(autoGenerate = true)
    public int id;
    public String dateTime;
    public String quantity;
    public String coinName;
    public String priceUsd;

    public Purchase(String dateTime, String quantity, String coinName, String priceUsd)
    {
        this.dateTime = dateTime;
        this.quantity = quantity;
        this.coinName = coinName;
        this.priceUsd = priceUsd;
    }
}


class PurchaseRepository {
    private PurchaseDao mPurchaseDao;
    private LiveData<List<Purchase>> mAllPurchases;
    PurchaseRepository(Application application) {
        PurchaseRoomDatabase db = PurchaseRoomDatabase.getDatabase(application);
        mPurchaseDao = db.PurchaseDao();
        mAllPurchases = mPurchaseDao.getAllPurchases();
    }
    void insert(Purchase purchase) {
        PurchaseRoomDatabase.databaseWriteExecutor.execute(() -> {
            mPurchaseDao.insert(purchase);
        });
    }

    LiveData<List<Purchase>> getAllPurchases() {
        return mAllPurchases;
    }
}

@Database(entities = {Purchase.class}, version = 1, exportSchema = false)
abstract class PurchaseRoomDatabase extends RoomDatabase {

    abstract PurchaseDao PurchaseDao();

    private static volatile PurchaseRoomDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    static PurchaseRoomDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (PurchaseRoomDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            PurchaseRoomDatabase.class, "word_database")
                            .addCallback(sRoomDatabaseCallback)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);

            // If you want to keep data through app restarts,
            // comment out the following block
            databaseWriteExecutor.execute(() -> {
                // Populate the database in the background.
                // If you want to start with more words, just add them.
              //  PurchaseDao dao = INSTANCE.PurchaseDao();
               // dao.deleteAll();

                /*DateFormat df = new SimpleDateFormat("EEE, d MMM yyyy, HH:mm");
                String date = df.format(Calendar.getInstance().getTime());

               Purchase word = new Purchase(date, 123, "Bitcoin", "12345");
                dao.insert(word);*/
            });
        }
    };
}