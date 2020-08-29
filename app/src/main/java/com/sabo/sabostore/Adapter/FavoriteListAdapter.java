package com.sabo.sabostore.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;
import com.sabo.sabostore.Activity.Main.ItemsDetailActivity;
import com.sabo.sabostore.Common.Common;
import com.sabo.sabostore.EventBus.RefreshFavoriteButtonEvent;
import com.sabo.sabostore.Model.ItemsModel;
import com.sabo.sabostore.R;
import com.sabo.sabostore.RoomDB.Favorite.FavoriteDataSource;
import com.sabo.sabostore.RoomDB.Favorite.FavoriteItem;
import com.sabo.sabostore.RoomDB.Favorite.LocalFavoriteDataSource;
import com.sabo.sabostore.RoomDB.RoomDBHost;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import maes.tech.intentanim.CustomIntent;

public class FavoriteListAdapter extends RecyclerView.Adapter<FavoriteListAdapter.ViewHolder> {

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private DatabaseReference storeRef;

    private Context context;
    private List<FavoriteItem> favoriteItemList;
    private FavoriteDataSource favoriteDataSource;

    public FavoriteListAdapter(Context context, List<FavoriteItem> favoriteItemList) {
        this.context = context;
        this.favoriteItemList = favoriteItemList;
        favoriteDataSource = new LocalFavoriteDataSource(RoomDBHost.getInstance(context).favoriteDAO());

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        storeRef = FirebaseDatabase.getInstance().getReference(Common.STORE_REF);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_favorite_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FavoriteItem list = favoriteItemList.get(position);

        Picasso.get().load(list.getItemImg()).into(holder.ivItemImg);
        holder.tvItemName.setText(list.getItemName());
        holder.tvItemType.setText(list.getItemType());
        holder.tvItemPrice.setText(new StringBuilder("$ ").append(list.getItemPrice()).toString());

        /** Check State of Favorite or Not */
        if (favoriteDataSource.isFavorite(firebaseUser.getUid(), list.getItemId(), list.getItemName()) == 1) {
            holder.imgFav.setImageResource(R.drawable.ic_favorite_true);
        } else {
            holder.imgFav.setImageResource(R.drawable.ic_favorite_false);
        }

        holder.btnFav.setOnClickListener(v -> {
            if (favoriteDataSource.isFavorite(firebaseUser.getUid(), list.getItemId(), list.getItemName()) != 1) {
                addOrRemoveFavorite(list, holder, true);
            } else {
                addOrRemoveFavorite(list, holder, false);
            }
        });

        /** Detail Item */
        String var = list.getItemId();
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
                        if (snapshot.exists()) {
                            ItemsModel itemsModel = new ItemsModel();
                            for (DataSnapshot items : snapshot.getChildren()) {
                                itemsModel = items.getValue(ItemsModel.class);
                            }

                            ItemsModel itemList = itemsModel;
                            holder.cvMain.setOnClickListener(v -> {
                                Common.selectedItem = itemList;
                                Common.itemType = list.getItemType();
                                Common.favoriteItemId = list.getItemId();
                                context.startActivity(new Intent(context, ItemsDetailActivity.class));
                                CustomIntent.customType(context, Common.Anim_Left_to_Right);
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        holder.cvMain.setOnClickListener(v -> {
                            new SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
                                    .setContentText("Oops!")
                                    .setContentText(error.getMessage())
                                    .show();
                        });
                    }
                });
    }

    private void addOrRemoveFavorite(FavoriteItem list, ViewHolder holder, boolean isAdd) {

        if (isAdd) {
            favoriteDataSource.insertFavorite(list);
            holder.imgFav.setImageResource(R.drawable.ic_favorite_true);
            Toast.makeText(context, "Add to favorite.", Toast.LENGTH_SHORT).show();
        } else {
            favoriteDataSource.deleteFavorite(list.getUid(), list.getItemName());
            holder.imgFav.setImageResource(R.drawable.ic_favorite_false);
            Toast.makeText(context, "Remove from favorite.", Toast.LENGTH_SHORT).show();
            EventBus.getDefault().postSticky(new RefreshFavoriteButtonEvent(true));
        }
    }

    @Override
    public int getItemCount() {
        return favoriteItemList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        CardView cvMain;
        RelativeLayout btnFav;
        ImageView ivItemImg, imgFav;
        TextView tvItemName, tvItemType, tvItemPrice;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            cvMain = itemView.findViewById(R.id.cvMain);
            ivItemImg = itemView.findViewById(R.id.ivItemImg);
            imgFav = itemView.findViewById(R.id.imgFav);
            tvItemName = itemView.findViewById(R.id.tvItemName);
            tvItemType = itemView.findViewById(R.id.tvItemType);
            tvItemPrice = itemView.findViewById(R.id.tvItemPrice);

            btnFav = itemView.findViewById(R.id.btnFav);
        }
    }
}
