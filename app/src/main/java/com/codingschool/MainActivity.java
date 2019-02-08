package com.codingschool;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.view.ContextThemeWrapper;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.codingschool.R;

import static utils.Constants.USER_TOKEN;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            mTextMessage.setText("Nav Camera");
        } else if (id == R.id.nav_gallery) {
            mTextMessage.setText("Nav Gallery");
        } else if (id == R.id.nav_slideshow) {
            mTextMessage.setText("Nav SlideShow");
        } else if (id == R.id.nav_manage) {
            mTextMessage.setText("Nav Manage");
        } else if (id == R.id.nav_share) {
            mTextMessage.setText("Nav Share");
        } else if (id == R.id.nav_logout) {
            //set AlertDialog before signout
            ContextThemeWrapper crt = new ContextThemeWrapper(this, R.style.AlertDialog);
            AlertDialog.Builder builder = new AlertDialog.Builder(crt);
            builder.setMessage(R.string.signout_message)
                .setPositiveButton(R.string.positive_button,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mSharedPreferences
                                    .edit()
                                    .putString(USER_TOKEN, null)
                                    .apply();
                            Intent i = LoginActivity.getStartIntent(MainActivity.this);
                            startActivity(i);
                            finish();
                        }
                    }
                ).setNegativeButton(R.string.negative_button,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    }
                );

            builder.create().show();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private TextView mTextMessage;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.bottom_review:
                    mTextMessage.setText(R.string.bottom_review);
                    return true;
                case R.id.bottom_notice:
                    mTextMessage.setText(R.string.bottom_notice);
                    return true;
                case R.id.bottom_home:
                    mTextMessage.setText(R.string.bottom_home);
                    return true;
                case R.id.bottom_lecture:
                    mTextMessage.setText(R.string.bottom_lecture);
                    return true;
                case R.id.bottom_cs:
                    mTextMessage.setText(R.string.bottom_cs);
                    return true;
            }
            return false;
        }
    };

    public static Intent getStartIntent(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        return intent;
    }

}
