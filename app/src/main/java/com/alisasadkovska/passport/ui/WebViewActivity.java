package com.alisasadkovska.passport.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.content.ContextCompat;

import com.alisasadkovska.passport.R;
import com.alisasadkovska.passport.common.Common;
import com.alisasadkovska.passport.common.CustomWebChromeClient;

import java.util.Objects;

public class WebViewActivity extends AppCompatActivity implements CustomWebChromeClient.ProgressListener {

    private ProgressBar mProgressBar;
    private String path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);

        Bundle b = getIntent().getExtras();
        assert b != null;
        String country = b.getString("name");

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(country);
        setSupportActionBar(toolbar);

        if (getSupportActionBar()!=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }


        toolbar.setTitleTextColor(ContextCompat.getColor(this,android.R.color.black));
        Objects.requireNonNull(toolbar.getNavigationIcon()).setColorFilter(getResources().getColor(android.R.color.black), PorterDuff.Mode.SRC_ATOP);



       if (country!=null){
           if (country.contains(" "))
               country = country.replace(" ", "_");
       }


        path = Common.WIKIPEDIA+country;

        WebView mWebView = this.findViewById(R.id.webView);
        mProgressBar = findViewById(R.id.progressBar);

        mProgressBar.setMax(100);
        mWebView.setWebChromeClient(new CustomWebChromeClient(this));

        mWebView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(path);
                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                mProgressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                mProgressBar.setVisibility(View.GONE);
            }
        });
        mWebView.loadUrl(path);
        mProgressBar.setProgress(0);
    }

    @Override
    public void onUpdateProgress(int progressValue) {
        mProgressBar.setProgress(progressValue);
        if (progressValue == 100) {
            mProgressBar.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_webview_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.internet_button:
                CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                CustomTabsIntent customTabsIntent = builder.build();
                customTabsIntent.launchUrl(this, Uri.parse(path));
                return true;
            case R.id.share_button:
                Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                String shareBody = getString(R.string.check_out) + getString(R.string.app_name) + " " + this.path;
                sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "Country Article");
                sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(sharingIntent, getString(R.string.share_with)));
                return true;
            case  android.R.id.home: {
                Intent intent = new Intent(WebViewActivity.this, CountryDetail.class);
                startActivity(intent);
                finish();
            }
        }
        finish();
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(WebViewActivity.this, CountryDetail.class);
        startActivity(intent);
        finish();
    }
}
