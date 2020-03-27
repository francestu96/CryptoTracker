package com.example.cryptotracker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.example.cryptotracker.utils.Purchase;
import com.example.cryptotracker.utils.PurchaseViewModel;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class BuyActivity extends AppCompatActivity {

    private String mCoinName;
    private String mPriceTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buy);
        Intent mIntent = getIntent();

        mCoinName = mIntent.getStringExtra("Name");
        mPriceTxt = mIntent.getStringExtra("Price");
        ((TextView) findViewById(R.id.CoinName)).setText(mCoinName);
        ((TextView) findViewById(R.id.CoinPrice)).setText(mPriceTxt);
    }

    public void onBuyClick(View v)
    {
       PurchaseViewModel mPurchaseViewModel = new ViewModelProvider(this).get(PurchaseViewModel.class);

        DateFormat df = new SimpleDateFormat("EEE, d MMM yyyy, HH:mm");
        String date = df.format(Calendar.getInstance().getTime());

        EditText qu = findViewById(R.id.Quantity);
        if(qu.getText().toString().equals("")|| qu.getText().toString().startsWith("0"))
            return;
        Purchase purchase = new Purchase(date, qu.getText().toString(), mCoinName.toLowerCase(), mPriceTxt);
        mPurchaseViewModel.insert(purchase);
        finish();
    }
}
