package com.sabo.sabostore.Activity.Main;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.andremion.counterfab.CounterFab;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.ms.square.android.expandabletextview.ExpandableTextView;
import com.nex3z.notificationbadge.NotificationBadge;
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;
import com.sabo.sabostore.Common.Common;
import com.sabo.sabostore.EventBus.CounterCartEvent;
import com.sabo.sabostore.EventBus.NavCartEvent;
import com.sabo.sabostore.EventBus.RefreshFavoriteButtonEvent;
import com.sabo.sabostore.Model.ItemsModel;
import com.sabo.sabostore.R;
import com.sabo.sabostore.RoomDB.Cart.CartDataSource;
import com.sabo.sabostore.RoomDB.Cart.CartItem;
import com.sabo.sabostore.RoomDB.Cart.LocalCartDataSource;
import com.sabo.sabostore.RoomDB.Favorite.FavoriteDataSource;
import com.sabo.sabostore.RoomDB.Favorite.FavoriteItem;
import com.sabo.sabostore.RoomDB.Favorite.LocalFavoriteDataSource;
import com.sabo.sabostore.RoomDB.RoomDBHost;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import maes.tech.intentanim.CustomIntent;
import nl.dionsegijn.steppertouch.OnStepCallback;
import nl.dionsegijn.steppertouch.StepperTouch;

