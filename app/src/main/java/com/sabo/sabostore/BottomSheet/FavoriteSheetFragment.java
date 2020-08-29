package com.sabo.sabostore.BottomSheet;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;
import com.sabo.sabostore.Activity.Main.ItemsDetailActivity;
import com.sabo.sabostore.Common.Common;
import com.sabo.sabostore.EventBus.BottomSheetDialogEvent;
import com.sabo.sabostore.Model.ItemsModel;
import com.sabo.sabostore.R;
import com.sabo.sabostore.RoomDB.Favorite.FavoriteItem;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import maes.tech.intentanim.CustomIntent;

/**
 * A simple {@link Fragment} subclass.
 */
public class FavoriteSheetFragment extends BottomSheetDialogFragment {

    public static FavoriteSheetFragment instance;

    private DatabaseReference storeRef;

    private TextView tvItemName, tvItemType, tvItemPrice;
    private Button btnDetail;
    private ImageView ivItemImg;

    public static FavoriteSheetFragment getInstance() {
        if (instance == null)
            instance = new FavoriteSheetFragment();
        return instance;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_favorite_sheet, container, false);

        storeRef = FirebaseDatabase.getInstance().getReference(Common.STORE_REF);

        initViews(root);
        loadData();

        return root;
    }

    private void initViews(View root) {
        tvItemName = root.findViewById(R.id.tvItemName);
        tvItemType = root.findViewById(R.id.tvItemType);
        tvItemPrice = root.findViewById(R.id.tvItemPrice);
        btnDetail = root.findViewById(R.id.btnDetail);
        ivItemImg = root.findViewById(R.id.ivItemImg);
    }

    private void loadData() {
        FavoriteItem favoriteItem = Common.selectedFavorite;

        Picasso.get().load(favoriteItem.getItemImg()).into(ivItemImg);
        tvItemName.setText(favoriteItem.getItemName());
        tvItemType.setText(favoriteItem.getItemType());
        tvItemPrice.setText(new StringBuilder("Price").append("\n$ ").append(favoriteItem.getItemPrice()).toString());

        String var = favoriteItem.getItemId();
        String[] split = var.split("&");
        String item_id = split[0];
        String id = split[1];


        storeRef.child(item_id)
                .child("items")
                .orderByChild("id")
                .equalTo(id)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            ItemsModel itemsModel = new ItemsModel();
                            for (DataSnapshot items : snapshot.getChildren()) {
                                itemsModel = items.getValue(ItemsModel.class);
                            }

                            ItemsModel list = itemsModel;
                            btnDetail.setOnClickListener(v -> {
                                instance.dismiss();
                                Common.selectedItem = list;
                                startActivity(new Intent(getContext(), ItemsDetailActivity.class));
                                CustomIntent.customType(getContext(), Common.Anim_Left_to_Right);
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        btnDetail.setOnClickListener(v -> {
                            instance.dismiss();
                            new SweetAlertDialog(getContext(), SweetAlertDialog.WARNING_TYPE)
                                    .setTitleText("Oops!")
                                    .setContentText(error.getMessage())
                                    .show();
                        });
                    }
                });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ((View) getView().getParent()).setBackgroundColor(Color.TRANSPARENT);
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void favoriteBottomSheet(BottomSheetDialogEvent event) {
        if (event.isClicked()) {
            loadData();
        }
    }
}
