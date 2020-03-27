package com.example.cryptotracker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cryptotracker.model.CoinFavoritesModel;
import com.example.cryptotracker.model.CoinModel;
import com.example.cryptotracker.utils.CurrencyListAdapterUtils;
import com.example.cryptotracker.utils.ItemClickListener;
import com.example.cryptotracker.utils.MyDatabaseFactory;
import com.github.ivbaranov.mfb.MaterialFavoriteButton;
import com.squareup.picasso.Picasso;

import java.lang.ref.WeakReference;
import java.util.ArrayList;


public class CurrencyListAdapter extends RecyclerView.Adapter<CurrencyListAdapter.ViewHolder> {

    private ArrayList<CoinModel> currencyList;
    private CurrencyListAdapter.ViewHolder viewHolder;
    private String priceStringResource;
    private String mktCapStringResource;
    private String volumeStringResource;
    private String pctChangeNotAvailableStringResource;
    private String negativePercentStringResource;
    private String positivePercentStringResource;
    private String symbolAndFullNameStringResource;
    private int positiveGreenColor;
    private int negativeRedColor;
    private ItemClickListener rowListener;
    private WeakReference<AppCompatActivity> contextRef;
    private WeakReference<MyDatabaseFactory> dbRef;
    private WeakReference<CurrencyListFragment.FavoritesListUpdater> favsUpdateCallbackRef;

    public CurrencyListAdapter(CurrencyListFragment.FavoritesListUpdater favsUpdateCallback, ArrayList<CoinModel> currencyList,
                               MyDatabaseFactory db, AppCompatActivity context, ItemClickListener listener) {
        this.currencyList = currencyList;
        this.contextRef = new WeakReference<>(context);
        this.rowListener = listener;
        this.dbRef = new WeakReference<>(db);
        this.mktCapStringResource = this.contextRef.get().getString(R.string.mkt_cap_format);
        this.volumeStringResource = this.contextRef.get().getString(R.string.volume_format);
        this.negativePercentStringResource = this.contextRef.get().getString(R.string.negative_pct_change_format);
        this.positivePercentStringResource = this.contextRef.get().getString(R.string.positive_pct_change_format);
        this.priceStringResource = this.contextRef.get().getString(R.string.unrounded_price_format);
        this.pctChangeNotAvailableStringResource = this.contextRef.get().getString(R.string.not_available_pct_change_text_with_time);
        this.symbolAndFullNameStringResource = this.contextRef.get().getString(R.string.nameAndSymbol);
        this.negativeRedColor = this.contextRef.get().getResources().getColor(R.color.percentNegativeRed);
        this.positiveGreenColor = this.contextRef.get().getResources().getColor(R.color.percentPositiveGreen);
        this.favsUpdateCallbackRef = new WeakReference<>(favsUpdateCallback);
    }

