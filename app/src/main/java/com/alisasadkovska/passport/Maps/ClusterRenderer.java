package com.alisasadkovska.passport.Maps;

import android.content.Context;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;


public class ClusterRenderer extends DefaultClusterRenderer<MarkerItem> {

    public ClusterRenderer(Context context, GoogleMap map, ClusterManager<MarkerItem> clusterManager) {
        super(context, map, clusterManager);
        clusterManager.setRenderer(this);
    }


    @Override
    protected void onBeforeClusterItemRendered(MarkerItem myItem, MarkerOptions markerOptions) {
        if (myItem.getIcon() != null) {
            markerOptions.icon(myItem.getIcon());
        }
        markerOptions.visible(true);
    }
}