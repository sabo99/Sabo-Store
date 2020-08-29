package com.sabo.sabostore.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sabo.sabostore.Activity.Main.ItemsActivity;
import com.sabo.sabostore.Common.Common;
import com.sabo.sabostore.Model.StoreModel;
import com.sabo.sabostore.R;
import com.squareup.picasso.Picasso;

import java.util.List;

import maes.tech.intentanim.CustomIntent;

public class CategoriesAdapter extends RecyclerView.Adapter<CategoriesAdapter.ViewHolder> {

    private int lastPosition = -1;
    private DatabaseReference storeRef;
    private Context context;
    private List<StoreModel> storeModelList;

    public CategoriesAdapter(Context context, List<StoreModel> storeModelList) {
        this.context = context;
        this.storeModelList = storeModelList;
        storeRef = FirebaseDatabase.getInstance().getReference(Common.STORE_REF);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_categories_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StoreModel list = storeModelList.get(position);
        Picasso.get().load(list.getImage()).into(holder.ivItemImg);
        holder.tvItemName.setText(list.getName());
        holder.tvItemDesc.setText(list.getDesc());


        holder.rlItemView.setOnClickListener(v -> {
            Common.selectedStoreItems = list;
            context.startActivity(new Intent(context, ItemsActivity.class));
            CustomIntent.customType(context, Common.Anim_Left_to_Right);
        });

        setAnimation(holder.itemView, position);
    }

    private void setAnimation(View view, int position) {
        if (position > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(context, R.anim.slide_in_right);
            view.startAnimation(animation);
            lastPosition = position;
        }
    }

    @Override
    public int getItemCount() {
        return storeModelList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        RelativeLayout rlItemView;
        ImageView ivItemImg;
        TextView tvItemName, tvItemDesc;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            rlItemView = itemView.findViewById(R.id.rlItemView);
            ivItemImg = itemView.findViewById(R.id.ivItemImg);
            tvItemName = itemView.findViewById(R.id.tvItemName);
            tvItemDesc = itemView.findViewById(R.id.tvItemDescription);
        }
    }
}
