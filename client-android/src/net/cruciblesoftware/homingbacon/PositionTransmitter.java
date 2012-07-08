package net.cruciblesoftware.homingbacon;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Toast;

class PositionTransmitter implements LocationListener{
    private static final String TAG = PositionTransmitter.class.getSimpleName();
    private static final int TWO_MINUTES = 1000 * 60 * 2;
    private static final int UPDATE_INTERVAL = 15000;

    private boolean isGpsAvail;
    private boolean isNetworkAvail;

    private Location bestLocation;
    private Activity activity;
    private LocationManager locManager;

    private class TransmitTask extends AsyncTask<Location, Void, String> {
        @Override
        protected String doInBackground(Location... params) {
            String returnString = null;
            try {
                // send data
                Location loc = params[0];
                String urlStr = //
                        "http://cruciblesoftware.net/homingbacon/setposition.php?" +
                        "username=stevehb&latitude=" + loc.getLatitude() + "&" +
                        "longitude=" + loc.getLongitude() + "&" +
                        "accuracy=" + loc.getAccuracy() + "&" +
                        "epoch_time=" + loc.getTime();
                DebugLog.log(TAG, "transmitting location with " + urlStr);
                HttpClient client = new DefaultHttpClient();
                HttpResponse resp;
                resp = client.execute(new HttpGet(urlStr));

                // get response
                BufferedReader reader = new BufferedReader(new InputStreamReader(resp.getEntity().getContent()));
                String line = "";
                StringBuilder jsonBuff = new StringBuilder();
                while((line = reader.readLine()) != null) { jsonBuff.append(line); }
                DebugLog.log(TAG, "response=" + jsonBuff.toString());

                // check response
                JSONObject json = new JSONObject(jsonBuff.toString());
                if(json.getString("status").equalsIgnoreCase("SUCCESS")) {
                    returnString = activity.getString(R.string.transmit_server_error);
                }
            } catch (ClientProtocolException e) {
                returnString = activity.getString(R.string.transmit_http_exception);
                DebugLog.log(TAG, "ClientProtocolException: " + e.getLocalizedMessage());
            } catch (IOException e) {
                returnString = activity.getString(R.string.transmit_io_exception);
                DebugLog.log(TAG, "IOException: " + e.getLocalizedMessage());
            } catch (JSONException e) {
                returnString = activity.getString(R.string.transmit_json_exception);
                DebugLog.log(TAG, "JSONException: " + e.getLocalizedMessage());
            }
            return returnString;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Toast.makeText(activity, result, Toast.LENGTH_SHORT).show();
        }
    }

    public PositionTransmitter(Activity a) {
        super();
        activity = a;
        locManager = (LocationManager)(activity.getSystemService(Context.LOCATION_SERVICE));

        // set up CheckBox listener
        CheckBox checkBox = (CheckBox)(activity.findViewById(R.id.transmit_checkbox));
        checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton button, boolean isChecked) {
                if(isChecked)
                    startTransmitting();
                else
                    stopTransmitting();
            }
        });

        if(checkBox.isChecked()) {
            onLocationChanged(locManager.getLastKnownLocation(LocationManager.GPS_PROVIDER));
            onLocationChanged(locManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER));
            onLocationChanged(locManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER));
            startTransmitting();
        } else {
            stopTransmitting();
        }
    }

    void startTransmitting() {
        DebugLog.log(TAG, "starting transmission");
        testProviders();
        if(isNetworkAvail)
            locManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, UPDATE_INTERVAL, 0, this);
        if(isGpsAvail)
            locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, UPDATE_INTERVAL, 0, this);
    }

    void stopTransmitting() {
        DebugLog.log(TAG, "stopping transmission");
        locManager.removeUpdates(this);
    }

    @Override
    public void onLocationChanged(Location loc) {
        if(loc == null)
            return;
        if(!isBetterLocation(loc))
            return;

        bestLocation = loc;
        new TransmitTask().execute(new Location[] { bestLocation });
    }

    @Override
    public void onProviderDisabled(String provider) {
        if(provider.equals(LocationManager.NETWORK_PROVIDER))
            isNetworkAvail = false;
        else if(provider.equals(LocationManager.GPS_PROVIDER))
            isGpsAvail = false;
        DebugLog.log(TAG, "provider disabled: " + provider);
    }


    @Override
    public void onProviderEnabled(String provider) {
        if(provider.equals(LocationManager.NETWORK_PROVIDER))
            isNetworkAvail = true;
        else if(provider.equals(LocationManager.GPS_PROVIDER))
            isGpsAvail = true;
        DebugLog.log(TAG, "provider enabled: " + provider);
    }


    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        switch(status) {
        case LocationProvider.OUT_OF_SERVICE:
            DebugLog.log(TAG, "location provider " + provider + " is out of service");
            if(provider.equalsIgnoreCase(LocationManager.NETWORK_PROVIDER)) {
                isNetworkAvail = false;
            } else if(provider.equalsIgnoreCase(LocationManager.GPS_PROVIDER)) {
                isGpsAvail = false;
            }
            break;

        case LocationProvider.TEMPORARILY_UNAVAILABLE:
            break;

        case LocationProvider.AVAILABLE:
            DebugLog.log(TAG, "location provider " + provider + " is now available");
            if(provider.equalsIgnoreCase(LocationManager.NETWORK_PROVIDER)) {
                isNetworkAvail = true;
            } else if(provider.equalsIgnoreCase(LocationManager.GPS_PROVIDER)) {
                isGpsAvail = true;
            }
            break;
        }
    }

    private void testProviders() {
        isNetworkAvail = locManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        isGpsAvail = locManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    /* Logic copied from
     * http://developer.android.com/guide/topics/location/obtaining-user-location.html
     */
    private boolean isBetterLocation(Location location) {
        if(location == null)
            return false;
        if(bestLocation == null)
            return true;

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - bestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;
        if(isSignificantlyNewer)
            return true;
        if(isSignificantlyOlder)
            return false;

        // Check whether the new location fix is more or less accurate
        float accuracyDelta = location.getAccuracy() - bestLocation.getAccuracy();
        boolean isLessAccurate = accuracyDelta > 0f;
        boolean isMoreAccurate = accuracyDelta < 0f;
        boolean isMuchLessAccurate = accuracyDelta > 200f;
        boolean hasSameProvider = isSameProvider(location.getProvider(), bestLocation.getProvider());
        if(isMoreAccurate)
            return true;
        if(isNewer && !isLessAccurate)
            return true;
        if(isNewer && !isMuchLessAccurate && hasSameProvider)
            return true;

        // new location does not meet any criteria
        return false;
    }

    // Check whether two providers are the same
    private boolean isSameProvider(String provider1, String provider2) {
        if(provider1 == null) {
            return (provider2 == null);
        }
        return provider1.equals(provider2);
    }
}
