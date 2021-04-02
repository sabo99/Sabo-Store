package com.sabo.sabostore.ui.cart;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;
import com.sabo.sabostore.Activity.Main.Checkout.CheckOutActivity;
import com.sabo.sabostore.Adapter.CartAdapter;
import com.sabo.sabostore.Common.Common;
import com.sabo.sabostore.Common.SwipeHelper;
import com.sabo.sabostore.EventBus.CounterCartEvent;
import com.sabo.sabostore.EventBus.HideAppBarCartEvent;
import com.sabo.sabostore.EventBus.HideAppBarProfileEvent;
import com.sabo.sabostore.EventBus.HideFabEvent;
import com.sabo.sabostore.EventBus.UpdateItemInCartEvent;
import com.sabo.sabostore.R;
import com.sabo.sabostore.RoomDB.Cart.CartDataSource;
import com.sabo.sabostore.RoomDB.Cart.CartItem;
import com.sabo.sabostore.RoomDB.Cart.LocalCartDataSource;
import com.sabo.sabostore.RoomDB.RoomDBHost;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import maes.tech.intentanim.CustomIntent;

public class CartFragment extends Fragment {

    private CartViewModel cartViewModel;

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private Parcelable rvCartViewState;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private CartDataSource cartDataSource;
    private CartAdapter adapter;
    private RecyclerView rvCart;
    private LinearLayout emptyCart;
    private TextView tvSubTotalPrice, tvDeliveryCost, tvDeliveryCostDesc, tvTotalPrice;
    private CardView cvText, groupCheckout;
    private Button btnCheckout;

    double deliveryCost = Common.deliveryCost, minimumFreeDelivery = Common.minimumPriceFreeDelivery;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        cartViewModel =
                ViewModelProviders.of(this).get(CartViewModel.class);

        View root = inflater.inflate(R.layout.fragment_cart, container, false);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        cartDataSource = new LocalCartDataSource(RoomDBHost.getInstance(getContext()).cartDAO());

        initViews(root);
        loadCart();

        cartViewModel.getMutableLiveData().observe(getViewLifecycleOwner(), cartItems -> {
            adapter = new CartAdapter(getContext(), cartItems);
            if (cartItems == null || cartItems.isEmpty()) {
                rvCart.setVisibility(View.GONE);
                cvText.setVisibility(View.GONE);
                groupCheckout.setVisibility(View.GONE);
                emptyCart.setVisibility(View.VISIBLE);
            } else {
                rvCart.setVisibility(View.VISIBLE);
                cvText.setVisibility(View.VISIBLE);
                groupCheckout.setVisibility(View.VISIBLE);
                emptyCart.setVisibility(View.GONE);
                rvCart.setAdapter(adapter);
            }
        });

