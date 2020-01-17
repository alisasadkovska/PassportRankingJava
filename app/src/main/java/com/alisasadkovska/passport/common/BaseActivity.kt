package com.alisasadkovska.passport.common

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.alisasadkovska.passport.R
import io.github.inflationx.calligraphy3.CalligraphyConfig
import io.github.inflationx.calligraphy3.CalligraphyInterceptor
import io.github.inflationx.viewpump.ViewPump
import io.github.inflationx.viewpump.ViewPumpContextWrapper

abstract class BaseActivity : AppCompatActivity(){

    var themeId = 0
    private lateinit var tinyDB: TinyDB

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ViewPump.init(ViewPump.builder()
                .addInterceptor(CalligraphyInterceptor(
                        CalligraphyConfig.Builder()
                                .setDefaultFontPath(Common.fontPath)
                                .setFontAttrId(R.attr.fontPath)
                                .build())).build())

        tinyDB = TinyDB(this)
        themeId = tinyDB.getInt(Common.THEME_ID)
        Utils.onActivityCreateSetTheme(this, themeId)
    }
}