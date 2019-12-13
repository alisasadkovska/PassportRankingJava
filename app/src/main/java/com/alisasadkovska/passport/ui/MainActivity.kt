package com.alisasadkovska.passport.ui

import `in`.galaxyofandroid.spinerdialog.SpinnerDialog
import android.Manifest
import android.animation.Animator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.location.Geocoder
import android.location.LocationManager
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.view.View
import android.view.ViewAnimationUtils
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.alisasadkovska.passport.Model.CountryModel
import com.alisasadkovska.passport.R
import com.alisasadkovska.passport.common.Common
import com.alisasadkovska.passport.common.TinyDB
import com.droidnet.DroidListener
import com.droidnet.DroidNet
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import dmax.dialog.SpotsDialog
import es.dmoral.toasty.Toasty
import io.github.inflationx.calligraphy3.CalligraphyConfig
import io.github.inflationx.calligraphy3.CalligraphyInterceptor
import io.github.inflationx.viewpump.ViewPump
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import io.paperdb.Paper
import kotlinx.android.synthetic.main.activity_main.*
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import pub.devrel.easypermissions.EasyPermissions.PermissionCallbacks
import java.io.IOException
import java.util.*

open class MainActivity : AppCompatActivity(), PermissionCallbacks, DroidListener {
    var countryNames = ArrayList<String>()
    var countryFlags = ArrayList<String>()
    var tinyDB: TinyDB? = null

    private lateinit var spinnerDialog:SpinnerDialog
    private lateinit var mDroidNet: DroidNet
    private lateinit var wifiManager: WifiManager

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
        setContentView(R.layout.activity_main)
        Paper.init(this)
        tinyDB = TinyDB(this)
        dataFromFirebase
        mDroidNet = DroidNet.getInstance()
        mDroidNet.addInternetConnectivityListener(this)
        wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        submitBtn.isEnabled = false
        submitBtn.setOnClickListener {
            submitBtn.startLoading()
            submitBtn.postDelayed({
                submitBtn.loadingSuccessful()
                submitBtn.animationEndAction = {
                    toNextPage()
                }
            }, 1000)
        }

