package com.alisasadkovska.passport.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.view.MenuItem
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.alisasadkovska.passport.Maps.ClusterRenderer
import com.alisasadkovska.passport.Maps.MarkerItem
import com.alisasadkovska.passport.Model.CountryModel
import com.alisasadkovska.passport.R
import com.alisasadkovska.passport.common.BaseActivity
import com.alisasadkovska.passport.common.Common
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.clustering.ClusterManager
import es.dmoral.toasty.Toasty
import io.paperdb.Paper
import kotlinx.android.synthetic.main.activity_maps.*
import java.util.*

class MapsActivity : BaseActivity(), OnMapReadyCallback {


    private var country = ArrayList<CountryModel>()
    private var statusList = ArrayList<Long>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        toolbar.title = getString(R.string.menu_map)
        setSupportActionBar(toolbar)

        supportActionBar!!.setDisplayShowTitleEnabled(true)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)

        country = Paper.book().read(Common.CountryModel)
        statusList = Paper.book().read(Common.StatusList)

        mapView.onCreate(savedInstanceState)
        mapView.onResume()
        mapView.getMapAsync(this)
    }

    private fun getIcon(status: Long): BitmapDescriptor {
        return when (status) {
            0L -> bitmapDescriptorFromVector(this, R.drawable.ic_vpn_lock_red_24dp)
            1L -> if (themeId == 1)
                bitmapDescriptorFromVector(this, R.drawable.ic_important_devices_yellow_24dp)
            else
                bitmapDescriptorFromVector(this, R.drawable.ic_important_devices_lime_24dp)
            2L -> bitmapDescriptorFromVector(this, R.drawable.ic_flight_land_blue_24dp)
            3L -> bitmapDescriptorFromVector(this, R.drawable.ic_flight_green_24dp)
            else -> if (themeId == 1)
                bitmapDescriptorFromVector(this, R.drawable.ic_home_white_24dp)
            else
                bitmapDescriptorFromVector(this, R.drawable.ic_home_black_24dp)
        }
    }

    private fun getStatus(statusLong: Long?): String {
        return when (statusLong) {
            0L -> getString(R.string.visa_required)
            1L -> getString(R.string.eTA)
            2L -> getString(R.string.on_arrival)
            3L -> getString(R.string.visa_free)
            else -> ""
        }
    }

    private fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor {
        val vectorDrawable = ContextCompat.getDrawable(context, vectorResId)!!
        vectorDrawable.setBounds(0, 0, vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight)
        val bitmap = Bitmap.createBitmap(vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
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

    override fun onMapReady(googleMap: GoogleMap) {
        try {
            MapsInitializer.initialize(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            val res: Int = if (themeId == 0)
                R.raw.mapstyle_light
            else
                R.raw.mapstyle
            val success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, res))
            if (!success) {
                Toasty.error(this, getString(R.string.style_parsing_failed), 5).show()
            }
        } catch (e: Resources.NotFoundException) {
            Toasty.error(this, getString(R.string.cant_find_style) + e, 5).show()
        }


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), Common.RequestCameraPermissionId)
            return
        }

        googleMap.isMyLocationEnabled = true
        googleMap.uiSettings.isZoomControlsEnabled = true

        val lat = Paper.book().read<Double>(Common.Latitude)
        val longitude = Paper.book().read<Double>(Common.Longitude)
        val current = LatLng(lat, longitude)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(current, 6f))

        val clusterManager = ClusterManager<MarkerItem>(this, googleMap)
        ClusterRenderer(this, googleMap, clusterManager)

        googleMap.setOnCameraIdleListener(clusterManager)

        for (i in country.indices) {

            val latitude = country[i].latitude
            val long = country[i].longitude
            val markerPos = LatLng(latitude, long)
            val title = country[i].name

            val snippet = getStatus(statusList[i])
            val icon = getIcon(statusList[i])

            clusterManager.addItem(MarkerItem(markerPos, title, snippet, icon))
            clusterManager.cluster()
        }


        googleMap.setOnInfoWindowClickListener { marker ->
            val intent = Intent(this, CountryDetail::class.java)
            Common.COUNTRY = marker.title
            startActivity(intent)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }
}
