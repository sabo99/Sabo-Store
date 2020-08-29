package com.sabo.sabostore.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sabo.sabostore.Common.Common;
import com.sabo.sabostore.R;
import com.sabo.sabostore.RoomDB.Cart.CartItem;

import java.util.List;

public class OrderListItemsAdapter extends RecyclerView.Adapter<OrderListItemsAdapter.ViewHolder> {

    private Context context;
    private List<CartItem> cartItemList;

    public OrderListItemsAdapter(Context context, List<CartItem> cartItemList) {
        this.context = context;
        this.cartItemList = cartItemList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_order_list_items_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CartItem list = cartItemList.get(position);

        holder.tvItemName.setText(list.getItemName());
        holder.tvItemQuantity.setText(new StringBuilder().append(list.getItemQuantity()).append(" pcs"));

        double itemTotalPrice = (double) list.getItemQuantity() * list.getItemPrice();
        holder.tvItemTotalPrice.setText(new StringBuilder("$ ").append(Common.formatPrice(itemTotalPrice)).toString());

    }

    @Override
    public int getItemCount() {
        return cartItemList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvItemName, tvItemQuantity, tvItemTotalPrice;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvItemName = itemView.findViewById(R.id.tvItemName);
            tvItemQuantity = itemView.findViewById(R.id.tvItemQuantity);
            tvItemTotalPrice = itemView.findViewById(R.id.tvItemTotalPrice);
        }
    }
}
