package net.cruciblesoftware.homingbacon.client;

import net.cruciblesoftware.homingbacon.JsonKeys;
import android.app.Activity;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

class MapSystem extends BaseControl implements Message.Listener {
    private static final String TAG = "HB: " + MapSystem.class.getSimpleName();
    private static final int EARTH_RADIUS = 6371000;
    private final static double BEARING = 0.0;

    private Activity activity;
    private MapView mapView;
    private MapController mapController;
    private int zoomLevel = 18;

    MapSystem(Activity a) {
        activity = a;
        mapView = (MapView) (activity.findViewById(R.id.map));
        mapController = mapView.getController();

        DebugLog.log(TAG, "turning on zoom controls");
        mapView.setBuiltInZoomControls(true);
        mapController.setZoom(14);
    }

    @Override
    public void receiveMessage(Message msg) {
        switch(msg.type) {
        case NEW_FRIEND_LOCATION:
            JsonParser parser = new JsonParser();
            JsonObject json = parser.parse(msg.data).getAsJsonObject();
            updateMap(json.getAsJsonObject(JsonKeys.LATITUDE).getAsDouble(),
                    json.getAsJsonObject(JsonKeys.LONGITUDE).getAsDouble(),
                    json.getAsJsonObject(JsonKeys.ACCURACY).getAsDouble(),
                    json.getAsJsonObject(JsonKeys.EPOCH_TIME).getAsLong());
            break;
        default:
            break;
        }
    }

    private void updateMap(double lat, double lon, double accuracy, long time) {
        mapController.setCenter(new GeoPoint((int) (lat * 1000000), (int) (lon * 1000000)));
        DebugLog.log(TAG, "remapping to lat=" + lat + ", lon=" + lon + ", accuracy=" + accuracy);

        // determine optimal range to show in mapview
        if(accuracy < 1000)
            accuracy = accuracy * 2;
        if(accuracy < 150)
            accuracy = 150;

        // calculate lat/lon radius for zoom
        // from http://www.movable-type.co.uk/scripts/latlong.html
        final double angDist = accuracy / EARTH_RADIUS;
        final double lat1 = Math.toRadians(lat);
        final double lon1 = Math.toRadians(lon);
        final double cosLat1 = Math.cos(lat1);
        final double sinLat1 = Math.sin(lat1);
        final double cosAngDist = Math.cos(angDist);
        final double sinAngDist = Math.sin(angDist);
        final double lat2 = Math.asin(sinLat1 * cosAngDist + cosLat1
                * sinAngDist * Math.cos(BEARING));
        final double lon2 = lon1
                + Math.atan2(Math.sin(BEARING) * sinAngDist * cosLat1,
                        cosAngDist - sinLat1 * Math.sin(lat2));
        final double latSpan = Math.toDegrees(Math.abs(lat2 - lat1));
        final double lonSpan = Math.toDegrees(Math.abs(lon2 - lon1));
        mapController.zoomToSpan((int) (latSpan * 1000000), (int) (lonSpan * 1000000));
        zoomLevel = mapView.getZoomLevel();
    }
}
