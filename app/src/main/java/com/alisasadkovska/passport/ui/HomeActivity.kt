package com.alisasadkovska.passport.ui

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.alisasadkovska.passport.Model.Country
import com.alisasadkovska.passport.R
import com.alisasadkovska.passport.adapter.MenuAdapter
import com.alisasadkovska.passport.adapter.PassportAdapter
import com.alisasadkovska.passport.common.BaseActivity
import com.alisasadkovska.passport.common.Common
import com.alisasadkovska.passport.common.Common.StatusList
import com.alisasadkovska.passport.common.TinyDB
import com.alisasadkovska.passport.common.Utils
import com.alisasadkovska.passport.menu.Menu
import com.google.android.gms.ads.AdRequest
import com.google.android.material.appbar.AppBarLayout
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import es.dmoral.toasty.Toasty
import io.github.inflationx.calligraphy3.CalligraphyConfig
import io.github.inflationx.calligraphy3.CalligraphyInterceptor
import io.github.inflationx.viewpump.ViewPump
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import io.paperdb.Paper
import kotlinx.android.synthetic.main.activity_home.*
import java.util.*
import kotlin.collections.ArrayList

class HomeActivity : BaseActivity() {
    private var countryName = ""
    private var passportCoverUrl = ""

    private var countryFlags = ArrayList<String>()
    private var countries: DatabaseReference?= null
    private var visaList:MutableList<Country> = ArrayList()
    private lateinit var passportAdapter: PassportAdapter

    private var activeFilter = ALL


    companion object{
        const val VISA_FREE = 3
        const val VISA_ON_ARRIVAL = 2
        const val VISA_ETA = 1
        const val VISA_REQUIRED = 0
        const val ALL = "all"
        const val FREE = "free"
        const val ETA = "eta"
        const val REQUIRED = "required"
        const val ARRIVAL = "arrival"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        if (Paper.book().contains(Common.CountryName))
            countryName = Paper.book().read(Common.CountryName)
        else goToSplash()

        countries = Common.database.getReference(Common.Countries)
        countries!!.keepSynced(true)
        visaList = getCountries()

        assignMenu()

        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)

        setSupportActionBar(toolbar)
        appbar.addOnOffsetChangedListener(object : AppBarLayout.BaseOnOffsetChangedListener<AppBarLayout>{

            var scrollRange = -1

            override fun onOffsetChanged(appBarLayout: AppBarLayout?, verticalOffset: Int) {
                if (scrollRange==-1)
                    scrollRange= appBarLayout!!.totalScrollRange

                if (scrollRange+verticalOffset==0){
                    collapsing_toolbar.title = countryName
                    collapsingLayout.visibility = View.INVISIBLE
                    adView.visibility = View.VISIBLE
                }else {
                    collapsing_toolbar.title = " "
                    collapsingLayout.visibility = View.VISIBLE
                    adView.visibility = View.GONE
                }
            }

        })

