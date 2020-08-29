package com.sabo.sabostore.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;
import com.sabo.sabostore.Common.Common;
import com.sabo.sabostore.Model.OrderModel;
import com.sabo.sabostore.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class OrderHistoryAdapter extends RecyclerView.Adapter<OrderHistoryAdapter.ViewHolder> {

    private Context context;
    private List<OrderModel> orderModelList;
    private Calendar calendar;
    private Date date;
    private SimpleDateFormat simpleDateFormat;

    public OrderHistoryAdapter(Context context, List<OrderModel> orderModelList) {
        this.context = context;
        this.orderModelList = orderModelList;
        calendar = Calendar.getInstance();
        simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_order_history_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OrderModel list = orderModelList.get(position);

        String x = list.getOrderNumber();
        String xr = x.substring(0, 8);

        calendar.setTimeInMillis(list.getOrderDate());
        date = new Date(list.getOrderDate());

        holder.tvOrderNumber.setText(new StringBuilder("Order #").append(xr));
        holder.tvOrderDate.setText(new StringBuilder(Common.getDateOfWeek(calendar.get(Calendar.DAY_OF_WEEK)))
                .append(" ")
                .append(simpleDateFormat.format(date)));
        holder.tvTotalPrice.setText(new StringBuilder("$ ").append(Common.formatPrice(list.getTotalPayment())));
        holder.tvOrderStatus.setText(Common.covertStatus(list.getOrderStatus()));

        checkOrderStatus(holder, list.getOrderStatus());

        holder.tvMore.setOnClickListener(v -> {
            showDetailOrder(list);
        });

        /** Set OrderListItems */
        setOrderListItems(holder, list);
    }

    private void checkOrderStatus(ViewHolder holder, int orderStatus) {
        switch (orderStatus){
            case 0 :
                holder.ivStatus.setImageResource(R.drawable.ic_close_black_24dp);
                break;
            case 1 :
                holder.ivStatus.setImageResource(R.drawable.ic_history);
                break;
            case 2 :
                holder.ivStatus.setImageResource(R.drawable.ic_on_process);
                break;
            case 3 :
                holder.ivStatus.setImageResource(R.drawable.ic_shipped);
                break;
            case 4 :
                holder.ivStatus.setImageResource(R.drawable.ic_received);
                break;
            default:
                break;
        }
    }

    private void setOrderListItems(ViewHolder holder, OrderModel list) {
        OrderListItemsAdapter adapter = new OrderListItemsAdapter(context, list.getCartItems());
        holder.rvItemOrderListItems.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        holder.rvItemOrderListItems.setAdapter(adapter);
    }

    private void showDetailOrder(OrderModel list) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_order_detail_layout, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(context).setView(view).setCancelable(false);

        RecyclerView rvOrderDetail;
        ImageView ivDeliveryInfo;
        TextView tvShipping, tvSubTotalPrice, tvDeliveryCost, tvTotalPrice;
        Button btnClose;
        rvOrderDetail = view.findViewById(R.id.rvOrderDetail);
        tvShipping = view.findViewById(R.id.tvShipping);
        tvSubTotalPrice = view.findViewById(R.id.tvSubTotalPrice);
        tvDeliveryCost = view.findViewById(R.id.tvDeliveryCost);
        tvTotalPrice = view.findViewById(R.id.tvTotalPrice);
        ivDeliveryInfo = view.findViewById(R.id.ivDeliveryInfo);
        btnClose = view.findViewById(R.id.btnClose);

        OrderDetailAdapter adapter = new OrderDetailAdapter(context, list.getCartItems());
        rvOrderDetail.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        rvOrderDetail.setAdapter(adapter);

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialog1 -> {
            btnClose.setOnClickListener(v -> {
                dialog.dismiss();
            });

            tvShipping.setOnClickListener(v -> {
                new SweetAlertDialog(context, SweetAlertDialog.CUSTOM_IMAGE_TYPE)
                        .setCustomImage(R.drawable.ic_info_type)
                        .setTitleText("Shipping Info!")
                        .setContentText(list.getName() + "\n" + list.getAddress() + ", " + list.getZip() + " " + list.getCity())
                        .show();
            });

            ivDeliveryInfo.setOnClickListener(v -> {
                new SweetAlertDialog(context, SweetAlertDialog.CUSTOM_IMAGE_TYPE)
                        .setCustomImage(R.drawable.ic_info_type)
                        .setTitleText("Delivery Info!")
                        .setContentText("Your order in above $" + (int) Common.deliveryCost + " " +
                                "\na FREE delivery cost.")
                        .show();
            });


            if (list.getTotalPayment() > Common.minimumPriceFreeDelivery){
                tvDeliveryCost.setText("FREE");
                tvSubTotalPrice.setText(new StringBuilder("$ ").append(Common.formatPrice(list.getTotalPayment())).toString());
            }

            else {
                tvDeliveryCost.setText(new StringBuilder("$ ").append(Common.formatPrice(Common.deliveryCost)).toString());
                tvSubTotalPrice.setText(new StringBuilder("$ ").append(Common.formatPrice(list.getTotalPayment() - Common.deliveryCost).toString()));
            }

            tvTotalPrice.setText(new StringBuilder("$ ").append(Common.formatPrice(list.getTotalPayment())).toString());

        });

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setGravity(Gravity.CENTER);
        dialog.show();
    }

    @Override
    public int getItemCount() {
        return orderModelList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView ivStatus;
        TextView tvOrderNumber, tvOrderDate, tvOrderStatus, tvTotalPrice, tvMore;
        RecyclerView rvItemOrderListItems;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            ivStatus = itemView.findViewById(R.id.ivStatus);
            tvOrderNumber = itemView.findViewById(R.id.tvOrderNumber);
            tvOrderDate = itemView.findViewById(R.id.tvOrderDate);
            tvOrderStatus = itemView.findViewById(R.id.tvOrderStatus);
            tvTotalPrice = itemView.findViewById(R.id.tvTotalPrice);
            tvMore = itemView.findViewById(R.id.tvMore);
            rvItemOrderListItems = itemView.findViewById(R.id.rvItemOrderListItems);
        }
    }
}