        if (Paper.book().contains(Common.CountryName)) {
            goToHomeActivity()
            addBtn.visibility = View.GONE
            submitBtn.visibility = View.GONE
        } else isFirstRun
    }

    private val isFirstRun: Unit get() {}


    private fun toNextPage() {
        val cx = (submitBtn!!.left + submitBtn!!.right) / 2
        val cy = (submitBtn!!.top + submitBtn!!.bottom) / 2
        val animator = ViewAnimationUtils.createCircularReveal(animate_view, cx, cy, 0f, resources.displayMetrics.heightPixels * 1.2f)
        animator.duration = 5000
        animator.interpolator = AccelerateDecelerateInterpolator()
        animate_view!!.visibility = View.VISIBLE
        animator.start()
        animator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animator: Animator) {}
            override fun onAnimationEnd(animator: Animator) {
                startActivity(Intent(this@MainActivity, Home::class.java))
                submitBtn!!.reset()
            }

            override fun onAnimationCancel(animator: Animator) {}
            override fun onAnimationRepeat(animator: Animator) {}
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        mDroidNet.removeInternetConnectivityChangeListener(this)
    }


    private fun askForLocationPermission() {
        val perms = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        if (EasyPermissions.hasPermissions(this, *perms)) {
            setCountryBasedOnUserLocation()
        } else {
            EasyPermissions.requestPermissions(this, getString(R.string.location_rationale),
                    123, *perms)
        }
    }


    @SuppressLint("MissingPermission")
    private fun setCountryBasedOnUserLocation(){
        var countryName: String
        val lm = applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val geocode = Geocoder(applicationContext)
        for (provider in lm.allProviders){
            val location = lm.getLastKnownLocation(provider)
            if (location != null) {
                progressBar!!.visibility = View.VISIBLE
                try {
                    val addresses = geocode.getFromLocation(location.latitude, location.longitude, 1)
                    if (addresses != null && addresses.size > 0) {
                        countryName = addresses[0].countryName
                        selectCountry(countryName)
                        break
                    }
                } catch (e: IOException) {
                    progressBar!!.visibility = View.GONE
                    e.printStackTrace()
                }
            }
        }
    }

    private fun selectCountry(country_name: String) {
        val country = Common.getDatabase().getReference(Common.Country_Model)
        val query = country.orderByChild(Common.Name).equalTo(country_name)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (postSnap in dataSnapshot.children) {
                    val model = postSnap.getValue(CountryModel::class.java)!!
                    Picasso.get()
                            .load(model.cover)
                            .into(passportCover, object : Callback {
                                override fun onSuccess() {
                                    progressBar!!.visibility = View.GONE
                                }

                                override fun onError(e: Exception) {}
                            })
                    countryName!!.text = country_name
                    Paper.book().write(Common.CountryName, country_name)
                    Paper.book().write(Common.Cover, model.cover)
                    Paper.book().write(Common.Latitude, model.latitude)
                    Paper.book().write(Common.Longitude, model.longitude)
                    submitBtn!!.isEnabled = true
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toasty.error(baseContext, getString(R.string.error_toast) + databaseError.message, 5).show()
            }
        })
    }

    private val dataFromFirebase: Unit
        get() {
            val countryModel = Common.getDatabase().getReference(Common.Country_Model)
            countryModel.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (postSnap in dataSnapshot.children) {
                        val model = postSnap.getValue(CountryModel::class.java)
                        Common.countryModel.add(model)
                        val countryNamesData = postSnap.child(Common.Name).getValue(String::class.java)!!
                        countryNames.add(countryNamesData)
                        Paper.book().write(Common.CountryList, countryNames)
                        val countryFlagsData = postSnap.child(Common.Flag).getValue(String::class.java)!!
                        countryFlags.add(countryFlagsData)
                        Paper.book().write(Common.FlagList, countryFlags)
                        spinnerDialog = SpinnerDialog(this@MainActivity, countryNames, getString(R.string.select_your_country), R.style.AppTheme, getString(R.string.close))
                        spinnerDialog.setCancellable(true)
                        spinnerDialog.setShowKeyboard(false)
                        spinnerDialog.setTitleColor(resources.getColor(R.color.colorAccent))
                        spinnerDialog.setTitleColor(resources.getColor(R.color.colorPrimaryText))
                        spinnerDialog.setCloseColor(resources.getColor(R.color.visa_required))
                        spinnerDialog.bindOnSpinerListener { _: String?, pos: Int ->
                            progressBar!!.visibility = View.VISIBLE
                            assert(model != null)
                            Picasso.get()
                                    .load(Common.countryModel[pos].cover)
                                    .into(passportCover, object : Callback {
                                        override fun onSuccess() {
                                            progressBar!!.visibility = View.GONE
                                        }

                                        override fun onError(e: Exception) {}
                                    })
                            countryName!!.text = Common.countryModel[pos].name
                            Paper.book().write(Common.CountryName, Common.countryModel[pos].name)
                            Paper.book().write(Common.Cover, Common.countryModel[pos].cover)
                            Paper.book().write(Common.Latitude, Common.countryModel[pos].latitude)
                            Paper.book().write(Common.Longitude, Common.countryModel[pos].longitude)
                            submitBtn!!.isEnabled = true
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Toasty.error(baseContext, getString(R.string.error_toast) + databaseError.message, 5).show()
                }
            })
        }

    private fun showSnackbar() {
        val snackbar = Snackbar
                .make(root!!, "No internet connection!", Snackbar.LENGTH_INDEFINITE)
                .setTextColor(resources.getColor(R.color.colorPrimary))
                .setAction("CONNECT") { if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) wifiManager!!.isWifiEnabled = true else startActivity(Intent(Settings.ACTION_WIFI_SETTINGS)) }
        snackbar.setActionTextColor(Color.RED)
        snackbar.show()
    }

    private fun goToHomeActivity() {
        addBtn!!.isEnabled = false
        submitBtn!!.isEnabled = false
        val splash = findViewById<ImageView>(R.id.splashImage)
        splash.visibility = View.VISIBLE
        SpotsDialog.Builder()
                .setContext(this)
                .setCancelable(false)
                .setMessage(getString(R.string.app_name))
                .build()
                .show()
        val handler = Handler()
        val runnable = Runnable {
            startActivity(Intent(this@MainActivity, Home::class.java))
            finish()
        }
        handler.postDelayed(runnable, 3000)
    }

    override fun onResume() {
        super.onResume()
        if (!Paper.book().contains(Common.CountryName)) askForLocationPermission()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        setCountryBasedOnUserLocation()
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {}
    override fun onInternetConnectivityChanged(isConnected: Boolean) {
        if (isConnected) {
            if (!Paper.book().contains(Common.CountryName)) {
                askForLocationPermission()
                addBtn!!.isEnabled = true
                addBtn!!.setOnClickListener {
                    if (countryNames.size > 0) {
                        spinnerDialog.showSpinerDialog()
                    } else {
                        Toasty.error(applicationContext, getString(R.string.poor_internet), Toast.LENGTH_SHORT, true).show()
                    }
                }
            }
        } else {
            addBtn!!.isEnabled = false
            if (!Paper.book().contains(Common.CountryName)) showSnackbar() else Toasty.warning(applicationContext, getString(R.string.no_internet), Toast.LENGTH_SHORT, true).show()
        }
    }
}