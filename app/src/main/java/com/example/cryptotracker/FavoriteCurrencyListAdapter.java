package com.example.cryptotracker;

import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cryptotracker.model.CoinFavoritesModel;
import com.example.cryptotracker.model.CoinModel;
import com.example.cryptotracker.utils.CurrencyListAdapterUtils;
import com.example.cryptotracker.utils.ItemClickListener;
import com.example.cryptotracker.utils.MyDatabaseFactory;
import com.squareup.picasso.Picasso;

import java.lang.ref.WeakReference;
import java.util.ArrayList;


public class FavoriteCurrencyListAdapter extends RecyclerView.Adapter<FavoriteCurrencyListAdapter.ViewHolder> {
    private ArrayList<CoinModel> currencyList;
    private FavoriteCurrencyListAdapter.ViewHolder viewHolder;
    private String pctChangeNotAvailableStringResource;
    private String negativePercentStringResource;
    private String positivePercentStringResource;
    private String priceStringResource;
    private String mktCapStringResource;
    private String volumeStringResource;
    private String symbolAndFullNameStringResource;
    private int positiveGreenColor;
    private int negativeRedColor;
    private ItemClickListener rowListener;
    private WeakReference<AppCompatActivity> contextRef;
    private WeakReference<MyDatabaseFactory> dbRef;
    private WeakReference<FavoriteCurrencyListFragment.AllCoinsListUpdater> favsUpdateCallbackRef;
    private FavoriteCurrencyListAdapter me;

    public FavoriteCurrencyListAdapter(FavoriteCurrencyListFragment.AllCoinsListUpdater favsUpdateCallback, ArrayList<CoinModel> currencyList,
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
        this.me = this;
    }

    public void setFavoriteButtonClickListener(final FavoriteCurrencyListAdapter.ViewHolder holder, final int position) {
        holder.trashButton.setOnClickListener(new View.OnClickListener() {
            CoinModel item = currencyList.get(position);
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(contextRef.get())
                        .setMessage("Unfavorite " + item.getSymbol() + "?")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                CoinFavoritesModel favs = dbRef.get().getFavorites();
                                favs.favoritesMap.remove(item.getSymbol());
                                favs.favoriteList.remove(item.getSymbol());
                                dbRef.get().saveCoinFavorites(favs);
                                currencyList.remove(position);
                                notifyDataSetChanged();
                                favsUpdateCallbackRef.get().allCoinsModifyFavorites(item);
                            }})
                        .setNegativeButton(android.R.string.no, null).show();
            }
        });
    }

    @Override
    public void onBindViewHolder(final FavoriteCurrencyListAdapter.ViewHolder holder, final int position) {
        CoinModel item = currencyList.get(position);
        CurrencyListAdapterUtils.setPercentChangeTextView(holder.dayChangeTextView, item.getPercent_change_24h(),
                CurrencyListActivity.DAY, negativePercentStringResource, positivePercentStringResource, negativeRedColor, positiveGreenColor, pctChangeNotAvailableStringResource);
        if (item.getMarket_cap_usd() == null) {
            holder.currencyListMarketcapTextView.setText("N/A");
        } else {
            holder.currencyListMarketcapTextView.setText(String.format(mktCapStringResource, Double.parseDouble(item.getMarket_cap_usd())));
        }
        if (item.getRank() == null) {
            holder.rankTextViewFavs.setText("N/A");
        } else {
            holder.rankTextViewFavs.setText(item.getRank());
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
        setFavoriteButtonClickListener(holder, position);
    }

    @Override
    public FavoriteCurrencyListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemLayoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_favs_currency_list, parent, false);
        viewHolder = new FavoriteCurrencyListAdapter.ViewHolder(itemLayoutView, rowListener);
        return viewHolder;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView currencyListfullNameTextView;
        private TextView oneHourChangeTextView;
        private TextView dayChangeTextView;
        private TextView weekChangeTextView;
        private TextView currencyListCurrPriceTextView;
        private TextView currencyListVolumeTextView;
        private TextView rankTextViewFavs;
        private TextView currencyListMarketcapTextView;
        private ImageView currencyListCoinImageView;
        protected ImageView trashButton;
        private ItemClickListener listener;

        private ViewHolder(View itemLayoutView, ItemClickListener listener)
        {
            super(itemLayoutView);
            itemLayoutView.setOnClickListener(this);
            rankTextViewFavs = itemLayoutView.findViewById(R.id.rankTextViewFavs);
            currencyListfullNameTextView = itemLayoutView.findViewById(R.id.currencyListfullNameTextView);
            currencyListCurrPriceTextView = itemLayoutView.findViewById(R.id.currencyListCurrPriceTextView);
            currencyListCoinImageView = itemLayoutView.findViewById(R.id.currencyListCoinImageView);
            currencyListVolumeTextView = itemLayoutView.findViewById(R.id.currencyListVolumeTextView);
            currencyListMarketcapTextView = itemLayoutView.findViewById(R.id.currencyListMarketcapTextView);
            trashButton = itemLayoutView.findViewById(R.id.favsCurrencyListTrashImage);
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
    }

    public ArrayList<CoinModel> getCurrencyList() {
        return currencyList;
    }
}
