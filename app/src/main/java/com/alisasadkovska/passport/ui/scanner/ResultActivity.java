package com.alisasadkovska.passport.ui.scanner;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.alisasadkovska.passport.R;
import com.alisasadkovska.passport.common.Common;
import com.alisasadkovska.passport.common.TinyDB;
import com.alisasadkovska.passport.common.Utils;
import com.alisasadkovska.passport.ui.Home;

import io.github.inflationx.calligraphy3.CalligraphyConfig;
import io.github.inflationx.calligraphy3.CalligraphyInterceptor;
import io.github.inflationx.viewpump.ViewPump;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

public class ResultActivity extends AppCompatActivity {


    TinyDB tinyDB;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }


    public static final String KEY_FIRST_NAME = "firstName";
    public static final String KEY_LAST_NAME = "lastName";
    public static final String KEY_GENDER = "gender";
    public static final String KEY_STATE = "state";
    public static final String KEY_NATIONALITY = "nationality";
    public static final String KEY_PHOTO = "photo";
    public static final String KEY_PHOTO_BASE64 = "photoBase64";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewPump.init(ViewPump.builder()
                .addInterceptor(new CalligraphyInterceptor(
                        new CalligraphyConfig.Builder()
                                .setDefaultFontPath("fonts/Manjari-Regular.ttf")
                                .setFontAttrId(R.attr.fontPath)
                                .build())).build());

        tinyDB = new TinyDB(this);
        Utils.onActivityCreateSetTheme(this, tinyDB.getInt(Common.THEME_ID));
        setContentView(R.layout.activity_result);

        ((TextView) findViewById(R.id.output_first_name)).setText(getIntent().getStringExtra(KEY_FIRST_NAME));
        ((TextView) findViewById(R.id.output_last_name)).setText(getIntent().getStringExtra(KEY_LAST_NAME));
        ((TextView) findViewById(R.id.output_gender)).setText(getIntent().getStringExtra(KEY_GENDER));
        ((TextView) findViewById(R.id.output_state)).setText(getIntent().getStringExtra(KEY_STATE));
        ((TextView) findViewById(R.id.output_nationality)).setText(getIntent().getStringExtra(KEY_NATIONALITY));

        if (getIntent().hasExtra(KEY_PHOTO)) {
            ((ImageView) findViewById(R.id.view_photo)).setImageBitmap(getIntent().getParcelableExtra(KEY_PHOTO));
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(ResultActivity.this, NfcScannerActivity.class);
        startActivity(intent);
    }
}
