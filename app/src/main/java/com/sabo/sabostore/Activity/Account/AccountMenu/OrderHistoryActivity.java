package com.sabo.sabostore.Activity.Account.AccountMenu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
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
import com.sabo.sabostore.Adapter.OrderHistoryAdapter;
import com.sabo.sabostore.Common.Common;
import com.sabo.sabostore.Model.OrderModel;
import com.sabo.sabostore.R;

import java.util.ArrayList;
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
                        if (snapshot.exists()){
                            orderModelList.clear();
                            for (DataSnapshot ds : snapshot.getChildren()){
                                OrderModel orderModel = ds.getValue(OrderModel.class);
                                orderModel.setKey(ds.getKey());
                                orderModelList.add(orderModel);
                            }
                            progressBar.setVisibility(View.GONE);

                            adapter = new OrderHistoryAdapter(OrderHistoryActivity.this, orderModelList);
                            rvOrderHistory.setAdapter(adapter);
                            rvOrderHistory.setVisibility(View.VISIBLE);
                            emptyOrderHistory.setVisibility(View.GONE);

                        }
                        else {
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
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            CustomIntent.customType(this, Common.Anim_Right_to_Left);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void finish() {
        super.finish();
        CustomIntent.customType(this, Common.Anim_Right_to_Left);
    }
}
