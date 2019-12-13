package com.alisasadkovska.passport.common;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.alisasadkovska.passport.Model.CountryModel;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class Common {

    public static List<CountryModel> countryModel = new ArrayList<>();
    public static final String WIKIPEDIA = "https://en.m.wikipedia.org/wiki/";

    public static String COUNTRY;

    public static final int RequestCameraPermissionId = 1001;

    //firebase
    public static final String Country_Model ="CountryModel";
    public static final String Countries = "Countries";
    public static final String Top = "Top";
    public static final String Name = "Name";
    public static final String name = "name";
    public static final String progress = "progress";
    public static final String Flag = "Image";
    public static final String TotalScore = "totalScore";
    public static final String Color = "color";
    public static final String Continent = "continent";
    public static final String Industry = "investment";

    //tiny db
    public static final String IS_DARK_MODE = "is_dark_mode";
    public static final String THEME_ID = "theme_id";
    public static final String PASSPORT_NUMBER = "passportNumber";
    public static final String EXPIRATION_DATE ="expiration";
    public static final String BIRTH_DATE = "birth";

    //paperDB
    public static final String CountryName = "CountryName";
    public static final String Cover = "Cover";
    public static final String Latitude = "Latitude";
    public static final String Longitude = "Longitude";
    public static final String MobilityScore = "MobilityScore";

    public static final String StatusList = "StatusList";
    public static final String CountryList = "CountryList";
    public static final String FlagList = "FlagList";



    //big fat
    public static List<String>bigCountries(){
        List<String>big = new ArrayList<>();
        big.add("Russian Federation");
        big.add("China");
        big.add("United States");
        big.add("Brazil");
        big.add("India");
        big.add("Australia");
        big.add("Canada");
        big.add("Argentina");
        big.add("Chile");
        big.add("Peru");
        big.add("Kazakhstan");
        return big;
    }


    private static FirebaseDatabase mDatabase;
    public static FirebaseDatabase getDatabase() {
        if (mDatabase == null)
            mDatabase = FirebaseDatabase.getInstance();
        return mDatabase;
    }

    public static boolean isConnectedToInternet(Context context)
    {
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null)
        {
            NetworkInfo[]info = connectivityManager.getAllNetworkInfo();

            for (NetworkInfo networkInfo : info) {
                if (networkInfo.getState() == NetworkInfo.State.CONNECTED)
                    return true;
            }
        }
        return false;
    }



}
