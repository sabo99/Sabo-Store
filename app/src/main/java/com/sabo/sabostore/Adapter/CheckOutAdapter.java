package com.sabo.sabostore.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sabo.sabostore.Common.Common;
import com.sabo.sabostore.R;
import com.sabo.sabostore.RoomDB.Cart.CartItem;
import com.squareup.picasso.Picasso;

import java.util.List;

public class CheckOutAdapter extends RecyclerView.Adapter<CheckOutAdapter.ViewHolder>{

    private Context context;
    private List<CartItem> cartItemList;

    public CheckOutAdapter(Context context, List<CartItem> cartItemList) {
        this.context = context;
        this.cartItemList = cartItemList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_checkout_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CartItem list = cartItemList.get(position);

        Picasso.get().load(list.getItemImage()).into(holder.ivItemImg);
        holder.tvItemName.setText(list.getItemName());
        holder.tvItemPrice.setText(new StringBuilder("$ ").append(Common.formatPrice(list.getItemPrice())).toString());
        holder.tvItemQuantity.setText(String.valueOf(list.getItemQuantity()));
    }

    @Override
    public int getItemCount() {
        return cartItemList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView ivItemImg;
        TextView tvItemName, tvItemPrice, tvItemQuantity;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivItemImg = itemView.findViewById(R.id.ivItemImg);
            tvItemName = itemView.findViewById(R.id.tvItemName);
            tvItemPrice = itemView.findViewById(R.id.tvItemPrice);
            tvItemQuantity = itemView.findViewById(R.id.tvItemQuantity);
        }
    }
}
