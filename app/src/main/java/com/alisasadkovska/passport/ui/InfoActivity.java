package com.alisasadkovska.passport.ui;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.alisasadkovska.passport.R;

import mehdi.sakout.aboutpage.AboutPage;
import mehdi.sakout.aboutpage.Element;

public class InfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
}
