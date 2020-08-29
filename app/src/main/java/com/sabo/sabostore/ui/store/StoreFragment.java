package com.sabo.sabostore.ui.store;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.lid.lib.LabelTextView;
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;
import com.sabo.sabostore.Activity.Main.ItemsDetailActivity;
import com.sabo.sabostore.Adapter.SliderAdapter;
import com.sabo.sabostore.Common.Common;
import com.sabo.sabostore.EventBus.HideAppBarCartEvent;
import com.sabo.sabostore.EventBus.HideAppBarProfileEvent;
import com.sabo.sabostore.EventBus.HideFabEvent;
import com.sabo.sabostore.Model.ItemsModel;
import com.sabo.sabostore.R;

import com.smarteist.autoimageslider.IndicatorAnimations;
import com.smarteist.autoimageslider.SliderAnimations;
import com.smarteist.autoimageslider.SliderView;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;

import maes.tech.intentanim.CustomIntent;


public class StoreFragment extends Fragment {

    private StoreViewModel storeViewModel;
    private DatabaseReference rootRef;

    private SliderView sliderView;
    private SliderAdapter sliderAdapter;
    private RecyclerView rvItems_01, rvItems_02, rvItems_03, rvItems_04, rvItems_05, rvItems_06, rvItems_07, rvItems_08;
    private SweetAlertDialog loading;
    private View viewSpace;

    private int lastPosition = -1;
    private String itemName_01, itemName_02, itemName_03, itemName_04, itemName_05, itemName_06, itemName_07, itemName_08,
            itemKey_01, itemKey_02, itemKey_03, itemKey_04, itemKey_05, itemKey_06, itemKey_07, itemKey_08,
            itemType_01, itemType_02, itemType_03, itemType_04, itemType_05, itemType_06, itemType_07, itemType_08;


    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().postSticky(new HideAppBarProfileEvent(true));
        EventBus.getDefault().postSticky(new HideAppBarCartEvent(false));
        EventBus.getDefault().postSticky(new HideFabEvent(true));

