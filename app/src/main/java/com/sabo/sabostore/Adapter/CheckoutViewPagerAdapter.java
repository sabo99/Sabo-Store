package com.sabo.sabostore.Adapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.sabo.sabostore.Activity.Main.Checkout.ViewPager.ConfirmationFragment;
import com.sabo.sabostore.Activity.Main.Checkout.ViewPager.PaymentFragment;
import com.sabo.sabostore.Activity.Main.Checkout.ViewPager.ShippingFragment;

public class CheckoutViewPagerAdapter extends FragmentPagerAdapter {

    public CheckoutViewPagerAdapter(@NonNull FragmentManager fm) {
        super(fm);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                ShippingFragment shippingFragment = new ShippingFragment();
                return shippingFragment;
            case 1:
                PaymentFragment paymentFragment = new PaymentFragment();
                return paymentFragment;
            case 2:
                ConfirmationFragment confirmationFragment = new ConfirmationFragment();
                return confirmationFragment;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position){
            case 0 :
                return "SHIPPING";
            case 1 :
                return "PAYMENT";
            case 2 :
                return "CONFIRMATION";
            default:
                return null;
        }
    }


}
