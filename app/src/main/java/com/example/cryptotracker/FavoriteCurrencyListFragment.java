package com.example.cryptotracker;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.afollestad.materialdialogs.MaterialDialog;
import com.example.cryptotracker.model.CoinFavoritesModel;
import com.example.cryptotracker.model.CoinLookUpModel;
import com.example.cryptotracker.model.CoinModel;

import com.example.cryptotracker.service.CoinGeckoService;
import com.example.cryptotracker.utils.ItemClickListener;
import com.example.cryptotracker.utils.MyDatabaseFactory;
import com.google.gson.Gson;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import cz.msebera.android.httpclient.Header;

import static android.content.Context.MODE_PRIVATE;
import static com.example.cryptotracker.utils.SortUtils.sortList;
import static com.example.cryptotracker.CurrencyListActivity.SORT_SETTING;

public class FavoriteCurrencyListFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private View rootView;

    private SwipeRefreshLayout swipeRefreshLayout;
    private MyDatabaseFactory db;
    private RecyclerView currencyRecyclerView;
    private FavoriteCurrencyListAdapter adapter;
    private ArrayList<CoinModel> currencyItemFavsList = new ArrayList<>();
    private AllCoinsListUpdater favsUpdateCallback;
    private AppCompatActivity mContext;
    private HashMap<String, String> nameToIDMap = new HashMap<>();
    private SharedPreferences sharedPreferences;

    public interface AllCoinsListUpdater {
        void allCoinsModifyFavorites(CoinModel coin);
        void performAllCoinsSort();
    }

    public FavoriteCurrencyListFragment() {
    }

    public static FavoriteCurrencyListFragment newInstance() {
        return new FavoriteCurrencyListFragment();
    }

    public void performFavsSort() {
        int sortType = sharedPreferences.getInt(SORT_SETTING, 1);
        sortList(adapter.getCurrencyList(), sortType);
        adapter.notifyDataSetChanged();
    }

    public void getCurrencyList() {
        swipeRefreshLayout.setRefreshing(true);
        CoinGeckoService.getCoinList(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                CoinModel[] result = new Gson().fromJson(response.toString(), CoinModel[].class);

                Parcelable recyclerViewState;
                recyclerViewState = currencyRecyclerView.getLayoutManager().onSaveInstanceState();
                currencyItemFavsList.clear();
                CoinFavoritesModel favs = db.getFavorites();

                for (CoinModel coin : result) {
                    if (favs.favoritesMap.get(coin.getSymbol()) != null) {
                        currencyItemFavsList.add(coin);
                    }
                }
                getQuickSearch();

                swipeRefreshLayout.setRefreshing(false);
                currencyRecyclerView.getLayoutManager().onRestoreInstanceState(recyclerViewState);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {
                Log.e("Favorite currency list", "getCurrencyList() Server Error: " + response.toString());
                swipeRefreshLayout.setRefreshing(false);
            }
        });        
    }

    public void getQuickSearch() {
        CoinGeckoService.getCoinsLookUp(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try{
                    CoinLookUpModel[] result = new Gson().fromJson(response.getJSONArray("data").toString(), CoinLookUpModel[].class);
                    nameToIDMap = new HashMap<>();
                    Parcelable recyclerViewState;
                    recyclerViewState = currencyRecyclerView.getLayoutManager().onSaveInstanceState();
                    for (CoinLookUpModel node : result) {
                        nameToIDMap.put(node.getName(), node.getId());
                    }
                    for (CoinModel coin : currencyItemFavsList) {
                        if (nameToIDMap.get(coin.getId()) != null) {
                            coin.setId(nameToIDMap.get(coin.getId()));
                        }
                    }
                    adapter.setCurrencyList(currencyItemFavsList);
                    int sortType = sharedPreferences.getInt(SORT_SETTING, 1);
                    sortList(adapter.getCurrencyList(), sortType);
                    adapter.notifyDataSetChanged();
                    favsUpdateCallback.performAllCoinsSort();
                    currencyRecyclerView.getLayoutManager().onRestoreInstanceState(recyclerViewState);
                    swipeRefreshLayout.setRefreshing(false);
                }
                catch (JSONException e){
                    Log.e("JSONException", e.getMessage());
                }
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {
                Log.e("Favorite currency list", "getQuickSearch() Server Error: " + response.toString());
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        favsUpdateCallback = (AllCoinsListUpdater) context;
        mContext = (AppCompatActivity) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_favorite_currency_list, container, false);
        setHasOptionsMenu(true);
        this.db = MyDatabaseFactory.getInstance(getActivity());
        sharedPreferences = getContext().getSharedPreferences(CurrencyDetailsActivity.SHAREDPREF_SETTINGS, MODE_PRIVATE);
        currencyRecyclerView = rootView.findViewById(R.id.currency_favs_recycler_view);
        HorizontalDividerItemDecoration divider = new HorizontalDividerItemDecoration.Builder(getActivity()).build();
        currencyRecyclerView.addItemDecoration(divider);
        LinearLayoutManager llm = new LinearLayoutManager(mContext);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        currencyRecyclerView.setLayoutManager(llm);
        adapter = new FavoriteCurrencyListAdapter(favsUpdateCallback, currencyItemFavsList, db, (AppCompatActivity) getActivity(), new ItemClickListener() {
            @Override
            public void onItemClick(int position, View v) {
                Intent intent = new Intent(getActivity(), CurrencyDetailsActivity.class);
                intent.putExtra(CurrencyDetailsActivity.ARG_SYMBOL, currencyItemFavsList.get(position).getSymbol());
                intent.putExtra(CurrencyDetailsActivity.ARG_ID, currencyItemFavsList.get(position).getId());
                intent.putExtra(CurrencyDetailsActivity.COIN_OBJECT, currencyItemFavsList.get(position));
                getActivity().startActivity(intent);
            }
        });
        currencyRecyclerView.setAdapter(adapter);
        swipeRefreshLayout = rootView.findViewById(R.id.currency_favs_swipe_refresh);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(true);
                getCurrencyList();
            }
        });
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (rootView != null) { // Hide keyboard when we enter this tab
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(rootView.getWindowToken(), 0);
        }
        mContext.getSupportActionBar().setTitle(getResources().getString(R.string.app_name));
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, MenuInflater inflater) {
        getActivity().getMenuInflater().inflate(R.menu.favorite_currency_list_tab_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.sort_favs_button:
                int sortType = sharedPreferences.getInt(SORT_SETTING, 1);
                new MaterialDialog.Builder(getActivity())
                        .title(R.string.sort_by)
                        .items(R.array.sort_options)
                        .buttonRippleColor(getContext().getResources().getColor(R.color.colorPrimary))
                        .itemsCallbackSingleChoice(sortType, new MaterialDialog.ListCallbackSingleChoice() {
                            @Override
                            public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                sortList(adapter.getCurrencyList(), which);
                                adapter.notifyDataSetChanged();
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putInt(SORT_SETTING, which);
                                editor.apply();
                                favsUpdateCallback.performAllCoinsSort();
                                Toast toast = Toast.makeText(getContext(), "Sorting by: " + text, Toast.LENGTH_SHORT);
                                toast.show();
                                return true;
                            }
                        })
                        .show();
                return true;
            case R.id.purchased_btn:
                Intent purchasedActivity = new Intent(getActivity(), PurchaseList.class);
                startActivity(purchasedActivity);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onRefresh() {
        getCurrencyList();
    }

    public void removeFavorite(CoinModel coin) {
        ArrayList<CoinModel> currentFavs = adapter.getCurrencyList();
        Iterator<CoinModel> currFavsIterator = currentFavs.iterator();
        while (currFavsIterator.hasNext()) {
            CoinModel currCoin = currFavsIterator.next();
            if (currCoin.getId().equals(coin.getId())) {
                currFavsIterator.remove();
                adapter.notifyDataSetChanged();
                return;
            }
        }
    }

    public void addFavorite(CoinModel coin) {
        currencyItemFavsList.add(0, coin);
        adapter.notifyDataSetChanged();
    }

}
