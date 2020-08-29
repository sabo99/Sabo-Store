package com.sabo.sabostore.ui.home;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.sabo.sabostore.RoomDB.Favorite.FavoriteItem;

import java.util.List;

public class HomeViewModel extends ViewModel {
    private MutableLiveData<List<FavoriteItem>> listMutableLiveData;

    public HomeViewModel() {
        listMutableLiveData = new MutableLiveData<>();
    }

    public MutableLiveData<List<FavoriteItem>> getListMutableLiveData() {
        return listMutableLiveData;
    }

    public void setListMutableLiveData(List<FavoriteItem> favoriteItems) {
        listMutableLiveData.setValue(favoriteItems);
    }
}
