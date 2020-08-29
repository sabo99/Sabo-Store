package com.sabo.sabostore.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;
import com.sabo.sabostore.Activity.Main.HomeActivity;
import com.sabo.sabostore.Common.Common;
import com.sabo.sabostore.Common.Preferences;
import com.sabo.sabostore.Model.UserModel;
import com.sabo.sabostore.R;

import maes.tech.intentanim.CustomIntent;

import static android.text.Html.fromHtml;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener {

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private DatabaseReference userRef;

    private RelativeLayout main;
    private TextInputLayout tilUsername, tilEmail, tilPassword;
    private EditText etUsername, etEmail, etPassword;
    private TextView tvSignIn;
    private Button btnSignUp;
    private ProgressBar progressBar;
    private String username, email, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        firebaseAuth = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference(Common.USER_REF);

        initViews();
    }

    private void initViews() {
        main = findViewById(R.id.main);
        tilUsername = findViewById(R.id.tilUsername);
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnSignUp = findViewById(R.id.btnSignUp);
        tvSignIn = findViewById(R.id.tvSignIn);
        progressBar = findViewById(R.id.progressBar);

        tvSignIn.setText(fromHtml("<font color='#000000'>Already have an account? " +
                "</font><font color='"+getResources().getColor(R.color.colorPrimaryDark)+"'><b>Sign In</b></font>"));

        btnSignUp.setOnClickListener(this);
        tvSignIn.setOnClickListener(this);
        main.setOnClickListener(this);
    }

    @Override
    public void finish() {
        super.finish();
        CustomIntent.customType(this, Common.Anim_Right_to_Left);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnSignUp :
                signUp();
                break;
            case R.id.tvSignIn :
                finish();
                break;
            case R.id.main :
                clearFocus();
                break;
        }
    }

    private void signUp() {
        username = etUsername.getText().toString();
        email = etEmail.getText().toString();
        password = etPassword.getText().toString();

        if (checkFields(true, username, email, password)){
            clearFocus();
            progressBar.setVisibility(View.VISIBLE);
            btnSignUp.setEnabled(false);

            UserModel userModel = new UserModel();
            userModel.setName(username);
            userModel.setEmail(email);
            userModel.setStatus("on");
            userModel.setPhone("");
            userModel.setImage("");

            firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()){
                                insertDataInFirebase(userModel, email, password);
                            }
                            else {
                                progressBar.setVisibility(View.GONE);
                                btnSignUp.setEnabled(true);
                                new SweetAlertDialog(SignUpActivity.this, SweetAlertDialog.WARNING_TYPE)
                                        .setTitleText("Oops!")
                                        .setContentText(task.getException().getMessage())
                                        .show();
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressBar.setVisibility(View.GONE);
                            btnSignUp.setEnabled(true);
                            Log.d("createUser", e.getMessage());
                        }
                    });
        }
    }

    private void insertDataInFirebase(UserModel userModel, String email, String password) {
        firebaseUser = firebaseAuth.getCurrentUser();
        userRef.child(firebaseUser.getUid())
                .setValue(userModel)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            progressBar.setVisibility(View.GONE);
                            btnSignUp.setEnabled(true);
                            initClear();

                            Preferences.setEmail(getBaseContext(), email);
                            Preferences.setPassword(getBaseContext(), password);

                            startActivity(new Intent(SignUpActivity.this, HomeActivity.class));
                            CustomIntent.customType(SignUpActivity.this, Common.Anim_Right_to_Left);
                            finish();
                            Toast.makeText(SignUpActivity.this, "Welcome, You're already registered.", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            progressBar.setVisibility(View.GONE);
                            btnSignUp.setEnabled(true);
                            new SweetAlertDialog(SignUpActivity.this, SweetAlertDialog.WARNING_TYPE)
                                    .setTitleText("Oops!")
                                    .setContentText(task.getException().getMessage())
                                    .show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressBar.setVisibility(View.GONE);
                        btnSignUp.setEnabled(true);

                        Log.d("signUp", e.getMessage());
                    }
                });
    }

    private void clearFocus(){
        etPassword.clearFocus();
        etEmail.clearFocus();
        etUsername.clearFocus();
    }

    private void initClear() {
        tilUsername.setHelperTextEnabled(false);
        tilEmail.setHelperTextEnabled(false);
        tilPassword.setHelperTextEnabled(false);
        etEmail.setError(null);
        etEmail.setText(null);
        etPassword.setText(null);
        etUsername.setText(null);
        etPassword.clearFocus();
        etEmail.clearFocus();
        etUsername.clearFocus();
    }

    private boolean checkFields(boolean checked, String username, String email, String password) {
        if (TextUtils.isEmpty(username)){
            checked = false;
            tilUsername.setHelperTextEnabled(true);
            tilUsername.setHelperText("Please fill out this field.");
            tilUsername.setHelperTextColor(ColorStateList.valueOf(getResources().getColor(R.color.colorWarning)));
        }

        if (TextUtils.isEmpty(email)) {
            checked = false;
            tilEmail.setHelperTextEnabled(true);
            tilEmail.setHelperText("Please fill out this field.");
            tilEmail.setHelperTextColor(ColorStateList.valueOf(getResources().getColor(R.color.colorWarning)));
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            checked = false;
            etEmail.setError("Email format is wrong!");
        }

        if (password.length() < 6){
            checked = false;
            tilPassword.setHelperTextEnabled(true);
            tilPassword.setHelperTextColor(ColorStateList.valueOf(getResources().getColor(R.color.colorWarning)));
            tilPassword.setHelperText("Minimum 6 digit password.");
        }

        if (!TextUtils.isEmpty(username))
            tilUsername.setHelperTextEnabled(false);
        if (!TextUtils.isEmpty(email))
            tilEmail.setHelperTextEnabled(false);
        if (Patterns.EMAIL_ADDRESS.matcher(email).matches())
            etEmail.setError(null);
        if (password.length() >= 6)
            tilPassword.setHelperTextEnabled(false);

        return checked;
    }
}
