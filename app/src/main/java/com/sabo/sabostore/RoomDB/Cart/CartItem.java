package com.sabo.sabostore.RoomDB.Cart;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;

@Entity(tableName = "Cart", primaryKeys = {"uid", "itemId"})
public class CartItem {

    @NonNull
    @ColumnInfo(name = "itemId")
    private String itemId;

    @ColumnInfo(name = "itemName")
    private String itemName;

    @ColumnInfo(name = "itemImage")
    private String itemImage;

    @ColumnInfo(name = "itemPrice")
    private Double itemPrice;

    @ColumnInfo(name = "itemQuantity")
    private int itemQuantity;

//    @ColumnInfo(name = "itemExtraPrice")
//    private Double itemExtraPrice;

    @ColumnInfo(name = "userEmail")
    private String userEmail;

    @NonNull
    @ColumnInfo(name = "uid")
    private String uid;


    @NonNull
    public String getItemId() {
        return itemId;
    }

    public void setItemId(@NonNull String itemId) {
        this.itemId = itemId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getItemImage() {
        return itemImage;
    }

    public void setItemImage(String itemImage) {
        this.itemImage = itemImage;
    }

    public Double getItemPrice() {
        return itemPrice;
    }

    public void setItemPrice(Double itemPrice) {
        this.itemPrice = itemPrice;
    }

    public int getItemQuantity() {
        return itemQuantity;
    }

    public void setItemQuantity(int itemQuantity) {
        this.itemQuantity = itemQuantity;
    }

//    public Double getItemExtraPrice() {
//        return itemExtraPrice;
//    }
//
//    public void setItemExtraPrice(Double itemExtraPrice) {
//        this.itemExtraPrice = itemExtraPrice;
//    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    @NonNull
    public String getUid() {
        return uid;
    }

    public void setUid(@NonNull String uid) {
        this.uid = uid;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == this)
            return true;
        if (!(obj instanceof  CartItem))
            return false;
        CartItem cartItem = (CartItem)obj;
        return cartItem.getItemId().equals(this.itemId);
    }
}
