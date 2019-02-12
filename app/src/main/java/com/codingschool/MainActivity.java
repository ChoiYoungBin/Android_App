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
import com.codingschool.ui.home.HomeFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import utils.Constants;
import utils.JsonArrayUtil;
import utils.StaticClass;

import static utils.Constants.USER_ACCESS_CATS;
import static utils.Constants.USER_CENTER_ID;
import static utils.Constants.USER_CENTER_NAME;
import static utils.Constants.USER_EMAIL;
import static utils.Constants.USER_FAMILIES;
import static utils.Constants.USER_FCM_ID;
import static utils.Constants.USER_FULL_NAME;
import static utils.Constants.USER_GEDER;
import static utils.Constants.USER_LOGIN_NAME;
import static utils.Constants.USER_PHOTOS;
import static utils.Constants.USER_REGED;
import static utils.Constants.USER_RELATION;
import static utils.Constants.USER_STATUS;
import static utils.Constants.USER_TELS;
import static utils.Constants.USER_TOKEN;
import static utils.Constants.USER__ID;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "MainActivity";

    private SharedPreferences mSharedPreferences;
    private int mPreviousMenuId; // 이전 선택된 Menu ID 를 저장
    private String mToken;
    private Handler mHandler;
    private NavigationView mNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Drawer layout 처리
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        // Drawer navigation 메뉴 View 를 저장함
        mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);

        // Bottom navigation 메뉴 View 를 처리
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        mHandler = new Handler(Looper.getMainLooper());

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mToken = mSharedPreferences.getString(USER_TOKEN, null);

        // REST API 를 이용하여 사용자 정보를 가져옴
        getProfileInfo();

        mPreviousMenuId = R.id.bottom_home; // 초기에는 Home 메뉴를 사용하여 시작함

        // Fragment 처리 ( 첫 번째 : HomeFragment )
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        transaction.add(R.id.container, HomeFragment.newInstance());
        transaction.commit();

        // Drawer 메뉴에서 Group 메뉴들을 보이지 않게 함
        mNavigationView.getMenu().setGroupVisible(R.id.group_edu_center,false);
        mNavigationView.getMenu().setGroupVisible(R.id.group_notice,false);
        mNavigationView.getMenu().setGroupVisible(R.id.group_cs,false);
        mNavigationView.getMenu().setGroupVisible(R.id.group_me,false);
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

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Drawer navigation 메뉴를 처리
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

        // 그룹 타이틀 메뉴를 선택한 경우 sub 메뉴를 나타나게 함
        if(menu_id == R.id.menu_edu_center) {
            if(mPreviousMenuId == menu_id) { // 같은 메뉴를 두번 누른 경우
                mPreviousMenuId = 0;
                mNavigationView.getMenu().setGroupVisible(R.id.group_edu_center,false);
            }
            else {
                mPreviousMenuId = menu_id;
                mNavigationView.getMenu().setGroupVisible(R.id.group_edu_center, true);
            }
            mNavigationView.getMenu().setGroupVisible(R.id.group_notice,false);
            mNavigationView.getMenu().setGroupVisible(R.id.group_cs,false);
            mNavigationView.getMenu().setGroupVisible(R.id.group_me,false);

            return true; // 이곳에서 종료...
        }
        else if(menu_id == R.id.menu_notice) {
            mNavigationView.getMenu().setGroupVisible(R.id.group_edu_center,false);
            if(mPreviousMenuId == menu_id) { // 같은 메뉴를 두번 누른 경우
                mPreviousMenuId = 0;
                mNavigationView.getMenu().setGroupVisible(R.id.group_notice,false);
            }
            else {
                mPreviousMenuId = menu_id;
                mNavigationView.getMenu().setGroupVisible(R.id.group_notice, true);
            }
            mNavigationView.getMenu().setGroupVisible(R.id.group_cs,false);
            mNavigationView.getMenu().setGroupVisible(R.id.group_me,false);

            return true; // 이곳에서 종료...
        }
        else if(menu_id == R.id.menu_cs) {
            mNavigationView.getMenu().setGroupVisible(R.id.group_edu_center,false);
            mNavigationView.getMenu().setGroupVisible(R.id.group_notice,false);
            if(mPreviousMenuId == menu_id) { // 같은 메뉴를 두번 누른 경우
                mPreviousMenuId = 0;
                mNavigationView.getMenu().setGroupVisible(R.id.group_cs,false);
            }
            else {
                mPreviousMenuId = menu_id;
                mNavigationView.getMenu().setGroupVisible(R.id.group_cs, true);
            }
            mNavigationView.getMenu().setGroupVisible(R.id.group_me,false);

            return true; // 이곳에서 종료...
        }
        else if(menu_id == R.id.menu_me) {
            mNavigationView.getMenu().setGroupVisible(R.id.group_edu_center,false);
            mNavigationView.getMenu().setGroupVisible(R.id.group_notice,false);
            mNavigationView.getMenu().setGroupVisible(R.id.group_cs,false);
            if(mPreviousMenuId == menu_id) { // 같은 메뉴를 두번 누른 경우
                mPreviousMenuId = 0;
                mNavigationView.getMenu().setGroupVisible(R.id.group_me,false);
            }
            else {
                mPreviousMenuId = menu_id;
                mNavigationView.getMenu().setGroupVisible(R.id.group_me, true);
            }

            return true; // 이곳에서 종료...
        }

        // Drawer layout 를 닫음
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        if(mPreviousMenuId == menu_id) // 이전 메뉴와 같은 경우 별다른 action 없음
            return true;

        // 정상적인 메뉴에 따른 Fragment 처리
        return FragmentMenuChange(menu_id);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            // Bottom navigation 메뉴 처리
            if(mPreviousMenuId == item.getItemId()) // 이전 메뉴와 같은 경우 별다른 action 없음
                return true;

            return FragmentMenuChange(item.getItemId());
        }
    };

    public static Intent getStartIntent(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        return intent;
    }

    // REST API 를 통하여 사용자 정보를 가져와 Global(static) 변수에 저장
    private void getProfileInfo() {
        // String uri = "http://10.150.101.185/api/user";
        String uri = Constants.API_USER;
        Log.d(TAG, "url=" + uri);

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
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "request Failed, message = " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull final Response response) throws IOException {
                final String res = Objects.requireNonNull(response.body()).string();

                try {
                    JSONObject json_result = new JSONObject(res);

                    // 인증 실패의 경우 : {"auth":false,"message":"Failed to authenticate token."}
                    // 인증 성공의 경우 : {"result":true,"data":{"access_cat":["parent"],"families":[{"_id":"5c515b3bf700fd05594bb205", ...}}

                    JSONObject object = new JSONObject(json_result.getString("data"));

                    JSONArray tempJsonArray = new JSONArray(object.getString(USER_ACCESS_CATS));
                    ArrayList userAccessCatList;
                    if(tempJsonArray.length() != 0) {
                        userAccessCatList = JsonArrayUtil.convert(tempJsonArray);
                        Log.d(TAG, "userAccessCatList = " + userAccessCatList.toString());
                    }
                    else {
                        userAccessCatList = new ArrayList();
                    }
                    StaticClass.access_cats = (ArrayList<String>)userAccessCatList;

                    tempJsonArray = new JSONArray(object.getString(USER_FAMILIES));
                    ArrayList userFamiliesList;
                    if(tempJsonArray.length() != 0) {
                        userFamiliesList = JsonArrayUtil.convert(tempJsonArray);
                        Log.d(TAG,"userFamiliesList = " + userFamiliesList.toString());
                    }
                    else {
                        userFamiliesList = new ArrayList();
                    }
                    StaticClass.families = (ArrayList<Object>)userFamiliesList;

                    // ybchoi temp 사용방법 예제 start
                    if(StaticClass.access_cats != null) {
                        for (int i = 0; i < StaticClass.access_cats.size(); i++) {
                            String t_obj = StaticClass.access_cats.get(i);

                            Log.d(TAG, "access_cat = " + t_obj);
                        }
                    }
                    if(StaticClass.families != null) {
                        for (int i = 0; i < StaticClass.families.size(); i++) {
                            Object t_obj = StaticClass.families.get(i);
                            JSONObject t_j_obj = new JSONObject(t_obj.toString());

                            Log.d(TAG, "id = " + t_j_obj.getString("_id"));
                            Log.d(TAG, "name = " + t_j_obj.getString("name"));
                            Log.d(TAG, "relation = " + t_j_obj.getString("relation"));
                        }
                    }
                    // ybchoi temp end

                    tempJsonArray = new JSONArray(object.getString(USER_TELS));
                    ArrayList userTelList;
                    if(tempJsonArray.length() != 0) {
                        userTelList = JsonArrayUtil.convert(tempJsonArray);
                        Log.d(TAG,"userTelList = " + userTelList.toString());
                    }
                    else {
                        userTelList = new ArrayList();
                    }
                    StaticClass.tels = (ArrayList<String>)userTelList;

                    tempJsonArray = new JSONArray(object.getString(USER_PHOTOS));
                    ArrayList userPhotoList;
                    if(tempJsonArray.length() != 0) {
                        userPhotoList = JsonArrayUtil.convert(tempJsonArray);
                        Log.d(TAG,"userFamilies = " + userPhotoList.toString());
                    }
                    else {
                        userPhotoList = new ArrayList();
                    }
                    StaticClass.photos = (ArrayList<String>)userPhotoList;

                    StaticClass._id = object.getString(USER__ID);
                    StaticClass.center_id = object.getString(USER_CENTER_ID);
                    StaticClass.center_name = object.getString(USER_CENTER_NAME);
                    StaticClass.full_name = object.getString(USER_FULL_NAME);
                    StaticClass.login_name = object.getString(USER_LOGIN_NAME);
                    StaticClass.gender = object.getString(USER_GEDER);
                    StaticClass.relation = object.getString(USER_RELATION);
                    StaticClass.email = object.getString(USER_EMAIL);
                    StaticClass.status = object.getInt(USER_STATUS);
                    StaticClass.reged = object.getString(USER_REGED);
                    StaticClass.fcm_id = object.getString(USER_FCM_ID);
                } catch (JSONException e) {
                    // 실패의 경우 로그인 화면으로 이동
                    mSharedPreferences
                            .edit()
                            .putString(USER_TOKEN, null)
                            .apply();
                    Intent i = LoginActivity.getStartIntent(MainActivity.this);
                    startActivity(i);
                    finish();
                }
            }
        });
    }

    private boolean FragmentMenuChange(int menu_id) {
        mPreviousMenuId = menu_id; // 메뉴 아이디를 저장

        switch (menu_id) {
            // side 메뉴 처리
            case R.id.nav_lecture_progress:
            case R.id.bottom_lecture:
                return true;

            case R.id.nav_lecture_reviews:
            case R.id.bottom_review:
                return true;

            case R.id.nav_course_overviews:
                return true;

            case R.id.nav_all:
            case R.id.bottom_notice:
                return true;

            case R.id.nav_my_messages:
                return true;

            case R.id.nav_public_notices:
                return true;

            case R.id.nav_community:
                return true;

            case R.id.nav_blog:
            case R.id.bottom_cs:
                return true;

            case R.id.nav_instagram:
                return true;

            case R.id.nav_facebook:
                return true;

            case R.id.nav_help:
                return true;

            case R.id.nav_account:
                return true;

            case R.id.nav_purchases:
                return true;

            case R.id.nav_terms_privacy:
                return true;

            case R.id.nav_settings:
                return true;


            // bottom 메뉴 처리
            case R.id.bottom_home:
//                home_fragment
                return true;
        }
        return false;
    }
 }
