package com.example.cryptotracker.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;
import java.util.Map;


public class CoinModel implements Parcelable {
    private String id;
    private String symbol;
    private String image;
    private String name;
    private String current_price;
    private String market_cap;
    private String market_cap_rank;
    private String total_volume;
    private String available_supply;
    private String total_supply;
    private String circulating_supply;
    private String price_change_percentage_24h;
    private String last_updated;

    public CoinModel(Parcel in){
        String[] data = new String[13];

        in.readStringArray(data);
        this.id = data[0];
        this.symbol = data[1];
        this.image = data[2];
        this.name = data[3];
        this.current_price = data[4];
        this.market_cap = data[5];
        this.market_cap_rank = data[6];
        this.total_volume = data[7];
        this.available_supply = data[8];
        this.total_supply = data[9];
        this.circulating_supply = data[10];
        this.price_change_percentage_24h = data[11];
        this.last_updated = data[12];
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<CoinModel> CREATOR = new Parcelable.Creator<CoinModel>() {
        @Override
        public CoinModel createFromParcel(Parcel source) {
            return new CoinModel(source);
        }
        @Override
        public CoinModel[] newArray(int size) {
            return new CoinModel[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[]{
                this.id,
                this.symbol,
                this.image,
                this.name,
                this.current_price,
                this.market_cap,
                this.market_cap_rank,
                this.total_volume,
                this.available_supply,
                this.total_supply,
                this.circulating_supply,
                this.price_change_percentage_24h,
                this.last_updated
        });
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getImageUrl() {
        return image;
    }

    public String getRank() {
        return market_cap_rank;
    }

    public String getPrice_usd() {
        return current_price;
    }

    public String getPrice_btc() {
        return "1";
    }

    public String getMarket_cap_usd() {
        return market_cap;
    }

    public String getAvailable_supply() {
        return available_supply;
    }

    public String getMax_supply() {
        return total_supply;
    }

    public String getPercent_change_24h() {
        return price_change_percentage_24h;
    }


    public String getLast_updated() {
        return last_updated;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }
}
