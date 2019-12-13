package com.alisasadkovska.passport.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.alisasadkovska.passport.R;
import com.alisasadkovska.passport.common.Common;
import com.alisasadkovska.passport.common.TinyDB;
import com.alisasadkovska.passport.common.Utils;
import com.alisasadkovska.passport.ui.Fragments.CompareFragment;
import com.alisasadkovska.passport.ui.Fragments.ExploreFragment;
import com.alisasadkovska.passport.ui.Fragments.HomeFragment;
import com.alisasadkovska.passport.ui.Fragments.MapFragment;
import com.alisasadkovska.passport.ui.Fragments.NewsFragment;
import com.alisasadkovska.passport.ui.Fragments.RankingFragment;
import com.alisasadkovska.passport.ui.scanner.NfcScannerActivity;
import com.droidnet.DroidListener;
import com.droidnet.DroidNet;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import io.github.inflationx.calligraphy3.CalligraphyConfig;
import io.github.inflationx.calligraphy3.CalligraphyInterceptor;
import io.github.inflationx.viewpump.ViewPump;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;
import io.paperdb.Paper;

public class Home extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, DroidListener{

    Toolbar toolbar;
    TinyDB tinyDB;

    private DroidNet mDroidNet;
    private WifiManager wifiManager;

    FrameLayout root;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MobileAds.initialize(this,
                getString(R.string.NAL_admob_api_key));

        ViewPump.init(ViewPump.builder()
                .addInterceptor(new CalligraphyInterceptor(
                        new CalligraphyConfig.Builder()
                                .setDefaultFontPath("fonts/Manjari-Regular.ttf")
                                .setFontAttrId(R.attr.fontPath)
                                .build())).build());

        tinyDB = new TinyDB(this);
        Utils.onActivityCreateSetTheme(this, tinyDB.getInt(Common.THEME_ID));



        setContentView(R.layout.activity_home);


        if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, Common.RequestCameraPermissionId);
        }
        
        mDroidNet = DroidNet.getInstance();
        mDroidNet.addInternetConnectivityListener(this);

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.menu_ranking));
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        View header = navigationView.getHeaderView(0);
        Button nfcScannerButton = header.findViewById(R.id.scannerButton);
        nfcScannerButton.setOnClickListener(v -> {
            Intent intent = new Intent(Home.this, NfcScannerActivity.class);
            startActivity(intent);
        });

        navigationView.setItemIconTintList(null);
        navigationView.getMenu().getItem(0).setChecked(true);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        setDefaultFragment();


        MobileAds.initialize(this, initializationStatus -> {
        });
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDroidNet.removeInternetConnectivityChangeListener(this);
    }

    private void showSnackbar() {
        root = findViewById(R.id.container);
        Snackbar snackbar = Snackbar
                .make(root, "No internet connection!", Snackbar.LENGTH_INDEFINITE)
                .setAction("CONNECT", view -> {
                    if(android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.Q)
                        wifiManager.setWifiEnabled(true);
                    else
                        startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                });
        snackbar.setActionTextColor(Color.RED);
        snackbar.setTextColor(Color.WHITE);
        snackbar.show();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent(Home.this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }else if(id == R.id.action_info){
            Intent intent = new Intent(Home.this, InfoActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        Fragment selectedFragment = null;
        int id = item.getItemId();

        switch (id){
            case R.id.nav_home:
                selectedFragment = HomeFragment.newInstance(this);
                toolbar.setTitle(getString(R.string.menu_home));
                break;
                case R.id.nav_news:
                selectedFragment = NewsFragment.newInstance(this);
                toolbar.setTitle(getString(R.string.news));
                break;
            case R.id.nav_compare:
                selectedFragment = CompareFragment.newInstance(this);
                toolbar.setTitle(getString(R.string.menu_compare));
                break;
            case R.id.nav_ranking:
                selectedFragment = RankingFragment.newInstance(this);
                toolbar.setTitle(getString(R.string.menu_ranking));
                break;
            case R.id.nav_explore:
                selectedFragment = ExploreFragment.newInstance(this);
                toolbar.setTitle(getString(R.string.menu_explore));
                break;
            case R.id.nav_map:
                selectedFragment = MapFragment.newInstance(this, tinyDB);
                toolbar.setTitle(Paper.book().read(Common.CountryName));
                break;

        }

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        assert selectedFragment != null;
        transaction.replace(R.id.container, selectedFragment);
        transaction.commit();

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    void setDefaultFragment()
    {
       FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, HomeFragment.newInstance(this));
        transaction.commit();
    }

    @Override
    public void onInternetConnectivityChanged(boolean isConnected) {
        if (!isConnected){
            showSnackbar();
        }
    }
}
