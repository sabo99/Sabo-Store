package com.sabo.sabostore.RoomDB;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.sabo.sabostore.RoomDB.Cart.CartDAO;
import com.sabo.sabostore.RoomDB.Cart.CartItem;
import com.sabo.sabostore.RoomDB.Favorite.FavoriteDAO;
import com.sabo.sabostore.RoomDB.Favorite.FavoriteItem;

@Database(version = 1, entities = {CartItem.class, FavoriteItem.class}, exportSchema = false)
public abstract class RoomDBHost extends RoomDatabase {

    public abstract CartDAO cartDAO();
    public abstract FavoriteDAO favoriteDAO();
    public static RoomDBHost instance;

    public static RoomDBHost getInstance(Context context) {
        if (instance == null)
            instance = Room.databaseBuilder(context, RoomDBHost.class, "SaboStore")
                    .allowMainThreadQueries()
                    .build();
        return instance;
    }
}
