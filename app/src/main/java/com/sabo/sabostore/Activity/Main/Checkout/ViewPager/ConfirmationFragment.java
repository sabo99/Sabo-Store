package com.sabo.sabostore.Activity.Main.Checkout.ViewPager;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;
import com.sabo.sabostore.Adapter.CheckOutAdapter;
import com.sabo.sabostore.Common.Common;
import com.sabo.sabostore.EventBus.ConfirmationEvent;
import com.sabo.sabostore.R;
import com.sabo.sabostore.RoomDB.Cart.CartDataSource;
import com.sabo.sabostore.RoomDB.Cart.LocalCartDataSource;
import com.sabo.sabostore.RoomDB.RoomDBHost;

import org.greenrobot.eventbus.EventBus;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class ConfirmationFragment extends Fragment {

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private CartDataSource cartDataSource;

    private TextView tvName, tvShipping, tvTotalPayment, tvEditShipping, tvEditOrder;
    private RecyclerView rvCheckout;
    private CheckOutAdapter adapter;
    private ProgressBar progressBar;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_confirmation, container, false);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        cartDataSource = new LocalCartDataSource(RoomDBHost.getInstance(getContext()).cartDAO());

        initViews(root);
        loadOrder();

        return root;
    }

    private void initViews(View root) {
        tvName = root.findViewById(R.id.tvName);
        tvShipping = root.findViewById(R.id.tvShipping);
        tvTotalPayment = root.findViewById(R.id.tvTotalPayment);
        rvCheckout = root.findViewById(R.id.rvCheckOut);
        tvTotalPayment = root.findViewById(R.id.tvTotalPayment);
        tvEditShipping = root.findViewById(R.id.tvEditShipping);
        tvEditOrder = root.findViewById(R.id.tvEditOrder);
        progressBar = root.findViewById(R.id.progressBar);

        String shippingAddress = new StringBuilder(Common.shipping.getAddress())
                .append(", ").append(Common.shipping.getZip())
                .append(" ").append(Common.shipping.getCity()).toString();

        tvName.setText(Common.shipping.getName());
        tvShipping.setText(shippingAddress);
        tvTotalPayment.setText(new StringBuilder("$ ").append(Common.formatPrice(Common.totalPayment)).toString());

        rvCheckout.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        tvEditShipping.setOnClickListener(v -> {
            EventBus.getDefault().postSticky(new ConfirmationEvent(true, false));
        });

        tvEditOrder.setOnClickListener(v -> {
            EventBus.getDefault().postSticky(new ConfirmationEvent(false, true));
        });
    }

    private void loadOrder() {
        progressBar.setVisibility(View.VISIBLE);

        compositeDisposable.add(cartDataSource.getAllCart(firebaseUser.getUid())
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(cartItems -> {
            progressBar.setVisibility(View.GONE);

            adapter = new CheckOutAdapter(getContext(), cartItems);
            rvCheckout.setAdapter(adapter);
        }, throwable -> {
            progressBar.setVisibility(View.GONE);
            new SweetAlertDialog(getContext(), SweetAlertDialog.WARNING_TYPE)
                    .setTitleText("Oops!")
                    .setContentText(throwable.getMessage())
                    .show();
        }));
    }

    @Override
    public void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }
}