        assignUI()
        loadData()
    }

    private fun getCountries(): MutableList<Country> {
        val countryList = java.util.ArrayList<Country>()
        val query = countries!!.orderByKey().equalTo(countryName)

        query.addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onCancelled(databaseError: DatabaseError) {
                Toasty.error(this@HomeActivity, databaseError.message, Toasty.LENGTH_SHORT).show()
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (postSnap in dataSnapshot.children){
                    val treeMap = TreeMap<String, Long>(postSnap.value as Map<String, Long>)
                    val status = java.util.ArrayList<Long>()

                    for (entry in treeMap.entries){
                        countryList.addAll(setOf(Country(entry.key, entry.value)))
                        status.add(entry.value)
                        Paper.book().write(StatusList, status)

                        val visaFree = Collections.frequency(status, 3.toLong())
                        val visaEta = Collections.frequency(status, 1.toLong())
                        val visaOnArrival = Collections.frequency(status, 2.toLong())
                        val visaRequired = Collections.frequency(status, 0.toLong())
                        val total = visaFree + visaOnArrival + visaEta

                        Paper.book().write(Common.MobilityScore, total)

                        textTotal.text = total.toString()
                        textEVisa.text = visaEta.toString()
                        textVisaOnArrival.text = visaOnArrival.toString()
                        textVisaRequired.text = visaRequired.toString()
                        textVisaFree.text = visaFree.toString()

                        progressCountry.progress = visaOnArrival
                        progressCountry.secondaryProgress = total

                        passportAdapter.notifyDataSetChanged()
                        coordinator.visibility = View.VISIBLE
                        progressLoad.visibility = View.GONE
                    }
                }
            }

        })

        return countryList
    }

    private fun goToSplash() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }


    private fun assignUI(){
        //visaList = Paper.book().read(Cache)
        passportCoverUrl = Paper.book().read(Common.Cover)
        countryFlags = Paper.book().read(Common.FlagList)

        textCountryName.text = countryName
        Picasso.get().load(passportCoverUrl).into(passportCover, object : Callback {
            override fun onSuccess() {
                coverProgress.visibility = View.GONE
            }
            override fun onError(e: Exception) {
                coverProgress.visibility = View.GONE
                Picasso.get().load(R.drawable.ic_terrain_black_24dp)
                        .into(passportCover)
            } })

        passportCover.setOnClickListener {
            scaleCoverImage(passportCoverUrl, countryName) }

        recycler.setHasFixedSize(true)
        recycler.itemAnimator = DefaultItemAnimator()
        val linearLayoutManager = LinearLayoutManager(this)
        recycler.layoutManager = linearLayoutManager

    }

    private fun assignMenu() {
        val menuList = listOf(
                Menu(getString(R.string.menu_compare), R.drawable.compare),
                Menu(getString(R.string.menu_map), R.drawable.google),
                Menu(getString(R.string.menu_ranking), R.drawable.rank),
                Menu(getString(R.string.menu_explore), R.drawable.explorer),
                Menu(getString(R.string.about), R.drawable.info)
        )

        recyclerMenu.setHasFixedSize(true)
        recyclerMenu.itemAnimator = DefaultItemAnimator()
        recyclerMenu.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        val menuAdapter = MenuAdapter(this,menuList)
        recyclerMenu.adapter = menuAdapter
    }

    private fun loadData() {
        passportAdapter = PassportAdapter(this, visaList, countryFlags)
        recycler.adapter = passportAdapter
    }

    override fun onCreateOptionsMenu(menu: android.view.Menu): Boolean {
        if (themeId==0) menuInflater.inflate(R.menu.home_light_menu, menu)
        else menuInflater.inflate(R.menu.home_dark_menu, menu)

        val mSearch = menu.findItem(R.id.action_search)
        val searchView = mSearch.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(s: String): Boolean {
                return false
            }

            override fun onQueryTextChange(s: String): Boolean {
                passportAdapter.filter.filter(s)
                return false
            }
        })

        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean{
        when(item.itemId){
            R.id.action_settings->{
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
            }
            R.id.allCountries->{
                if (activeFilter == ALL)
                    return false

                loadData()
                activeFilter = ALL
            }
            R.id.visaFree->{
                if (activeFilter == FREE)
                    return false

                filterList(VISA_FREE)
                activeFilter = FREE
            }
            R.id.visaOnArrival->{
                if (activeFilter == ARRIVAL)
                    return false

                filterList(VISA_ON_ARRIVAL)
                activeFilter = ARRIVAL
            }
            R.id.visaEta->{
                if (activeFilter == ETA)
                    return false

                filterList(VISA_ETA)
                activeFilter = ETA
            }
            R.id.visaRequired->{
                if (activeFilter == REQUIRED)
                    return false

                filterList(VISA_REQUIRED)
                activeFilter = REQUIRED
            }
        }
        return super.onOptionsItemSelected(item)
    }
    private fun filterList(filterValue: Int) {
      val filteredList: List<Country> = visaList.filter { country -> country.visaStatus == filterValue.toLong()}
        passportAdapter = PassportAdapter(this, filteredList as MutableList<Country>, countryFlags)
        recycler.adapter = passportAdapter
    }

    @SuppressLint("InflateParams")
    private fun scaleCoverImage(coverPath: String, countryName: String) {
        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setCancelable(true)
        alertDialog.setTitle(countryName)
        val inflater = this.layoutInflater
        val scaleDialogue = inflater.inflate(R.layout.passport_item, null)
        val coverScaled = scaleDialogue.findViewById<ImageView>(R.id.coverScale)
        Picasso.get()
                .load(coverPath)
                .error(R.drawable.ic_terrain_black_24dp)
                .placeholder(R.drawable.progress_animation)
                .into(coverScaled)
        alertDialog.setNegativeButton(getString(R.string.close)) { dialogInterface: DialogInterface, _: Int -> dialogInterface.dismiss() }
        val alert = alertDialog.create()
        alert.setView(scaleDialogue)
        alert.show()
    }
}


