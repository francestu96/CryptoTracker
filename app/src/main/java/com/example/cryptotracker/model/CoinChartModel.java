package com.example.cryptotracker.model;


import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class CoinChartModel implements Parcelable {

    List<List<Float>> market_caps;
    List<List<Float>> prices;
    List<List<Float>> total_volumes;

    public List<List<Float>> getPriceUSD() {
        return prices;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public CoinChartModel(Parcel in) {

        market_caps = new ArrayList<List<Float>>();
        in.readList(market_caps, ArrayList.class.getClassLoader());

        prices = new ArrayList<List<Float>>();
        in.readList(prices, ArrayList.class.getClassLoader());

        /*price_usd = new ArrayList<List<Float>>();
        in.readList(price_usd, ArrayList.class.getClassLoader());*/

        total_volumes = new ArrayList<List<Float>>();
        in.readList(total_volumes, ArrayList.class.getClassLoader());
    }

    public static final Parcelable.Creator<CoinChartModel> CREATOR = new Parcelable.Creator<CoinChartModel>() {
        @Override
        public CoinChartModel createFromParcel(Parcel source) {
            return new CoinChartModel(source);  //using parcelable constructor
        }
        @Override
        public CoinChartModel[] newArray(int size) {
            return new CoinChartModel[size];
        }
    };

    public void writeToParcel(Parcel dest, int flags) {
    }
}
