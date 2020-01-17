package com.alisasadkovska.passport.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.alisasadkovska.passport.Model.Country
import com.alisasadkovska.passport.R
import com.alisasadkovska.passport.adapter.PassportAdapter
import com.alisasadkovska.passport.common.BaseActivity
import com.alisasadkovska.passport.common.Common
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.AppBarLayout.Behavior.DragCallback
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import es.dmoral.toasty.Toasty
import io.paperdb.Paper
import kotlinx.android.synthetic.main.activity_country_detail.*
import java.util.*

class CountryDetail : BaseActivity() {
    private var menu: Menu? = null

    var queryCountry = ""
    lateinit var passportAdapter: PassportAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_country_detail)

        queryCountry = Common.COUNTRY
        if (queryCountry=="")
            goToHomeActivity()

        setSupportActionBar(toolbar)

        supportActionBar!!.setDisplayShowTitleEnabled(true)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)

        val countryFlags = Paper.book().read<ArrayList<String>>(Common.FlagList)

        appbar.addOnOffsetChangedListener(object : AppBarLayout.BaseOnOffsetChangedListener<AppBarLayout>{

            var scrollRange = -1

            override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.totalScrollRange
                }
                if (scrollRange + verticalOffset == 0) {
                    collapsing_toolbar.title = queryCountry
                    showOption()
                } else  {
                    collapsing_toolbar.title = " "
                    hideOption()
                }
            }

        })


        val params = appbar.layoutParams as CoordinatorLayout.LayoutParams
        val behavior = AppBarLayout.Behavior()
        behavior.setDragCallback(object : DragCallback() {
            override fun canDrag(appBarLayout: AppBarLayout): Boolean {
                return false
            }
        })

        params.behavior = behavior

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
            fabFlag.visibility = View.GONE
            mapView.visibility = View.GONE
            no_internet.visibility = View.VISIBLE
            mapProgress.visibility = View.GONE
        }

        recycler.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(this)
        recycler.layoutManager = layoutManager
        recycler.itemAnimator = DefaultItemAnimator()
        passportAdapter = PassportAdapter(applicationContext, countries, countryFlags)

        fab.setOnClickListener {
            if (Common.isConnectedToInternet(this))
            goToWikipedia() else  Toasty.warning(this, getString(R.string.no_internet), Toasty.LENGTH_SHORT).show()
        }
    }

    private fun goToHomeActivity() {
        val intent = Intent(this@CountryDetail, MainActivity::class.java)
        startActivity(intent)
    }

    private val countries: ArrayList<Country>
        get() {
            val countryList = ArrayList<Country>()
            Common.database.getReference(Common.Countries)
                    .orderByKey().equalTo(queryCountry).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (postSnap in dataSnapshot.children) {

                        val treeMap = TreeMap<String, Long>(postSnap.value as Map<String, Long>)
                        val status = ArrayList<Long>()

                        for (entry in treeMap.entries) {
                            countryList.addAll(setOf(Country(entry.key, entry.value)))
                            status.add(entry.value)

                            val visaFree = Collections.frequency(status, 3L)
                            val visaOnArrival = Collections.frequency(status, 2L)
                            val visaEta = Collections.frequency(status, 1L)
                            val visaRequired = Collections.frequency(status, 0L)
                            val total = visaFree + visaOnArrival + visaEta

                            textTotal.text = total.toString()
                            textVisaFree.text = visaFree.toString()
                            textEVisa.text = visaEta.toString()
                            textVisaOnArrival.text = visaOnArrival.toString()
                            textVisaRequired.text = visaRequired.toString()
                            recycler.adapter = passportAdapter
                            passportAdapter.notifyDataSetChanged()
                            loading_recycler.visibility = View.GONE
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Toasty.error(this@CountryDetail, databaseError.message, Toasty.LENGTH_SHORT).show()
                    loading_recycler.visibility = View.GONE
                }
            })
            return countryList
        }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        this.menu = menu
        menuInflater.inflate(R.menu.menu_action_country_detail, menu)
        hideOption()
        return true
    }

    private fun hideOption() {
        val item = menu?.findItem(R.id.wikipedia)
        item?.isVisible = false
        fab.show()
    }

    private fun showOption() {
        val item = menu?.findItem(R.id.wikipedia)
        item?.isVisible = true
        fab.hide()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.wikipedia->{
                if (Common.isConnectedToInternet(this))goToWikipedia()
                else Toasty.warning(this, getString(R.string.no_internet), Toasty.LENGTH_SHORT).show()
            }
            android.R.id.home->{
                finish()
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun goToWikipedia() {
        val browserIntent = Intent(this@CountryDetail, WebViewActivity::class.java)
        browserIntent.putExtra("name", queryCountry)
        startActivity(browserIntent)
    }

    private fun loadMap(){
        Common.database.getReference(Common.Country_Model).orderByChild(Common.Name).equalTo(queryCountry)
                .addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (postSnap in dataSnapshot.children) {
                    val latitude = postSnap.child("Latitude").value as Double?
                    val longitude = postSnap.child("Longitude").value as Double?
                    mapView.getMapAsync { map: GoogleMap ->
                        if (latitude != null && longitude != null) {
                            val current = LatLng(latitude, longitude)
                            var scale = 6f
                            if (Common.bigCountries().contains(queryCountry)) scale = 3f
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(current, scale))
                            mapProgress.visibility = View.GONE
                            val finalScale = scale
                            fabFlag.setOnClickListener { map.moveCamera(CameraUpdateFactory.newLatLngZoom(current, finalScale)) }
                        }
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                mapProgress.visibility = View.GONE
                Toasty.error(this@CountryDetail, getString(R.string.error_toast) + databaseError.message, 5).show()
            }
        })
    }
    public override fun onResume() {
        super.onResume()
        mapView.onResume()
    }
    public override fun onPause() {
        super.onPause()
        mapView.onPause()
    }
    public override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }
    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }
}