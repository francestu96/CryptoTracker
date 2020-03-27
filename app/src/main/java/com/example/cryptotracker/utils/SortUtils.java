package com.example.cryptotracker.utils;

import com.example.cryptotracker.model.CoinModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


public class SortUtils {
    public static void sortList(ArrayList<CoinModel> currencyList, int number) {
        switch (number) {
            // Name A-Z
            case 0:
                Collections.sort(currencyList, new Comparator<CoinModel>() {
                    @Override
                    public int compare(CoinModel lhs, CoinModel rhs) {
                        return lhs.getName().compareTo(rhs.getName());
                    }
                });
                break;
            // Market Cap
            case 1:
                Collections.sort(currencyList, new Comparator<CoinModel>() {
                    @Override
                    public int compare(CoinModel lhs, CoinModel rhs) {
                    return Integer.parseInt(lhs.getRank()) < Integer.parseInt(rhs.getRank()) ? -1 : Integer.parseInt(lhs.getRank()) > Integer.parseInt(rhs.getRank()) ? +1 : 0;
                    }
                });
                break;
            // Price
            case 2:
                Collections.sort(currencyList, new Comparator<CoinModel>() {
                    @Override
                    public int compare(CoinModel lhs, CoinModel rhs) {
                        if (lhs.getPrice_usd() == null && rhs.getPrice_usd() == null) {
                            return 0;
                        }
                        if (lhs.getPrice_usd() == null) {
                            return 1;
                        }
                        if (rhs.getPrice_usd() == null) {
                            return -1;
                        }
                        float comp = Float.parseFloat(rhs.getPrice_usd()) - Float.parseFloat(lhs.getPrice_usd());
                        return floatComp(comp);
                    }
                });
                break;
            // Change 24h
            case 3:
                Collections.sort(currencyList, new Comparator<CoinModel>() {
                    @Override
                    public int compare(CoinModel lhs, CoinModel rhs) {
                        if (lhs.getPercent_change_24h() == null && rhs.getPercent_change_24h() == null) {
                            return 0;
                        }
                        if (lhs.getPercent_change_24h() == null) {
                            return 1;
                        }
                        if (rhs.getPercent_change_24h() == null) {
                            return -1;
                        }
                        float comp = Float.parseFloat(rhs.getPercent_change_24h()) - Float.parseFloat(lhs.getPercent_change_24h());
                        return floatComp(comp);
                    }
                });
                break;
            // Market Cap LH
            case 4:
                Collections.sort(currencyList, new Comparator<CoinModel>() {
                    @Override
                    public int compare(CoinModel lhs, CoinModel rhs) {
                        return Integer.parseInt(rhs.getRank()) < Integer.parseInt(lhs.getRank()) ? -1 : Integer.parseInt(rhs.getRank()) > Integer.parseInt(lhs.getRank()) ? +1 : 0;
                    }
                });
                break;
            // Price LH
            case 5:
                Collections.sort(currencyList, new Comparator<CoinModel>() {
                    @Override
                    public int compare(CoinModel lhs, CoinModel rhs) {
                        if (lhs.getPrice_usd() == null && rhs.getPrice_usd() == null) {
                            return 0;
                        }
                        if (lhs.getPrice_usd() == null || rhs.getPrice_usd() == null) {
                            return Integer.parseInt(rhs.getRank()) < Integer.parseInt(lhs.getRank()) ? -1 : Integer.parseInt(rhs.getRank()) > Integer.parseInt(lhs.getRank()) ? +1 : 0;
                        }
                        float comp = Float.parseFloat(rhs.getPrice_usd()) - Float.parseFloat(lhs.getPrice_usd());
                        return floatCompLH(comp);
                    }
                });
                break;
            // Change 24h LH
            case 6:
                Collections.sort(currencyList, new Comparator<CoinModel>() {
                    @Override
                    public int compare(CoinModel lhs, CoinModel rhs) {
                        if (lhs.getPercent_change_24h() == null && rhs.getPercent_change_24h() == null) {
                            return 0;
                        }
                        if (lhs.getPercent_change_24h() == null) {
                            return 1;
                        }
                        if (rhs.getPercent_change_24h() == null) {
                            return -1;
                        }
                        float comp = Float.parseFloat(rhs.getPercent_change_24h()) - Float.parseFloat(lhs.getPercent_change_24h());
                        return floatCompLH(comp);
                    }
                });
                break;
        }
    }

    private static int floatComp(float f) {
        if (f == 0) {
            return 0;
        } else if (f < 0) {
            return -1;
        } else {
            return 1;
        }
    }

    private static int floatCompLH(float f) {
        if (f == 0) {
            return 0;
        } else if (f < 0) {
            return 1;
        } else {
            return -1;
        }
    }
}
