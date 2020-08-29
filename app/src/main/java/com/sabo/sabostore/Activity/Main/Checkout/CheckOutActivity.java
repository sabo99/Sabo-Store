package com.sabo.sabostore.Activity.Main.Checkout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;
import com.sabo.sabostore.Adapter.CheckoutViewPagerAdapter;
import com.sabo.sabostore.Common.Common;
import com.sabo.sabostore.Common.ILoadOffSetTimeListener;
import com.sabo.sabostore.EventBus.ConfirmationEvent;
import com.sabo.sabostore.EventBus.CounterCartEvent;
import com.sabo.sabostore.EventBus.ShippingEvent;
import com.sabo.sabostore.Model.OrderModel;
import com.sabo.sabostore.R;
import com.sabo.sabostore.RoomDB.Cart.CartDataSource;
import com.sabo.sabostore.RoomDB.Cart.LocalCartDataSource;
import com.sabo.sabostore.RoomDB.RoomDBHost;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.SimpleDateFormat;
import java.util.Date;

import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import maes.tech.intentanim.CustomIntent;

public class CheckOutActivity extends AppCompatActivity implements ILoadOffSetTimeListener {

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private DatabaseReference orderRef;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private CartDataSource cartDataSource;
    private ILoadOffSetTimeListener offSetTimeListener;

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private CheckoutViewPagerAdapter adapter;
    private TextView tvTotalPayment;
    private Button btnToPayment, btnToConfirm, btnConfirmPay;
    private SweetAlertDialog loadingPay;

