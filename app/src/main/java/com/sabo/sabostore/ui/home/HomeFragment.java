package com.sabo.sabostore.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;
import com.sabo.sabostore.API.APICurrency;
import com.sabo.sabostore.Activity.Account.AccountMenu.FavoriteActivity;
import com.sabo.sabostore.Activity.Main.SearchActivity;
import com.sabo.sabostore.Adapter.CategoriesAdapter;
import com.sabo.sabostore.Adapter.FavoriteHomeAdapter;
import com.sabo.sabostore.BottomSheet.FavoriteSheetFragment;
import com.sabo.sabostore.Common.Common;
import com.sabo.sabostore.EventBus.HideAppBarCartEvent;
import com.sabo.sabostore.EventBus.HideAppBarProfileEvent;
import com.sabo.sabostore.EventBus.BottomSheetDialogEvent;
import com.sabo.sabostore.EventBus.HideFabEvent;
import com.sabo.sabostore.EventBus.RefreshFavoriteButtonEvent;
import com.sabo.sabostore.EventBus.AllCategoriesClickEvent;
import com.sabo.sabostore.Model.ItemsModel;
import com.sabo.sabostore.Model.SliderModel;
import com.sabo.sabostore.Model.StoreModel;
import com.sabo.sabostore.R;
import com.sabo.sabostore.RoomDB.Favorite.FavoriteDataSource;
import com.sabo.sabostore.RoomDB.Favorite.FavoriteItem;
import com.sabo.sabostore.RoomDB.Favorite.LocalFavoriteDataSource;
import com.sabo.sabostore.RoomDB.RoomDBHost;
import com.squareup.picasso.Picasso;