public class ItemsDetailActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private FavoriteDataSource favoriteDataSource;
    private CartDataSource cartDataSource;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    private CollapsingToolbarLayout collapsing;
    private Toolbar toolbar;
    private StepperTouch stepperTouch;
    private CounterFab btnCart;
    private NotificationBadge badge;
    private RelativeLayout btnFav, includeCart;
    private ImageView ivItemImg, imgFav;
    private TextView tvItemName, tvItemPrice, tvItemDescription, tvItemSpecification;
    private ExpandableTextView etvItemDescription;

    private int quantity = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_items_detail);

        favoriteDataSource = new LocalFavoriteDataSource(RoomDBHost.getInstance(this).favoriteDAO());
        cartDataSource = new LocalCartDataSource(RoomDBHost.getInstance(this).cartDAO());

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        initViews();
        loadData();
    }

    private void initViews() {
        collapsing = findViewById(R.id.collapsing);
        toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        stepperTouch = findViewById(R.id.stepperTouch);
        btnCart = findViewById(R.id.btnCart);

        ivItemImg = findViewById(R.id.ivItemImg);
        tvItemName = findViewById(R.id.tvItemName);
        tvItemPrice = findViewById(R.id.tvItemPrice);
        tvItemDescription = findViewById(R.id.tvItemDescription);
        etvItemDescription = findViewById(R.id.etvItemDescription);
        tvItemSpecification = findViewById(R.id.tvItemSpecification);

        btnFav = findViewById(R.id.btnFav);
        imgFav = findViewById(R.id.imgFav);

        includeCart = toolbar.findViewById(R.id.includeCart);
        badge = toolbar.findViewById(R.id.badge);

        includeCart.setOnClickListener(v -> {
            startActivity(new Intent(this, HomeActivity.class));
            EventBus.getDefault().postSticky(new NavCartEvent(true));
        });

        stepperTouch.setCount(0);
        stepperTouch.setMinValue(0);
        stepperTouch.setMaxValue(999);
        stepperTouch.setSideTapEnabled(true);
        stepperTouch.addStepCallback(new OnStepCallback() {
            @Override
            public void onStep(int i, boolean b) {
                quantity = i;
            }
        });


        btnCart.setOnClickListener(v -> {
            if (quantity == 0)
                Toast.makeText(this, "Please add the number of items.", Toast.LENGTH_SHORT).show();
            else if (quantity > 0) {
                double price = Common.selectedItem.getPrice() / Common.ratesIDR;

                CartItem cartItem = new CartItem();
                cartItem.setUid(firebaseUser.getUid());
                cartItem.setUserEmail(firebaseUser.getEmail());

                cartItem.setItemId(Common.selectedItem.getId());
                cartItem.setItemName(Common.selectedItem.getName());
                cartItem.setItemImage(Common.selectedItem.getImage());
                cartItem.setItemPrice(Double.valueOf(Common.formatPrice(price)));
                cartItem.setItemQuantity(quantity);

                cartDataSource.getItemWithAllOptionsInCart(firebaseUser.getUid(),
                        cartItem.getItemId())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new SingleObserver<CartItem>() {
                            @Override
                            public void onSubscribe(Disposable d) {

                            }

                            @Override
                            public void onSuccess(CartItem cartItemFromDB) {
                                if (cartItemFromDB.equals(cartItem)) {
                                    /** Already in database, just update */
                                    cartItemFromDB.setItemQuantity(cartItemFromDB.getItemQuantity() + cartItem.getItemQuantity());

                                    cartDataSource.updateCartItems(cartItemFromDB)
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe(new SingleObserver<Integer>() {
                                                @Override
                                                public void onSubscribe(Disposable d) {

                                                }

                                                @Override
                                                public void onSuccess(Integer integer) {
                                                    Log.d("s", integer.toString());
                                                    Toast.makeText(ItemsDetailActivity.this, "Update cart.", Toast.LENGTH_SHORT).show();
                                                    EventBus.getDefault().postSticky(new CounterCartEvent(true));
                                                }

                                                @Override
                                                public void onError(Throwable e) {
                                                    new SweetAlertDialog(getBaseContext(), SweetAlertDialog.WARNING_TYPE)
                                                            .setTitleText("[UPDATE CART]")
                                                            .setContentText(e.getMessage())
                                                            .show();
                                                }
                                            });
                                } else {
                                    /** When item not available in cart, insert new data */
                                    compositeDisposable.add(cartDataSource.insertOrReplaceAll(cartItem)
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe(() -> {
                                                Toast.makeText(getBaseContext(), "Add to Cart", Toast.LENGTH_SHORT).show();
                                                EventBus.getDefault().postSticky(new CounterCartEvent(true));
                                            }, throwable -> {
                                                new SweetAlertDialog(getBaseContext(), SweetAlertDialog.ERROR_TYPE)
                                                        .setTitleText("[CART ERROR]")
                                                        .setContentText(throwable.getMessage())
                                                        .show();
                                            }));
                                }
                            }

                            @Override
                            public void onError(Throwable e) {
                                if (e.getMessage().contains("empty")) {
                                    /** Default, if Cart is empty, this code will be fired */
                                    compositeDisposable.add(cartDataSource.insertOrReplaceAll(cartItem)
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe(() -> {
                                                Toast.makeText(ItemsDetailActivity.this, "Add to cart.", Toast.LENGTH_SHORT).show();
                                                EventBus.getDefault().postSticky(new CounterCartEvent(true));
                                            }, throwable -> {

                                            }));
                                } else
                                    Log.d("[GET CART]", e.getMessage());
                            }
                        });
            }
        });

    }

    private void loadData() {
        ItemsModel itemsModel = Common.selectedItem;
        double price = itemsModel.getPrice() / Common.ratesIDR;

        getSupportActionBar().setTitle(itemsModel.getName());
        Picasso.get().load(itemsModel.getImage()).into(ivItemImg);
        tvItemName.setText(itemsModel.getName());
        tvItemPrice.setText(new StringBuilder("$ ").append(Common.formatPrice(price)).toString());
        etvItemDescription.setText(itemsModel.getDescription());
        tvItemSpecification.setText(itemsModel.getSpecification().replace("/n", "\n"));

        collapsing.setOnClickListener(v -> {
            Common.selectedItemImage = itemsModel.getImage();
            startActivity(new Intent(this, ItemDetailImagePreviewActivity.class));
            CustomIntent.customType(this, Common.Anim_Fadein_to_Fadeout);
        });


        if (favoriteDataSource.isFavorite(firebaseUser.getUid(), Common.favoriteItemId, itemsModel.getName()) == 1) {
            imgFav.setImageResource(R.drawable.ic_favorite_true);
        } else {
            imgFav.setImageResource(R.drawable.ic_favorite_false);
        }

        btnFav.setOnClickListener(v -> {
            FavoriteItem favoriteItem = new FavoriteItem();
            favoriteItem.setUid(firebaseUser.getUid());
            favoriteItem.setItemId(Common.favoriteItemId);
            favoriteItem.setItemImg(itemsModel.getImage());
            favoriteItem.setItemName(itemsModel.getName());
            favoriteItem.setItemPrice(Common.formatPrice(price));
            favoriteItem.setItemType(Common.itemType);

            if (favoriteDataSource.isFavorite(firebaseUser.getUid(), Common.favoriteItemId, itemsModel.getName()) != 1) {
                addOrRemoveFavorite(favoriteItem, true);
            } else {
                addOrRemoveFavorite(favoriteItem, false);
            }
        });
    }

    private void addOrRemoveFavorite(FavoriteItem list, boolean isAdd) {
        if (isAdd) {
            favoriteDataSource.insertFavorite(list);
            imgFav.setImageResource(R.drawable.ic_favorite_true);
            Toast.makeText(this, "Add to favorite.", Toast.LENGTH_SHORT).show();
        } else {
            favoriteDataSource.deleteFavorite(list.getUid(), list.getItemName());
            imgFav.setImageResource(R.drawable.ic_favorite_false);
            Toast.makeText(this, "Remove from favorite.", Toast.LENGTH_SHORT).show();
            EventBus.getDefault().postSticky(new RefreshFavoriteButtonEvent(true));
        }
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                CustomIntent.customType(this, Common.Anim_Right_to_Left);
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        countCartItems();
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

    @Override
    protected void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }

    @Override
    public void finish() {
        super.finish();
        CustomIntent.customType(this, Common.Anim_Right_to_Left);
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onCounterCartItems(CounterCartEvent event){
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

                        if (badge == null) return;
                        if (integer == 0) {
                            badge.setVisibility(View.INVISIBLE);
                            badge.setText(null);
                        } else {
                            badge.setVisibility(View.VISIBLE);
                            badge.setText(String.valueOf(integer));
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (e.getMessage().contains("empty")){
                            badge.setVisibility(View.INVISIBLE);
                            badge.setText(null);
                        }
                    }
                });
    }
}