    private int state = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_out);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        orderRef = FirebaseDatabase.getInstance().getReference(Common.ORDER_REF);
        cartDataSource = new LocalCartDataSource(RoomDBHost.getInstance(this).cartDAO());

        offSetTimeListener = this;

        loadingPay = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        loadingPay.getProgressHelper().setBarColor(getResources().getColor(R.color.colorPrimary));
        loadingPay.setTitleText("Please wait...");

        initViews();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
        btnToPayment = findViewById(R.id.btnToPayment);
        btnToConfirm = findViewById(R.id.btnToConfirm);
        btnConfirmPay = findViewById(R.id.btnConfirmPay);
        tvTotalPayment = findViewById(R.id.tvTotalPayment);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Checkout");
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_white);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        tvTotalPayment.setText(new StringBuilder("$ ").append(Common.formatPrice(Common.totalPayment)).toString());

        adapter = new CheckoutViewPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);

        checkState();

        btnToPayment.setOnClickListener(v -> {
            if (state == 0) {
                EventBus.getDefault().postSticky(new ShippingEvent(true, 0));
//                viewPager.setCurrentItem(viewPager.getCurrentItem() + 1, true);
//                state = 1;
//                checkState();
            }
        });

        btnToConfirm.setOnClickListener(v -> {
            if (state == 1) {
                viewPager.setCurrentItem(viewPager.getCurrentItem() + 1, true);
                state = 2;
                checkState();
            }
        });

        btnConfirmPay.setOnClickListener(v -> {
//            state = 0;
//            checkState();
//            finish();

            if (state == 2) {
                compositeDisposable.add(cartDataSource.getAllCart(firebaseUser.getUid())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(cartItems -> {

                            if (!cartItems.isEmpty() && cartItems != null) {
                                OrderModel orderModel = new OrderModel();
                                orderModel.setCartItems(cartItems);
                                orderModel.setName(Common.shipping.getName());
                                orderModel.setPhone(Common.shipping.getPhone());
                                orderModel.setAddress(Common.shipping.getAddress());
                                orderModel.setZip(Common.shipping.getZip());
                                orderModel.setCity(Common.shipping.getCity());
                                orderModel.setTotalPayment(Double.parseDouble(Common.formatPrice(Common.totalPayment)));
                                orderModel.setOrderStatus(1); /** Status 0 : Ordered */

                                createDate(orderModel);
                            }
                        }, throwable -> {
                            new SweetAlertDialog(CheckOutActivity.this, SweetAlertDialog.WARNING_TYPE)
                                    .setTitleText("Oops!")
                                    .setContentText(throwable.getMessage())
                                    .show();
                        }));
            }

        });
    }

    /**
     * Create Date in Long From Firebase OffSetTime
     */
    private void createDate(OrderModel orderModel) {
        loadingPay.show();

        final DatabaseReference offSetRef = FirebaseDatabase.getInstance().getReference(".info/serverTimeOffset");
        offSetRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    long offset = snapshot.getValue(Long.class);
                    long estimatedServerTimeMs = System.currentTimeMillis() + offset;
                    final long orderDate = estimatedServerTimeMs;

                    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                    Date resultDate = new Date(orderDate);
                    String x = sdf.format(resultDate);

                    Log.d("xa", x);

                    new Handler().postDelayed(() -> {
                        offSetTimeListener.onLoadTimeSuccess(orderModel, orderDate);
                    }, 1000);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                loadingPay.dismiss();
                offSetTimeListener.onLoadTimeFailed(error.getMessage());
            }
        });
    }

    @Override
    public void onLoadTimeSuccess(OrderModel orderModel, long offSetTime) {
        /** Set OrderDate in long */
        orderModel.setOrderDate(offSetTime);
        orderModel.setUserUID(firebaseUser.getUid());
        orderModel.setOrderNumber(Common.createOrderNumber());

        writeOrderToFirebase(orderModel);
    }

    @Override
    public void onLoadTimeFailed(String message) {
        new SweetAlertDialog(CheckOutActivity.this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("Oops!")
                .setContentText(message)
                .show();
    }

    /**
     * Write Order to Firebase
     */
    private void writeOrderToFirebase(OrderModel orderModel) {
        if (orderModel.getCartItems() != null || !orderModel.getCartItems().isEmpty()) {
            orderRef.child(orderModel.getOrderNumber())
                    .setValue(orderModel)
                    .addOnFailureListener(e -> {
                        loadingPay.dismiss();

                        new SweetAlertDialog(CheckOutActivity.this, SweetAlertDialog.WARNING_TYPE)
                                .setTitleText("Oops!")
                                .setContentText(e.getMessage())
                                .show();
                    })
                    .addOnCompleteListener(task -> {
                        cartDataSource.cleanCart(firebaseUser.getUid())
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new SingleObserver<Integer>() {
                                    @Override
                                    public void onSubscribe(Disposable d) {

                                    }

                                    @Override
                                    public void onSuccess(Integer integer) {
                                        EventBus.getDefault().postSticky(new CounterCartEvent(true));

                                        loadingPay.setTitleText("Success!")
                                                .setContentText("Order placed successfully.")
                                                .setConfirmText("Ok")
                                                .setConfirmClickListener(sweetAlertDialog -> {
                                                    sweetAlertDialog.dismiss();
                                                    state = 0;
                                                    checkState();
                                                    finish();
                                                })
                                                .changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                                    }

                                    @Override
                                    public void onError(Throwable e) {
                                        if (!e.getMessage().contains("empty"))
                                            new SweetAlertDialog(CheckOutActivity.this, SweetAlertDialog.WARNING_TYPE)
                                                    .setTitleText("Oops!")
                                                    .setContentText(e.getMessage())
                                                    .show();
                                    }
                                });
                    });
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                CustomIntent.customType(this, Common.Anim_Right_to_Left);
                finish();
                state = 0;
            }
            break;
        }

        return super.onOptionsItemSelected(item);
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
        compositeDisposable.clear();
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (state == 0) {
            finish();
            super.onBackPressed();
        }
        if (state == 1) {
            viewPager.setCurrentItem(viewPager.getCurrentItem() - 1, true);
            state = 0;
            checkState();
        }
        if (state == 2) {
            viewPager.setCurrentItem(viewPager.getCurrentItem() - 1, true);
            state = 1;
            checkState();
        }
    }

    private void checkState() {
        if (state == 0) {
            btnToPayment.setVisibility(View.VISIBLE);
            btnToConfirm.setVisibility(View.GONE);
            btnConfirmPay.setVisibility(View.GONE);

            viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                    if (viewPager.getAdapter().getCount() > 1)
                        if (state == 0)
                            viewPager.setCurrentItem(0);
                }

                @Override
                public void onPageSelected(int position) {

                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
        }
        if (state == 1) {
            btnToPayment.setVisibility(View.GONE);
            btnToConfirm.setVisibility(View.VISIBLE);
            btnConfirmPay.setVisibility(View.GONE);

            viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                    if (viewPager.getAdapter().getCount() > 1)
                        if (state == 1)
                            viewPager.setCurrentItem(1);
                }

                @Override
                public void onPageSelected(int position) {

                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
        }
        if (state == 2) {
            btnToPayment.setVisibility(View.GONE);
            btnToConfirm.setVisibility(View.GONE);
            btnConfirmPay.setVisibility(View.VISIBLE);

            viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                    if (viewPager.getAdapter().getCount() > 1)
                        if (state == 2)
                            viewPager.setCurrentItem(2);
                }

                @Override
                public void onPageSelected(int position) {

                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
        }
    }

    /**
     * After finish fill information Shipping, Next Step of Checkout (PAYMENT)
     */
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onShippingSuccess(ShippingEvent event) {
        if (event.isChecked() && event.getState() == 1) {
            viewPager.setCurrentItem(viewPager.getCurrentItem() + 1, true);
            state = 1;
            checkState();
            event.setChecked(false);
            event.setState(-1);
        }
    }

    /** Edit Shipping, From ConfirmationFragment back to ShippingFragment */
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEditShipping (ConfirmationEvent event){
        if (event.isEditShipping()){
            viewPager.setCurrentItem(viewPager.getCurrentItem() - 2, true);
            state = 0;
            checkState();
            event.setEditShipping(false);
        }

        if (event.isEditOrder()){
            state = 0;
            checkState();
            finish();
            event.setEditOrder(false);
        }
    }
}