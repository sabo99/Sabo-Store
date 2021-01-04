package com.sabo.sabostore.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;
import com.sabo.sabostore.Activity.Main.HomeActivity;
import com.sabo.sabostore.Common.Common;
import com.sabo.sabostore.Common.Preferences;
import com.sabo.sabostore.Model.UserModel;
import com.sabo.sabostore.R;

import java.util.HashMap;
import java.util.Map;

import maes.tech.intentanim.CustomIntent;

import static android.text.Html.fromHtml;

public class SignInActivity extends AppCompatActivity implements View.OnClickListener {

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private DatabaseReference userRef;

    private AlertDialog.Builder builder;
    private AlertDialog dialog;

    private RelativeLayout main;
    private TextInputLayout tilEmail, tilPassword;
    private EditText etEmail, etPassword;
    private Button btnSignIn;
    private TextView tvSignUp, tvForgotPassword;
    private ProgressBar progressBar;
    private String email, password;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        userRef = FirebaseDatabase.getInstance().getReference(Common.USER_REF);
        initViews();
    }

    private void initViews() {
        main = findViewById(R.id.main);
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnSignIn = findViewById(R.id.btnSignIn);
        tvSignUp = findViewById(R.id.tvSignUp);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        progressBar = findViewById(R.id.progressBar);

        tvSignUp.setText(fromHtml("<font color='#000000'>Don't have an account? " +
                "</font><font color='" + getResources().getColor(R.color.colorPrimaryDark) + "'><b>Sign Up</b></font>"));

        tvForgotPassword.setText(fromHtml("<font'>" + getResources().getString(R.string.textForgotPassword) + "</font>"));

        main.setOnClickListener(this);
        btnSignIn.setOnClickListener(this);
        tvForgotPassword.setOnClickListener(this);
        tvSignUp.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.main:
                clearFocus();
                break;
            case R.id.btnSignIn:
                signIn();
                break;
            case R.id.tvForgotPassword:
                forgotPassword();
                break;
            case R.id.tvSignUp: {
                initClear();
                startActivity(new Intent(SignInActivity.this, SignUpActivity.class));
                CustomIntent.customType(this, Common.Anim_Left_to_Right);
            }
            break;
        }
    }

    private void forgotPassword() {
        View view = LayoutInflater.from(this).inflate(R.layout.forgot_password_layout, null);
        /** InitViews */
        TextInputLayout tilEmail;
        EditText etEmail;
        Button btnNegative, btnPositive;

        tilEmail = view.findViewById(R.id.tilEmail);
        etEmail = view.findViewById(R.id.etEmail);
        btnPositive = view.findViewById(R.id.btnPositive);
        btnNegative = view.findViewById(R.id.btnNegative);
        /** ------------------------------------------------ */

        builder = new AlertDialog.Builder(this).setCancelable(false).setView(view);
        dialog = builder.create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                btnNegative.setOnClickListener(v -> {
                    dialog.dismiss();
                });

                btnPositive.setOnClickListener(v -> {
                    String email = etEmail.getText().toString();
                    if (TextUtils.isEmpty(email)) {
                        tilEmail.setHelperTextEnabled(true);
                        tilEmail.setHelperText("Please fill out this field.");
                        tilEmail.setHelperTextColor(ColorStateList.valueOf(getResources().getColor(R.color.colorWarning)));
                    } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        tilEmail.setHelperTextEnabled(false);
                        etEmail.setError("Email format is wrong!");
                    } else {
                        SweetAlertDialog loading = new SweetAlertDialog(v.getContext(), SweetAlertDialog.PROGRESS_TYPE);
                        loading.getProgressHelper().setBarColor(getResources().getColor(R.color.colorPrimary));
                        loading.setTitleText("Please wait...").show();

                        /** Check Email Exists */
                        userRef.orderByChild("email")
                                .equalTo(email)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.exists()) {
                                            firebaseAuth.sendPasswordResetEmail(email)
                                                    .addOnCompleteListener(task -> {
                                                        if (task.isSuccessful()) {
                                                            loading.dismiss();
                                                            new SweetAlertDialog(SignInActivity.this, SweetAlertDialog.CUSTOM_IMAGE_TYPE)
                                                                    .setCustomImage(R.drawable.mail)
                                                                    .setTitleText("Check in your mail!")
                                                                    .setContentText("We just emailed you with the instructions to reset your password.")
                                                                    .setConfirmClickListener(sweetAlertDialog -> {
                                                                        sweetAlertDialog.dismiss();
                                                                        dialog.dismiss();
                                                                    })
                                                                    .show();
                                                        } else {
                                                            loading.dismiss();
                                                            Log.d("task_sendPasswordReset", task.getException().getMessage());
                                                        }
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        loading.dismiss();
                                                        new SweetAlertDialog(SignInActivity.this, SweetAlertDialog.WARNING_TYPE)
                                                                .setTitleText("Oops!")
                                                                .setContentText(e.getMessage())
                                                                .setConfirmClickListener(sweetAlertDialog -> {
                                                                    sweetAlertDialog.dismiss();
                                                                })
                                                                .show();
                                                    });
                                        } else {
                                            loading.dismiss();
                                            new SweetAlertDialog(SignInActivity.this, SweetAlertDialog.WARNING_TYPE)
                                                    .setTitleText("Oops!")
                                                    .setContentText("Sorry, your email is not registered!")
                                                    .show();
                                        }

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        loading.dismiss();
                                        Log.d("checkEmailExists", error.getMessage());
                                    }
                                });
                    }
                });
            }
        });

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }


    private void signIn() {
        email = etEmail.getText().toString();
        password = etPassword.getText().toString();

        if (checkFields(true, email, password)) {

            clearFocus();
            progressBar.setVisibility(View.VISIBLE);
            btnSignIn.setEnabled(false);

            firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {

                                Preferences.setEmail(getBaseContext(), email);
                                Preferences.setPassword(getBaseContext(), password);
                                checkStatus();
                            } else {
                                progressBar.setVisibility(View.INVISIBLE);
                                btnSignIn.setEnabled(true);

                                new SweetAlertDialog(SignInActivity.this, SweetAlertDialog.WARNING_TYPE)
                                        .setTitleText("Oops!")
                                        .setContentText(task.getException().getMessage())
                                        .show();
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressBar.setVisibility(View.INVISIBLE);
                            btnSignIn.setEnabled(true);
                            Log.d("SignIn", e.getMessage());
                        }
                    });
        }
    }

    private void checkStatus() {
        firebaseUser = firebaseAuth.getCurrentUser();
        userRef.child(firebaseUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            UserModel userModel = dataSnapshot.getValue(UserModel.class);
                            if (userModel.getStatus().equals("on")) {
                                progressBar.setVisibility(View.INVISIBLE);
                                btnSignIn.setEnabled(true);
                                new SweetAlertDialog(SignInActivity.this, SweetAlertDialog.WARNING_TYPE)
                                        .setTitleText("Oops!")
                                        .setContentText("User already login.")
                                        .show();
                                firebaseUser = null;
                                firebaseAuth.signOut();
                            }

                            if (userModel.getStatus().equals("off"))
                                updateStatusSignIn();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        progressBar.setVisibility(View.INVISIBLE);
                        btnSignIn.setEnabled(true);
                        Log.d("checkStatus", error.getMessage());
                    }
                });
    }

    private void updateStatusSignIn() {
        firebaseUser = firebaseAuth.getCurrentUser();

        Map<String, Object> updateStatus = new HashMap<>();
        updateStatus.put(Common.KEY_STATUS, "on");

        userRef.child(firebaseUser.getUid())
                .updateChildren(updateStatus)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            initClear();
                            progressBar.setVisibility(View.INVISIBLE);
                            btnSignIn.setEnabled(true);
                            startActivity(new Intent(SignInActivity.this, HomeActivity.class));
                            CustomIntent.customType(SignInActivity.this, Common.Anim_Fadein_to_Fadeout);
                            finish();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressBar.setVisibility(View.INVISIBLE);
                        btnSignIn.setEnabled(true);
                        Log.d("updateStatus", e.getMessage());
                    }
                });
    }


    private void clearFocus() {
        etPassword.clearFocus();
        etEmail.clearFocus();
    }

    private void initClear() {
        tilEmail.setHelperTextEnabled(false);
        tilPassword.setHelperTextEnabled(false);
        etEmail.setError(null);
        etEmail.setText(null);
        etPassword.setText(null);
        etPassword.clearFocus();
        etEmail.clearFocus();
    }


    private boolean checkFields(boolean checked, String email, String password) {
        if (TextUtils.isEmpty(email)) {
            checked = false;
            tilEmail.setHelperTextEnabled(true);
            tilEmail.setHelperText("Please fill out this field.");
            tilEmail.setHelperTextColor(ColorStateList.valueOf(getResources().getColor(R.color.colorWarning)));
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            checked = false;
            etEmail.setError("Email format is wrong!");
        }

        if (password.length() < 6) {
            checked = false;
            tilPassword.setHelperTextEnabled(true);
            tilPassword.setHelperTextColor(ColorStateList.valueOf(getResources().getColor(R.color.colorWarning)));
            tilPassword.setHelperText("Minimum 6 digit password.");
        }

        if (!TextUtils.isEmpty(email))
            tilEmail.setHelperTextEnabled(false);
        if (Patterns.EMAIL_ADDRESS.matcher(email).matches())
            etEmail.setError(null);
        if (password.length() >= 6)
            tilPassword.setHelperTextEnabled(false);

        return checked;
    }
}
