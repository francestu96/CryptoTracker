package com.example.cryptotracker;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.collection.ArrayMap;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.widget.TextView;

import com.example.cryptotracker.model.CoinModel;
import com.example.cryptotracker.model.PurchaseCoinModel;
import com.example.cryptotracker.service.CoinGeckoService;
import com.example.cryptotracker.utils.Purchase;
import com.example.cryptotracker.utils.PurchaseViewModel;
import com.google.gson.Gson;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

import static java.lang.Float.parseFloat;


public class PurchaseList extends AppCompatActivity {

    private RecyclerView purchaseRecyclerView;
    private TextView purchaseTotalTextView ;
    private ArrayList<PurchaseCoinModel> mViewItemList = new ArrayList<>();
    private ArrayMap<String, CoinModel> mItems = new ArrayMap<>();
    private PurchaseListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchase_list);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        purchaseTotalTextView = findViewById(R.id.purchaseTotal);
        purchaseRecyclerView = findViewById(R.id.list_purchased);
        HorizontalDividerItemDecoration divider = new HorizontalDividerItemDecoration.Builder(this).build();
        purchaseRecyclerView.addItemDecoration(divider);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        purchaseRecyclerView.setLayoutManager(llm);
        adapter = new PurchaseListAdapter(mViewItemList);
        purchaseRecyclerView.setAdapter(adapter);
        getCurrencyList();
    }

    public void getCurrencyList() {
        CoinGeckoService.getCoinList(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                    CoinModel[] result = new Gson().fromJson(response.toString(), CoinModel[].class);
                    for(CoinModel coin : result)
                        mItems.put(coin.getId(), coin);
                    fillList();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {
                Log.e("Purchase List", "getCurrencyList() Server Error: " + response.toString());
            }
        });
    }

    public void fillList()
    {
        PurchaseViewModel mPurchaseViewModel = new ViewModelProvider(this).get(PurchaseViewModel.class);
        mPurchaseViewModel.getAllPurchases().observe(this, new Observer<List<Purchase>>()
        {
            @Override
            public void onChanged(@Nullable final List<Purchase> purchases)
            {
                float differences =0;
                assert purchases != null;
                for(Purchase purchase : purchases)
                {
                    CoinModel cm = mItems.get(purchase.coinName);
                    if(cm!=null)
                    {
                        float diff = parseFloat(purchase.quantity)*(parseFloat(purchase.priceUsd) - parseFloat(cm.getPrice_usd()));
                        PurchaseCoinModel model = new PurchaseCoinModel();
                        model.coinName = purchase.coinName;
                        model.dateTime = purchase.dateTime;
                        model.priceUsd = purchase.priceUsd;
                        model.quantity = purchase.quantity;
                        model.purchasedPrice = cm.getPrice_usd();
                        model.image = cm.getImageUrl();
                        mViewItemList.add(model);
                        differences += diff;
                    }
                }

                purchaseTotalTextView.setText("Total profit: $ " + differences);
                adapter = new PurchaseListAdapter(mViewItemList);
                purchaseRecyclerView.setAdapter(adapter);
            }
        });
    }
}