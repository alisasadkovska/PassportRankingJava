package com.alisasadkovska.passport.ui.Fragments;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.alisasadkovska.passport.Maps.ClusterRenderer;
import com.alisasadkovska.passport.Maps.MarkerItem;
import com.alisasadkovska.passport.R;
import com.alisasadkovska.passport.common.Common;
import com.alisasadkovska.passport.common.TinyDB;
import com.alisasadkovska.passport.ui.CountryDetail;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.clustering.ClusterManager;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import es.dmoral.toasty.Toasty;
import io.paperdb.Paper;


public class MapFragment extends Fragment implements OnMapReadyCallback {

    private Context context;
    private TinyDB tinyDB;

    public static MapFragment newInstance(Context context, TinyDB tinyDB)
    {
        return new MapFragment(context, tinyDB);
    }

    private MapFragment(Context context, TinyDB tinyDB) {
        this.context = context;
        this.tinyDB = tinyDB;
    }

    private DatabaseReference countries;
    private ArrayList<Long>statusList = new ArrayList<>();

    private MapView mMapView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        countries = Common.getDatabase().getReference(Common.Countries);

        if (Paper.book().contains(Common.StatusList)){
            statusList = Paper.book().read(Common.StatusList);
            if (statusList.size()!=Common.countryModel.size())
                getStatusList();
        }else
            getStatusList();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View myFragment = inflater.inflate(R.layout.fragment_map, container, false);

        mMapView = myFragment.findViewById(R.id.mapView);

        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();
        mMapView.getMapAsync(this);

        return myFragment;
    }

    private void getStatusList(){
        statusList.clear();
        Query query = countries.orderByKey().equalTo(Paper.book().read(Common.CountryName));
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnap: dataSnapshot.getChildren()){
                    if (dataSnapshot.exists()){
                        Map<String,Long> data = (Map)postSnap.getValue();
                        assert data != null;
                        Map<String, Long> treeMap = new TreeMap<>(data);

                        ArrayList<Long>status = new ArrayList<>();

                        for (Map.Entry<String,Long> entry : treeMap.entrySet()){
                            status.add(entry.getValue());
                            Paper.book().write(Common.StatusList, status);
                            statusList.addAll(status);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toasty.error(context, databaseError.getMessage(), Toasty.LENGTH_SHORT).show();
            }
        });
    }

    private BitmapDescriptor getIcon(Long status) {
        BitmapDescriptor icon;

        if (status==0){
            icon = bitmapDescriptorFromVector(context, R.drawable.ic_vpn_lock_red_24dp);
        }
        else if (status==1){
            if (tinyDB.getInt(Common.THEME_ID)==1)
                icon = bitmapDescriptorFromVector(context, R.drawable.ic_important_devices_yellow_24dp);
            else
                icon = bitmapDescriptorFromVector(context, R.drawable.ic_important_devices_orange_24dp);
        }
        else if (status == 2){
            icon = bitmapDescriptorFromVector(context, R.drawable.ic_flight_land_blue_24dp);
        }
        else if (status==3){
            icon = bitmapDescriptorFromVector(context, R.drawable.ic_flight_green_24dp);
        }
        else {
            if (tinyDB.getInt(Common.THEME_ID)==1)
                icon = bitmapDescriptorFromVector(context, R.drawable.ic_home_white_24dp);
            else
                icon = bitmapDescriptorFromVector(context, R.drawable.ic_home_black_24dp);
        }

        return icon;
    }

    private String getStatus(Long statusLong) {
        String status;

        if (statusLong==0)
            status = getString(R.string.visa_required);
        else if (statusLong==1)
            status = getString(R.string.eTA);
        else if (statusLong == 2)
            status = getString(R.string.on_arrival);
        else if (statusLong==3)
            status = getString(R.string.visa_free);
        else
            status ="";

        return status;
    }

    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        assert vectorDrawable != null;
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        try {
            MapsInitializer.initialize(context);
        }catch (Exception e) {
            e.printStackTrace();
        }

        try {
            int res;
            if (tinyDB.getInt(Common.THEME_ID)==0)
                res = R.raw.mapstyle_light;
            else
                res = R.raw.mapstyle;
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            context, res));
            if (!success) {
                Toasty.error(context, "Style parsing failed.",5).show();
            }
        } catch (Resources.NotFoundException e) {
            Toasty.error(context, "Can't find style. Error: " + e,5).show();
        }


        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(Objects.requireNonNull(getActivity()),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Common.RequestCameraPermissionId);
            return;
        }

        googleMap.setMyLocationEnabled(true);
        googleMap.getUiSettings().setZoomControlsEnabled(true);

        double lat = Paper.book().read(Common.Latitude);
        double longitude = Paper.book().read(Common.Longitude);
        LatLng current = new LatLng(lat,longitude);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(current,6f));

        ClusterManager<MarkerItem> clusterManager = new ClusterManager<>(context, googleMap);
        new ClusterRenderer(context, googleMap, clusterManager);

        googleMap.setOnCameraIdleListener(clusterManager);

        for (int i=0; i < Common.countryModel.size(); i++){

            Double _lat = Common.countryModel.get(i).getLatitude();
            Double _lon = Common.countryModel.get(i).getLongitude();
            LatLng markerPos = new LatLng(_lat, _lon);
            String title = Common.countryModel.get(i).getName();

            String snippet = getStatus(statusList.get(i));
            BitmapDescriptor icon = getIcon(statusList.get(i));

            clusterManager.addItem(new MarkerItem(markerPos, title, snippet, icon));
            clusterManager.cluster();
        }


        googleMap.setOnInfoWindowClickListener(marker -> {
            Common.COUNTRY = marker.getTitle();
            Intent intent = new Intent(getActivity(), CountryDetail.class);
            startActivity(intent);
        });

    }
}