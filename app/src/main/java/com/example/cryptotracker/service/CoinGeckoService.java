package com.example.cryptotracker.service;

import android.util.Log;

import com.example.cryptotracker.CurrencyDetailsActivity;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.HttpHeaders;
import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.client.utils.URIBuilder;
import cz.msebera.android.httpclient.message.BasicNameValuePair;

public class CoinGeckoService {
    public static final String COINGECKO_COINS_URL = "https://api.coingecko.com/api/v3/coins/markets";
    public static final String COINGECKO_CHART_URL = "https://api.coingecko.com/api/v3/coins/%s/market_chart/range";

    private static AsyncHttpClient client = new AsyncHttpClient();

    public static void getCoinList(AsyncHttpResponseHandler responseHandler) {
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("vs_currency","USD"));
        params.add(new BasicNameValuePair("per_page","100"));

        try{
            URIBuilder query = new URIBuilder(COINGECKO_COINS_URL);
            query.addParameters(params);
            client.get(query.toString(), responseHandler);
        }
        catch (URISyntaxException e){
            Log.e("URISyntaxException", e.getMessage());
        }
    }

    public static void getCoinsChartData(String cryptoId, long startTime, long endTime, AsyncHttpResponseHandler responseHandler) {
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("vs_currency", "USD"));
        params.add(new BasicNameValuePair("from", String.valueOf(startTime)));
        params.add(new BasicNameValuePair("to", String.valueOf(endTime)));
        try{
            URIBuilder query = new URIBuilder(String.format(COINGECKO_CHART_URL, cryptoId));
            query.addParameters(params);

            client.get(query.toString(), responseHandler);
        }
        catch (URISyntaxException e){
            Log.e("URISyntaxException", e.getMessage());
        }
    }

    public static void getCoinsLookUp(AsyncHttpResponseHandler responseHandler) {
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("vs_currency","USD"));
        params.add(new BasicNameValuePair("per_page","100"));

        try{
            URIBuilder query = new URIBuilder(COINGECKO_COINS_URL);
            query.addParameters(params);
            client.get(query.toString(), responseHandler);
        }
        catch (URISyntaxException e){
            Log.e("URISyntaxException", e.getMessage());
        }
    }
}
