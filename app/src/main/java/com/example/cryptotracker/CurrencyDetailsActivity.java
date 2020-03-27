package com.example.cryptotracker;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;

import com.example.cryptotracker.formatters.CurrencyFormatter;
import com.example.cryptotracker.formatters.MonthSlashDayDateFormatter;
import com.example.cryptotracker.formatters.MonthSlashYearFormatter;
import com.example.cryptotracker.formatters.TimeDateFormatter;
import com.example.cryptotracker.model.CoinChartModel;
import com.example.cryptotracker.model.CoinModel;
import com.example.cryptotracker.service.CoinGeckoService;
import com.example.cryptotracker.utils.LockableNestedScrollView;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.gson.Gson;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.nex3z.togglebuttongroup.SingleSelectToggleGroup;

import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import cz.msebera.android.httpclient.Header;

public class CurrencyDetailsActivity extends AppCompatActivity  implements OnChartValueSelectedListener {
    public CustomViewPager viewPager;

    private Toolbar toolbar;
    private SimpleDateFormat fullDateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z", Locale.ENGLISH);
    private CurrencyFormatter currencyFormatter;
    private String cryptoID;
    private String symbol;
    private int chartFillColor;
    private int chartBorderColor;
    private int percentageColor;
    private LineChart lineChart;
    private IAxisValueFormatter XAxisFormatter;
    public final IAxisValueFormatter monthSlashDayXAxisFormatter = new MonthSlashDayDateFormatter();
    public final TimeDateFormatter dayCommaTimeDateFormatter = new TimeDateFormatter();
    public final MonthSlashYearFormatter monthSlashYearFormatter = new MonthSlashYearFormatter();
    private String currentTimeWindow = "";
    private SingleSelectToggleGroup buttonGroup;
    public final static DecimalFormat rawNumberFormat = new DecimalFormat("#,###.##");
    private LockableNestedScrollView nestedScrollView;
    private int displayWidth;
    private ProgressBar chartProgressBar;
    private String tsymbol;
    private SharedPreferences sharedPreferences;
    private long startTime;
    private long endTime;
    NumberFormat chartUSDPriceFormat = NumberFormat.getInstance();


    public static final String SHAREDPREF_SETTINGS = "cryptotracker_settings";
    public static final String CHART_SPINNER_SETTING = "chart_spinner_setting";
    public static final String ARG_SYMBOL = "symbol";
    public static final String ARG_ID = "ID";
    public static final String COIN_OBJECT = "COIN_OBJECT";


    public void setColors(float percentChange) {
        if (percentChange >= 0) {
            chartFillColor = ResourcesCompat.getColor(getResources(), R.color.materialLightGreen, null);
            chartBorderColor = ResourcesCompat.getColor(getResources(), R.color.darkGreen, null);
            percentageColor = ResourcesCompat.getColor(getResources(), R.color.percentPositiveGreen, null);
        }
        else {
            chartFillColor = ResourcesCompat.getColor(getResources(), R.color.materialLightRed, null);
            chartBorderColor = ResourcesCompat.getColor(getResources(), R.color.darkRed, null);
            percentageColor = ResourcesCompat.getColor(getResources(), R.color.percentNegativeRed, null);
        }
    }

    public void setUpChart() {
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setDrawAxisLine(true);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM_INSIDE);
        xAxis.setAvoidFirstLastClipping(true);
        lineChart.getAxisLeft().setEnabled(true);
        lineChart.getAxisLeft().setDrawGridLines(false);
        lineChart.getXAxis().setDrawGridLines(false);
        lineChart.getAxisRight().setEnabled(false);
        lineChart.getLegend().setEnabled(false);
        lineChart.setDoubleTapToZoomEnabled(false);
        lineChart.setScaleEnabled(false);
        lineChart.getDescription().setEnabled(false);
        lineChart.setContentDescription("");
        lineChart.setNoDataText(getString(R.string.noChartDataString));
        lineChart.setNoDataTextColor(R.color.darkRed);
        lineChart.setOnChartValueSelectedListener(this);
        lineChart.setOnChartGestureListener(new OnChartGestureListener() {
            @Override
            public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
                YAxis yAxis = lineChart.getAxisLeft();
                // Allow scrolling in the right and left margins
                if (me.getX() > yAxis.getLongestLabel().length() * yAxis.getTextSize() &&
                        me.getX() < displayWidth - lineChart.getViewPortHandler().offsetRight()) {
                    if(viewPager != null){
                        viewPager.setPagingEnabled(false);
                    }
                    nestedScrollView.setScrollingEnabled(false);
                }
            }

            @Override
            public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
                if(viewPager != null){
                    viewPager.setPagingEnabled(true);
                }
                nestedScrollView.setScrollingEnabled(true);
            }

