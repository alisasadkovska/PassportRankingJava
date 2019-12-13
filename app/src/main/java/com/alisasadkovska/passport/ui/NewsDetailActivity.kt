package com.alisasadkovska.passport.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.alisasadkovska.passport.R
import com.alisasadkovska.passport.common.Common
import com.alisasadkovska.passport.common.Common.THEME_ID
import com.alisasadkovska.passport.common.TinyDB
import com.alisasadkovska.passport.common.Utils
import com.google.android.material.appbar.AppBarLayout
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import es.dmoral.toasty.Toasty
import io.github.inflationx.calligraphy3.CalligraphyConfig
import io.github.inflationx.calligraphy3.CalligraphyInterceptor
import io.github.inflationx.viewpump.ViewPump
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import kotlinx.android.synthetic.main.activity_news_detail.*
import kotlin.math.abs

class NewsDetailActivity : AppCompatActivity(), AppBarLayout.OnOffsetChangedListener {

    private lateinit var tinyDB: TinyDB
    private var isHideToolbarView = false
    private var mUrl:String?=null
    private var mImg:String?=null
    private var mTitle:String?=null
    private var mDate:String?=null
    private var mSource:String?=null
    private var mAuthor:String?=null

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase!!))
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ViewPump.init(ViewPump.builder()
                .addInterceptor(CalligraphyInterceptor(
                        CalligraphyConfig.Builder()
                                .setDefaultFontPath("fonts/Manjari-Regular.ttf")
                                .setFontAttrId(R.attr.fontPath)
                                .build())).build())

        tinyDB = TinyDB(this)
        Utils.onActivityCreateSetTheme(this, tinyDB.getInt(THEME_ID))
        setContentView(R.layout.activity_news_detail)

        toolbar.title = ""
        collapsing_toolbar.title = ""

        setSupportActionBar(toolbar)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)


        appbar.addOnOffsetChangedListener(this)

        val intent = intent
        mUrl = intent.getStringExtra("url")
        mImg = intent.getStringExtra("img")
        mTitle = intent.getStringExtra("title")
        mDate = intent.getStringExtra("date")
        mSource = intent.getStringExtra("source")
        mAuthor = intent.getStringExtra("author")

      Picasso.get().load(mImg).into(coverImage, object :Callback{
          override fun onSuccess() {
              progressBarCover.visibility = View.GONE
          }

          override fun onError(e: Exception?) {
              progressBarCover.visibility = View.GONE
              Toasty.error(this@NewsDetailActivity, e!!.message.toString(), Toasty.LENGTH_SHORT).show()
          }

      })

        title_on_appbar.text=mSource
        subtitle_on_appbar.text=mUrl
        date.text = Utils.DateFormat(mDate)
        textTitle.text=mTitle

        val author:String = if (mAuthor!=null)
            "\u2022 $mAuthor" else ""

        textTime.text = mSource + author + " \u2022 " + Utils.DateToTimeFormat(mDate)

        initWebView(mUrl)
    }

    private fun initWebView(mUrl: String?) {
        webView.settings.loadsImagesAutomatically=true
        webView.settings.domStorageEnabled=true
        webView.settings.setSupportZoom(true)
        webView.settings.builtInZoomControls=true
        webView.settings.displayZoomControls=true
        webView.scrollBarStyle=View.SCROLLBARS_INSIDE_OVERLAY
        webView.webViewClient= WebViewClient()
        webView.loadUrl(mUrl)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        supportFinishAfterTransition()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onOffsetChanged(appBarLayout: AppBarLayout?, verticalOffset: Int) {
        val maxScroll = appBarLayout!!.totalScrollRange
        val percentage = abs(verticalOffset).toFloat() / maxScroll.toFloat()

        if (percentage == 1f && isHideToolbarView) {
            date_behavior.visibility = View.GONE
            title_appbar.visibility = View.VISIBLE
            isHideToolbarView = !isHideToolbarView
        } else if (percentage < 1f && !isHideToolbarView) {
            date_behavior.visibility = View.VISIBLE
            title_appbar.visibility = View.GONE
            isHideToolbarView = !isHideToolbarView
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (tinyDB.getInt(THEME_ID)==1)
        menuInflater.inflate(R.menu.activity_webview_menu_light, menu)
        else
            menuInflater.inflate(R.menu.activity_webview_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.internet_button->{
                val i = Intent(Intent.ACTION_VIEW)
                i.data = Uri.parse(mUrl)
                startActivity(i)
                return true
            }
            R.id.share_button->{
                try {
                    val i = Intent(Intent.ACTION_SEND)
                    i.type = "text/plan"
                    i.putExtra(Intent.EXTRA_SUBJECT, mSource)
                    val body = "$mTitle\n$mUrl" + getString(R.string.share_from) + getString(R.string.app_name)
                    i.putExtra(Intent.EXTRA_TEXT, body)
                    startActivity(Intent.createChooser(i, getString(R.string.share_with)))
                } catch (e: java.lang.Exception) {
                    Toasty.error(this, getString(R.string.share_error), Toasty.LENGTH_SHORT).show()
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }
}