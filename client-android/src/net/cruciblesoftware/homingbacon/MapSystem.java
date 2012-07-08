package net.cruciblesoftware.homingbacon;

import android.app.Activity;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;

class MapSystem {
    private static final String TAG = MapSystem.class.getSimpleName();
    private static final int EARTH_RADIUS = 6371000;
    private final static double BEARING = 0.0;

    private final Activity activity;
    private final MapView mapView;
    private final MapController mapController;

    private double lastLat, lastLon, lastAccuracy;
    private long lastEpochTime = 0;
    private int zoomLevel = 14;

    MapSystem(Activity a) {
        activity = a;
        mapView = (MapView)(activity.findViewById(R.id.map));
        mapView.setBuiltInZoomControls(true);
        mapView.setClickable(true);
        mapController = mapView.getController();
    }

    void updateMap(double lat, double lon, double accuracy, long epochTime) {
        if(lastEpochTime > epochTime)
            return;

        mapController.setCenter(new GeoPoint((int) (lat * 1000000), (int) (lon * 1000000)));

        // determine optimal range to show in mapview
        if(accuracy < 1000) accuracy = accuracy * 2;
        if(accuracy < 150) accuracy = 150;

        // calculate lat/lon radius for zoom
        // from http://www.movable-type.co.uk/scripts/latlong.html
        final double angDist = accuracy / EARTH_RADIUS;
        final double lat1 = Math.toRadians(lat);
        final double lon1 = Math.toRadians(lon);
        final double cosLat1 = Math.cos(lat1);
        final double sinLat1 = Math.sin(lat1);
        final double cosAngDist = Math.cos(angDist);
        final double sinAngDist = Math.sin(angDist);
        final double lat2 = Math.asin(sinLat1 * cosAngDist + cosLat1 * sinAngDist * Math.cos(BEARING));
        final double lon2 = lon1 + Math.atan2(Math.sin(BEARING) * sinAngDist * cosLat1, cosAngDist - sinLat1 * Math.sin(lat2));
        final double latSpan = Math.toDegrees(Math.abs(lat2 - lat1));
        final double lonSpan = Math.toDegrees(Math.abs(lon2 - lon1));
        mapController.zoomToSpan((int) (latSpan * 1000000), (int) (lonSpan * 1000000));
        zoomLevel = mapView.getZoomLevel();

        lastLat = lat;
        lastLon = lon;
        lastAccuracy = accuracy;
        lastEpochTime = epochTime;

        DebugLog.log(TAG, "point (" + lat + "," + lon + ") with accuracy="
                + accuracy + " gives latSpan=" + latSpan + ", lonSpan="
                + lonSpan + ", zoomLevel=" + zoomLevel);
    }
}
