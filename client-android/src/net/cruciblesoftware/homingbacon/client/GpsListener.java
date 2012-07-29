package net.cruciblesoftware.homingbacon.client;

import net.cruciblesoftware.homingbacon.UrlParameters;
import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;

import com.google.gson.JsonObject;

class GpsListener implements LocationListener{
    private static final String TAG = "20: " + TransmitControl.class.getSimpleName();
    private static final int TWO_MINUTES = 1000 * 60 * 2;
    private static final int FIVE_SECONDS = 1000 * 5;
    private static final int TEN_METERS = 10;

    private boolean isNetAvail = false;
    private boolean isNetLive = false;
    private boolean isGpsAvail = false;
    private boolean isGpsLive = false;

    private Activity activity;
    private PostOffice post;
    private LocationManager locManager;

    Location bestLocation;

    GpsListener(Activity a, PostOffice p) {
        activity = a;
        post = p;
        locManager = (LocationManager)activity.getSystemService(Context.LOCATION_SERVICE);
        isNetAvail = locManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        isGpsAvail = locManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    void activiate() {
        if(isNetAvail && !isNetLive) {
            locManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                    FIVE_SECONDS, TEN_METERS, this);
        }
        if(isGpsAvail && !isGpsLive) {
            locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    FIVE_SECONDS, TEN_METERS, this);
        }
    }

    void deactivate() {
        locManager.removeUpdates(this);
        isGpsLive = false;
        isNetLive = false;
    }

    @Override
    public void onLocationChanged(Location loc) {
        if(loc != null) {
            DebugLog.log(TAG, "offsetFrom current time: " +
                    ((System.currentTimeMillis() - loc.getTime()) / 1000) + " seconds");
        }

        // test incoming location
        if(isBetterLocation(loc)) {
            bestLocation = loc;
            JsonObject json = new JsonObject();
            json.addProperty(UrlParameters.LATITUDE, bestLocation.getLatitude());
            json.addProperty(UrlParameters.LONGITUDE, bestLocation.getLongitude());
            json.addProperty(UrlParameters.ACCURACY, bestLocation.getAccuracy());
            json.addProperty(UrlParameters.EPOCH_TIME, bestLocation.getTime());
            DebugLog.log(TAG, "new best location from " +
                    loc.getProvider() + ": " + json.toString());
            post.dispatchMessage(new Message(Message.Type.NEW_USER_LOCATION, json.toString()));
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
        if(provider.equals(LocationManager.NETWORK_PROVIDER)) {
            isNetAvail = false;
        } else if(provider.equals(LocationManager.GPS_PROVIDER)) {
            isGpsAvail = false;
        }
    }

    @Override
    public void onProviderEnabled(String provider) {
        if(provider.equals(LocationManager.NETWORK_PROVIDER)) {
            isNetAvail = true;
        } else if(provider.equals(LocationManager.GPS_PROVIDER)) {
            isGpsAvail = true;
        }
    }

    /* I think this method is called only on Android 1.6. See
     * http://code.google.com/p/android/issues/detail?id=9433
     */
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        switch(status) {
        case LocationProvider.OUT_OF_SERVICE:
            if(provider.equals(LocationManager.NETWORK_PROVIDER)) {
                isNetAvail = false;
            } else if(provider.equals(LocationManager.GPS_PROVIDER)) {
                isGpsAvail = false;
            }
            break;

        case LocationProvider.AVAILABLE:
            if(provider.equals(LocationManager.NETWORK_PROVIDER)) {
                isNetAvail = true;
            } else if(provider.equals(LocationManager.GPS_PROVIDER)) {
                isGpsAvail = true;
            }
            break;
        }
    }

    /* Logic copied from
     * http://developer.android.com/guide/topics/location/obtaining-user-location.html
     */
    private boolean isBetterLocation(Location location) {
        if(location == null) {
            DebugLog.log(TAG, "rejecting new location because it is null");
            return false;
        } else if(bestLocation == null) {
            DebugLog.log(TAG, "accepting new location because no current location");
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - bestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if(isSignificantlyNewer) {
            DebugLog.log(TAG, "accepting new location because it is significantly newer");
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if(isSignificantlyOlder) {
            DebugLog.log(TAG, "rejecting new location because it is significantly older");
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - bestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                bestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if(isMoreAccurate) {
            DebugLog.log(TAG, "accepting new location because it is more accurate");
            return true;
        } else if(isNewer && !isLessAccurate) {
            DebugLog.log(TAG, "accepting new location because it is newer and equally accurate");
            return true;
        } else if(isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            DebugLog.log(TAG, "accepting new location because it is newer, not less accurate, and from same provider");
            return true;
        }
        DebugLog.log(TAG, "rejecting new location because it matched no acceptance criteria");
        return false;
    }

    // Check whether two providers are the same
    private boolean isSameProvider(String provider1, String provider2) {
        if(provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }
}
