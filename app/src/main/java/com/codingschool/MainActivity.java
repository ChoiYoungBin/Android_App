package com.codingschool;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.view.ContextThemeWrapper;
import android.util.Log;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import utils.Constants;

import static utils.Constants.USER_ACCESS_CAT;
import static utils.Constants.USER_CENTER_NAME;
import static utils.Constants.USER_EMAIL;
import static utils.Constants.USER_FAMILIES;
import static utils.Constants.USER_PHOTOS;
import static utils.Constants.USER_STATUS;
import static utils.Constants.USER_TELS;
import static utils.Constants.USER_TOKEN;
import static utils.Constants.USER__ID;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "MainActivity";

    private SharedPreferences mSharedPreferences;
    private int mPreviousMenuId;
    private String mToken;
    private Handler mHandler;

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

        mHandler = new Handler(Looper.getMainLooper());

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mToken = mSharedPreferences.getString(USER_TOKEN, null);

        getProfileInfo();
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
        int menu_id = item.getItemId();

        if (menu_id== R.id.nav_logout) {
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

            return true; // 이곳에서 종료... Login 화면으로 이동
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        if(mPreviousMenuId == menu_id)
            return true; // 이곳에서 종료... 별다른 action 없음

        // Fragment 처리
        // Initially city fragment
        Fragment fragment;
        FragmentManager fragmentManager = getSupportFragmentManager();


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

    private void getProfileInfo() {

        // String uri = "http://10.150.101.185/api/user";
        String uri = Constants.API_USER;

        Log.v(TAG, "url=" + uri);

        //Set up client
        OkHttpClient client = new OkHttpClient();

        RequestBody formBody = new FormBody.Builder().build();

        Request request = new Request.Builder()
                .header("authorization", mToken)
                .url(uri)
                .post(formBody)
                .build();

        //Setup callback
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "request Failed, message = " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                final String res = Objects.requireNonNull(response.body()).string();

                try {
                    JSONObject json_result = new JSONObject(res);
                    JSONObject object = new JSONObject(json_result.getString("data"));

                    JSONArray userAccessCatList = new JSONArray(object.getString(USER_ACCESS_CAT));
                    for(int i = 0 ; i<userAccessCatList.length(); i++){
                        String userAccessCat = userAccessCatList.get(i).toString();
                        Log.d(TAG,"userAccessCat = " + userAccessCat);
                    }

                    JSONArray userFamiliesList = new JSONArray(object.getString(USER_FAMILIES));
                    for(int i = 0 ; i<userFamiliesList.length(); i++){
                        JSONObject userFamilies = userFamiliesList.getJSONObject(i);
                        Log.d(TAG,"userFamilies = " + userFamilies.toString());
                    }

                    String userTels = object.getString(USER_TELS);
                    String userPhotos = object.getString(USER_PHOTOS);
                    String user_Id = object.getString(USER__ID);
                    String userCenterName = object.getString(USER_CENTER_NAME);

//                    mSharedPreferences.edit().putString(USER_EMAIL, userName);
//                    mSharedPreferences.edit().putString(USER_STATUS, status);

                    mSharedPreferences.edit().apply();

                    // fillNavigationView(fullName, imageURL);

                } catch (JSONException e) {
                    Log.e(TAG, "Error parsing user JSON, " + e.getMessage());
                }
            }
        });
    }

    /*
    "access_cat":["parent"],
    "families":[{"_id":"5c515b3bf700fd05594bb205","name":"ChoiRang","relation":"daughter"}],
    "tels":[],
    "photos":[],
    "comments":[],
    "_id":"5c515ae8f700fd05594bb204",
    "center_id":"5c0f6714c8e5f40230d3f94e",
    "center_name":"LEGO Education CodingSchool HQ",
    "first_name":"Young",
    "last_name":"Choi",
    "full_name":"ChoiYoung",
    "login_name":"ybchoi",
    "gender":"M",
    "user_type":2,
    "relation":"father",
    "birth":"2010-11-25",
    "address":{"address":"","zipcode":"","city":""},
    "email":"ybchoi@codingschool.co.kr",
    "started":"",
    "status":100,
    "description":"",
    "reged":"2019-01-30T08:06:00.362Z",
    "passwd":"1",
    "fcm_id":"slfjsdflskflskldlsdflsqoweiroewm",
    */

 }
