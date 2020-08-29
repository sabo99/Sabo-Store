package com.sabo.sabostore.Activity.Account.AccountMenu.AccountSettings.SubMenu;

import androidx.appcompat.app.AppCompatActivity;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;
import com.sabo.sabostore.Common.Common;
import com.sabo.sabostore.Common.Preferences;
import com.sabo.sabostore.R;

import java.util.HashMap;
import java.util.Map;

import maes.tech.intentanim.CustomIntent;

public class ChangeEmailActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private DatabaseReference userRef;

    private LinearLayout llState1, llState2;
    private ImageButton btnBack;
    private Button btnNext, btnChange;
    private ProgressBar progressBar;
    private TextInputLayout tilOldEmail,  tilNewEmail;
    private EditText etOldEmail, etNewEmail;
    private String oldEmail, newEmail;
    private int state = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_email);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        userRef = FirebaseDatabase.getInstance().getReference(Common.USER_REF).child(firebaseUser.getUid());

        initViews();
    }

    private void initViews() {

        btnBack = findViewById(R.id.btnBack);
        llState1 = findViewById(R.id.llState1);
        llState2 = findViewById(R.id.llState2);

        progressBar = findViewById(R.id.progressBar);
        tilOldEmail = findViewById(R.id.tilOldEmail);
        tilNewEmail = findViewById(R.id.tilNewEmail);
        etOldEmail = findViewById(R.id.etOldEmail);
        etNewEmail = findViewById(R.id.etNewEmail);
        btnNext = findViewById(R.id.btnNext);
        btnChange = findViewById(R.id.btnChange);

        if (state == 0) {
            llState1.setVisibility(View.VISIBLE);
            llState2.setVisibility(View.INVISIBLE);
        }
        if (state == 1) {
            llState1.setVisibility(View.INVISIBLE);
            llState2.setVisibility(View.VISIBLE);
        }


        btnBack.setOnClickListener(v -> {
            if (state == 0) {
                CustomIntent.customType(this, Common.Anim_Right_to_Left);
                finish();
            }
            if (state == 1) {
                Animation animation = AnimationUtils.loadAnimation(this, R.anim.slide_to_right);
                llState2.setAnimation(animation);
                llState2.setVisibility(View.INVISIBLE);

                new Handler().postDelayed(() -> {
                    Animation animation1 = AnimationUtils.loadAnimation(this, R.anim.slide_from_left_to_right);
                    llState1.setVisibility(View.VISIBLE);
                    llState1.setAnimation(animation1);
                    state = 0;
                }, 50);
            }
        });

        btnNext.setOnClickListener(v -> {
            oldEmail = etOldEmail.getText().toString();

            if (TextUtils.isEmpty(oldEmail)) {
                tilOldEmail.setHelperTextEnabled(true);
                tilOldEmail.setHelperText("Please fill out this field.");
                tilOldEmail.setHelperTextColor(ColorStateList.valueOf(getResources().getColor(R.color.colorWarning)));
            } else if (!Patterns.EMAIL_ADDRESS.matcher(oldEmail).matches()) {
                tilOldEmail.setHelperTextEnabled(false);
                etOldEmail.setError("Email format is wrong!");
            } else {
                btnNext.setEnabled(false);
                tilOldEmail.setHelperTextEnabled(false);
                progressBar.setVisibility(View.VISIBLE);

                AuthCredential credential = EmailAuthProvider.getCredential(oldEmail, Preferences.getPassword(getBaseContext()));
                firebaseUser.reauthenticate(credential)
                        .addOnSuccessListener(aVoid -> {
                            btnNext.setEnabled(true);
                            progressBar.setVisibility(View.GONE);
                            nextStep(oldEmail);
                        })
                        .addOnFailureListener(e -> {
                            btnNext.setEnabled(true);
                            progressBar.setVisibility(View.GONE);
                            new SweetAlertDialog(ChangeEmailActivity.this, SweetAlertDialog.WARNING_TYPE)
                                    .setTitleText("Oops!")
                                    .setContentText(e.getMessage())
                                    .show();
                            etOldEmail.requestFocus();

                            /** If onFailure reauthenticate FirebaseAuth has sign out, u can add this to fix null FirebaseAuth */
                            AuthCredential signIn = EmailAuthProvider.getCredential(Preferences.getEmail(getBaseContext()), Preferences.getPassword(getBaseContext()));
                            firebaseAuth.signInWithCredential(signIn);
                        });
            }

        });

    }

    /** Next Step... Input new Email */
    private void nextStep(String oldEmail) {
        etNewEmail.setText("");

        Animation animation = AnimationUtils.loadAnimation(ChangeEmailActivity.this, R.anim.slide_to_left);
        llState1.startAnimation(animation);
        llState1.setVisibility(View.INVISIBLE);

        new Handler().postDelayed(() -> {
            progressBar.setVisibility(View.GONE);
            Animation animation1 = AnimationUtils.loadAnimation(ChangeEmailActivity.this, R.anim.slide_from_right_to_left);
            llState2.setVisibility(View.VISIBLE);
            llState2.setAnimation(animation1);
            state = 1;
        }, 50);


        btnChange.setOnClickListener(v -> {
            newEmail = etNewEmail.getText().toString();
            if (TextUtils.isEmpty(newEmail)) {
                tilNewEmail.setHelperTextEnabled(true);
                tilNewEmail.setHelperText("Please fill out this field.");
                tilNewEmail.setHelperTextColor(ColorStateList.valueOf(getResources().getColor(R.color.colorWarning)));
            } else if (!Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
                tilNewEmail.setErrorEnabled(false);
                etOldEmail.setError("Email format is wrong!");
            } else if (newEmail.equals(oldEmail)) {
                new SweetAlertDialog(ChangeEmailActivity.this, SweetAlertDialog.WARNING_TYPE)
                        .setTitleText("Oops!")
                        .setContentText("You cannot change with the same email.")
                        .show();
            } else {
                new SweetAlertDialog(ChangeEmailActivity.this, SweetAlertDialog.WARNING_TYPE)
                        .setTitleText("Are you sure?")
                        .setContentText("Change new email.")
                        .showCancelButton(true)
                        .setCancelText("No")
                        .setCancelClickListener(sweetAlertDialog -> {
                            btnChange.setEnabled(true);
                            sweetAlertDialog.dismissWithAnimation();
                            sweetAlertDialog.dismiss();
                            progressBar.setVisibility(View.GONE);
                        })
                        .setConfirmText("Yes")
                        .setConfirmClickListener(sweetAlertDialog -> {
                            btnChange.setEnabled(false);

                            sweetAlertDialog.dismiss();
                            SweetAlertDialog loading = new SweetAlertDialog(ChangeEmailActivity.this, SweetAlertDialog.PROGRESS_TYPE);
                            loading.getProgressHelper().setBarColor(getResources().getColor(R.color.colorPrimary));
                            loading.setTitleText("Please wait...").show();

                            /** Handler to delay Post in 1000 millisecond (1 second) */
                            new Handler().postDelayed(() -> {
                                updateEmailUser(newEmail, loading);
                            }, 1000);
                        })
                        .show();
            }
        });
    }

    /** Update Email User in :
     *  - Authentication Firebase with Email&Password (only Email is updated)
     *  - FirebaseDatabase -> Database Realtime
     *  - SharedPreference -> SF_EMAIL
     * @param newEmail
     * @param loading
     */
    private void updateEmailUser(String newEmail, SweetAlertDialog loading) {
        firebaseUser.updateEmail(newEmail)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Map<String, Object> updateEmail = new HashMap<>();
                        updateEmail.put(Common.KEY_EMAIL, newEmail);

                        userRef.updateChildren(updateEmail)
                                .addOnSuccessListener(aVoid -> {
                                    btnChange.setEnabled(true);
                                    etNewEmail.setText("");

                                    loading.dismissWithAnimation();
                                    loading.dismiss();
                                    progressBar.setVisibility(View.GONE);

                                    /** Update SharePreference EMAIL */
                                    Preferences.setEmail(getBaseContext(), newEmail);

                                    new SweetAlertDialog(ChangeEmailActivity.this, SweetAlertDialog.SUCCESS_TYPE)
                                            .setTitleText("Success!")
                                            .setContentText("Email successfully changed.")
                                            .setConfirmClickListener(sweetAlertDialog1 -> {
                                                sweetAlertDialog1.dismiss();

                                                state = 0;
                                                finish();
                                            })
                                            .show();
                                })
                                .addOnFailureListener(e -> {
                                    btnChange.setEnabled(true);
                                    loading.dismiss();
                                    Log.d("e", e.getMessage());
                                });
                    } else {
                        btnChange.setEnabled(true);
                        loading.dismiss();
                        progressBar.setVisibility(View.GONE);
                        new SweetAlertDialog(ChangeEmailActivity.this, SweetAlertDialog.WARNING_TYPE)
                                .setTitleText("Warning!")
                                .setContentText(task.getException().toString())
                                .show();
                    }
                })
                .addOnFailureListener(e -> {
                    btnChange.setEnabled(true);
                    loading.dismiss();
                    Log.d("e", e.getMessage());
                });
    }


    @Override
    public void finish() {
        super.finish();
        if (state == 0)
            CustomIntent.customType(this, Common.Anim_Right_to_Left);
    }

    @Override
    public void onBackPressed() {
        if (state == 0)
            super.onBackPressed();
        if (state == 1) {
            Animation animation = AnimationUtils.loadAnimation(this, R.anim.slide_to_right);
            llState2.setAnimation(animation);
            llState2.setVisibility(View.INVISIBLE);

            new Handler().postDelayed(() -> {
                Animation animation1 = AnimationUtils.loadAnimation(this, R.anim.slide_from_left_to_right);
                llState1.setVisibility(View.VISIBLE);
                llState1.setAnimation(animation1);
                state = 0;
            }, 50);
        }
    }
}
