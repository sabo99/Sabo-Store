package com.sabo.sabostore.Activity.Account;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sabo.sabostore.Activity.Account.AccountMenu.AccountSettings.AccountSettingsActivity;
import com.sabo.sabostore.Activity.Account.AccountMenu.FavoriteActivity;
import com.sabo.sabostore.Activity.Account.AccountMenu.OrderHistoryActivity;
import com.sabo.sabostore.Activity.Account.AccountMenu.Profile.ProfileActivity;
import com.sabo.sabostore.Common.Common;
import com.sabo.sabostore.Common.Preferences;
import com.sabo.sabostore.Model.UserModel;
import com.sabo.sabostore.R;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;
import maes.tech.intentanim.CustomIntent;

public class AccountActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private DatabaseReference userRef;

    private LinearLayout llProfile, llAccount, llFavorite, llHistoryPurchase;
    private CircleImageView civProfilePhoto;
    private TextView tvName, tvEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        userRef = FirebaseDatabase.getInstance().getReference(Common.USER_REF);

        initViews();

    }

    private void initViews() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Account");

        llProfile = findViewById(R.id.llProfile);
        llAccount = findViewById(R.id.llAccountSettings);
        llFavorite = findViewById(R.id.llFavorite);
        llHistoryPurchase = findViewById(R.id.llOrderView);

        civProfilePhoto = findViewById(R.id.civProfilePhoto);
        tvName = findViewById(R.id.tvProfileName);
        tvEmail = findViewById(R.id.tvProfileEmail);

        llProfile.setOnClickListener(v -> {
            startActivity(new Intent(AccountActivity.this, ProfileActivity.class));
            CustomIntent.customType(this, Common.Anim_Fadein_to_Fadeout);
        });

        llAccount.setOnClickListener(v -> {
            startActivity(new Intent(AccountActivity.this, AccountSettingsActivity.class));
            CustomIntent.customType(this, Common.Anim_Left_to_Right);
        });

        llFavorite.setOnClickListener(v -> {
            startActivity(new Intent(AccountActivity.this, FavoriteActivity.class));
            CustomIntent.customType(this, Common.Anim_Left_to_Right);
        });

        llHistoryPurchase.setOnClickListener(v -> {
            startActivity(new Intent(AccountActivity.this, OrderHistoryActivity.class));
            CustomIntent.customType(this, Common.Anim_Left_to_Right);
        });

        /** User profile */
        retrieveData();

    }

    private void retrieveData() {
        tvEmail.setText(Preferences.getEmail(getBaseContext()));

        userRef.child(firebaseUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            UserModel userModel = snapshot.getValue(UserModel.class);

                            if (userModel.getImage().equals(""))
                                Picasso.get().load(R.drawable.no_profile).into(civProfilePhoto);
                            else
                                Picasso.get().load(userModel.getImage()).placeholder(R.drawable.no_profile).into(civProfilePhoto);

                            tvName.setText(userModel.getName());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
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
    protected void onResume() {
        super.onResume();
        retrieveData();
    }
}
