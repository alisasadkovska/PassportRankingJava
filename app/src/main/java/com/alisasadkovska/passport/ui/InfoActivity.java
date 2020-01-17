package com.alisasadkovska.passport.ui;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatDelegate;

import com.alisasadkovska.passport.R;
import com.alisasadkovska.passport.common.BaseActivity;

import mehdi.sakout.aboutpage.AboutPage;
import mehdi.sakout.aboutpage.Element;

public class InfoActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        simulateDayNight(getThemeId());
        View aboutView = new AboutPage(this)
                .isRTL(false)
                .setImage(R.mipmap.ic_launcher)
                .addItem(new Element().setTitle(getString(R.string.version)))
                .setDescription(getString(R.string.about_app))
                .addGroup(getString(R.string.about_contact_us))
                .addEmail("alisasadkovska@gmail.com")
                .addGitHub("alisasadkovska")
                .addYoutube("UCq32fGN7VKR4yBBPqgZ_AMA",getString(R.string.tutorial))
                .addPlayStore(getPackageName())
                .create();

        setContentView(aboutView);
    }

    private void simulateDayNight(int themeId) {
        final int DAY = 0;
        final int NIGHT = 1;

        int currentNightMode = getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK;
        if (themeId == DAY && currentNightMode != Configuration.UI_MODE_NIGHT_NO) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else if (themeId == NIGHT && currentNightMode != Configuration.UI_MODE_NIGHT_YES) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
    }
}
