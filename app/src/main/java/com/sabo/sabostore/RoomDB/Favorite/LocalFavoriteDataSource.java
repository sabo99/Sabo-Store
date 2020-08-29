package com.sabo.sabostore.RoomDB.Favorite;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Single;

public class LocalFavoriteDataSource implements FavoriteDataSource {

    FavoriteDAO favoriteDAO;

    public LocalFavoriteDataSource(FavoriteDAO favoriteDAO) {
        this.favoriteDAO = favoriteDAO;
    }

    @Override
    public Flowable<List<FavoriteItem>> getAllFavorite(String uid) {
        return favoriteDAO.getAllFavorite(uid);
    }

    @Override
    public Flowable<List<FavoriteItem>> getLimitFavorite(String uid) {
        return favoriteDAO.getLimitFavorite(uid);
    }

    @Override
    public int isFavorite(String uid, String itemId, String itemName) {
        return favoriteDAO.isFavorite(uid, itemId, itemName);
    }

    @Override
    public void insertFavorite(FavoriteItem... favoriteItems) {
        favoriteDAO.insertFavorite(favoriteItems);
    }

    @Override
    public void deleteFavorite(String uid, String itemName) {
        favoriteDAO.deleteFavorite(uid, itemName);
    }

    @Override
    public void clearAllFavorite(String uid) {
        favoriteDAO.clearAllFavorite(uid);
    }
}
