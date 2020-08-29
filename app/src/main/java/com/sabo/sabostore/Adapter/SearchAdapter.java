package com.sabo.sabostore.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.lid.lib.LabelTextView;
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

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private DatabaseReference storeRef;

    private Context context;
    private List<ItemsModel> itemsModelList;
    private FavoriteDataSource favoriteDataSource;

    public SearchAdapter(Context context, List<ItemsModel> itemsModelList) {
        this.context = context;
        this.itemsModelList = itemsModelList;
        favoriteDataSource = new LocalFavoriteDataSource(RoomDBHost.getInstance(context).favoriteDAO());

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        storeRef = FirebaseDatabase.getInstance().getReference(Common.STORE_REF);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_items_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ItemsModel list = itemsModelList.get(position);

        double resultPrice = list.getPrice() / Common.ratesIDR;

        Picasso.get().load(list.getImage()).into(holder.ivItemImg);
        holder.tvItemName.setText(list.getName());
        holder.tvItemPrice.setText(new StringBuilder("$ ")
                .append(Common.formatPrice(resultPrice)).toString());

        String var = list.getType();
        if (var.contains(" ")){
            String[] split = var.split(" ");
            String res = split[1];
            holder.ltvItemType.setLabelText(res);
        }
        else
            holder.ltvItemType.setLabelText(var);


//        storeRef.child(list.getKey())
//                .addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot snapshot) {
//
//                        StoreModel storeModel = snapshot.getValue(StoreModel.class);
//
//
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError error) {
//
//                    }
//                });

        String itemId = new StringBuilder().append(list.getKey()).append("&").append(list.getId()).toString();
        String itemPrice = Common.formatPrice(resultPrice);
        String itemType = list.getType();

        FavoriteItem favoriteItem = new FavoriteItem();
        favoriteItem.setUid(firebaseUser.getUid());
        favoriteItem.setItemId(itemId);
        favoriteItem.setItemImg(list.getImage());
        favoriteItem.setItemName(list.getName());
        favoriteItem.setItemPrice(itemPrice);
        favoriteItem.setItemType(itemType);


        /** Check State of Favorite or Not */
        if (favoriteDataSource.isFavorite(firebaseUser.getUid(), itemId, list.getName()) == 1) {
            holder.imgFav.setImageResource(R.drawable.ic_favorite_true);
        } else {
            holder.imgFav.setImageResource(R.drawable.ic_favorite_false);
        }

        holder.btnFav.setOnClickListener(v -> {
            if (favoriteDataSource.isFavorite(firebaseUser.getUid(), itemId, list.getName()) != 1) {
                addOrRemoveFavorite(favoriteItem, holder, true);
            } else {
                addOrRemoveFavorite(favoriteItem, holder, false);
            }
        });

        holder.btnDetail.setOnClickListener(v -> {
            Common.selectedItem = list;
            Common.itemType = itemType;
            Common.favoriteItemId = itemId;
            Common.selectedItem.setKey(String.valueOf(position));
            context.startActivity(new Intent(context, ItemsDetailActivity.class));
//                            ((Activity) context).finish();
            CustomIntent.customType(context, Common.Anim_Left_to_Right);
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
        return itemsModelList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        RelativeLayout btnFav;
        TextView tvItemName, tvItemPrice;
        LabelTextView ltvItemType;
        ImageView ivItemImg, imgFav;
        Button btnDetail;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            ltvItemType = itemView.findViewById(R.id.ltvItemType);
            ivItemImg = itemView.findViewById(R.id.ivItemImg);
            tvItemName = itemView.findViewById(R.id.tvItemName);
            tvItemPrice = itemView.findViewById(R.id.tvItemPrice);
            btnFav = itemView.findViewById(R.id.btnFav);
            imgFav = itemView.findViewById(R.id.imgFav);
            btnDetail = itemView.findViewById(R.id.btnDetail);
        }
    }
}
