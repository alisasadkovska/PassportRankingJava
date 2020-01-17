package com.alisasadkovska.passport.common

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import com.google.firebase.database.FirebaseDatabase

object Common {
    const val WIKIPEDIA = "https://en.m.wikipedia.org/wiki/"
    const val RequestCameraPermissionId = 1001

    var COUNTRY:String = ""

    //database
    const val Country_Model = "CountryModel"
    const val Countries = "Countries"
    const val Top = "Top"
    const val Name = "Name"
    const val name = "name"
    const val progress = "progress"
    const val Flag = "Image"
    const val TotalScore = "totalScore"
    const val Color = "color"
    const val Continent = "continent"
    const val Industry = "investment"
    //tiny db
    const val THEME_ID = "theme_id"

    //paperDB
    const val CountryModel = "CountryModel"
    const val CountryName = "CountryName"
    const val Cover = "Cover"
    const val Latitude = "Latitude"
    const val Longitude = "Longitude"
    const val MobilityScore = "MobilityScore"
    const val StatusList = "StatusList"
    const val CountryList = "CountryList"
    const val FlagList = "FlagList"
    const val fontPath = "fonts/Manjari-Regular.ttf"


    fun bigCountries(): List<String> {
        val big: MutableList<String> = ArrayList()
        big.add("Russian Federation")
        big.add("China")
        big.add("United States")
        big.add("Brazil")
        big.add("India")
        big.add("Australia")
        big.add("Canada")
        big.add("Argentina")
        big.add("Chile")
        big.add("Peru")
        big.add("Kazakhstan")
        big.add("Ukraine")
        return big
    }


    private var mDatabase: FirebaseDatabase? = null

    val database: FirebaseDatabase
         get() {
            if (mDatabase == null)
                mDatabase = FirebaseDatabase.getInstance()
            return mDatabase!!
        }
    fun isConnectedToInternet(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val info = connectivityManager.allNetworkInfo
        for (networkInfo in info) {
            if (networkInfo.state == NetworkInfo.State.CONNECTED) return true
        }
        return false
    }
}