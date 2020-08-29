package com.sabo.sabostore.Activity.Account.AccountMenu.AccountSettings.SubMenu;

import androidx.appcompat.app.AppCompatActivity;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;
import com.sabo.sabostore.Common.Common;
import com.sabo.sabostore.Common.Preferences;
import com.sabo.sabostore.R;

import maes.tech.intentanim.CustomIntent;

public class ResetPasswordActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;

    private ImageButton btnBack;
    private ProgressBar progressBar;
    private Button btnReset;
    private TextInputLayout tilPassword;
    private EditText etPassword;
    private String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        initViews();
    }

    private void initViews() {

        btnBack = findViewById(R.id.btnBack);
        progressBar = findViewById(R.id.progressBar);
        btnReset = findViewById(R.id.btnReset);
        tilPassword = findViewById(R.id.tilPassword);
        etPassword = findViewById(R.id.etPassword);

        btnReset.setOnClickListener(v -> {
            password = etPassword.getText().toString();
            if (password.length() < 6){
                tilPassword.setHelperText("Minimal length of password is 6 digit!");
                tilPassword.setHelperTextEnabled(true);
                tilPassword.setHelperTextColor(ColorStateList.valueOf(getResources().getColor(R.color.colorWarning)));
            }
            else {
                btnReset.setEnabled(false);
                progressBar.setVisibility(View.VISIBLE);
                tilPassword.setHelperTextEnabled(false);
                new SweetAlertDialog(ResetPasswordActivity.this, SweetAlertDialog.WARNING_TYPE)
                        .setTitleText("Are you sure?")
                        .setContentText("Reset to the new password.")
                        .showCancelButton(true)
                        .setCancelText("No")
                        .setCancelClickListener(sweetAlertDialog -> {
                            btnReset.setEnabled(true);
                            sweetAlertDialog.dismissWithAnimation();
                            sweetAlertDialog.dismiss();
                            progressBar.setVisibility(View.GONE);
                        })
                        .setConfirmText("Yes")
                        .setConfirmClickListener(sweetAlertDialog -> {
                            sweetAlertDialog.dismissWithAnimation();
                            sweetAlertDialog.dismiss();
                            progressBar.setVisibility(View.GONE);

                            SweetAlertDialog loading = new SweetAlertDialog(ResetPasswordActivity.this, SweetAlertDialog.PROGRESS_TYPE);
                            loading.getProgressHelper().setBarColor(getResources().getColor(R.color.colorPrimary));
                            loading.setTitleText("Please wait...").show();

                            AuthCredential credential = EmailAuthProvider.getCredential(Preferences.getEmail(getBaseContext()), Preferences.getPassword(getBaseContext()));
                            firebaseUser.reauthenticate(credential)
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()){
                                            resetPassword(password, loading);
                                        }
                                        else
                                            loading.dismiss();
                                    })
                                    .addOnFailureListener(e -> {
                                        btnReset.setEnabled(true);
                                        loading.dismiss();
                                    });
                        })
                        .show();
            }
        });

        btnBack.setOnClickListener(v -> {
            CustomIntent.customType(this, Common.Anim_Right_to_Left);
            finish();
        });

    }

    private void resetPassword(String password, SweetAlertDialog loading) {
        firebaseUser.updatePassword(password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        btnReset.setEnabled(true);
                        loading.dismiss();
                        clearFocus();

                        /** Update SharePreference PASSWORD */
                        Preferences.setPassword(getBaseContext(), password);

                        new SweetAlertDialog(ResetPasswordActivity.this, SweetAlertDialog.SUCCESS_TYPE)
                                .setTitleText("Success!")
                                .setContentText("Password reset is successful")
                                .setConfirmClickListener(sweetAlertDialog -> {
                                    sweetAlertDialog.dismiss();
                                    finish();
                                })
                                .show();
                    }
                    else{
                        btnReset.setEnabled(true);
                        loading.dismiss();
                        new SweetAlertDialog(ResetPasswordActivity.this, SweetAlertDialog.WARNING_TYPE)
                                .setTitleText("Warning!")
                                .setContentText(task.getException().getMessage())
                                .show();
                    }

                })
                .addOnFailureListener(e -> {
                    btnReset.setEnabled(true);
                    loading.dismiss();
                    Log.d("e", e.getMessage());
                });
    }

    private void clearFocus() {
        etPassword.setText("");
        etPassword.clearComposingText();
        etPassword.clearFocus();
        tilPassword.setHelperTextEnabled(false);
    }

    @Override
    public void finish() {
        super.finish();
        CustomIntent.customType(this, Common.Anim_Right_to_Left);
    }
}