        loadPopularSlider();
        loadItems();
    }

    @Override
    public void onStart() {
        super.onStart();
        sliderView.setAutoCycle(true);
    }

    @Override
    public void onStop() {
        sliderView.setAutoCycle(false);
        super.onStop();
    }


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        storeViewModel =
                ViewModelProviders.of(this).get(StoreViewModel.class);
        View root = inflater.inflate(R.layout.fragment_store, container, false);

        rootRef = FirebaseDatabase.getInstance().getReference();

        initViews(root);

        return root;
    }

    private void initViews(View root) {
        viewSpace = root.findViewById(R.id.viewSpace);
        sliderView = root.findViewById(R.id.sliderView);
        rvItems_01 = root.findViewById(R.id.rvMouse_01);
        rvItems_02 = root.findViewById(R.id.rvMouse_02);
        rvItems_03 = root.findViewById(R.id.rvMouse_03);
        rvItems_04 = root.findViewById(R.id.rvKeyboard);
        rvItems_05 = root.findViewById(R.id.rvSpeaker_01);
        rvItems_06 = root.findViewById(R.id.rvSpeaker_02);
        rvItems_07 = root.findViewById(R.id.rvHeadset_01);
        rvItems_08 = root.findViewById(R.id.rvHeadset_02);

        sliderView.setIndicatorAnimation(IndicatorAnimations.THIN_WORM);
        sliderView.setSliderTransformAnimation(SliderAnimations.SIMPLETRANSFORMATION);
        sliderView.setAutoCycleDirection(SliderView.AUTO_CYCLE_DIRECTION_RIGHT);
        sliderView.setIndicatorSelectedColor(Color.WHITE);
        sliderView.setIndicatorUnselectedColor(Color.GRAY);
        sliderView.setScrollTimeInSec(5);
        sliderView.setAutoCycle(true);

        rvItems_01.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvItems_02.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvItems_03.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvItems_04.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvItems_05.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvItems_06.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvItems_07.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvItems_08.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
    }


    /**
     * Popular Slider
     */

    private void loadPopularSlider() {
        storeViewModel.getSliderLiveData().observe(this, sliderModels -> {
            sliderAdapter = new SliderAdapter(getContext(), sliderModels);
            sliderView.setSliderAdapter(sliderAdapter);
            sliderView.startAutoCycle();
        });
    }


    /**
     * Load All of Items
     */
    private void loadItems() {

        loading = new SweetAlertDialog(getContext(), SweetAlertDialog.PROGRESS_TYPE);
        loading.getProgressHelper().setBarColor(getResources().getColor(R.color.colorPrimary));
        loading.setTitleText("Please wait...").show();

        FirebaseRecyclerOptions<ItemsModel> options_01 =
                new FirebaseRecyclerOptions.Builder<ItemsModel>()
                        .setQuery(rootRef.child(Common.STORE_REF).child("item_01").child("items"), ItemsModel.class)
                        .build();

        FirebaseRecyclerOptions<ItemsModel> options_02 =
                new FirebaseRecyclerOptions.Builder<ItemsModel>()
                        .setQuery(rootRef.child(Common.STORE_REF).child("item_02").child("items"), ItemsModel.class)
                        .build();

        FirebaseRecyclerOptions<ItemsModel> options_03 =
                new FirebaseRecyclerOptions.Builder<ItemsModel>()
                        .setQuery(rootRef.child(Common.STORE_REF).child("item_03").child("items"), ItemsModel.class)
                        .build();

        FirebaseRecyclerOptions<ItemsModel> options_04 =
                new FirebaseRecyclerOptions.Builder<ItemsModel>()
                        .setQuery(rootRef.child(Common.STORE_REF).child("item_04").child("items"), ItemsModel.class)
                        .build();

        FirebaseRecyclerOptions<ItemsModel> options_05 =
                new FirebaseRecyclerOptions.Builder<ItemsModel>()
                        .setQuery(rootRef.child(Common.STORE_REF).child("item_05").child("items"), ItemsModel.class)
                        .build();


        FirebaseRecyclerOptions<ItemsModel> options_06 =
                new FirebaseRecyclerOptions.Builder<ItemsModel>()
                        .setQuery(rootRef.child(Common.STORE_REF).child("item_06").child("items"), ItemsModel.class)
                        .build();

        FirebaseRecyclerOptions<ItemsModel> options_07 =
                new FirebaseRecyclerOptions.Builder<ItemsModel>()
                        .setQuery(rootRef.child(Common.STORE_REF).child("item_07").child("items"), ItemsModel.class)
                        .build();


        FirebaseRecyclerOptions<ItemsModel> options_08 =
                new FirebaseRecyclerOptions.Builder<ItemsModel>()
                        .setQuery(rootRef.child(Common.STORE_REF).child("item_08").child("items"), ItemsModel.class)
                        .build();

        storeViewModel.getItemName_01().observe(this, s -> {
            if (s.contains(" ")) {
                String[] split = s.split(" ");
                String key = split[0];
                String result = split[2];
                String type = split[1] + " " + split[2];

                itemKey_01 = key;
                itemName_01 = result;
                itemType_01 = type;
            }
        });

        storeViewModel.getItemName_02().observe(this, s -> {
            if (s.contains(" ")) {
                String[] split = s.split(" ");
                String key = split[0];
                String result = split[2];
                String type = split[1] + " " + split[2];

                itemKey_02 = key;
                itemName_02 = result;
                itemType_02 = type;
            }
        });

        storeViewModel.getItemName_03().observe(this, s -> {
            if (s.contains(" ")) {
                String[] split = s.split(" ");
                String key = split[0];
                String result = split[2];
                String type = split[1] + " " + split[2];

                itemKey_03 = key;
                itemName_03 = result;
                itemType_03 = type;
            }
        });

        storeViewModel.getItemName_04().observe(this, s -> {
            if (s.contains(" ")) {
                String[] split = s.split(" ");
                String key = split[0];
                String result = split[1];
                String type = split[1];

                itemKey_04 = key;
                itemName_04 = result;
                itemType_04 = type;

            }
        });

        storeViewModel.getItemName_05().observe(this, s -> {
            if (s.contains(" ")) {
                String[] split = s.split(" ");
                String key = split[0];
                String result = split[2];
                String type = split[1] + " " + split[2];

                itemKey_05 = key;
                itemName_05 = result;
                itemType_05 = type;
            }
        });

        storeViewModel.getItemName_06().observe(this, s -> {
            if (s.contains(" ")) {
                String[] split = s.split(" ");
                String key = split[0];
                String result = split[2];
                String type = split[1] + " " + split[2];

                itemKey_06 = key;
                itemName_06 = result;
                itemType_06 = type;
            }
        });

        storeViewModel.getItemName_07().observe(this, s -> {
            if (s.contains(" ")) {
                String[] split = s.split(" ");
                String key = split[0];
                String result = split[2];
                String type = split[1] + " " + split[2];

                itemKey_07 = key;
                itemName_07 = result;
                itemType_07 = type;
            }
        });

        storeViewModel.getItemName_08().observe(this, s -> {
            if (s.contains(" ")) {
                String[] split = s.split(" ");
                String key = split[0];
                String result = split[2];
                String type = split[1] + " " + split[2];

                itemKey_08 = key;
                itemName_08 = result;
                itemType_08 = type;
            }
        });

        FirebaseRecyclerAdapter<ItemsModel, ItemsViewHolder> adapter_01 = new FirebaseRecyclerAdapter<ItemsModel, ItemsViewHolder>(options_01) {
            @Override
            protected void onBindViewHolder(@NonNull ItemsViewHolder holder, int position, @NonNull ItemsModel model) {
                double resultPrice = model.getPrice() / Common.ratesIDR;

                Picasso.get().load(model.getImage()).into(holder.ivItemImg);
                holder.tvItemName.setText(model.getName());
                holder.tvItemPrice.setText(new StringBuilder().append("$ ")
                        .append(Common.formatPrice(resultPrice)).toString());

                holder.ltvItemType.setLabelText(itemName_01);

                /** Item_id (item_01...etc)
                 * itemKey_01
                 */
                holder.rlItemView.setOnClickListener(v -> {
                    Common.selectedItem = model;
                    Common.itemType = itemType_01;
                    Common.favoriteItemId = itemKey_01+"&"+model.getId();
                    startActivity(new Intent(getContext(), ItemsDetailActivity.class));
                    CustomIntent.customType(getContext(), Common.Anim_Left_to_Right);
                });

                setAnimation(holder.itemView, position);
            }

            @NonNull
            @Override
            public ItemsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new ItemsViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_store_layout, parent, false));
            }
        };

        FirebaseRecyclerAdapter<ItemsModel, ItemsViewHolder> adapter_02 = new FirebaseRecyclerAdapter<ItemsModel, ItemsViewHolder>(options_02) {
            @Override
            protected void onBindViewHolder(@NonNull ItemsViewHolder holder, int position, @NonNull ItemsModel model) {
                double resultPrice = model.getPrice() / Common.ratesIDR;

                Picasso.get().load(model.getImage()).into(holder.ivItemImg);
                holder.tvItemName.setText(model.getName());
                holder.tvItemPrice.setText(new StringBuilder().append("$ ")
                        .append(Common.formatPrice(resultPrice)).toString());

                holder.ltvItemType.setLabelText(itemName_02);

                /** Item_id (item_02...etc)
                 * itemKey_02
                 */
                holder.rlItemView.setOnClickListener(v -> {
                    Common.selectedItem = model;
                    Common.itemType = itemType_02;
                    Common.favoriteItemId = itemKey_02+"&"+model.getId();
                    startActivity(new Intent(getContext(), ItemsDetailActivity.class));
                    CustomIntent.customType(getContext(), Common.Anim_Left_to_Right);
                });

                setAnimation(holder.itemView, position);
            }

            @NonNull
            @Override
            public ItemsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new ItemsViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_store_layout, parent, false));
            }
        };

        FirebaseRecyclerAdapter<ItemsModel, ItemsViewHolder> adapter_03 = new FirebaseRecyclerAdapter<ItemsModel, ItemsViewHolder>(options_03) {
            @Override
            protected void onBindViewHolder(@NonNull ItemsViewHolder holder, int position, @NonNull ItemsModel model) {
                double resultPrice = model.getPrice() / Common.ratesIDR;

                Picasso.get().load(model.getImage()).into(holder.ivItemImg);
                holder.tvItemName.setText(model.getName());
                holder.tvItemPrice.setText(new StringBuilder().append("$ ")
                        .append(Common.formatPrice(resultPrice)).toString());

                holder.ltvItemType.setLabelText(itemName_03);

                /** Item_id (item_03...etc)
                 * itemKey_03
                 */
                holder.rlItemView.setOnClickListener(v -> {
                    Common.selectedItem = model;
                    Common.itemType = itemType_03;
                    Common.favoriteItemId = itemKey_03+"&"+model.getId();
                    startActivity(new Intent(getContext(), ItemsDetailActivity.class));
                    CustomIntent.customType(getContext(), Common.Anim_Left_to_Right);
                });

                setAnimation(holder.itemView, position);
            }

            @NonNull
            @Override
            public ItemsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new ItemsViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_store_layout, parent, false));
            }
        };

        FirebaseRecyclerAdapter<ItemsModel, ItemsViewHolder> adapter_04 = new FirebaseRecyclerAdapter<ItemsModel, ItemsViewHolder>(options_04) {
            @Override
            protected void onBindViewHolder(@NonNull ItemsViewHolder holder, int position, @NonNull ItemsModel model) {
                double resultPrice = model.getPrice() / Common.ratesIDR;

                Picasso.get().load(model.getImage()).into(holder.ivItemImg);
                holder.tvItemName.setText(model.getName());
                holder.tvItemPrice.setText(new StringBuilder().append("$ ")
                        .append(Common.formatPrice(resultPrice)).toString());

                holder.ltvItemType.setLabelText(itemName_04);

                /** Item_id (item_04...etc)*/
                model.setKey(itemKey_04);

                holder.rlItemView.setOnClickListener(v -> {
                    Common.selectedItem = model;
                    Common.itemType = itemType_04;
                    Common.favoriteItemId = itemKey_04+"&"+model.getId();
                    startActivity(new Intent(getContext(), ItemsDetailActivity.class));
                    CustomIntent.customType(getContext(), Common.Anim_Left_to_Right);
                });

                setAnimation(holder.itemView, position);
            }

            @NonNull
            @Override
            public ItemsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new ItemsViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_store_layout, parent, false));
            }
        };

        FirebaseRecyclerAdapter<ItemsModel, ItemsViewHolder> adapter_05 = new FirebaseRecyclerAdapter<ItemsModel, ItemsViewHolder>(options_05) {
            @Override
            protected void onBindViewHolder(@NonNull ItemsViewHolder holder, int position, @NonNull ItemsModel model) {
                double resultPrice = model.getPrice() / Common.ratesIDR;

                Picasso.get().load(model.getImage()).into(holder.ivItemImg);
                holder.tvItemName.setText(model.getName());
                holder.tvItemPrice.setText(new StringBuilder().append("$ ")
                        .append(Common.formatPrice(resultPrice)).toString());

                holder.ltvItemType.setLabelText(itemName_05);

                /** Item_id (item_05...etc)
                 * itemKey_05
                 */
                holder.rlItemView.setOnClickListener(v -> {
                    Common.selectedItem = model;
                    Common.itemType = itemType_05;
                    Common.favoriteItemId = itemKey_05+"&"+model.getId();
                    startActivity(new Intent(getContext(), ItemsDetailActivity.class));
                    CustomIntent.customType(getContext(), Common.Anim_Left_to_Right);
                });

                setAnimation(holder.itemView, position);
            }

            @NonNull
            @Override
            public ItemsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new ItemsViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_store_layout, parent, false));
            }
        };

        FirebaseRecyclerAdapter<ItemsModel, ItemsViewHolder> adapter_06 = new FirebaseRecyclerAdapter<ItemsModel, ItemsViewHolder>(options_06) {
            @Override
            protected void onBindViewHolder(@NonNull ItemsViewHolder holder, int position, @NonNull ItemsModel model) {
                double resultPrice = model.getPrice() / Common.ratesIDR;

                Picasso.get().load(model.getImage()).into(holder.ivItemImg);
                holder.tvItemName.setText(model.getName());
                holder.tvItemPrice.setText(new StringBuilder().append("$ ")
                        .append(Common.formatPrice(resultPrice)).toString());

                holder.ltvItemType.setLabelText(itemName_06);

                /** Item_id (item_06...etc)
                 * itemKey_06
                 */
                holder.rlItemView.setOnClickListener(v -> {
                    Common.selectedItem = model;
                    Common.itemType = itemType_06;
                    Common.favoriteItemId = itemKey_06+"&"+model.getId();
                    startActivity(new Intent(getContext(), ItemsDetailActivity.class));
                    CustomIntent.customType(getContext(), Common.Anim_Left_to_Right);
                });

                setAnimation(holder.itemView, position);
            }

            @NonNull
            @Override
            public ItemsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new ItemsViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_store_layout, parent, false));
            }
        };

        FirebaseRecyclerAdapter<ItemsModel, ItemsViewHolder> adapter_07 = new FirebaseRecyclerAdapter<ItemsModel, ItemsViewHolder>(options_07) {
            @Override
            protected void onBindViewHolder(@NonNull ItemsViewHolder holder, int position, @NonNull ItemsModel model) {
                double resultPrice = model.getPrice() / Common.ratesIDR;

                Picasso.get().load(model.getImage()).into(holder.ivItemImg);
                holder.tvItemName.setText(model.getName());
                holder.tvItemPrice.setText(new StringBuilder().append("$ ")
                        .append(Common.formatPrice(resultPrice)).toString());

                holder.ltvItemType.setLabelText(itemName_07);

                /** Item_id (item_07...etc)
                 * itemKey_07
                 */
                holder.rlItemView.setOnClickListener(v -> {
                    Common.selectedItem = model;
                    Common.itemType = itemType_07;
                    Common.favoriteItemId = itemKey_07+"&"+model.getId();
                    startActivity(new Intent(getContext(), ItemsDetailActivity.class));
                    CustomIntent.customType(getContext(), Common.Anim_Left_to_Right);
                });

                setAnimation(holder.itemView, position);
            }

            @NonNull
            @Override
            public ItemsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new ItemsViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_store_layout, parent, false));
            }
        };

        FirebaseRecyclerAdapter<ItemsModel, ItemsViewHolder> adapter_08 = new FirebaseRecyclerAdapter<ItemsModel, ItemsViewHolder>(options_08) {
            @Override
            protected void onBindViewHolder(@NonNull ItemsViewHolder holder, int position, @NonNull ItemsModel model) {
                double resultPrice = model.getPrice() / Common.ratesIDR;

                Picasso.get().load(model.getImage()).into(holder.ivItemImg);
                holder.tvItemName.setText(model.getName());
                holder.tvItemPrice.setText(new StringBuilder().append("$ ")
                        .append(Common.formatPrice(resultPrice)).toString());

                holder.ltvItemType.setLabelText(itemName_08);

                /** Item_id (item_08...etc)
                 * itemKey_08
                 */
                holder.rlItemView.setOnClickListener(v -> {
                    Common.selectedItem = model;
                    Common.itemType = itemType_08;
                    Common.favoriteItemId = itemKey_08+"&"+model.getId();
                    startActivity(new Intent(getContext(), ItemsDetailActivity.class));
                    CustomIntent.customType(getContext(), Common.Anim_Left_to_Right);
                });

                setAnimation(holder.itemView, position);
            }

            @NonNull
            @Override
            public ItemsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new ItemsViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_store_layout, parent, false));
            }
        };

        new Handler().postDelayed(() -> {
            rvItems_01.setAdapter(adapter_01);
            adapter_01.startListening();
            rvItems_01.smoothScrollToPosition(rvItems_01.getAdapter().getItemCount());

            rvItems_02.setAdapter(adapter_02);
            adapter_02.startListening();
            rvItems_02.smoothScrollToPosition(rvItems_02.getAdapter().getItemCount());

            rvItems_03.setAdapter(adapter_03);
            adapter_03.startListening();
            rvItems_03.smoothScrollToPosition(rvItems_03.getAdapter().getItemCount());

            rvItems_04.setAdapter(adapter_04);
            adapter_04.startListening();
            rvItems_04.smoothScrollToPosition(rvItems_04.getAdapter().getItemCount());

            rvItems_05.setAdapter(adapter_05);
            adapter_05.startListening();
            rvItems_05.smoothScrollToPosition(rvItems_05.getAdapter().getItemCount());

            rvItems_06.setAdapter(adapter_06);
            adapter_06.startListening();
            rvItems_06.smoothScrollToPosition(rvItems_06.getAdapter().getItemCount());

            rvItems_07.setAdapter(adapter_07);
            adapter_07.startListening();
            rvItems_07.smoothScrollToPosition(rvItems_07.getAdapter().getItemCount());

            rvItems_08.setAdapter(adapter_08);
            adapter_08.startListening();
            rvItems_08.smoothScrollToPosition(rvItems_08.getAdapter().getItemCount());
        },10);

        new Handler().postDelayed(() -> {

            loading.dismissWithAnimation();
            viewSpace.setVisibility(View.GONE);
        }, 1000);

    }

    public static class ItemsViewHolder extends RecyclerView.ViewHolder {

        RelativeLayout rlItemView;
        LabelTextView ltvItemType;
        TextView tvItemName, tvItemPrice;
        ImageView ivItemImg;

        public ItemsViewHolder(@NonNull View itemView) {
            super(itemView);

            rlItemView = itemView.findViewById(R.id.rlItemView);
            tvItemName = itemView.findViewById(R.id.tvItemName);
            tvItemPrice = itemView.findViewById(R.id.tvItemPrice);
            ltvItemType = itemView.findViewById(R.id.ltvItemType);
            ivItemImg = itemView.findViewById(R.id.ivItemImg);
        }
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

}
