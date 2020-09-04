package com.sabo.sabostore.Activity.Main;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.andremion.counterfab.CounterFab;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nex3z.notificationbadge.NotificationBadge;
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;
import com.sabo.sabostore.API.APICurrency;
import com.sabo.sabostore.Activity.Account.AccountActivity;
import com.sabo.sabostore.Activity.Account.AccountMenu.OrderHistoryActivity;
import com.sabo.sabostore.Activity.Account.AccountMenu.Profile.ProfilePhotoPreviewActivity;
import com.sabo.sabostore.Activity.Account.AccountMenu.Profile.ProfileActivity;
import com.sabo.sabostore.Activity.SignInActivity;
import com.sabo.sabostore.Common.Common;
import com.sabo.sabostore.Common.Preferences;
import com.sabo.sabostore.EventBus.CounterCartEvent;
import com.sabo.sabostore.EventBus.HideAppBarCartEvent;
import com.sabo.sabostore.EventBus.HideAppBarProfileEvent;
import com.sabo.sabostore.EventBus.HideFabEvent;
import com.sabo.sabostore.EventBus.NavCartEvent;
import com.sabo.sabostore.EventBus.PreviewPhotoEvent;
import com.sabo.sabostore.EventBus.AllCategoriesClickEvent;
import com.sabo.sabostore.Model.CurrencyRates.CurrencyModel;
import com.sabo.sabostore.Model.CurrencyRates.Rates;
import com.sabo.sabostore.Model.DeliveryCostModel;
import com.sabo.sabostore.Model.ItemsModel;
import com.sabo.sabostore.Model.StoreModel;
import com.sabo.sabostore.Model.UserModel;
import com.sabo.sabostore.R;
import com.sabo.sabostore.RoomDB.Cart.CartDataSource;
import com.sabo.sabostore.RoomDB.Cart.LocalCartDataSource;
import com.sabo.sabostore.RoomDB.RoomDBHost;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import maes.tech.intentanim.CustomIntent;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity {

    private DatabaseReference rootRef;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private APICurrency mService;
    private CartDataSource cartDataSource;

    private AppBarConfiguration mAppBarConfiguration;
    private NavController navController;
    private DrawerLayout drawer;
    private NavigationView navigationView;

    private Toolbar toolbar;
    private RelativeLayout includeProfile, includeCart;
    private CounterFab fab;
    private NotificationBadge badge;
    private CircleImageView civPhotoHeader, civProfilePhoto;
    private TextView tvNameHeader;
    private ProgressBar progressBarHeader;
    private SweetAlertDialog mainLoading;

    private String urlPhoto = "";
    private boolean hideAppBarProfile = false, hideAppBarCart = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        rootRef = FirebaseDatabase.getInstance().getReference();
        cartDataSource = new LocalCartDataSource(RoomDBHost.getInstance(this).cartDAO());
        mService = Common.getAPIExchangeRates();

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            navController.navigate(R.id.nav_cart);
        });

        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_store, R.id.nav_cart)
                .setDrawerLayout(drawer)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        mainLoading = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        mainLoading.getProgressHelper().setBarColor(getResources().getColor(R.color.colorPrimary));
        mainLoading.setTitleText("Loading").show();

        /** Get Currency Rates IDR */
        getCurrencyRates();

        /** Get Delivery Cost */
        getDeliveryCost();

        /** Get All Item For Search */
        getAllSearch();

    }

    /** Get Delivery Cost */
    private void getDeliveryCost() {
        rootRef.child(Common.DELIVERY_REF)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            DeliveryCostModel costModel = snapshot.getValue(DeliveryCostModel.class);

                            Common.deliveryCost = costModel.getCost();
                            Common.minimumPriceFreeDelivery = costModel.getSubtotal();
                            Log.d("cost", String.valueOf(costModel.getCost()));
                            Log.d("free", String.valueOf(costModel.getSubtotal()));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    /**
     * Get All Item For Search
     */
    private List<ItemsModel> searchTempList = new ArrayList<>();

    private void getAllSearch() {

        rootRef.child(Common.STORE_REF).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ItemsModel itemsModel,
                        finalItemsModel,
                        itemKey = new ItemsModel();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    itemKey.setKey(ds.getKey());
                    if (ds.child("items").getValue() != null) {
                        for (DataSnapshot items : ds.child("items").getChildren()) {
                            itemsModel = items.getValue(ItemsModel.class);

                            finalItemsModel = itemsModel;
                            finalItemsModel.setKey(itemKey.getKey());

                            ItemsModel finalItemsModel1 = finalItemsModel;
                            rootRef.child(Common.STORE_REF)
                                    .child(ds.getKey())
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            StoreModel storeModel = snapshot.getValue(StoreModel.class);

                                            finalItemsModel1.setType(storeModel.getName());
                                            searchTempList.add(finalItemsModel1);


                                            mainLoading.dismiss();
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            progressBarHeader.setVisibility(View.GONE);
                                            mainLoading.dismiss();
                                        }
                                    });
                        }
                    }
                }
                Common.searchItems = searchTempList;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBarHeader.setVisibility(View.GONE);
                mainLoading.dismiss();
            }
        });
    }

    /**
     * Get Currency Rates IDR
     */
    private void getCurrencyRates() {
        mService.getExchangeRatesAPI().enqueue(new Callback<CurrencyModel>() {
            @Override
            public void onResponse(Call<CurrencyModel> call, Response<CurrencyModel> response) {
                if (response.isSuccessful() && response.body() != null) {
                    CurrencyModel currencyModel = response.body();
                    Rates rates = currencyModel.getRates();

                    Common.ratesIDR = rates.getIDR();
                }
            }

            @Override
            public void onFailure(Call<CurrencyModel> call, Throwable t) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        initViewsUserHeader(navigationView, toolbar);
        countCartItems();
    }

    private void initViewsUserHeader(NavigationView navigationView, Toolbar toolbar) {
        View headerView = navigationView.getHeaderView(0);
        civPhotoHeader = headerView.findViewById(R.id.civPhotoHeader);
        tvNameHeader = headerView.findViewById(R.id.tvNameHeader);
        progressBarHeader = headerView.findViewById(R.id.progressBar);

        includeProfile = toolbar.findViewById(R.id.includeProfile);
        includeCart = toolbar.findViewById(R.id.includeCart);
        civProfilePhoto = toolbar.findViewById(R.id.civProfilePhoto);

        badge = toolbar.findViewById(R.id.badge);


        includeCart.setOnClickListener(v -> {
            navController.navigate(R.id.nav_cart);
        });


        /** Check Hide or Show AppBar Profile */
        if (hideAppBarProfile)
            includeProfile.setVisibility(View.GONE);
        else
            includeProfile.setVisibility(View.VISIBLE);

        /** Check Hide or Show AppBar Cart */
        if (hideAppBarCart)
            includeCart.setVisibility(View.GONE);
        else
            includeCart.setVisibility(View.VISIBLE);


        /** Set Photo profile from FirebaseDatabase */
        if (firebaseAuth != null) {
            rootRef.child(Common.USER_REF).child(firebaseUser.getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                UserModel userModel = snapshot.getValue(UserModel.class);

                                if (snapshot.hasChild("image")) {
                                    if (userModel.getImage().equals("")) {
                                        urlPhoto = userModel.getImage();
                                        Picasso.get().load(R.drawable.no_profile).into(civPhotoHeader);
                                        Picasso.get().load(R.drawable.no_profile).into(civProfilePhoto);
                                    } else {
                                        urlPhoto = userModel.getImage();
                                        Picasso.get().load(userModel.getImage()).placeholder(R.drawable.no_profile).into(civPhotoHeader);
                                        Picasso.get().load(userModel.getImage()).placeholder(R.drawable.no_profile).into(civProfilePhoto);
                                    }
                                }


                                tvNameHeader.setText(userModel.getName());

                                progressBarHeader.setVisibility(View.GONE);

                                mainLoading.dismissWithAnimation();
                                mainLoading.dismiss();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            progressBarHeader.setVisibility(View.GONE);
                            mainLoading.dismiss();
                            Picasso.get().load(R.drawable.no_profile).into(civPhotoHeader);
                            tvNameHeader.setText("No Connection");
                        }
                    });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    public void signOut(MenuItem item) {
        drawer.closeDrawers();
        new SweetAlertDialog(HomeActivity.this, SweetAlertDialog.NORMAL_TYPE)
                .setTitleText("Sign Out")
                .setContentText("Are you sure?")
                .showCancelButton(true)
                .setCancelClickListener(sweetAlertDialog -> {
                    sweetAlertDialog.dismissWithAnimation();
                    sweetAlertDialog.dismiss();
                })
                .setCancelText("No")
                .setConfirmText("Yes")
                .setConfirmClickListener(sweetAlertDialog -> {
                    updateStatusOff_SignOut(sweetAlertDialog);
                })
                .show();
    }

    private void updateStatusOff_SignOut(SweetAlertDialog sweetAlertDialog) {
        Map<String, Object> updateStatus = new HashMap<>();
        updateStatus.put(Common.KEY_STATUS, "off");

        FirebaseDatabase.getInstance().getReference(Common.USER_REF)
                .child(firebaseUser.getUid())
                .updateChildren(updateStatus)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        sweetAlertDialog.dismissWithAnimation();
                        sweetAlertDialog.dismiss();

                        firebaseAuth.signOut();
                        Preferences.clearPreferences(getBaseContext());
                        Intent i = new Intent(HomeActivity.this, SignInActivity.class);
                        CustomIntent.customType(HomeActivity.this, Common.Anim_Fadein_to_Fadeout);
                        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(i);
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    new SweetAlertDialog(HomeActivity.this, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Error Sign Out!")
                            .setContentText(e.getMessage())
                            .show();
                });
    }

    private void updateStatusOn() {
        Map<String, Object> updateStatus = new HashMap<>();
        updateStatus.put(Common.KEY_STATUS, "on");

        FirebaseDatabase.getInstance().getReference(Common.USER_REF)
                .child(firebaseUser.getUid())
                .updateChildren(updateStatus)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("task", "Success");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.d("task", e.getMessage());
                });
    }

    private void updateStatusOff() {
        Map<String, Object> updateStatus = new HashMap<>();
        updateStatus.put(Common.KEY_STATUS, "off");

        FirebaseDatabase.getInstance().getReference(Common.USER_REF)
                .child(firebaseUser.getUid())
                .updateChildren(updateStatus)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("task", "Success");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.d("task", e.getMessage());
                });
    }

    /** Go to AccountActivity */
    public void account(MenuItem item) {
        drawer.closeDrawers();
        startActivity(new Intent(HomeActivity.this, AccountActivity.class));
        CustomIntent.customType(this, Common.Anim_Left_to_Right);
    }

    /** Go to ProfileActivity */
    public void profile(View view) {
        drawer.closeDrawers();
        startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
        CustomIntent.customType(this, Common.Anim_Fadein_to_Fadeout);
    }

    /** Show Profile Photo */
    public void previewPhoto(View view) {
        drawer.closeDrawers();
        EventBus.getDefault().postSticky(new PreviewPhotoEvent(true, false, urlPhoto));
        startActivity(new Intent(HomeActivity.this, ProfilePhotoPreviewActivity.class));
        CustomIntent.customType(this, Common.Anim_Fadein_to_Fadeout);
    }

    /** Go to OrderHistoryActivity */
    public void orderHistory(MenuItem item) {
        drawer.closeDrawers();
        startActivity(new Intent(this, OrderHistoryActivity.class));
        CustomIntent.customType(this, Common.Anim_Left_to_Right);
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        /** On Start Activity or User used this APP update Status to "on" */
        updateStatusOn();
    }

    @Override
    protected void onStop() {
        /** On Stop Activity or User not used this APP update Status to "off" */
        updateStatusOff();
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        /** On Destroy Activity or User not used this APP update Status to "off" */
        updateStatusOff();
        super.onDestroy();
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void isHideAppBarProfile(HideAppBarProfileEvent event) {
        if (event.isProfileHidden()) {
            hideAppBarProfile = event.isProfileHidden();
            initViewsUserHeader(navigationView, toolbar);
        } else {
            hideAppBarProfile = event.isProfileHidden();
            initViewsUserHeader(navigationView, toolbar);
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void isHideAppBarCart(HideAppBarCartEvent event) {
        if (event.isCartHidden()) {
            hideAppBarCart = event.isCartHidden();
            initViewsUserHeader(navigationView, toolbar);
        } else {
            hideAppBarCart = event.isCartHidden();
            initViewsUserHeader(navigationView, toolbar);
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void isHideFab(HideFabEvent event) {
        if (event.isFabHidden())
            fab.hide();
        else
            fab.show();
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onSeeAllCategories(AllCategoriesClickEvent event) {
        if (event.isClicked()) {
            navController.navigate(R.id.nav_store);
            event.setClicked(false);
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onCountCartItems(CounterCartEvent event) {
        if (event.isSuccess())
            countCartItems();
    }

    private void countCartItems() {
        cartDataSource.countItemInCart(firebaseUser.getUid())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Integer>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(Integer integer) {
                        fab.setCount(integer);

                        if (badge == null) return;
                        if (fab.getCount() == 0) {
                            badge.setVisibility(View.INVISIBLE);
                            badge.setText(null);
                        } else {
                            badge.setVisibility(View.VISIBLE);
                            badge.setText(String.valueOf(integer));
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (e.getMessage().contains("empty")) {
                            fab.setCount(0);
                            badge.setVisibility(View.INVISIBLE);
                            badge.setText(null);
                        }
                    }
                });
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onClickNavCart(NavCartEvent event) {
        if (event.isClicked()) {
            navController.navigate(R.id.nav_cart);
            event.setClicked(false);
        }
    }
}
