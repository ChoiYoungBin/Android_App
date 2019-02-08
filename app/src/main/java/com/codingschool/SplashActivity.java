package com.codingschool;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.eftimoff.androipathview.PathView;

import static utils.Constants.USER_TOKEN;

public class SplashActivity extends AppCompatActivity {
    private SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);

        final PathView pathView = findViewById(R.id.pathView);
        pathView.getPathAnimator()
                .delay(1000)
                .duration(1000)
                .interpolator(new AccelerateDecelerateInterpolator())
                .start();

        pathView.useNaturalColors();
        pathView.setFillAfter(true);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        final Intent i;
        if (mSharedPreferences.getString(USER_TOKEN, null) != null) {
            i = MainActivity.getStartIntent(SplashActivity.this);
        } else {
            i = LoginActivity.getStartIntent(SplashActivity.this);
        }

        new Handler().postDelayed( new Runnable() {
            public void run() {
                startActivity(i);
                finish();
            }}, 2000);
    }
}
