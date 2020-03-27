package com.example.cryptotracker.utils;

import android.widget.TextView;

public class CurrencyListAdapterUtils {

    public static void setPercentChangeTextView(TextView textView, String pctChange, String time,
                                                String negativePercentStringResource, String positivePercentStringResource,
                                                int negativeRedColor, int positiveGreenColor, String pctChangeNotAvailableStringResource) {
        if (pctChange == null) {
            textView.setText(String.format(pctChangeNotAvailableStringResource, time));
        } else {
            double change = Double.parseDouble(pctChange);
            if (change < 0) {
                textView.setText(String.format(negativePercentStringResource, time, change));
                textView.setTextColor(negativeRedColor);
            } else {
                textView.setText(String.format(positivePercentStringResource, time, change));
                textView.setTextColor(positiveGreenColor);
            }
        }
    }

}
