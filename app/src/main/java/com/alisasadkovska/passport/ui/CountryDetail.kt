package com.alisasadkovska.passport.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.AsyncTask
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.alisasadkovska.passport.Adapter.PassportAdapter
import com.alisasadkovska.passport.Model.Country
import com.alisasadkovska.passport.R
import com.alisasadkovska.passport.common.Common
import com.alisasadkovska.passport.common.TinyDB
import com.alisasadkovska.passport.common.Utils
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.AppBarLayout.Behavior.DragCallback
import com.google.android.material.appbar.AppBarLayout.OnOffsetChangedListener
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.squareup.picasso.Picasso.LoadedFrom
import com.squareup.picasso.Target
import es.dmoral.toasty.Toasty
import io.github.inflationx.calligraphy3.CalligraphyConfig
import io.github.inflationx.calligraphy3.CalligraphyInterceptor
import io.github.inflationx.viewpump.ViewPump
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import io.paperdb.Paper
import kotlinx.android.synthetic.main.activity_country_detail.*
import kotlinx.android.synthetic.main.fragment_news.*
import java.util.*

class CountryDetail : AppCompatActivity() {
    var tinyDB: TinyDB? = null

    var passportAdapter: PassportAdapter? = null
    private var menu: Menu? = null

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ViewPump.init(ViewPump.builder()
                .addInterceptor(CalligraphyInterceptor(
                        CalligraphyConfig.Builder()
                                .setDefaultFontPath("fonts/Manjari-Regular.ttf")
                                .setFontAttrId(R.attr.fontPath)
                                .build())).build())
        tinyDB = TinyDB(this)
        Utils.onActivityCreateSetTheme(this, tinyDB!!.getInt(Common.THEME_ID))
        if (Common.COUNTRY == null) goToHomeActivity()
        setContentView(R.layout.activity_country_detail)
        setSupportActionBar(toolbar)

        if (supportActionBar != null) {
            supportActionBar!!.setDisplayShowTitleEnabled(true)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setDisplayShowHomeEnabled(true)
        }

        val countryFlags = Paper.book().read<ArrayList<String>>(Common.FlagList)

        val appBarLayout = findViewById<AppBarLayout>(R.id.appbar)