        return root;
    }

    private void initViews(View root) {
        rvCart = root.findViewById(R.id.rvCart);
        emptyCart = root.findViewById(R.id.EmptyCart);
        cvText = root.findViewById(R.id.cvText);
        groupCheckout = root.findViewById(R.id.groupCheckout);
        tvSubTotalPrice = root.findViewById(R.id.tvSubTotalPrice);
        tvDeliveryCost = root.findViewById(R.id.tvDeliveryCost);
        tvDeliveryCostDesc = root.findViewById(R.id.tvDeliveryCostDesc);
        tvTotalPrice = root.findViewById(R.id.tvTotalPrice);
        btnCheckout = root.findViewById(R.id.btnCheckout);

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("My Cart");

        emptyCart.setVisibility(View.GONE);

        setHasOptionsMenu(true);

        tvDeliveryCostDesc.setText(new StringBuilder("Your order in above $ ").append(minimumFreeDelivery).append("\na FREE delivery."));

        rvCart.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        rvCart.setHasFixedSize(true);

        SwipeHelper swipeHelper = new SwipeHelper(getContext(), rvCart, 300) {
            @Override
            public void instantiateButtonSwipe(RecyclerView.ViewHolder viewHolder, List<ButtonSwipe> buf) {
                buf.add(new ButtonSwipe(getContext(), "Remove", 40, 0, getResources().getColor(R.color.red_btn_bg_color),
                        pos -> {
                            CartItem cartItem = adapter.getItemAtPosition(pos);
                            cartDataSource.deleteCartItem(cartItem)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new SingleObserver<Integer>() {
                                        @Override
                                        public void onSubscribe(Disposable d) {

                                        }

                                        @Override
                                        public void onSuccess(Integer integer) {
                                            adapter.notifyItemRemoved(pos);

                                            sumAllItemCart();

                                            EventBus.getDefault().postSticky(new CounterCartEvent(true));
//                                            Toast.makeText(getContext(), "Item removed.", Toast.LENGTH_SHORT).show();
                                            Log.d("cartRemove", integer.toString());
                                        }

                                        @Override
                                        public void onError(Throwable e) {
                                            new SweetAlertDialog(getContext(), SweetAlertDialog.ERROR_TYPE)
                                                    .setTitleText("Oops!")
                                                    .setContentText(e.getMessage())
                                                    .show();
                                        }
                                    });
                        }));
            }
        };

        /** Sum Total Price */
        sumAllItemCart();


        btnCheckout.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), CheckOutActivity.class));
            CustomIntent.customType(getContext(), Common.Anim_Left_to_Right);
        });
    }


    private void sumAllItemCart() {
        cartDataSource.sumPriceInCart(firebaseUser.getUid())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Double>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(Double aDouble) {
                        /** If Your Subtotal Cost above minimumFreeDelivery u get Free Delivery Cost */
                        if (aDouble > minimumFreeDelivery){
                            tvDeliveryCost.setText(new StringBuilder("$ ").append(0.0).toString());
                            tvSubTotalPrice.setText(new StringBuilder("$ ").append(Common.formatPrice(aDouble)).toString());
                            double totalPrice = 0.0 + aDouble;

                            tvTotalPrice.setText(new StringBuilder("$ ").append(Common.formatPrice(totalPrice)).toString());
                            Common.totalPayment = totalPrice;
                        }
                        else {
                            tvDeliveryCost.setText(new StringBuilder("$ ").append(deliveryCost).toString());
                            tvSubTotalPrice.setText(new StringBuilder("$ ").append(Common.formatPrice(aDouble)).toString());
                            double totalPrice = deliveryCost + aDouble;

                            tvTotalPrice.setText(new StringBuilder("$ ").append(Common.formatPrice(totalPrice)).toString());
                            Common.totalPayment = totalPrice;
                        }

                    }

                    @Override
                    public void onError(Throwable e) {
                        if (e.getMessage().contains("Query returned empty")){
                            emptyCart.setVisibility(View.VISIBLE);
                            Log.d("e", e.getMessage());
                        }

                    }
                });
    }

    private void loadCart() {
        compositeDisposable.add(cartDataSource.getAllCart(firebaseUser.getUid())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(cartItems -> {
                    cartViewModel.setMutableLiveData(cartItems);
                }, throwable -> {
                    cartViewModel.setMutableLiveData(null);
                }));
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.cart, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_clear_cart:
                showDialogClearCart();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDialogClearCart() {
        new SweetAlertDialog(getContext(), SweetAlertDialog.WARNING_TYPE)
                .setTitleText("Clear All Cart!")
                .setContentText("Are your sure?")
                .showCancelButton(true)
                .setCancelText("No")
                .setCancelClickListener(sweetAlertDialog -> {
                    sweetAlertDialog.dismiss();
                })
                .setConfirmText("Clear")
                .setConfirmClickListener(sweetAlertDialog -> {
                    sweetAlertDialog.dismiss();
                    clearCart();
                })
                .show();
    }

    private void clearCart() {
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
                        if (integer != 0)
                            new SweetAlertDialog(getContext(), SweetAlertDialog.SUCCESS_TYPE)
                                    .setTitleText("Success!")
                                    .setContentText("Clear cart success.")
                                    .show();
                        else
                            new SweetAlertDialog(getContext(), SweetAlertDialog.NORMAL_TYPE)
                                    .setTitleText("No Items!")
                                    .setContentText("Cart is empty.")
                                    .setConfirmText("Close")
                                    .show();
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (e.getMessage().contains("empty")) {

                        } else
                            new SweetAlertDialog(getContext(), SweetAlertDialog.ERROR_TYPE)
                                    .setTitleText("Oops!")
                                    .setContentText(e.getMessage())
                                    .show();
                    }
                });
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        compositeDisposable.clear();
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().postSticky(new HideAppBarProfileEvent(true));
        EventBus.getDefault().postSticky(new HideAppBarCartEvent(true));
        EventBus.getDefault().postSticky(new HideFabEvent(true));

        loadCart();
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onUpdateItemInCart(UpdateItemInCartEvent event) {
        if (event.getCartItem() != null) {
            rvCartViewState = rvCart.getLayoutManager().onSaveInstanceState();
            cartDataSource.updateCartItems(event.getCartItem())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<Integer>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onSuccess(Integer integer) {
                            sumAllItemCart();
                            rvCart.getLayoutManager().onRestoreInstanceState(rvCartViewState);
                        }

                        @Override
                        public void onError(Throwable e) {

                        }
                    });
        }
    }

}
