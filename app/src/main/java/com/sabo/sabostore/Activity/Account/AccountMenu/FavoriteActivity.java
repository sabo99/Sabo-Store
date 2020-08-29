package com.sabo.sabostore.Activity.Account.AccountMenu;

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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ferfalk.simplesearchview.SimpleSearchView;
import com.ferfalk.simplesearchview.SimpleSearchViewListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;
import com.sabo.sabostore.Adapter.FavoriteHomeAdapter;
import com.sabo.sabostore.Adapter.FavoriteListAdapter;
import com.sabo.sabostore.Common.Common;
import com.sabo.sabostore.R;
import com.sabo.sabostore.RoomDB.Favorite.FavoriteDataSource;
import com.sabo.sabostore.RoomDB.Favorite.FavoriteItem;
import com.sabo.sabostore.RoomDB.Favorite.LocalFavoriteDataSource;
import com.sabo.sabostore.RoomDB.RoomDBHost;

import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import maes.tech.intentanim.CustomIntent;

public class FavoriteActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private FavoriteDataSource favoriteDataSource;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    private Toolbar toolbar;
    private SimpleSearchView simpleSearchView;
    private RecyclerView rvFavorite;
    private FavoriteListAdapter adapter, searchAdapter;
    private RelativeLayout emptyFav;
    private TextView tvEmptySearch;
    private ProgressBar progressBar;

    private List<FavoriteItem> tmpList = new ArrayList<>();
    private List<FavoriteItem> searchList = new ArrayList<>();
    private List<FavoriteItem> resultSearch;

    @Override
    protected void onResume() {
        super.onResume();

        loadFavoriteList();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        favoriteDataSource = new LocalFavoriteDataSource(RoomDBHost.getInstance(FavoriteActivity.this).favoriteDAO());

        initViews();

        progressBar.setVisibility(View.VISIBLE);
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        simpleSearchView = findViewById(R.id.searchView);
        rvFavorite = findViewById(R.id.rvFavorite);
        emptyFav = findViewById(R.id.emptyFav);
        tvEmptySearch = findViewById(R.id.tvEmptySearch);
        progressBar = findViewById(R.id.progressBar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Favorite");


        rvFavorite.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));


        simpleSearchView.setOnSearchViewListener(new SimpleSearchViewListener() {
            @Override
            public void onSearchViewClosedAnimation() {
                progressBar.setVisibility(View.VISIBLE);
                loadFavoriteList();
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
        for (FavoriteItem items : searchList) {
            if (items.getItemName().contains(query) || items.getItemName().toLowerCase().contains(query) ||
                    items.getItemType().contains(query) || items.getItemType().toLowerCase().contains(query) ||
                    String.valueOf(items.getItemPrice()).contains(query))
                resultSearch.add(items);
        }

        if (resultSearch.isEmpty()) {
            emptyFav.setVisibility(View.GONE);
            tvEmptySearch.setText("No results found for '" + query + "'");
        } else {
            tvEmptySearch.setText("");
        }


        searchAdapter = new FavoriteListAdapter(FavoriteActivity.this, resultSearch);
        rvFavorite.setAdapter(searchAdapter);
    }

    private void loadFavoriteList() {
        compositeDisposable.add(favoriteDataSource.getAllFavorite(firebaseUser.getUid())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(favoriteItems -> {
                    tmpList = favoriteItems;
                    searchList = favoriteItems;
                    adapter = new FavoriteListAdapter(this, tmpList);
                    if (tmpList.isEmpty() || tmpList == null) {
                        rvFavorite.setVisibility(View.GONE);
                        emptyFav.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.GONE);
                    } else {
                        rvFavorite.setVisibility(View.VISIBLE);
                        emptyFav.setVisibility(View.GONE);
                        new Handler().postDelayed(() -> {
                            progressBar.setVisibility(View.GONE);
                            rvFavorite.setAdapter(adapter);
                        }, 500);

                    }
                }, throwable -> {
                    new SweetAlertDialog(FavoriteActivity.this, SweetAlertDialog.WARNING_TYPE)
                            .setTitleText("Warning!")
                            .setContentText(throwable.getMessage())
                            .show();
                    tmpList = null;

                    progressBar.setVisibility(View.GONE);
                }));
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
        switch (item.getItemId()) {
            case android.R.id.home:
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
    public void onBackPressed() {
        if (simpleSearchView.onBackPressed()) {
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onStop() {
        super.onStop();
        simpleSearchView.closeSearch();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
    }
}
