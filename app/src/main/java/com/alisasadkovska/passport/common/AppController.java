package com.alisasadkovska.passport.common;

import android.app.Application;

import com.droidnet.DroidNet;
import com.google.firebase.database.FirebaseDatabase;

import org.spongycastle.jce.provider.BouncyCastleProvider;

import java.security.Security;

public class AppController extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        DroidNet.init(this);
        Security.insertProviderAt(new BouncyCastleProvider(), 1);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        DroidNet.getInstance().removeAllInternetConnectivityChangeListeners();
    }
}
