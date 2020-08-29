package com.sabo.sabostore.RoomDB.Favorite;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;


import io.reactivex.Flowable;
import io.reactivex.Single;

@Dao
public interface FavoriteDAO {

    @Query("SELECT * FROM Favorite WHERE uid=:uid ORDER BY itemName ASC")
    Flowable<List<FavoriteItem>> getAllFavorite(String uid);

    @Query("SELECT * FROM Favorite WHERE uid=:uid ORDER BY itemName LIMIT 3")
    Flowable<List<FavoriteItem>> getLimitFavorite(String uid);

    @Query("SELECT EXISTS (SELECT 1 FROM Favorite WHERE uid=:uid AND itemId=:itemId AND itemName=:itemName)")
    int isFavorite(String uid, String itemId, String itemName);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertFavorite(FavoriteItem... favoriteItems);

    @Query("DELETE FROM Favorite WHERE uid=:uid AND itemName=:itemName")
    void deleteFavorite(String uid, String itemName);

    @Query("DELETE FROM Favorite WHERE uid=:uid")
    void clearAllFavorite(String uid);
}
