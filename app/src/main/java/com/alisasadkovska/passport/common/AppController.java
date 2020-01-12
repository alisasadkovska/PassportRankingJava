package com.alisasadkovska.passport.common;

import android.app.Application;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.database.FirebaseDatabase;

import io.paperdb.Paper;

public class AppController extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        MobileAds.initialize(this);
        Paper.init(this);
    }
}
