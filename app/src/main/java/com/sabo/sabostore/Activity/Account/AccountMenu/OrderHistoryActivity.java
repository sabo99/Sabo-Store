package com.sabo.sabostore.Activity.Account.AccountMenu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;
import com.sabo.sabostore.Adapter.OrderHistoryAdapter;
import com.sabo.sabostore.Common.Common;
import com.sabo.sabostore.EventBus.UpdateOrderStatusEvent;
import com.sabo.sabostore.Model.OrderModel;
import com.sabo.sabostore.R;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import maes.tech.intentanim.CustomIntent;

public class OrderHistoryActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private DatabaseReference orderRef;

    private RecyclerView rvOrderHistory;
    private OrderHistoryAdapter adapter;
    private List<OrderModel> orderModelList = new ArrayList<>();
    private ProgressBar progressBar;
    private LinearLayout emptyOrderHistory;

    @Override
    protected void onResume() {
        super.onResume();
        loadOrderHistory();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        orderRef = FirebaseDatabase.getInstance().getReference(Common.ORDER_REF);

        initViews();
    }

    private void initViews() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Order History");

        rvOrderHistory = findViewById(R.id.rvOrderHistory);
        progressBar = findViewById(R.id.progressBar);
        emptyOrderHistory = findViewById(R.id.emptyOrderHistory);
        rvOrderHistory.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
    }

    private void loadOrderHistory() {
        emptyOrderHistory.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        orderRef.orderByChild("userUID")
                .equalTo(firebaseUser.getUid())
                .limitToLast(100)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            orderModelList.clear();
                            for (DataSnapshot ds : snapshot.getChildren()) {
                                OrderModel orderModel = ds.getValue(OrderModel.class);
                                orderModel.setKey(ds.getKey());
                                orderModelList.add(orderModel);
                            }
                            progressBar.setVisibility(View.GONE);

                            adapter = new OrderHistoryAdapter(OrderHistoryActivity.this, orderModelList);
                            rvOrderHistory.setAdapter(adapter);
                            rvOrderHistory.setVisibility(View.VISIBLE);
                            emptyOrderHistory.setVisibility(View.GONE);

                            /** Sort Z-A by OrderNumber*/
                            Collections.sort(orderModelList, new Comparator<OrderModel>() {
                                @Override
                                public int compare(OrderModel o1, OrderModel o2) {
                                    return o2.getOrderNumber().compareTo(o1.getOrderNumber());
                                }
                            });

                        } else {
                            progressBar.setVisibility(View.GONE);
                            rvOrderHistory.setVisibility(View.GONE);
                            emptyOrderHistory.setVisibility(View.VISIBLE);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.order_history, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                CustomIntent.customType(this, Common.Anim_Right_to_Left);
                finish();
            }
            break;
            case R.id.action_info:
                showDialogStatusInformation();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDialogStatusInformation() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_status_order_layout, null);
        SweetAlertDialog sweetStatus = new SweetAlertDialog(this, SweetAlertDialog.CUSTOM_IMAGE_TYPE)
                .setCustomImage(R.drawable.ic_info_type)
                .setTitleText("Status Information")
                .setConfirmText("Close")
                .setConfirmClickListener(sweetAlertDialog -> {
                    sweetAlertDialog.dismissWithAnimation();
                });
        sweetStatus.show();
        LinearLayout linearLayout = sweetStatus.findViewById(R.id.loading);
        int index = linearLayout.indexOfChild(linearLayout.findViewById(R.id.content_text));
        linearLayout.addView(view, index + 1);
    }

    @Override
    public void finish() {
        super.finish();
        CustomIntent.customType(this, Common.Anim_Right_to_Left);
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onUpdateOrderStatus(UpdateOrderStatusEvent event) {
        if (event.isUpdated()) {
            loadOrderHistory();
            event.setUpdated(false);
        }
    }
}
