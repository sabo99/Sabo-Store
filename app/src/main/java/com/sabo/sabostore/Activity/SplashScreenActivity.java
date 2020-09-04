package com.sabo.sabostore.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.sabo.sabostore.Activity.Main.HomeActivity;
import com.sabo.sabostore.Common.Common;
import com.sabo.sabostore.R;

import maes.tech.intentanim.CustomIntent;

public class SplashScreenActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private ImageView logoSplash;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        logoSplash = findViewById(R.id.logoSplash);

        splashAnim();
    }

    private void splashAnim() {
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        logoSplash.startAnimation(fadeIn);

        new Handler().postDelayed(() -> {
            if (firebaseUser != null) {
                startActivity(new Intent(SplashScreenActivity.this, HomeActivity.class));
                CustomIntent.customType(SplashScreenActivity.this, Common.Anim_Fadein_to_Fadeout);
                finish();
            } else {
                firebaseAuth.signOut();
                startActivity(new Intent(SplashScreenActivity.this, SignInActivity.class));
                CustomIntent.customType(SplashScreenActivity.this, Common.Anim_Fadein_to_Fadeout);
                finish();
            }
        }, 1600);
    }
}
