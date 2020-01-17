package com.alisasadkovska.passport.ui

import `in`.galaxyofandroid.spinerdialog.SpinnerDialog
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import com.alisasadkovska.passport.Model.CountryModel
import com.alisasadkovska.passport.R
import com.alisasadkovska.passport.common.BaseActivity
import com.alisasadkovska.passport.common.Common
import com.alisasadkovska.passport.common.Common.THEME_ID
import com.alisasadkovska.passport.common.TinyDB
import es.dmoral.toasty.Toasty
import io.paperdb.Paper
import kotlinx.android.synthetic.main.activity_settings.*
import java.util.*

class SettingsActivity : BaseActivity() {

    var changed = false


    private var country = ArrayList<CountryModel>()

    private lateinit var tinyDB: TinyDB
    private lateinit var spinnerDialog:SpinnerDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        tinyDB = TinyDB(this)

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

        day_night_switch.setDuration(500)

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
                Toasty.info(baseContext, getString(R.string.dark_mode_on), Toasty.LENGTH_SHORT).show()
                darkModeOn()
            } else {
                Toasty.info(baseContext, getString(R.string.dark_mode_off), Toasty.LENGTH_SHORT).show()
                darkModeOff()
            }
            changed = true
        }
    }

    private fun showSpinner() {
        spinnerDialog = SpinnerDialog(this, Paper.book().read<ArrayList<String>>(Common.CountryList), getString(R.string.select_your_country), getString(R.string.close))

        spinnerDialog.setCancellable(false)
        spinnerDialog.setShowKeyboard(false)

        spinnerDialog.setTitleColor(resources.getColor(R.color.darkColorPrimaryDark))
        spinnerDialog.setCloseColor(resources.getColor(R.color.visa_required))
        spinnerDialog.setItemColor(resources.getColor(R.color.visa_on_arrival_table))

        spinnerDialog.bindOnSpinerListener { _: String?, pos: Int ->
            val countryName = country[pos].name
            toolbar.title = countryName
            Paper.book().write(Common.CountryName, countryName)
            Paper.book().write(Common.Cover, country[pos].cover)
            Paper.book().write(Common.Latitude, country[pos].latitude)
            Paper.book().write(Common.Longitude, country[pos].longitude)
            Toasty.success(this, "changed to $countryName", Toasty.LENGTH_SHORT).show()
            changed = true
        }
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