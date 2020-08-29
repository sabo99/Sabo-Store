package com.sabo.sabostore.ui.cart;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.sabo.sabostore.RoomDB.Cart.CartItem;

import java.util.List;

public class CartViewModel extends ViewModel {

    private MutableLiveData<List<CartItem>> mutableLiveData;

    public CartViewModel() {
        mutableLiveData = new MutableLiveData<>();
    }

    public MutableLiveData<List<CartItem>> getMutableLiveData() {
        return mutableLiveData;
    }

    public void setMutableLiveData(List<CartItem> cartItemList) {
        mutableLiveData.setValue(cartItemList);
    }
}