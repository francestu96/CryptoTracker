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
import android.widget.SearchView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import static com.example.cryptotracker.utils.SortUtils.sortList;
import com.afollestad.materialdialogs.MaterialDialog;
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
import java.util.Arrays;
import java.util.HashMap;

import cz.msebera.android.httpclient.Header;

import static android.content.Context.MODE_PRIVATE;
import static com.example.cryptotracker.CurrencyListActivity.SORT_SETTING;

public class CurrencyListFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener,
        SearchView.OnQueryTextListener {
    public static final String SHAREDPREF_SETTINGS = "cryptoapp_settings";

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView currencyRecyclerView;
    private CurrencyListAdapter adapter;
    private ArrayList<CoinModel> currencyItemList;
    private ArrayList<CoinModel> filteredList = new ArrayList<>();
    private MenuItem searchItem;
    private SearchView searchView;
    private View rootView;
    private Context mContext;
    public static String currQuery = "";
    ArrayList<CoinModel> searchList;
    private HashMap<String, String> searchedSymbols = new HashMap<>();
    private HashMap<String, String> nameToIDMap = new HashMap<>();
    public static boolean searchViewFocused = false;
    private FavoritesListUpdater favsUpdateCallback;
    private SharedPreferences sharedPreferences;

    public static final String ARG_SYMBOL = "symbol";
    public static final String ARG_ID = "ID";
    public static final String COIN_OBJECT = "COIN_OBJECT";

    public interface FavoritesListUpdater {
        void removeFavorite(CoinModel coin);
        void addFavorite(CoinModel coin);
        void performFavsSort();
    }

    public CurrencyListFragment() {
    }

    public void performAllCoinsSort() {
        int sortType = sharedPreferences.getInt(SORT_SETTING, 1);
        sortList(adapter.getCurrencyList(), sortType);
        adapter.notifyDataSetChanged();
    }

    public void getCoinsLookUp() {
        CoinGeckoService.getCoinsLookUp(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                CoinLookUpModel[] result = new Gson().fromJson(response.toString(), CoinLookUpModel[].class);

                nameToIDMap = new HashMap();
                Parcelable recyclerViewState;
                recyclerViewState = currencyRecyclerView.getLayoutManager().onSaveInstanceState();
                for (CoinLookUpModel node : result) {
                    nameToIDMap.put(node.getName(), node.getId());
                }
                if (searchViewFocused) {
                    for (CoinModel coin: searchList) {
                        if (nameToIDMap.get(coin.getId()) != null) {
                            coin.setId(nameToIDMap.get(coin.getId()));
                        }
                    }
                    adapter.setCurrencyList(searchList);
                } else {
                    for (CoinModel coin : currencyItemList) {
                        if (coin.getId() != null && nameToIDMap.get(coin.getId()) != null) {
                            coin.setId(nameToIDMap.get(coin.getId()));
                        }
                    }
                    adapter.setCurrencyList(currencyItemList);
                }
                int sortType = sharedPreferences.getInt(SORT_SETTING, 1);
                sortList(adapter.getCurrencyList(), sortType);
                adapter.notifyDataSetChanged();
                favsUpdateCallback.performFavsSort();
                currencyRecyclerView.getLayoutManager().onRestoreInstanceState(recyclerViewState);
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {
                Log.e("Currency List", "getCoinsLookUp() Server Error: " + response.toString());
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    @Override
    public void onRefresh() {
        getCurrencyList();
    }

    public static CurrencyListFragment newInstance() {
        return new CurrencyListFragment();
    }

    public void getCurrencyList() {
        swipeRefreshLayout.setRefreshing(true);
        CoinGeckoService.getCoinList(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                CoinModel[] result = new Gson().fromJson(response.toString(), CoinModel[].class);
                if (searchViewFocused) {
                    searchedSymbols.clear();
                    searchList.clear();
                    for (CoinModel coin : filteredList) {
                        searchedSymbols.put(coin.getSymbol(), coin.getSymbol());
                    }
                    for (CoinModel coin : result) {
                        if (searchedSymbols.get(coin.getSymbol()) != null) {
                            searchList.add(coin);
                        }
                    }
                } else {
                    currencyItemList.clear();
                    currencyItemList.addAll(Arrays.asList(result));
                }

                getCoinsLookUp();
            }
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {
                Log.e("Currency List", "getCurrencyList() Server Error: " + response.toString());
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().invalidateOptionsMenu();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_all_currency_list, container, false);
        setHasOptionsMenu(true);
        MyDatabaseFactory db = MyDatabaseFactory.getInstance(mContext);
        sharedPreferences = getContext().getSharedPreferences(SHAREDPREF_SETTINGS, MODE_PRIVATE);
        searchList = new ArrayList<>();
        currencyRecyclerView = rootView.findViewById(R.id.currency_list_recycler_view);
        HorizontalDividerItemDecoration divider = new HorizontalDividerItemDecoration.Builder(mContext).build();
        currencyRecyclerView.addItemDecoration(divider);
        LinearLayoutManager llm = new LinearLayoutManager(mContext);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        currencyRecyclerView.setLayoutManager(llm);
        currencyItemList = new ArrayList<>();

       adapter = new CurrencyListAdapter(favsUpdateCallback, currencyItemList, db, (AppCompatActivity) mContext, new ItemClickListener() {
            @Override
            public void onItemClick(int position, View v) {
                Intent intent = new Intent(mContext, CurrencyDetailsActivity.class);
                intent.putExtra(ARG_SYMBOL, adapter.getCurrencyList().get(position).getSymbol());
                intent.putExtra(ARG_ID, adapter.getCurrencyList().get(position).getId());
                intent.putExtra(COIN_OBJECT, adapter.getCurrencyList().get(position));
                mContext.startActivity(intent);
            }
        });
        currencyRecyclerView.setAdapter(adapter);
        swipeRefreshLayout = rootView.findViewById(R.id.currency_list_swipe_refresh);
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.sort_button:
                int sortType = sharedPreferences.getInt(SORT_SETTING, 1);
                new MaterialDialog.Builder(getActivity())
                        .title(R.string.sort_by)
                        .items(R.array.sort_options)
                        .dividerColorRes(R.color.colorPrimary)
                        .widgetColorRes(R.color.colorPrimary)
                        .buttonRippleColorRes(R.color.colorPrimary)
                        .itemsCallbackSingleChoice(sortType, new MaterialDialog.ListCallbackSingleChoice() {
                            @Override
                            public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                sortList(adapter.getCurrencyList(), which);
                                adapter.notifyDataSetChanged();
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putInt(SORT_SETTING, which);
                                editor.apply();
                                favsUpdateCallback.performFavsSort();
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
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String query) {
        currQuery = query;
        query = query.toLowerCase();
        filteredList.clear();
        for (CoinModel coin : currencyItemList) {
            if (coin.getSymbol().toLowerCase().contains(query) || coin.getName().toLowerCase().contains(query)) {
                filteredList.add(coin);
            }
        }
        adapter.setCurrencyList(filteredList);
        return true;
    }

    private void showInputMethod(View view) {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(view, 0);
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        if (searchView != null && searchViewFocused) {
            ((AppCompatActivity)mContext).getSupportActionBar().setTitle("");
            searchView.requestFocusFromTouch();
            searchView.setIconified(false);
            searchView.setIconified(false);
            searchView.setQuery(currQuery, false);
            showInputMethod(rootView);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mContext = context;
        this.favsUpdateCallback = (FavoritesListUpdater) context;
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, MenuInflater inflater) {
        getActivity().getMenuInflater().inflate(R.menu.all_currency_list_tab_menu, menu);
        searchItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(this);
        // Detect SearchView icon clicks
        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchViewFocused = true;
                setItemsVisibility(menu, searchItem, false);
            }
        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                searchViewFocused = false;
                setItemsVisibility(menu, searchItem, true);
                return false;
            }
        });
        if (searchViewFocused) ((AppCompatActivity)mContext).getSupportActionBar().setTitle("");
        super.onCreateOptionsMenu(menu, inflater);
    }

    private void setItemsVisibility(Menu menu, MenuItem exception, boolean visible) {
        for (int i = 0; i < menu.size(); ++i) {
            MenuItem item = menu.getItem(i);
            if (item != exception) item.setVisible(visible);
        }
        if (!visible) {
            ((AppCompatActivity)mContext).getSupportActionBar().setTitle("");
        } else {
            ((AppCompatActivity)mContext).getSupportActionBar().setTitle(getResources().getString(R.string.app_name));
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        searchViewFocused = false;
    }

    public CurrencyListAdapter getAdapter() {
        return this.adapter;
    }
}
