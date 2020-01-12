package com.alisasadkovska.passport.ui

import `in`.galaxyofandroid.spinerdialog.SpinnerDialog
import android.Manifest
import android.animation.Animator
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.location.Geocoder
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewAnimationUtils
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.core.location.LocationManagerCompat
import com.alisasadkovska.passport.R
import com.alisasadkovska.passport.common.Common
import com.alisasadkovska.passport.common.Common.Cache
import com.alisasadkovska.passport.common.Common.Countries
import com.alisasadkovska.passport.common.Common.CountryName
import com.alisasadkovska.passport.common.Common.StatusList
import com.alisasadkovska.passport.common.Common.database
import com.alisasadkovska.passport.Model.Country
import com.alisasadkovska.passport.Model.CountryModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import dmax.dialog.SpotsDialog
import es.dmoral.toasty.Toasty
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import io.paperdb.Paper
import kotlinx.android.synthetic.main.activity_main.*
import pub.devrel.easypermissions.EasyPermissions
import pub.devrel.easypermissions.EasyPermissions.PermissionCallbacks
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity(), PermissionCallbacks {

    var countryNames = ArrayList<String>()
    var countryFlags = ArrayList<String>()

    var homeCountry = ""

    private lateinit var spinnerDialog: SpinnerDialog

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?){
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (Paper.book().contains(CountryName)){
            homeCountry = Paper.book().read(CountryName)
            startSplashScreen(homeCountry)
        }else{
            assignSpinnerDialogue()
            addBtn.setOnClickListener {
                if (countryNames.size != 0) {
                    spinnerDialog.showSpinerDialog()
                }
            }

            submitBtn.isEnabled = false
            submitBtn.setOnClickListener {
                addBtn.isEnabled = false
                updateList(homeCountry)
                submitBtn.startLoading()
                submitBtn.postDelayed({
                    submitBtn.loadingSuccessful()
                    submitBtn.animationEndAction = {
                        toNextPage()
                    }
                }, 1000)
            }
        }
    }

    override fun onResume(){
        super.onResume()
        if (homeCountry=="" && isLocationEnabled(applicationContext))
        askForLocationPermission()
    }

    private fun isLocationEnabled(mContext: Context): Boolean {
        val lm = mContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER) || lm.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER)
    }

    //first run
    private fun assignSpinnerDialogue() {
            val country: MutableList<CountryModel> = dataFromDatabase()

            spinnerDialog = SpinnerDialog(this@MainActivity, countryNames, getString(R.string.select_your_country), R.style.DialogAnimations_SmileWindow, getString(R.string.close))
            spinnerDialog.setCancellable(true)
            spinnerDialog.setShowKeyboard(false)
            spinnerDialog.setTitleColor(resources.getColor(R.color.colorAccent))
            spinnerDialog.setTitleColor(resources.getColor(R.color.colorPrimaryText))
            spinnerDialog.setCloseColor(resources.getColor(R.color.visa_required))

            spinnerDialog.bindOnSpinerListener { _: String, pos: Int ->
                progressBar.visibility = View.VISIBLE
                Picasso.get()
                        .load(country[pos].cover)
                        .into(passportCover, object : Callback {
                            override fun onSuccess() {
                                progressBar.visibility = View.GONE
                            }

                            override fun onError(e: Exception) {
                                Toasty.error(this@MainActivity, e.message.toString(), Toasty.LENGTH_SHORT).show()
                            }
                        })
                homeCountry = country[pos].name
                countryName.text = country[pos].name
                Paper.book().write(CountryName, country[pos].name)
                Paper.book().write(Common.Cover, country[pos].cover)
                Paper.book().write(Common.Latitude, country[pos].latitude)
                Paper.book().write(Common.Longitude, country[pos].longitude)
                submitBtn.isEnabled = true
            }
    }

    //must be executed before next activity starts
    private fun updateList(countryName: String){
        val dialog: AlertDialog = SpotsDialog.Builder()
                .setContext(this)
                .setCancelable(false)
                .setTheme(R.style.MaxDialogDark)
                .build()
        dialog.show()

        val visaList:MutableList<Country> = ArrayList()

        val countries = database.getReference(Countries)
        val query = countries.orderByKey().equalTo(countryName)

        query.addListenerForSingleValueEvent(object : ValueEventListener{



            override fun onCancelled(error: DatabaseError) {
                dialog.dismiss()
                Toasty.error(this@MainActivity, error.message, Toasty.LENGTH_SHORT).show()
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (postSnap in dataSnapshot.children){
                    if (dataSnapshot.exists()){
                        val status = ArrayList<Long>()
                        val data: Map<String, Long> = (postSnap.value as Map<String,Long>)
                        val treeMap: Map<String, Long> = TreeMap(data)

                        for ((key, value) in treeMap) {
                            visaList.addAll(setOf(Country(key, value)))
                            Paper.book().write(Cache, visaList)
                            status.add(value)
                            Paper.book().write(StatusList, status)
                        }

                        val visaFree = Collections.frequency(status, 3L)
                        val visaOnArrival = Collections.frequency(status, 2L)
                        val eVisa = Collections.frequency(status, 1L)
                        val visaRequired = Collections.frequency(status, 0L)
                        val totalScore = visaFree + visaOnArrival + eVisa

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


    private fun startSplashScreen(country: String) {
        updateList(country)
        startNewActivity()
    }

    private fun startNewActivity() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }

    //go to the next page when app is first run
    private fun toNextPage() {
        val cx = (submitBtn.left + submitBtn.right) / 2
        val cy = (submitBtn.top + submitBtn.bottom) / 2
        val animator = ViewAnimationUtils.createCircularReveal(animate_view, cx, cy, 0f, resources.displayMetrics.heightPixels * 1f)
        animator.duration = 1000
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.start()
        animate_view.visibility = View.VISIBLE
        animator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animator: Animator) {}
            override fun onAnimationEnd(animator: Animator) {
                startNewActivity()
            }

            override fun onAnimationCancel(animator: Animator) {}
            override fun onAnimationRepeat(animator: Animator) {}
        })
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
                progressBar.visibility = View.VISIBLE
                try {
                    val addresses = geocode.getFromLocation(location.latitude, location.longitude, 1)
                    if (addresses != null && addresses.size > 0) {
                        countryName = addresses[0].countryName
                        selectCountry(countryName)
                        break
                    }
                } catch (e: IOException) {
                    progressBar.visibility = View.GONE
                    e.printStackTrace()
                }
            }
        }
    }

    private fun selectCountry(country_name: String) {
        val country = database.getReference(Common.Country_Model)
        country.orderByChild(Common.Name).equalTo(country_name).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (postSnap in dataSnapshot.children) {
                    val model = postSnap.getValue(CountryModel::class.java)
                    if (model != null) {
                        Picasso.get()
                                .load(model.cover)
                                .into(passportCover, object : Callback {
                                    override fun onSuccess() {
                                        progressBar.visibility = View.GONE
                                    }

                                    override fun onError(e: Exception) {
                                        Picasso.get().load(R.drawable.ic_terrain_black_24dp).into(passportCover)
                                        progressBar.visibility = View.GONE
                                    }
                                })

                        homeCountry = country_name
                        countryName.text = country_name
                        Paper.book().write(CountryName, homeCountry)
                        Paper.book().write(Common.Cover, model.cover)
                        Paper.book().write(Common.Latitude, model.latitude)
                        Paper.book().write(Common.Longitude, model.longitude)
                        submitBtn.isEnabled = true
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                progressBar.visibility = View.GONE
                Toasty.error(this@MainActivity, getString(R.string.error_toast) + databaseError.message, Toasty.LENGTH_SHORT).show()
            }
        })
    }

    private fun dataFromDatabase():MutableList<CountryModel>{
        val countryModel: MutableList<CountryModel> = ArrayList()

        database.getReference(Common.Country_Model).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (postSnap in dataSnapshot.children) {
                    val model: CountryModel? = postSnap.getValue(CountryModel::class.java)
                    if (model != null) {
                        countryModel.add(model)
                        Paper.book().write(Common.CountryModel, countryModel)
                        val countryNamesData = postSnap.child(Common.Name).getValue(String::class.java)
                        if (countryNamesData != null) {
                            countryNames.add(countryNamesData)
                        }
                        Paper.book().write(Common.CountryList, countryNames)
                        val countryFlagsData = postSnap.child(Common.Flag).getValue(String::class.java)
                        if (countryFlagsData != null) {
                            countryFlags.add(countryFlagsData)
                        }
                        Paper.book().write(Common.FlagList, countryFlags)
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toasty.error(baseContext, getString(R.string.error_toast) + databaseError.message, Toasty.LENGTH_SHORT).show()
            }
        })
        return countryModel
        }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        setCountryBasedOnUserLocation()
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>){}
}