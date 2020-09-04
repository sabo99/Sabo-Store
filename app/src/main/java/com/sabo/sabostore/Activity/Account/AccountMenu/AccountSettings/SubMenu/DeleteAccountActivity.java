package com.sabo.sabostore.Activity.Account.AccountMenu.AccountSettings.SubMenu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;
import com.sabo.sabostore.Activity.SignInActivity;
import com.sabo.sabostore.Activity.SplashScreenActivity;
import com.sabo.sabostore.Common.Common;
import com.sabo.sabostore.Common.Preferences;
import com.sabo.sabostore.EventBus.CounterCartEvent;
import com.sabo.sabostore.R;
import com.sabo.sabostore.RoomDB.Cart.CartDataSource;
import com.sabo.sabostore.RoomDB.Cart.LocalCartDataSource;
import com.sabo.sabostore.RoomDB.Favorite.FavoriteDataSource;
import com.sabo.sabostore.RoomDB.Favorite.LocalFavoriteDataSource;
import com.sabo.sabostore.RoomDB.RoomDBHost;

import org.greenrobot.eventbus.EventBus;

import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import maes.tech.intentanim.CustomIntent;

public class DeleteAccountActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private DatabaseReference userRef, orderRef;
    private StorageReference storageRef;
    private FavoriteDataSource favoriteDataSource;
    private CartDataSource cartDataSource;

    private TextInputLayout tilEmail, tilPassword;
    private EditText etEmail, etPassword;
    private Button btnDeleteAccount;
    private ProgressBar progressBar;
    private String email, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_account);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        userRef = FirebaseDatabase.getInstance().getReference(Common.USER_REF).child(firebaseUser.getUid());
        orderRef = FirebaseDatabase.getInstance().getReference(Common.ORDER_REF);
        storageRef = FirebaseStorage.getInstance().getReference()
                .child(Common.USER_REF + "/" + firebaseUser.getUid() + "/profile.png");

        cartDataSource = new LocalCartDataSource(RoomDBHost.getInstance(this).cartDAO());
        favoriteDataSource = new LocalFavoriteDataSource(RoomDBHost.getInstance(this).favoriteDAO());

        initViews();
    }

    private void initViews() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Delete my account");

        progressBar = findViewById(R.id.progressBar);
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnDeleteAccount = findViewById(R.id.btnDeleteAccount);

        etEmail.setText(Preferences.getEmail(getBaseContext()));
        etEmail.setEnabled(false);

        btnDeleteAccount.setOnClickListener(v -> {
            email = etEmail.getText().toString();
            password = etPassword.getText().toString();

            if (password.length() < 0 || TextUtils.isEmpty(password)) {
                tilPassword.setHelperTextEnabled(true);
                tilPassword.setHelperText("Required!");
            } else {
                tilPassword.setHelperTextEnabled(false);
                new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                        .setTitleText("Are you sure?")
                        .setContentText("You won't be able to revert this account!")
                        .showCancelButton(true)
                        .setCancelText("No, cancel!")
                        .setCancelClickListener(sweetAlertDialog -> {
                            sweetAlertDialog.dismiss();
                            etPassword.setText("");
                            etPassword.requestFocus();
                        })
                        .setConfirmText("Yes, delete it!")
                        .setConfirmClickListener(sweetAlertDialog -> {
                            btnDeleteAccount.setEnabled(false);
                            progressBar.setVisibility(View.VISIBLE);
                            sweetAlertDialog.dismiss();

                            SweetAlertDialog loading = new SweetAlertDialog(DeleteAccountActivity.this, SweetAlertDialog.PROGRESS_TYPE);
                            loading.getProgressHelper().setBarColor(getResources().getColor(R.color.colorPrimary));
                            loading.setTitleText("Please wait...").show();

                            AuthCredential credential = EmailAuthProvider.getCredential(email, password);

                            firebaseUser.reauthenticate(credential)
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            btnDeleteAccount.setEnabled(true);
                                            progressBar.setVisibility(View.INVISIBLE);

                                            deleteAllOfAccount(loading);
                                        } else {
                                            sweetAlertDialog.dismiss();
                                            loading.dismissWithAnimation();
                                            btnDeleteAccount.setEnabled(true);
                                            progressBar.setVisibility(View.INVISIBLE);
                                            new SweetAlertDialog(DeleteAccountActivity.this, SweetAlertDialog.WARNING_TYPE)
                                                    .setTitleText("Oops!")
                                                    .setContentText(task.getException().getMessage())
                                                    .show();
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        sweetAlertDialog.dismiss();
                                        loading.dismissWithAnimation();
                                        btnDeleteAccount.setEnabled(true);
                                        progressBar.setVisibility(View.INVISIBLE);
                                        Log.d("e", e.getMessage());
                                    });
                        })
                        .show();

            }
        });
    }

    private void deleteAllOfAccount(SweetAlertDialog loading) {
        /** Remove Image Storage */
        if (storageRef != null)
            storageRef.delete();

        /** Remove All UserInformation */
        userRef.removeValue();
        orderRef.orderByChild("userUID").equalTo(firebaseUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            ds.getRef().removeValue();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        loading.dismiss();

        /** Clear All Favorite & Cart */
        favoriteDataSource.clearAllFavorite(firebaseUser.getUid());
        cartDataSource.cleanCart(firebaseUser.getUid()).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Integer>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(Integer integer) {
                        EventBus.getDefault().postSticky(new CounterCartEvent(true));
                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                });


        /** Remove Firebase Auth User */
        firebaseAuth.getCurrentUser().delete()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        /** Firebase SignOut */
                        firebaseAuth.signOut();

                        /** Remove All References */
                        Preferences.clearPreferences(getBaseContext());

                        loading.dismiss();
                        new SweetAlertDialog(DeleteAccountActivity.this, SweetAlertDialog.SUCCESS_TYPE)
                                .setTitleText("Success!")
                                .setContentText("Your account has been delete!")
                                .setConfirmClickListener(sweetAlertDialog -> {
                                    sweetAlertDialog.dismiss();
                                    Intent i = new Intent(DeleteAccountActivity.this, SplashScreenActivity.class);
                                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(i);
                                    finish();
                                }).show();
                    } else {
                        loading.dismiss();
                        Log.d("e", task.getException().getMessage());
                    }

                });
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
}