import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import maes.tech.intentanim.CustomIntent;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private FavoriteDataSource favoriteDataSource;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private DatabaseReference rootRef;

    private FavoriteHomeAdapter favoriteHomeAdapter;

    private RelativeLayout rl_action_search, emptyFavorite;
    private TextView tvSeeAllFavorite, tvSeeAllCategories;
    private RecyclerView rvCategories, rvMostPopular, rvFavorite;
    private CategoriesAdapter categoriesAdapter;

    private APICurrency mService;
    private int lastPosition = -1;

    @Override
    public void onResume() {
        super.onResume();
//        loadPopularSlider();

        EventBus.getDefault().postSticky(new HideAppBarProfileEvent(false));
        EventBus.getDefault().postSticky(new HideAppBarCartEvent(true));
        EventBus.getDefault().postSticky(new HideFabEvent(false));

        loadCategories();
        loadMostPopular();
        loadFavorite();
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        homeViewModel = ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        rootRef = FirebaseDatabase.getInstance().getReference();
        mService = Common.getAPIExchangeRates();

        favoriteDataSource = new LocalFavoriteDataSource(RoomDBHost.getInstance(getContext()).favoriteDAO());

        initViews(root);

        /** Load Data From RoomDBHost
         * FavoriteItems
         * */
        homeViewModel.getListMutableLiveData().observe(this, favoriteItems -> {
            favoriteHomeAdapter = new FavoriteHomeAdapter(getContext(), favoriteItems);
            if (favoriteItems.isEmpty() || favoriteItems == null) {
                emptyFavorite.setVisibility(View.VISIBLE);
                tvSeeAllFavorite.setVisibility(View.GONE);
                rvFavorite.setVisibility(View.GONE);
            } else {
                emptyFavorite.setVisibility(View.GONE);
                tvSeeAllFavorite.setVisibility(View.VISIBLE);
                rvFavorite.setVisibility(View.VISIBLE);
                rvFavorite.setAdapter(favoriteHomeAdapter);
            }
        });

        return root;
    }

    private void initViews(View root) {
        rl_action_search = root.findViewById(R.id.rl_action_search);

        rvCategories = root.findViewById(R.id.rvCategories);
        rvMostPopular = root.findViewById(R.id.rvMostPopular);
        rvFavorite = root.findViewById(R.id.rvFavorite);
        emptyFavorite = root.findViewById(R.id.emptyFav);
        tvSeeAllFavorite = root.findViewById(R.id.tvSeeAllFavorite);
        tvSeeAllCategories = root.findViewById(R.id.tvSeeAllCategories);


        rvCategories.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvMostPopular.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvFavorite.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));


        rl_action_search.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), SearchActivity.class));
            CustomIntent.customType(getContext(), Common.Anim_Bottom_to_Up);
        });

        tvSeeAllCategories.setOnClickListener(v -> {
            EventBus.getDefault().postSticky(new AllCategoriesClickEvent(true));
        });

        tvSeeAllFavorite.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), FavoriteActivity.class));
            CustomIntent.customType(getContext(), Common.Anim_Fadein_to_Fadeout);
        });
    }

    private void loadCategories() {
        List<StoreModel> tempList = new ArrayList<>();
        rootRef.child(Common.STORE_REF)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            StoreModel storeModel = ds.getValue(StoreModel.class);
                            storeModel.setItem_id(ds.getKey());
                            tempList.add(storeModel);
                        }

                        categoriesAdapter = new CategoriesAdapter(getContext(), tempList);
                        rvCategories.setAdapter(categoriesAdapter);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }


    /**
     * Most Popular
     */
    private void loadMostPopular() {
        FirebaseRecyclerOptions<SliderModel> options =
                new FirebaseRecyclerOptions.Builder<SliderModel>()
                        .setQuery(rootRef.child(Common.SLIDER_REF), SliderModel.class)
                        .build();

        FirebaseRecyclerAdapter<SliderModel, MostPopularViewHolder> adapter = new FirebaseRecyclerAdapter<SliderModel, MostPopularViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull MostPopularViewHolder holder, int position, @NonNull SliderModel model) {
                Picasso.get().load(model.getImage()).into(holder.ivItemImg);
                holder.tvItemName.setText(model.getDescription());

                rootRef.child(Common.STORE_REF)
                        .child(model.getItem_id())
                        .child("items")
                        .orderByChild("id")
                        .equalTo(model.getId())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    ItemsModel itemsModel = new ItemsModel();
                                    for (DataSnapshot items : snapshot.getChildren()) {
                                        itemsModel = items.getValue(ItemsModel.class);
                                    }

                                    double resultPrice = itemsModel.getPrice() / Common.ratesIDR;

                                    holder.tvItemPrice.setText(new StringBuilder("$ ")
                                            .append(Common.formatPrice(resultPrice)).toString());
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });


                String itemId = new StringBuilder().append(model.getItem_id()).append("&").append(model.getId()).toString();

                /** Check State of Favorite or Not */
                if (favoriteDataSource.isFavorite(firebaseUser.getUid(), itemId, model.getDescription()) == 1) {
                    holder.imgFav.setImageResource(R.drawable.ic_favorite_true);
                } else {
                    holder.imgFav.setImageResource(R.drawable.ic_favorite_false);
                }

                holder.btnFav.setOnClickListener(v -> {
                    if (favoriteDataSource.isFavorite(firebaseUser.getUid(), itemId, model.getDescription()) != 1) {
                        getDataFromFirebase(model, holder, true);
                    } else {
                        getDataFromFirebase(model, holder, false);
                    }
                });


                setAnimation(holder.itemView, position);
            }

            @NonNull
            @Override
            public MostPopularViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new MostPopularViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_most_popular_layout, parent, false));
            }
        };

        rvMostPopular.setAdapter(adapter);
        adapter.startListening();
        rvMostPopular.smoothScrollToPosition(rvMostPopular.getAdapter().getItemCount());
    }

    private void getDataFromFirebase(SliderModel model, MostPopularViewHolder holder, boolean isAdd) {

        rootRef.child(Common.STORE_REF)
                .child(model.getItem_id())
                .child("items")
                .orderByChild("id")
                .equalTo(model.getId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            ItemsModel tempItems = new ItemsModel();
                            for (DataSnapshot items : snapshot.getChildren()) {
                                tempItems = items.getValue(ItemsModel.class);
                            }

                            final ItemsModel itemsModel = tempItems;
                            rootRef.child(Common.STORE_REF)
                                    .child(model.getItem_id())
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if (snapshot.exists()) {
                                                StoreModel storeModel = snapshot.getValue(StoreModel.class);

                                                addOrRemoveFavorite(model, storeModel, itemsModel, holder, isAdd);

                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void addOrRemoveFavorite(SliderModel model, StoreModel storeModel, ItemsModel itemsModel,
                                     MostPopularViewHolder holder, boolean isAdd) {


        double countPrice = itemsModel.getPrice() / Common.ratesIDR;

        String itemPrice = Common.formatPrice(countPrice);
        String itemId = new StringBuilder().append(model.getItem_id()).append("&").append(model.getId()).toString();

        /** Set Favorite */
        FavoriteItem favoriteItem = new FavoriteItem();
        favoriteItem.setUid(firebaseUser.getUid());
        favoriteItem.setItemName(model.getDescription());
        favoriteItem.setItemId(itemId); /** Separator "&" */
        favoriteItem.setItemImg(model.getImage());
        favoriteItem.setItemType(storeModel.getName());
        favoriteItem.setItemPrice(itemPrice);

        if (isAdd) {
            favoriteDataSource.insertFavorite(favoriteItem);
            Toast.makeText(getContext(), "Add to Favorite.", Toast.LENGTH_SHORT).show();
            holder.imgFav.setImageResource(R.drawable.ic_favorite_true);

        } else {
            favoriteDataSource.deleteFavorite(firebaseUser.getUid(), model.getDescription());
            Toast.makeText(getContext(), "Remove to Favorite.", Toast.LENGTH_SHORT).show();
            holder.imgFav.setImageResource(R.drawable.ic_favorite_false);
        }

    }


    public static class MostPopularViewHolder extends RecyclerView.ViewHolder {

        ImageView ivItemImg;
        TextView tvItemName, tvItemPrice;
        RelativeLayout btnFav;
        ImageView imgFav;

        public MostPopularViewHolder(@NonNull View itemView) {
            super(itemView);

            ivItemImg = itemView.findViewById(R.id.ivItemImg);
            tvItemName = itemView.findViewById(R.id.tvItemName);
            tvItemPrice = itemView.findViewById(R.id.tvItemPrice);
            btnFav = itemView.findViewById(R.id.btnFav);
            imgFav = itemView.findViewById(R.id.imgFav);
        }
    }


    /**
     * Favorite
     */

    private void loadFavorite() {
        firebaseUser = firebaseAuth.getCurrentUser();
        compositeDisposable.add(favoriteDataSource.getLimitFavorite(firebaseUser.getUid()) /** Show LIMIT 3 Show item */
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(favoriteItems -> {
                    homeViewModel.setListMutableLiveData(favoriteItems);
                }, throwable -> {
                    new SweetAlertDialog(getContext(), SweetAlertDialog.WARNING_TYPE)
                            .setTitleText("Warning!")
                            .setContentText(throwable.getMessage())
                            .show();
                    homeViewModel.setListMutableLiveData(null);
                }));
    }


    /**
     * Set Anim Slide Scroll RecyclerView
     */
    private void setAnimation(View view, int position) {
        if (position > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.slide_in_right);
            view.startAnimation(animation);
            lastPosition = position;
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void favoriteBottomSheetDialog(BottomSheetDialogEvent event) {
        FavoriteSheetFragment favoriteSheetFragment = FavoriteSheetFragment.getInstance();
        if (event.isClicked()) {
            favoriteSheetFragment.show(getChildFragmentManager(), "FavoriteSheetFragment");
            event.setClicked(false);
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void refreshFavoriteButton(RefreshFavoriteButtonEvent event) {
        if (event.isRefresh()) {
            loadMostPopular();
            event.setRefresh(false);
        }
    }


}
