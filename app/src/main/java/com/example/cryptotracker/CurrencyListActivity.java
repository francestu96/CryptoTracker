package com.example.cryptotracker;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.example.cryptotracker.model.CoinModel;
import com.google.android.material.tabs.TabLayout;

public class CurrencyListActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener, CurrencyListFragment.FavoritesListUpdater, FavoriteCurrencyListFragment.AllCoinsListUpdater {
    public final static String DAY = "24h";
    public final static String SORT_SETTING = "sort_setting";

    private SectionsPagerAdapterCurrencyList currencyPageAdapter;
    public ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_currency_list);
        checkAppPermission();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TabLayout tabLayout = findViewById(R.id.currency_list_tabs);
        viewPager = findViewById(R.id.currency_list_tabs_container);

        currencyPageAdapter = new SectionsPagerAdapterCurrencyList(getSupportFragmentManager());
        viewPager.setAdapter(currencyPageAdapter);
        viewPager.setOffscreenPageLimit(2);
        viewPager.addOnPageChangeListener(this);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setSelectedTabIndicatorColor(Color.WHITE);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {

        Fragment fragment = currencyPageAdapter.getFragment(position);
        if (fragment != null) {
            fragment.onResume();
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    public void removeFavorite(CoinModel coin) {
        FavoriteCurrencyListFragment frag = (FavoriteCurrencyListFragment) currencyPageAdapter.getFragment(1);
        if (frag != null) {
            frag.removeFavorite(coin);
        }
    }

    public void addFavorite(CoinModel coin) {
        FavoriteCurrencyListFragment frag = (FavoriteCurrencyListFragment) currencyPageAdapter.getFragment(1);
        if (frag != null) {
            frag.addFavorite(coin);
        }
    }

    public void allCoinsModifyFavorites(CoinModel coin) {
        CurrencyListFragment frag = (CurrencyListFragment) currencyPageAdapter.getFragment(0);
        if (frag != null) {
            frag.getAdapter().notifyDataSetChanged();
        }
    }

    public void performFavsSort() {
        FavoriteCurrencyListFragment frag = (FavoriteCurrencyListFragment) currencyPageAdapter.getFragment(1);
        if (frag != null) {
            frag.performFavsSort();
        }
    }

    public void performAllCoinsSort() {
        CurrencyListFragment frag = (CurrencyListFragment) currencyPageAdapter.getFragment(0);
        if (frag != null) {
            frag.performAllCoinsSort();
        }
    }

    private void checkAppPermission(){
        Log.d("ACCESS_NETWORK_STATE", String.valueOf(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE)));
        Log.d("INTERNET", String.valueOf(ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET)));
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_NETWORK_STATE}, 1);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, 1);
        }
    }
}
