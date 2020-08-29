package com.sabo.sabostore.RoomDB.Favorite;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "Favorite", primaryKeys = {"uid", "itemId"})
public class FavoriteItem {

    @NonNull
    @ColumnInfo(name = "uid")
    private String uid;

    @NonNull
    @ColumnInfo(name = "itemId")
    private String itemId;

    @ColumnInfo(name = "itemImg")
    private String itemImg;

    @ColumnInfo(name = "itemName")
    private String itemName;

    @ColumnInfo(name = "itemPrice")
    private String itemPrice;

    @ColumnInfo(name = "itemType")
    private String itemType;


    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getItemImg() {
        return itemImg;
    }

    public void setItemImg(String itemImg) {
        this.itemImg = itemImg;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getItemPrice() {
        return itemPrice;
    }

    public void setItemPrice(String itemPrice) {
        this.itemPrice = itemPrice;
    }

    public String getItemType() {
        return itemType;
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
    }


    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == this)
            return true;
        if (!(obj instanceof FavoriteItem))
            return false;
        FavoriteItem favoriteItem = (FavoriteItem) obj;
        return favoriteItem.getItemId().equals(this.itemId) &&
                favoriteItem.getUid().equals(this.uid);
    }
}
