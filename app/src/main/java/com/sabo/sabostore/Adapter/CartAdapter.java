package com.sabo.sabostore.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sabo.sabostore.Common.Common;
import com.sabo.sabostore.EventBus.CounterCartEvent;
import com.sabo.sabostore.EventBus.UpdateItemInCartEvent;
import com.sabo.sabostore.R;
import com.sabo.sabostore.RoomDB.Cart.CartItem;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import nl.dionsegijn.steppertouch.StepperTouch;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {

    private Context context;
    private List<CartItem> cartItemList;

    public CartAdapter(Context context, List<CartItem> cartItemList) {
        this.context = context;
        this.cartItemList = cartItemList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_cart_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CartItem list = cartItemList.get(position);

        Picasso.get().load(list.getItemImage()).into(holder.ivItemImg);
        holder.tvItemName.setText(list.getItemName());
        holder.tvItemPrice.setText(new StringBuilder("$ ").append(Common.formatPrice(list.getItemPrice())).toString());
        holder.stepperTouch.setCount(list.getItemQuantity());

        holder.stepperTouch.setMinValue(1);
        holder.stepperTouch.setSideTapEnabled(true);
        holder.stepperTouch.addStepCallback((i, b) -> {
            /** When user click this button, will update in database */
            list.setItemQuantity(i);
            EventBus.getDefault().postSticky(new UpdateItemInCartEvent(list));
            EventBus.getDefault().postSticky(new CounterCartEvent(true));
        });

    }

    @Override
    public int getItemCount() {
        return cartItemList.size();
    }

    public CartItem getItemAtPosition(int pos) {
        return cartItemList.get(pos);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView ivItemImg;
        TextView tvItemName, tvItemPrice;
        StepperTouch stepperTouch;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            ivItemImg = itemView.findViewById(R.id.ivItemImg);
            tvItemName = itemView.findViewById(R.id.tvItemName);
            tvItemPrice = itemView.findViewById(R.id.tvItemPrice);
            stepperTouch = itemView.findViewById(R.id.stepperTouch);
        }
    }
}
