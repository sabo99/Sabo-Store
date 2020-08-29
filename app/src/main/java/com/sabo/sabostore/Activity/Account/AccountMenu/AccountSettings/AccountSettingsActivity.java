package com.sabo.sabostore.Activity.Account.AccountMenu.AccountSettings;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.LinearLayout;

import com.sabo.sabostore.Activity.Account.AccountMenu.AccountSettings.SubMenu.ChangeEmailActivity;
import com.sabo.sabostore.Activity.Account.AccountMenu.AccountSettings.SubMenu.DeleteAccountActivity;
import com.sabo.sabostore.Activity.Account.AccountMenu.AccountSettings.SubMenu.ResetPasswordActivity;
import com.sabo.sabostore.Common.Common;
import com.sabo.sabostore.R;

import maes.tech.intentanim.CustomIntent;

public class AccountSettingsActivity extends AppCompatActivity {

    private LinearLayout llResetPassword, llChangeEmail, llDeleteAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_settings);

        initViews();
    }

    private void initViews() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Account");

        llResetPassword = findViewById(R.id.llResetPassword);
        llChangeEmail = findViewById(R.id.llChangeEmail);
        llDeleteAccount = findViewById(R.id.llDeleteAccount);


        llResetPassword.setOnClickListener(v -> {
            startActivity(new Intent(AccountSettingsActivity.this, ResetPasswordActivity.class));
            CustomIntent.customType(this, Common.Anim_Left_to_Right);
        });

        llChangeEmail.setOnClickListener(v -> {
            startActivity(new Intent(AccountSettingsActivity.this, ChangeEmailActivity.class));
            CustomIntent.customType(this, Common.Anim_Left_to_Right);
        });

        llDeleteAccount.setOnClickListener(v -> {
            startActivity(new Intent(AccountSettingsActivity.this, DeleteAccountActivity.class));
            CustomIntent.customType(this, Common.Anim_Left_to_Right);
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
}
