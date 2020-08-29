package com.sabo.sabostore.Adapter;

import android.content.Context;
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
import com.sabo.sabostore.API.APICurrency;
import com.sabo.sabostore.Common.Common;
import com.sabo.sabostore.EventBus.BottomSheetDialogEvent;
import com.sabo.sabostore.EventBus.RefreshFavoriteButtonEvent;
import com.sabo.sabostore.R;
import com.sabo.sabostore.RoomDB.Favorite.FavoriteDataSource;
import com.sabo.sabostore.RoomDB.Favorite.FavoriteItem;
import com.sabo.sabostore.RoomDB.Favorite.LocalFavoriteDataSource;
import com.sabo.sabostore.RoomDB.RoomDBHost;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

public class FavoriteHomeAdapter extends RecyclerView.Adapter<FavoriteHomeAdapter.ViewHolder> {

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;

    private Context context;
    private List<FavoriteItem> favoriteItemList;
    private APICurrency mService;
    private FavoriteDataSource favoriteDataSource;

    public FavoriteHomeAdapter(Context context, List<FavoriteItem> favoriteItemList) {
        this.context = context;
        this.favoriteItemList = favoriteItemList;
        mService = Common.getAPIExchangeRates();

        favoriteDataSource = new LocalFavoriteDataSource(RoomDBHost.getInstance(context).favoriteDAO());

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_favorite_home_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FavoriteItem list = favoriteItemList.get(position);

        Picasso.get().load(list.getItemImg()).into(holder.ivItemImg);
        holder.tvItemName.setText(list.getItemName());
        holder.tvItemPrice.setText(new StringBuilder("$ ").append(list.getItemPrice()).toString());

        holder.cvMain.setOnLongClickListener(v -> {
            Common.selectedFavorite = list;
            Common.itemType = list.getItemType();
            Common.favoriteItemId = list.getItemId();
            EventBus.getDefault().postSticky(new BottomSheetDialogEvent(true));
            return true;
        });

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
    }

    private void addOrRemoveFavorite(FavoriteItem list, ViewHolder holder, boolean isAdd) {

        if (isAdd){
            favoriteDataSource.insertFavorite(list);
            holder.imgFav.setImageResource(R.drawable.ic_favorite_true);
            Toast.makeText(context, "Add to favorite.", Toast.LENGTH_SHORT).show();
        }
        else {
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
        RelativeLayout btnFav;
        ImageView ivItemImg, imgFav;
        TextView tvItemName, tvItemType, tvItemPrice;
        CardView cvMain;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            cvMain = itemView.findViewById(R.id.cvMain);
            ivItemImg = itemView.findViewById(R.id.ivItemImg);
            imgFav = itemView.findViewById(R.id.imgFav);
            tvItemName = itemView.findViewById(R.id.tvItemName);
            tvItemPrice = itemView.findViewById(R.id.tvItemPrice);
            btnFav = itemView.findViewById(R.id.btnFav);
        }
    }
}
