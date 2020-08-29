package com.sabo.sabostore.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;
import com.sabo.sabostore.Common.Common;
import com.sabo.sabostore.R;
import com.sabo.sabostore.RoomDB.Cart.CartItem;
import com.squareup.picasso.Picasso;

import java.util.List;

public class OrderDetailAdapter extends RecyclerView.Adapter<OrderDetailAdapter.ViewHolder> {

    private Context context;
    private List<CartItem> cartItemList;

    public OrderDetailAdapter(Context context, List<CartItem> cartItemList) {
        this.context = context;
        this.cartItemList = cartItemList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_order_detail_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CartItem list = cartItemList.get(position);

        Picasso.get().load(list.getItemImage()).into(holder.ivItemImg);
        holder.tvItemName.setText(list.getItemName());
        holder.tvItemQuantity.setText(new StringBuilder().append(list.getItemQuantity()).append(" pcs").toString());
        holder.tvItemPrice.setText(new StringBuilder("$ ").append(Common.formatPrice(list.getItemPrice())).append(" /pcs").toString());
    }

    @Override
    public int getItemCount() {
        return cartItemList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView ivItemImg, ivDeliveryInfo;
        TextView tvItemName, tvItemQuantity, tvItemPrice;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivItemImg = itemView.findViewById(R.id.ivItemImg);
            tvItemName = itemView.findViewById(R.id.tvItemName);
            tvItemQuantity = itemView.findViewById(R.id.tvItemQuantity);
            tvItemPrice = itemView.findViewById(R.id.tvItemPrice);
        }
    }
}
