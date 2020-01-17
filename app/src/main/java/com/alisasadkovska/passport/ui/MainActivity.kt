package com.alisasadkovska.passport.ui

import `in`.galaxyofandroid.spinerdialog.SpinnerDialog
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.Geocoder
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.WindowManager
import com.alisasadkovska.passport.Model.CountryModel
import com.alisasadkovska.passport.R
import com.alisasadkovska.passport.common.BaseActivity
import com.alisasadkovska.passport.common.Common
import com.alisasadkovska.passport.common.Common.CountryName
import com.alisasadkovska.passport.common.Common.database
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import es.dmoral.toasty.Toasty
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import io.paperdb.Paper
import kotlinx.android.synthetic.main.activity_main.*
import pub.devrel.easypermissions.EasyPermissions
import pub.devrel.easypermissions.EasyPermissions.PermissionCallbacks
import java.io.IOException

class MainActivity : BaseActivity(), PermissionCallbacks {

    var countryModel: MutableList<CountryModel> = ArrayList()
    var countryNames = ArrayList<String>()
    var countryFlags = ArrayList<String>()

    var homeCountry = ""

    private lateinit var spinnerDialog: SpinnerDialog

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dataFromDatabase()

        addBtn.setOnClickListener {
                if (countryNames.size>0) {
                    spinnerDialog.showSpinerDialog()
                }
            }

        submitBtn.setOnClickListener {
            toNextPage()
            }


        if (Paper.book().contains(CountryName)){
            homeCountry = Paper.book().read(CountryName)
            startSplashScreen()
        }

    }


    override fun onResume() {
        super.onResume()
        if (homeCountry==""&&isLocationEnabled(this))
                askForLocationPermission()
    }

    private fun isLocationEnabled(mContext: Context): Boolean {
        val lm = mContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER) || lm.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER)
    }

    @SuppressLint("SetTextI18n")
    private fun startSplashScreen() {
        addBtn.visibility = View.GONE
        submitBtn.visibility = View.GONE
        progressSplash.visibility = View.VISIBLE
        countryNameText.text = getString(R.string.app_name) + "\n" + getString(R.string.version)
        val handler = Handler()
        val runnable = {
            toNextPage()
        }
        handler.postDelayed(runnable, 3000)
    }


  private fun toNextPage(){
      startActivity(Intent(this@MainActivity, HomeActivity::class.java))
      finish()
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
                countryNameText.text = getString(R.string.location_setting_up)
                try {
                    val addresses = geocode.getFromLocation(location.latitude, location.longitude, 1)
                    if (addresses != null && addresses.size > 0) {
                        countryName = addresses[0].countryName
                        selectCountry(countryName)
                        break
                    }
                } catch (e: IOException) {
                    progressBar.visibility = View.GONE
                    countryNameText.text = ""
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
                        countryNameText.text = country_name
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

    private fun dataFromDatabase(){
        progressSplash.visibility = View.GONE

        if (Paper.book().contains(Common.CountryModel))
            Paper.book().delete(Common.CountryModel)
        if (Paper.book().contains(Common.CountryList))
            Paper.book().delete(Common.CountryList)
        if (Paper.book().contains(Common.FlagList))
            Paper.book().delete(Common.FlagList)

        val countryModelReference = database.getReference(Common.Country_Model)

        countryModelReference.addValueEventListener(object :ValueEventListener{
            override fun onCancelled(databaseError: DatabaseError) {
                Toasty.error(this@MainActivity, databaseError.message, Toasty.LENGTH_SHORT).show()
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
               for (postSnap in dataSnapshot.children){
                   val model = postSnap.getValue<CountryModel>(CountryModel::class.java)
                   countryModel.add(model!!)
                   Paper.book().write(Common.CountryModel, countryModel)
                   val names = postSnap.child(Common.Name).getValue(String::class.java)
                   countryNames.add(names!!)
                   Paper.book().write(Common.CountryList, countryNames)

                   val flags = postSnap.child(Common.Flag).getValue(String::class.java)
                   countryFlags.add(flags!!)
                   Paper.book().write(Common.FlagList, countryFlags)

                   spinnerDialog = SpinnerDialog(this@MainActivity,
                           countryNames,
                           getString(R.string.select_your_country),
                           getString(R.string.close))

                   spinnerDialog.setCancellable(false)
                   spinnerDialog.setShowKeyboard(false)

                   spinnerDialog.setTitleColor(resources.getColor(R.color.darkColorPrimaryDark))
                   spinnerDialog.setCloseColor(resources.getColor(R.color.visa_required))
                   spinnerDialog.setItemColor(resources.getColor(R.color.visa_on_arrival_table))

                   spinnerDialog.bindOnSpinerListener { _: String, pos: Int ->
                       progressBar.visibility = View.VISIBLE
                       Picasso.get()
                               .load(countryModel[pos].cover)
                               .into(passportCover, object : Callback {
                                   override fun onSuccess() {
                                       progressBar.visibility = View.GONE
                                   }

                                   override fun onError(e: Exception) {
                                       Toasty.error(this@MainActivity, e.message.toString(), Toasty.LENGTH_SHORT).show()
                                   }
                               })
                       homeCountry = countryModel[pos].name
                       countryNameText.text = countryModel[pos].name
                       Paper.book().write(CountryName, countryModel[pos].name)
                       Paper.book().write(Common.Cover, countryModel[pos].cover)
                       Paper.book().write(Common.Latitude, countryModel[pos].latitude)
                       Paper.book().write(Common.Longitude, countryModel[pos].longitude)
                       submitBtn.isEnabled = true
                   }

               }
            }
        })
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