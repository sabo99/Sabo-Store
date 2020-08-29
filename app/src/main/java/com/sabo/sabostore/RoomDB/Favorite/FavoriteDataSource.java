package com.sabo.sabostore.RoomDB.Favorite;

import java.util.List;

import io.reactivex.Flowable;

public interface FavoriteDataSource {

    Flowable<List<FavoriteItem>> getAllFavorite(String uid);

    Flowable<List<FavoriteItem>> getLimitFavorite(String uid);

    int isFavorite(String uid, String itemId, String itemName);

    void insertFavorite(FavoriteItem... favoriteItems);

    void deleteFavorite(String uid, String itemName);

    void clearAllFavorite(String uid);
}
