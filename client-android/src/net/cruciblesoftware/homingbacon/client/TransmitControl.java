package net.cruciblesoftware.homingbacon.client;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.CompoundButton;

class TransmitControl implements LocationListener {
    private static final String TAG = "20: " + TransmitControl.class.getSimpleName();
    private static final int TWO_MINUTES = 1000 * 60 * 2;
    private static final int FIVE_SECONDS = 1000 * 5;

    private boolean isNetAvail = false;
    private boolean isNetLive = false;
    private boolean isGpsAvail = false;
    private boolean isGpsLive = false;

    private UserSettings settings;
    private HomingBaconActivity activity;
    private LocationManager locationManager;
    private CheckBox transmitCheckbox;

    Location bestLocation;

    class UpdatePositionTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void...params) {
            String users = "";
            try {
                // get the username list from the server
                String urlReq = //
                        "http://homingbacon.appspot.com/homingbacon?" +
                        "action=setposition&username=" + settings.get(UserSettings.PREF_KEY_USERNAME) +
                        "&lat=" + bestLocation.getLatitude() +
                        "&lon=" + bestLocation.getLongitude() +
                        "&accuracy=" + bestLocation.getAccuracy() +
                        "&time=" + bestLocation.getTime();
                DebugLog.log(TAG, "using request url:\n\t" + urlReq);
                HttpClient client = new DefaultHttpClient();
                HttpResponse resp = client.execute(new HttpGet(urlReq));
                BufferedReader reader = new BufferedReader(new InputStreamReader(resp.getEntity().getContent()));
                String line = "";
                StringBuilder jsonBuff = new StringBuilder();
                while((line = reader.readLine()) != null) { jsonBuff.append(line); }

                // find the list and return it
                JSONObject json = new JSONObject(jsonBuff.toString());
                if(!json.getString("status").equalsIgnoreCase("success")) {
                    DebugLog.log(TAG, "error while processing location");
                }
            } catch (Exception e) {
                DebugLog.log(TAG, "could not set location for user '" +
                        settings.get(UserSettings.PREF_KEY_USERNAME) +
                        "': " + e.getLocalizedMessage());
            }
            return null;
        }
    }

    TransmitControl(HomingBaconActivity a) {
        activity = a;
        settings = UserSettings.getInstance();
        locationManager = (LocationManager)activity.getSystemService(Context.LOCATION_SERVICE);
        transmitCheckbox = (CheckBox)activity.findViewById(R.id.transmit_checkbox);
        transmitCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    if(isNetAvail && !isNetLive) {
                        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                                FIVE_SECONDS, 0, TransmitControl.this);
                    }
                    if(isGpsAvail && !isGpsLive) {
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                                FIVE_SECONDS, 0, TransmitControl.this);
                    }
                } else {
                    locationManager.removeUpdates(TransmitControl.this);
                    isGpsLive = false;
                    isNetLive = false;
                }
            }
        });
    }

    @Override
    public void onLocationChanged(Location loc) {
        if(loc != null) {
            DebugLog.log(TAG, "offsetFrom current time: " + ((System.currentTimeMillis() - loc.getTime()) / 1000) + " seconds");
        }

        // test incoming location
        if(isBetterLocation(loc)) {
            bestLocation = loc;
            DebugLog.log(TAG, "new best " + loc.getProvider() + " location: (" +
                    loc.getLatitude() + ", " + loc.getLongitude() + ") " +
                    "accuracy=" + loc.getAccuracy() + " time=" + loc.getTime());
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
        if(provider.equalsIgnoreCase(LocationManager.NETWORK_PROVIDER)) {
            isNetAvail = false;
        } else if(provider.equalsIgnoreCase(LocationManager.GPS_PROVIDER)) {
            isGpsAvail = false;
        }
    }

    @Override
    public void onProviderEnabled(String provider) {
        if(provider.equalsIgnoreCase(LocationManager.NETWORK_PROVIDER)) {
            isNetAvail = true;
        } else if(provider.equalsIgnoreCase(LocationManager.GPS_PROVIDER)) {
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
            if(provider.equalsIgnoreCase(LocationManager.NETWORK_PROVIDER)) {
                isNetAvail = false;
            } else if(provider.equalsIgnoreCase(LocationManager.GPS_PROVIDER)) {
                isGpsAvail = false;
            }
            break;

        case LocationProvider.AVAILABLE:
            if(provider.equalsIgnoreCase(LocationManager.NETWORK_PROVIDER)) {
                isNetAvail = true;
            } else if(provider.equalsIgnoreCase(LocationManager.GPS_PROVIDER)) {
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
