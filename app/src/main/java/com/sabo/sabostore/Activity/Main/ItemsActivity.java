package com.sabo.sabostore.Activity.Main;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ferfalk.simplesearchview.SimpleSearchView;
import com.ferfalk.simplesearchview.SimpleSearchViewListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sabo.sabostore.Adapter.ItemsAdapter;
import com.sabo.sabostore.Common.Common;
import com.sabo.sabostore.EventBus.UpdateStatusUserEvent;
import com.sabo.sabostore.Model.ItemsModel;
import com.sabo.sabostore.Model.StoreModel;
import com.sabo.sabostore.R;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import maes.tech.intentanim.CustomIntent;

public class ItemsActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private DatabaseReference storeRef;

    private Toolbar toolbar;
    private SimpleSearchView simpleSearchView;
    private RecyclerView rvItems;
    private TextView tvEmptySearch;
    private ProgressBar progressBar;

    private ItemsAdapter adapter, searchAdapter;
    private List<ItemsModel> tmpList = new ArrayList<>();
    private List<ItemsModel> searchList = new ArrayList<>();
    private List<ItemsModel> resultSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_items);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        storeRef = FirebaseDatabase.getInstance().getReference(Common.STORE_REF);

        initViews();
        loadData();
    }


    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        simpleSearchView = findViewById(R.id.searchView);
        rvItems = findViewById(R.id.rvItems);
        tvEmptySearch = findViewById(R.id.tvEmptySearch);
        progressBar = findViewById(R.id.progressBar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        rvItems.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        simpleSearchView.setOnSearchViewListener(new SimpleSearchViewListener() {
            @Override
            public void onSearchViewClosedAnimation() {
                loadData();
                tvEmptySearch.setText("");
                super.onSearchViewClosedAnimation();
            }
        });

        simpleSearchView.setOnQueryTextListener(new SimpleSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                startSearchByQuery(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                startSearchByQuery(newText);
                return true;
            }

            @Override
            public boolean onQueryTextCleared() {
                return false;
            }
        });

    }

    private void startSearchByQuery(String query) {
        resultSearch = new ArrayList<>();
        for (ItemsModel items : searchList) {
            if (items.getName().contains(query) || items.getName().toLowerCase().contains(query) ||
                    String.valueOf(items.getPrice()).contains(query))
                resultSearch.add(items);
        }

        if (resultSearch.isEmpty())
            tvEmptySearch.setText("No results found for '" + query + "'");
        else
            tvEmptySearch.setText("");


        searchAdapter = new ItemsAdapter(this, resultSearch);
        rvItems.setAdapter(searchAdapter);
    }

    private void loadData() {
        progressBar.setVisibility(View.VISIBLE);

        tmpList = Common.selectedStoreItems.getItems();

        getSupportActionBar().setTitle(Common.selectedStoreItems.getName());

        storeRef.child(Common.selectedStoreItems.getItem_id())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        StoreModel storeModel = snapshot.getValue(StoreModel.class);
                        Common.itemType = storeModel.getName();

                        searchList = tmpList;
                        adapter = new ItemsAdapter(ItemsActivity.this, tmpList);
                        rvItems.setAdapter(adapter);
                        progressBar.setVisibility(View.INVISIBLE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search, menu);

        MenuItem menuItem = menu.findItem(R.id.action_search);
        simpleSearchView.setMenuItem(menuItem);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home :
                CustomIntent.customType(this, Common.Anim_Right_to_Left);
                finish();
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
    protected void onStop() {
        tvEmptySearch.setText("");
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
        tvEmptySearch.setText("");
    }
}