    public void setFavoriteButtonClickListener(final CurrencyListAdapter.ViewHolder holder, final int position) {
        holder.favButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CoinFavoritesModel favs = dbRef.get().getFavorites();
                CoinModel item = currencyList.get(position);
                if (favs.favoritesMap.get(item.getSymbol()) == null) { // Coin is not a favorite yet. Add it.
                    favs.favoritesMap.put(item.getSymbol(), item.getSymbol());
                    favs.favoriteList.add(item.getSymbol());
                    holder.favButton.setFavorite(true, true);
                    favsUpdateCallbackRef.get().addFavorite(item);
                } else { // Coin is already a favorite, remove it
                    favs.favoritesMap.remove(item.getSymbol());
                    favs.favoriteList.remove(item.getSymbol());
                    holder.favButton.setFavorite(false, true);
                    favsUpdateCallbackRef.get().removeFavorite(item);
                }
                dbRef.get().saveCoinFavorites(favs);
            }
        });
    }

    @Override
    public void onBindViewHolder(final CurrencyListAdapter.ViewHolder holder, final int position) {
        CoinModel item = currencyList.get(position);
        CurrencyListAdapterUtils.setPercentChangeTextView(holder.dayChangeTextView, item.getPercent_change_24h(),
                CurrencyListActivity.DAY, negativePercentStringResource, positivePercentStringResource, negativeRedColor, positiveGreenColor, pctChangeNotAvailableStringResource);
        if (item.getMarket_cap_usd() == null) {
            holder.currencyListMarketcapTextView.setText("N/A");
        } else {
            holder.currencyListMarketcapTextView.setText(String.format(mktCapStringResource, Double.parseDouble(item.getMarket_cap_usd())));
        }
        if (item.getRank() == null) {
            holder.rankTextView.setText("N/A");
        } else {
            holder.rankTextView.setText(item.getRank());
        }
        if (item.getPrice_usd() == null) {
            holder.currencyListCurrPriceTextView.setText("N/A");
        } else {
            holder.currencyListCurrPriceTextView.setText(String.format(priceStringResource, item.getPrice_usd()));
        }
        holder.currencyListfullNameTextView.setText(String.format(this.symbolAndFullNameStringResource, item.getName(), item.getSymbol()));
        if (item.getImageUrl() != "") {
            Picasso.get().load(item.getImageUrl()).into(holder.currencyListCoinImageView);
        }
        CoinFavoritesModel favs = this.dbRef.get().getFavorites();
        boolean isFav = favs.favoritesMap.get(item.getSymbol()) != null;
        holder.favButton.setFavorite(isFav, false);
        setFavoriteButtonClickListener(holder, position);
    }

    @Override
    public CurrencyListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemLayoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_currency_list_item, parent, false);
        viewHolder = new CurrencyListAdapter.ViewHolder(itemLayoutView, rowListener);
        return viewHolder;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView currencyListfullNameTextView;
        private TextView oneHourChangeTextView;
        private TextView dayChangeTextView;
        private TextView weekChangeTextView;
        private TextView rankTextView;
        private TextView currencyListCurrPriceTextView;
        private TextView currencyListVolumeTextView;
        private TextView currencyListMarketcapTextView;
        private ImageView currencyListCoinImageView;
        private MaterialFavoriteButton favButton;
        private ItemClickListener listener;

        private ViewHolder(View itemLayoutView, ItemClickListener listener)
        {
            super(itemLayoutView);
            itemLayoutView.setOnClickListener(this);
            rankTextView = itemLayoutView.findViewById(R.id.rankTextView);
            currencyListfullNameTextView = itemLayoutView.findViewById(R.id.currencyListfullNameTextView);
            currencyListCurrPriceTextView = itemLayoutView.findViewById(R.id.currencyListCurrPriceTextView);
            currencyListCoinImageView = itemLayoutView.findViewById(R.id.currencyListCoinImageView);
            currencyListVolumeTextView = itemLayoutView.findViewById(R.id.currencyListVolumeTextView);
            currencyListMarketcapTextView = itemLayoutView.findViewById(R.id.currencyListMarketcapTextView);
            favButton = itemLayoutView.findViewById(R.id.currencyListFavButton);
            oneHourChangeTextView = itemLayoutView.findViewById(R.id.oneHourChangeTextView);
            dayChangeTextView = itemLayoutView.findViewById(R.id.dayChangeTextView);
            weekChangeTextView = itemLayoutView.findViewById(R.id.weekChangeTextView);
            this.listener = listener;
        }

        @Override
        public void onClick(View v) {
            listener.onItemClick(getAdapterPosition(), v);
        }
    }

    public int getItemCount() {
        return currencyList.size();
    }

    public void setCurrencyList(ArrayList<CoinModel> newCurrencyList) {
        this.currencyList = newCurrencyList;
        notifyDataSetChanged();
    }

    public ArrayList<CoinModel> getCurrencyList() {
        return currencyList;
    }

}
