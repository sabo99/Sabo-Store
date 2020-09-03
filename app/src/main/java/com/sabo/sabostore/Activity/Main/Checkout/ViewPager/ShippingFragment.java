package com.sabo.sabostore.Activity.Main.Checkout.ViewPager;

import android.graphics.Paint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;
import com.sabo.sabostore.Common.Common;
import com.sabo.sabostore.Common.Preferences;
import com.sabo.sabostore.EventBus.ShippingEvent;
import com.sabo.sabostore.Model.OrderModel;
import com.sabo.sabostore.Model.UserModel;
import com.sabo.sabostore.R;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import static android.text.Html.fromHtml;

public class ShippingFragment extends Fragment {

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private DatabaseReference userRef;

    private TextView tvPay, tvFree;
    private EditText etName, etPhoneNumber, etAddress, etZipCode, etCity;
    private Switch switchSaveShipping;
    private ProgressBar progressBar;

    double deliveryCost = Common.deliveryCost, minimumFreeDelivery = Common.minimumPriceFreeDelivery;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_shipping, container, false);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        userRef = FirebaseDatabase.getInstance().getReference(Common.USER_REF).child(firebaseUser.getUid());

        initViews(root);
        initUserInformation();

        return root;
    }

    private void initViews(View root) {
        etName = root.findViewById(R.id.etName);
        etPhoneNumber = root.findViewById(R.id.etPhone);
        etAddress = root.findViewById(R.id.etAddress);
        etZipCode = root.findViewById(R.id.etZIP);
        etCity = root.findViewById(R.id.etCity);
        tvPay = root.findViewById(R.id.tvPay);
        tvFree = root.findViewById(R.id.tvFree);
        progressBar = root.findViewById(R.id.progressBar);
        switchSaveShipping = root.findViewById(R.id.switchSaveShipping);

        tvPay.setText(new StringBuilder("$ ").append(Common.formatPrice(deliveryCost)).toString());

        if (Common.totalPayment > minimumFreeDelivery)
            tvPay.setPaintFlags(tvPay.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        else
            tvFree.setPaintFlags(tvFree.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

        switchSaveShipping.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    switchSaveShipping.setChecked(true);
                    buttonView.setOnClickListener(v -> {
                        Preferences.setSaveShipping(getContext(), true);
                        Preferences.setPhone(getContext(), etPhoneNumber.getText().toString());
                        Preferences.setAddress(getContext(), etAddress.getText().toString());
                        Preferences.setZIP(getContext(), etZipCode.getText().toString());
                        Preferences.setCity(getContext(), etCity.getText().toString());
                    });

                } else {
                    switchSaveShipping.setChecked(false);
                    buttonView.setOnClickListener(v -> {
                        Preferences.setSaveShipping(getContext(), false);
                        Preferences.clearShipping(getContext());
                    });
                }
            }
        });

        if (Preferences.getSaveShipping(getContext())) {
            switchSaveShipping.setChecked(true);
            etPhoneNumber.setText(Preferences.getPhone(getContext()));
            etAddress.setText(Preferences.getAddress(getContext()));
            etZipCode.setText(Preferences.getZip(getContext()));
            etCity.setText(Preferences.getCity(getContext()));
        } else
            switchSaveShipping.setChecked(false);

    }

    private void initUserInformation() {
        progressBar.setVisibility(View.VISIBLE);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    UserModel userModel = snapshot.getValue(UserModel.class);

                    etName.setText(userModel.getName());
                    etName.setEnabled(false);

                    if (userModel.getPhone().equals("")) {
                        etPhoneNumber.setEnabled(true);
                    } else {
                        etPhoneNumber.setText(userModel.getPhone());
                        etPhoneNumber.setEnabled(false);
                        etPhoneNumber.setError(null);
                    }

                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        clearErrorMsg();
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    private void clearErrorMsg() {
        etName.setError(null);
        etPhoneNumber.setError(null);
        etAddress.setError(null);
        etZipCode.setError(null);
        etCity.setError(null);
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onCheckedShipping(ShippingEvent event) {
        if (event.isChecked() && event.getState() == 0) {
            if (checkForm(true, etPhoneNumber.getText().toString(), etAddress.getText().toString(),
                    etZipCode.getText().toString(), etCity.getText().toString())) {


                EventBus.getDefault().postSticky(new ShippingEvent(true, 1));

                OrderModel orderModel = new OrderModel();
                orderModel.setName(etName.getText().toString());
                orderModel.setPhone(etPhoneNumber.getText().toString());
                orderModel.setAddress(etAddress.getText().toString());
                orderModel.setZip(etZipCode.getText().toString());
                orderModel.setCity(etCity.getText().toString());

                Common.shipping = orderModel;

                event.setChecked(false);
                event.setState(-1);
            }
        }
    }

    private boolean checkForm(boolean checked, String phone, String address, String zip, String city) {
        String msg = "Required!";
        if (TextUtils.isEmpty(phone)) {
            checked = false;
            etPhoneNumber.setError(msg);
        }
        if (TextUtils.isEmpty(address)) {
            checked = false;
            etAddress.setError(msg);
        }
        if (TextUtils.isEmpty(zip)) {
            checked = false;
            etZipCode.setError(msg);
        }
        if (TextUtils.isEmpty(city)) {
            checked = false;
            etCity.setError(msg);
        }
        return checked;
    }
}