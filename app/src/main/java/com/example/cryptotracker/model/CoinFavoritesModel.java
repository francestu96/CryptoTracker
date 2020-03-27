package com.example.cryptotracker.model;

import java.util.HashMap;
import java.util.List;

public class CoinFavoritesModel {
    public List<String> favoriteList;
    public HashMap<String, String> favoritesMap;

    public CoinFavoritesModel(List<String> favoriteList, HashMap<String, String> favoritesMap) {
        this.favoriteList = favoriteList;
        this.favoritesMap = favoritesMap;
    }
}
