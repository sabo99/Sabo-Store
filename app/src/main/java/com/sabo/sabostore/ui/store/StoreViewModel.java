package com.sabo.sabostore.ui.store;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sabo.sabostore.Common.Common;
import com.sabo.sabostore.Model.SliderModel;
import com.sabo.sabostore.Model.StoreModel;

import java.util.ArrayList;
import java.util.List;

public class StoreViewModel extends ViewModel {

    private DatabaseReference rootRef;
    private MutableLiveData<List<SliderModel>> sliderLiveData;
    private MutableLiveData<String> itemName_01, itemName_02, itemName_03, itemName_04, itemName_05, itemName_06, itemName_07, itemName_08;

    public StoreViewModel() {
        rootRef = FirebaseDatabase.getInstance().getReference();
    }

    public MutableLiveData<List<SliderModel>> getSliderLiveData() {
        if (sliderLiveData == null){
            sliderLiveData = new MutableLiveData<>();
            loadSlider();
        }
        return sliderLiveData;
    }


    public MutableLiveData<String> getItemName_01() {
        if (itemName_01 == null){
            itemName_01 = new MutableLiveData<>();

            loadItems();
        }
        return itemName_01;
    }

    public MutableLiveData<String> getItemName_02() {
        if (itemName_02 == null){
            itemName_02 = new MutableLiveData<>();

            loadItems();
        }
        return itemName_02;
    }

    public MutableLiveData<String> getItemName_03() {
        if (itemName_03 == null){
            itemName_03 = new MutableLiveData<>();

            loadItems();
        }
        return itemName_03;
    }

    public MutableLiveData<String> getItemName_04() {
        if (itemName_04 == null){
            itemName_04 = new MutableLiveData<>();
            loadItems();
        }
        return itemName_04;
    }

    public MutableLiveData<String> getItemName_05() {
        if (itemName_05 == null){
            itemName_05 = new MutableLiveData<>();
            loadItems();
        }
        return itemName_05;
    }

    public MutableLiveData<String> getItemName_06() {
        if (itemName_06 == null){
            itemName_06 = new MutableLiveData<>();
            loadItems();
        }
        return itemName_06;
    }

    public MutableLiveData<String> getItemName_07() {
        if (itemName_07 == null){
            itemName_07 = new MutableLiveData<>();
            loadItems();
        }
        return itemName_07;
    }

    public MutableLiveData<String> getItemName_08() {
        if (itemName_08 == null){
            itemName_08 = new MutableLiveData<>();
        }
        return itemName_08;
    }

    private void loadSlider() {
        List<SliderModel> tempList = new ArrayList<>();
        rootRef.child(Common.SLIDER_REF).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    SliderModel sliderModel = ds.getValue(SliderModel.class);
                    tempList.add(sliderModel);
                }
                sliderLiveData.setValue(tempList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadItems() {
        rootRef.child(Common.STORE_REF)
                .child("item_01")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        StoreModel storeModel = snapshot.getValue(StoreModel.class);

                        itemName_01.setValue("item_01 " + storeModel.getName());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        rootRef.child(Common.STORE_REF)
                .child("item_02")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        StoreModel storeModel = snapshot.getValue(StoreModel.class);

                        itemName_02.setValue("item_02 " + storeModel.getName());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        rootRef.child(Common.STORE_REF)
                .child("item_03")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        StoreModel storeModel = snapshot.getValue(StoreModel.class);

                        itemName_03.setValue("item_03 " + storeModel.getName());

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        rootRef.child(Common.STORE_REF)
                .child("item_04")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        StoreModel storeModel = snapshot.getValue(StoreModel.class);

                        itemName_04.setValue("item_04 " + storeModel.getName());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        rootRef.child(Common.STORE_REF)
                .child("item_05")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        StoreModel storeModel = snapshot.getValue(StoreModel.class);

                        itemName_05.setValue("item_05 " + storeModel.getName());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        rootRef.child(Common.STORE_REF)
                .child("item_06")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        StoreModel storeModel = snapshot.getValue(StoreModel.class);

                        itemName_06.setValue("item_06 " + storeModel.getName());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        rootRef.child(Common.STORE_REF)
                .child("item_07")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        StoreModel storeModel = snapshot.getValue(StoreModel.class);

                        itemName_07.setValue("item_07 " + storeModel.getName());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        rootRef.child(Common.STORE_REF)
                .child("item_08")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        StoreModel storeModel = snapshot.getValue(StoreModel.class);

                        itemName_08.setValue("item_08 " + storeModel.getName());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}