            @Override
            public void onChartLongPressed(MotionEvent me) {

            }

            @Override
            public void onChartDoubleTapped(MotionEvent me) {

            }

            @Override
            public void onChartSingleTapped(MotionEvent me) {

            }

            @Override
            public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {

            }

            @Override
            public void onChartScale(MotionEvent me, float scaleX, float scaleY) {

            }

            @Override
            public void onChartTranslate(MotionEvent me, float dX, float dY) {

            }
        });
    }

    public LineDataSet setUpLineDataSet(List<Entry> entries) {
        LineDataSet dataSet = new LineDataSet(entries, "Price");
        dataSet.setColor(chartBorderColor);
        dataSet.setFillColor(chartFillColor);
        dataSet.setDrawHighlightIndicators(true);
        dataSet.setDrawFilled(true);
        dataSet.setDrawCircles(true);
        dataSet.setCircleColor(chartBorderColor);
        dataSet.setDrawCircleHole(false);
        dataSet.setDrawValues(false);
        dataSet.setCircleRadius(1);
        dataSet.setHighlightLineWidth(2);
        dataSet.setHighlightEnabled(true);
        dataSet.setDrawHighlightIndicators(true);
        dataSet.setHighLightColor(chartBorderColor); // color for highlight indicator
        return dataSet;
    }

    public void getCMCChart() {
        final TextView percentChangeText = findViewById(R.id.percent_change);
        final TextView currPriceText = findViewById(R.id.current_price);
        lineChart.setEnabled(true);
        lineChart.clear();
        chartProgressBar.setVisibility(View.VISIBLE);
        CoinGeckoService.getCoinsChartData(this.cryptoID, this.startTime, this.endTime, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject success)
            {
                CoinChartModel result = new Gson().fromJson(success.toString(), CoinChartModel.class);
                List<Entry> closePrices = new ArrayList<>();

                for (List<Float> priceTimeUnit : result.getPriceUSD()) {
                    closePrices.add(new Entry(priceTimeUnit.get(0), priceTimeUnit.get(1)));
                }

                if (closePrices.size() == 0) {
                    lineChart.setData(null);
                    lineChart.setEnabled(false);
                    lineChart.invalidate();
                    percentChangeText.setText("");
                    currPriceText.setText("");
                    lineChart.setNoDataText(getString(R.string.noChartDataString));
                    chartProgressBar.setVisibility(View.GONE);
                    return;
                }

                XAxis xAxis = lineChart.getXAxis();
                xAxis.setValueFormatter(XAxisFormatter);
                TextView currentPriceTextView = findViewById(R.id.current_price);
                float currPrice = closePrices.get(closePrices.size() - 1).getY();
                TextView chartDateTextView = findViewById(R.id.graphFragmentDateTextView);
                chartDateTextView.setText(getFormattedFullDate(closePrices.get(closePrices.size() - 1).getX()));
                if (tsymbol.equals("USD")) {
                    currentPriceTextView.setText(String.format(getString(R.string.unrounded_usd_chart_price_format), String.valueOf(currPrice)));
                } else {
                    currentPriceTextView.setText(currencyFormatter.format(currPrice, "BTC"));
                }
                currentPriceTextView.setTextColor(Color.BLACK);
                float firstPrice = closePrices.get(0).getY();
                for (Entry e: closePrices) {
                    if (firstPrice != 0) {
                        break;
                    } else {
                        firstPrice = e.getY();
                    }
                }

                float difference = (currPrice - firstPrice);
                float percentChange = (difference / firstPrice) * 100;
                if (percentChange < 0) {
                    if (tsymbol.equals("USD")) {
                        percentChangeText.setText(String.format(getString(R.string.negative_variable_pct_change_with_dollars_format), currentTimeWindow, percentChange, Math.abs(difference)));
                    } else {
                        percentChangeText.setText(String.format(getString(R.string.negative_variable_pct_change_without_dollars_format), currentTimeWindow, percentChange));
                    }
                } else {
                    if (tsymbol.equals("USD")) {
                        percentChangeText.setText(String.format(getString(R.string.positive_variable_pct_change_with_dollars_format), currentTimeWindow, percentChange, Math.abs(difference)));
                    } else {
                        percentChangeText.setText(String.format(getString(R.string.positive_variable_pct_change_without_dollars_format), currentTimeWindow, percentChange));
                    }
                }
                setColors(percentChange);
                percentChangeText.setTextColor(percentageColor);
                LineDataSet dataSet = setUpLineDataSet(closePrices);
                LineData lineData = new LineData(dataSet);
                lineChart.setData(lineData);
                lineChart.animateX(800);
                chartProgressBar.setVisibility(View.GONE);
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {
                Log.e("Currency Details", "getCoinsChartData() Server Error: " + response.toString());
            }
        });
    }

    public void setDayChecked(Calendar cal) {
        long endTime = cal.getTimeInMillis() / 1000;
        cal.add(Calendar.DAY_OF_YEAR, -1);
        long startTime = cal.getTimeInMillis() / 1000;
        cal.clear();
        this.startTime =  startTime;
        this.endTime = endTime;
        //CURRENT_CHART_URL = String.format(COIN_MARKETCAP_CHART_URL_WINDOW, cryptoID, startTime, endTime);
        currentTimeWindow = getString(R.string.oneDay);
        XAxisFormatter = dayCommaTimeDateFormatter;
    }

    public void setWeekChecked(Calendar cal) {
        long endTime = cal.getTimeInMillis() / 1000;
        cal.add(Calendar.DAY_OF_YEAR, -7);
        long startTime = cal.getTimeInMillis() / 1000;
        cal.clear();
        this.startTime =  startTime;
        this.endTime = endTime;
        currentTimeWindow = getString(R.string.Week);
        XAxisFormatter = monthSlashDayXAxisFormatter;
    }

    public void setMonthChecked(Calendar cal) {
        long endTime = cal.getTimeInMillis() / 1000;
        cal.add(Calendar.MONTH, -1);
        long startTime = cal.getTimeInMillis() / 1000;
        cal.clear();
        this.startTime =  startTime;
        this.endTime = endTime;
        currentTimeWindow = getString(R.string.Month);
        XAxisFormatter = monthSlashDayXAxisFormatter;
    }

    public void setThreeMonthChecked(Calendar cal) {
        long endTime = cal.getTimeInMillis() / 1000;
        cal.add(Calendar.MONTH, -3);
        long startTime = cal.getTimeInMillis() / 1000;
        cal.clear();
        this.startTime =  startTime;
        this.endTime = endTime;
        currentTimeWindow = getString(R.string.threeMonth);
        XAxisFormatter = monthSlashDayXAxisFormatter;
    }

    public void setYearChecked(Calendar cal) {
        long endTime = cal.getTimeInMillis() / 1000;
        cal.add(Calendar.YEAR, -1);
        long startTime = cal.getTimeInMillis() / 1000;
        cal.clear();
        this.startTime =  startTime;
        this.endTime = endTime;
        currentTimeWindow = getString(R.string.Year);
        XAxisFormatter = monthSlashYearFormatter;
    }

    public void setAllTimeChecked() {
        currentTimeWindow = getString(R.string.AllTime);
        this.startTime =  0;
        this.endTime = 0;
        XAxisFormatter = monthSlashYearFormatter;
    }

    public void setTable(CoinModel coinObject) {
        String usdFormat = getString(R.string.usd_format);
        String negativePctFormat = getString(R.string.negative_pct_format);
        String positivePctFormat = getString(R.string.positive_pct_format);
        int negativeRedColor = getResources().getColor(R.color.percentNegativeRed);
        int positiveGreenColor = getResources().getColor(R.color.percentPositiveGreen);
        TextView nameTextView = findViewById(R.id.tableNameDataTextView);
        if (coinObject.getName() == null) {
            nameTextView.setText("N/A");
        } else {
            nameTextView.setText(coinObject.getName());
        }

        TextView priceUSDTextView = findViewById(R.id.tablePriceUSDDataTextView);
        if (coinObject.getPrice_usd() == null) {
            priceUSDTextView.setText("N/A");
        } else {
            priceUSDTextView.setText(String.format(usdFormat, Double.parseDouble(coinObject.getPrice_usd())));
        }

        TextView priceBTCTextView = findViewById(R.id.tablePriceBTCDataTextView);
        if (coinObject.getPrice_btc() == null) {
            priceBTCTextView.setText("N/A");
        } else {
            priceBTCTextView.setText(String.format(getString(R.string.btc_format), coinObject.getPrice_btc()));
        }

        TextView mktCapTextView = findViewById(R.id.tableMktCapDataTextView);
        if (coinObject.getMarket_cap_usd() == null) {
            mktCapTextView.setText("N/A");
        } else {
            mktCapTextView.setText(String.format(usdFormat, Double.parseDouble(coinObject.getMarket_cap_usd())));
        }

        TextView availSupplyTextView = findViewById(R.id.tableAvailableSupplyDataTextView);
        if (coinObject.getAvailable_supply() == null) {
            availSupplyTextView.setText("N/A");
        } else {
            availSupplyTextView.setText(rawNumberFormat.format(Double.parseDouble(coinObject.getAvailable_supply())));
        }

        TextView totalSupplyTextView = findViewById(R.id.tableTotalSupplyDataTextView);
        if (coinObject.getMax_supply() == null) {
            totalSupplyTextView.setText("N/A");
        } else {
            totalSupplyTextView.setText(rawNumberFormat.format(Double.parseDouble(coinObject.getMax_supply())));
        }

        TextView maxSupplyTextView = findViewById(R.id.tableMaxSupplyDataTextView);
        if (coinObject.getMax_supply() == null) {
            maxSupplyTextView.setText("N/A");
        } else {
            maxSupplyTextView.setText(rawNumberFormat.format(Double.parseDouble(coinObject.getMax_supply())));
        }

        TextView dayChangeTextView = findViewById(R.id.table24hrChangeDataTextView);
        if (coinObject.getPercent_change_24h() == null) {
            dayChangeTextView.setText("N/A");
        } else {
            double amount = Double.parseDouble(coinObject.getPercent_change_24h());
            if (amount >= 0) {
                dayChangeTextView.setText(String.format(positivePctFormat, amount));
                dayChangeTextView.setTextColor(positiveGreenColor);
            } else {
                dayChangeTextView.setText(String.format(negativePctFormat, amount));
                dayChangeTextView.setTextColor(negativeRedColor);
            }
        }
    }


    public void onCreateView()
    {
        lineChart = findViewById(R.id.chart);
        chartUSDPriceFormat = NumberFormat.getInstance();
        chartUSDPriceFormat.setMaximumFractionDigits(10);
        setUpChart();
        currencyFormatter = CurrencyFormatter.getInstance(this);
        WindowManager mWinMgr = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        displayWidth = mWinMgr.getDefaultDisplay().getWidth();
        chartProgressBar = findViewById(R.id.chartProgressSpinner);
        sharedPreferences = getSharedPreferences(SHAREDPREF_SETTINGS, MODE_PRIVATE);
        final ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item, getResources().getStringArray(R.array.chart_spinner_options));
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        tsymbol = sharedPreferences.getString(CHART_SPINNER_SETTING, "USD");

        nestedScrollView = findViewById(R.id.graphFragmentNestedScrollView);
        buttonGroup = findViewById(R.id.chart_interval_button_grp);
        setDayChecked(Calendar.getInstance());
        buttonGroup.check(R.id.dayButton);
        currentTimeWindow = getString(R.string.oneDay);

        buttonGroup.setOnCheckedChangeListener(new SingleSelectToggleGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SingleSelectToggleGroup group, int checkedId) {
                Calendar.getInstance();
                switch (checkedId) {
                    case R.id.dayButton:
                        setDayChecked(Calendar.getInstance());
                        getCMCChart();
                        break;
                    case R.id.weekButton:
                        setWeekChecked(Calendar.getInstance());
                        getCMCChart();
                        break;
                    case R.id.monthButton:
                        setMonthChecked(Calendar.getInstance());
                        getCMCChart();
                        break;
                    case R.id.threeMonthButton:
                        setThreeMonthChecked(Calendar.getInstance());
                        getCMCChart();
                        break;
                    case R.id.yearButton:
                        setYearChecked(Calendar.getInstance());
                        getCMCChart();
                        break;
                    case R.id.allTimeButton:
                        setAllTimeChecked();
                        getCMCChart();
                        break;
                }
            }
        });
        CoinModel coinObject = getIntent().getParcelableExtra(COIN_OBJECT);
        setTable(coinObject);
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_currency_details);
        toolbar = findViewById(R.id.toolbar_currency_details);
        setSupportActionBar(toolbar);

        symbol = getIntent().getStringExtra(ARG_SYMBOL);
        cryptoID = getIntent().getStringExtra(ARG_ID);
        onCreateView();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.detail_currency_tab_menu, menu);
        return true;
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        TextView currentPrice = findViewById(R.id.current_price);
        TextView dateTextView = findViewById(R.id.graphFragmentDateTextView);
        if (symbol.equals("USD")) {
            currentPrice.setText(String.format(getString(R.string.unrounded_usd_chart_price_format), String.valueOf(e.getY())));
        } else {
            currentPrice.setText(currencyFormatter.format(e.getY(), "BTC"));
        }
        dateTextView.setText(getFormattedFullDate(e.getX()));
    }

    @Override
    public void onNothingSelected() {

    }

    private String getFormattedFullDate(float unixSeconds) {
        Date date = new Date((long)unixSeconds);
        return fullDateFormat.format(date);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_buy:
                Intent purchasedActivity = new Intent(this, BuyActivity.class);
                purchasedActivity.putExtra("Name", cryptoID);
                CoinModel coinObject = getIntent().getParcelableExtra(COIN_OBJECT);
                purchasedActivity.putExtra("Price", coinObject.getPrice_usd());
                startActivity(purchasedActivity);
        }
        return true;
    }

}