        appBarLayout.addOnOffsetChangedListener(object : OnOffsetChangedListener {
            var isShow = false
            var scrollRange = -1
            override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.totalScrollRange
                }
                if (scrollRange + verticalOffset == 0) {
                    isShow = true
                    showOption()
                } else if (isShow) {
                    isShow = false
                    hideOption()
                }
            }
        })
        val params = appBarLayout.layoutParams as CoordinatorLayout.LayoutParams
        val behavior = AppBarLayout.Behavior()
        behavior.setDragCallback(object : DragCallback() {
            override fun canDrag(appBarLayout: AppBarLayout): Boolean {
                return false
            }
        })

        params.behavior = behavior
        val ctl = findViewById<CollapsingToolbarLayout>(R.id.collapsing_toolbar)
        ctl.title = Common.COUNTRY
        ctl.setExpandedTitleColor(resources.getColor(R.color.colorBlack))

        mapView.onCreate(savedInstanceState)
        mapView.onResume()

        try {
            MapsInitializer.initialize(applicationContext)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (Common.isConnectedToInternet(applicationContext)) {
            loadMap()
        } else {
            mapView.visibility = View.GONE
            no_internet.visibility = View.VISIBLE
            mapProgress.visibility = View.GONE
        }

        recycler.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(this)
        recycler.layoutManager = layoutManager
        recycler.itemAnimator = DefaultItemAnimator()
        passportAdapter = PassportAdapter(applicationContext, countries, countryFlags)

        fab.setOnClickListener { goToWikipedia() }
    }

    private fun goToHomeActivity() {
        val intent = Intent(this@CountryDetail, MainActivity::class.java)
        startActivity(intent)
    }

    private val countries: ArrayList<Country>
        get() {
            val countryList = ArrayList<Country>()
            val query = Common.getDatabase().getReference(Common.Countries)
                    .orderByKey().equalTo(Common.COUNTRY)
            query.addValueEventListener(object : ValueEventListener {
                @SuppressLint("NewApi")
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (postSnap in dataSnapshot.children) {
                        val data: Map<String, Long> = (postSnap.value as Map<String,Long>?)!!
                        val treeMap: Map<String, Long> = TreeMap(data)
                        val status = ArrayList<Long?>()

                        for ((key, value) in treeMap) {
                            countryList.addAll(setOf(Country(key, value)))
                            status.add(value)

                            val visaFree = Collections.frequency(status, 3.toLong())
                            val visaOnArrival = Collections.frequency(status, 2.toLong())
                            val visaEta = Collections.frequency(status, 1.toLong())
                            val visaRequired = Collections.frequency(status, 0.toLong())
                            val total = visaFree + visaOnArrival + visaEta

                            textTotal!!.text = total.toString()
                            textVisaFree!!.text = visaFree.toString()
                            textEVisa!!.text = visaEta.toString()
                            textVisaOnArrival!!.text = visaOnArrival.toString()
                            textVisaRequired!!.text = visaRequired.toString()
                            recycler!!.adapter = passportAdapter
                            passportAdapter!!.notifyDataSetChanged()
                            loading_recycler!!.visibility = View.GONE
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    loading_recycler!!.visibility = View.GONE
                }
            })
            return countryList
        }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this@CountryDetail, Home::class.java)
        startActivity(intent)
        finish()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        this.menu = menu
        menuInflater.inflate(R.menu.menu_action_country_detail, menu)
        hideOption()
        return true
    }

    private fun hideOption() {
        val item = menu!!.findItem(R.id.wikipedia)
        item.isVisible = false
        fab!!.show()
    }

    private fun showOption() {
        val item = menu!!.findItem(R.id.wikipedia)
        item.isVisible = true
        fab!!.hide()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.wikipedia) goToWikipedia()
        if (item.itemId == android.R.id.home) {
            val intent = Intent(this@CountryDetail, Home::class.java)
            startActivity(intent)
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun goToWikipedia() {
        val browserIntent = Intent(this@CountryDetail, WebViewActivity::class.java)
        startActivity(browserIntent)
    }


    private fun loadMap(){
        val mapQuery = Common.getDatabase().getReference(Common.Country_Model)
                .orderByChild(Common.Name).equalTo(Common.COUNTRY)
        mapQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (postSnap in dataSnapshot.children) {
                    val latitude = postSnap.child("Latitude").value as Double?
                    val longitude = postSnap.child("Longitude").value as Double?
                    mapView!!.getMapAsync { map: GoogleMap ->
                        if (latitude != null && longitude != null) {
                            val current = LatLng(latitude, longitude)
                            var scale = 6f
                            if (Common.bigCountries().contains(Common.COUNTRY)) scale = 3f
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(current, scale))
                            mapProgress!!.visibility = View.GONE
                            val flag = findViewById<FloatingActionButton>(R.id.flag)
                            val flagPath = postSnap.child(Common.Flag).value as String?
                            val ab = supportActionBar
                            Picasso.get().load(flagPath).into(object : Target {
                                override fun onBitmapLoaded(bitmap: Bitmap, from: LoadedFrom) {
                                    val drawable: Drawable = BitmapDrawable(resources, bitmap)
                                    assert(ab != null)
                                    ab!!.setIcon(drawable)
                                }

                                override fun onBitmapFailed(e: Exception, errorDrawable: Drawable) {}
                                override fun onPrepareLoad(placeHolderDrawable: Drawable) {}
                            })
                            val finalScale = scale
                            flag.setOnClickListener { map.moveCamera(CameraUpdateFactory.newLatLngZoom(current, finalScale)) }
                        }
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                mapProgress!!.visibility = View.GONE
                Toasty.error(baseContext, getString(R.string.error_toast) + databaseError.message, 5).show()
            }
        })
    }



    public override fun onResume() {
        super.onResume()
        mapView!!.onResume()
    }

    public override fun onPause() {
        super.onPause()
        mapView!!.onPause()
    }

    public override fun onDestroy() {
        super.onDestroy()
        mapView!!.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView!!.onLowMemory()
    }
}