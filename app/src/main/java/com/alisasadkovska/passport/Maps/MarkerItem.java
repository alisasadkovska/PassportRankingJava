package com.alisasadkovska.passport.Maps;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class MarkerItem implements ClusterItem {
    private LatLng latLng;
    private String title;
    private String snippet;
    private BitmapDescriptor icon;

    public MarkerItem(LatLng latLng, String title, String snippet, BitmapDescriptor icon) {
        this.latLng = latLng;
        this.title = title;
        this.snippet = snippet;
        this.icon = icon;
    }


    @Override
    public LatLng getPosition() {
        return latLng;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public String getSnippet() {
        return snippet;
    }


    public BitmapDescriptor getIcon() {
        return icon;
    }
}
