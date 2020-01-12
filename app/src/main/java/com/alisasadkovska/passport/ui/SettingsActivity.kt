package com.alisasadkovska.passport.ui

import `in`.galaxyofandroid.spinerdialog.SpinnerDialog
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.alisasadkovska.passport.R
import com.alisasadkovska.passport.common.Common
import com.alisasadkovska.passport.common.Common.THEME_ID
import com.alisasadkovska.passport.common.TinyDB
import com.alisasadkovska.passport.common.Utils
import com.alisasadkovska.passport.Model.Country
import com.alisasadkovska.passport.Model.CountryModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import dmax.dialog.SpotsDialog
import es.dmoral.toasty.Toasty
import io.github.inflationx.calligraphy3.CalligraphyConfig
import io.github.inflationx.calligraphy3.CalligraphyInterceptor
import io.github.inflationx.viewpump.ViewPump
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import io.paperdb.Paper
import kotlinx.android.synthetic.main.activity_settings.*
import java.util.*

class SettingsActivity : AppCompatActivity() {

    var visaFree = 0
    var visaRequired = 0
    var visaOnArrival = 0
    var eVisa = 0
    var totalScore = 0

    var themeId = 0

    var changed = false

    private var country = ArrayList<CountryModel>()

    private lateinit var tinyDB: TinyDB
    private lateinit var spinnerDialog:SpinnerDialog

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
        themeId = tinyDB.getInt(THEME_ID)
        Utils.onActivityCreateSetTheme(this, themeId)
        setContentView(R.layout.activity_settings)

        toolbar.title = getString(R.string.action_settings)
        setSupportActionBar(toolbar)

        supportActionBar!!.setDisplayShowTitleEnabled(true)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)

        if (Paper.book().contains(Common.CountryModel)) {
            country = Paper.book().read(Common.CountryModel)
        }

        changeCountryBtn.setOnClickListener {
            if (Common.isConnectedToInternet(this)) {
                showSpinner()
                spinnerDialog.showSpinerDialog()
            } else {
                Toasty.warning(applicationContext, getString(R.string.no_internet), Toast.LENGTH_SHORT, true).show()
            }
        }

        day_night_switch.setDuration(1000)

        when(themeId){
            0->{
                day_night_switch.setIsNight(false)
                day_night_switchTxt.text = getString(R.string.dark_mode_off)
            }
            1->{
                day_night_switch.setIsNight(true)
                day_night_switchTxt.text = getString(R.string.dark_mode_on)
            }
        }

        day_night_switch.setListener { isNight: Boolean ->
            if (isNight){
                Toasty.info(baseContext, getString(R.string.dark_mode_off), Toasty.LENGTH_SHORT).show()
                darkModeOn()
            } else {
                Toasty.info(baseContext, getString(R.string.dark_mode_on), Toasty.LENGTH_SHORT).show()
                darkModeOff()
            }
            changed = true
        }
    }

    private fun showSpinner() {
        spinnerDialog = SpinnerDialog(this, Paper.book().read<ArrayList<String>>(Common.CountryList), getString(R.string.select_your_country), R.style.DialogAnimations_SmileWindow, getString(R.string.close))
        spinnerDialog.setCancellable(true)
        spinnerDialog.setShowKeyboard(false)
        spinnerDialog.setTitleColor(resources.getColor(R.color.colorAccent))
        spinnerDialog.setTitleColor(resources.getColor(R.color.colorPrimaryText))
        spinnerDialog.setCloseColor(resources.getColor(R.color.visa_required))
        spinnerDialog.bindOnSpinerListener { _: String?, pos: Int ->
            val countryName = country[pos].name
            toolbar.title = countryName
            updateList(countryName)
            Paper.book().write(Common.CountryName, countryName)
            Paper.book().write(Common.Cover, country[pos].cover)
            Paper.book().write(Common.Latitude, country[pos].latitude)
            Paper.book().write(Common.Longitude, country[pos].longitude)
            Toasty.success(this, "changed to $countryName", Toasty.LENGTH_SHORT).show()
            changed = true
        }
    }

    private fun updateList(countryName: String) {
        val dialog: AlertDialog = SpotsDialog.Builder()
                .setContext(this)
                .setCancelable(false)
                .setTheme(R.style.MaxDialogDark)
                .build()
        dialog.show()

        val visaList:MutableList<Country> = ArrayList()

        val countries = Common.database.getReference(Common.Countries)
        val query = countries.orderByKey().equalTo(countryName)
        query.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                dialog.dismiss()
                Toasty.error(this@SettingsActivity, error.message, Toasty.LENGTH_SHORT).show()
            }

            override fun onDataChange(dataSnapshot: DataSnapshot){
                for (postSnap in dataSnapshot.children){
                    if (dataSnapshot.exists()){
                        val status = ArrayList<Long>()
                        val data: Map<String, Long> = (postSnap.value as Map<String,Long>)
                        val treeMap: Map<String, Long> = TreeMap(data)

                        for ((key, value) in treeMap) {
                            visaList.addAll(setOf(Country(key, value)))
                            Paper.book().write(Common.Cache, visaList)
                            status.add(value)
                            Paper.book().write(Common.StatusList, status)
                        }
                        visaFree = Collections.frequency(status, 3L)
                        visaOnArrival = Collections.frequency(status, 2L)
                        eVisa = Collections.frequency(status, 1L)
                        visaRequired = Collections.frequency(status, 0L)
                        totalScore = visaFree + visaOnArrival + eVisa

                        Paper.book().write(Common.MobilityScore, totalScore)
                        Paper.book().write(Common.visaFree, visaFree)
                        Paper.book().write(Common.visaOnArrival, visaOnArrival)
                        Paper.book().write(Common.eVisa, eVisa)
                        Paper.book().write(Common.visaRequired, visaRequired)

                        dialog.dismiss()
                    }
                }
            }
        })
    }

    private fun darkModeOff() {
        view.setBackgroundColor(resources.getColor(R.color.colorWhite))
        window.statusBarColor = resources.getColor(R.color.colorPrimaryDark)
        changeCountryBtn!!.setBackgroundColor(resources.getColor(R.color.colorAccent))
        day_night_switchTxt!!.setTextColor(resources.getColor(R.color.colorBlack))
        day_night_switchTxt!!.text = getString(R.string.dark_mode_off)
        toolbar.setBackgroundColor(resources.getColor(R.color.colorPrimary))
        toolbar.setTitleTextColor(resources.getColor(android.R.color.primary_text_light))
        supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_24dp)
        tinyDB.putInt(THEME_ID, 0)
    }

    private fun darkModeOn() {
        view.setBackgroundColor(resources.getColor(R.color.backgroundDark))
        window.statusBarColor = resources.getColor(R.color.darkColorPrimaryDark)
        changeCountryBtn!!.setBackgroundColor(resources.getColor(R.color.darkColorAccent))
        day_night_switchTxt!!.setTextColor(resources.getColor(R.color.colorWhite))
        day_night_switchTxt!!.text = getString(R.string.dark_mode_on)
        toolbar.setBackgroundColor(resources.getColor(R.color.darkColorPrimary))
        toolbar.setTitleTextColor(resources.getColor(android.R.color.primary_text_dark))
        supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp)
        tinyDB.putInt(THEME_ID, 1)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            goToHome()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun goToHome() {
        when {
            changed -> {
                val intent = Intent(this@SettingsActivity, HomeActivity::class.java)
                startActivity(intent)
                finish()
            }
            else -> finish()
        }


    }

    override fun onBackPressed() {
        goToHome()
    }
}