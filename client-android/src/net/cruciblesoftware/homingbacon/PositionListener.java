package net.cruciblesoftware.homingbacon;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Toast;

class PositionListener {
    private static final String TAG = PositionListener.class.getSimpleName();
    private static final int UPDATE_INTERVAL = 15000;

    private Activity activity;
    private MapSystem map;
    private String username;

    private Timer timer;
    private class ListenTask extends TimerTask {
        @Override
        public void run() {
            try {
                String urlStr = //
                        "http://cruciblesoftware.net/homingbacon/" +
                        "getposition.php?username=" + username;
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
                    Toast t = Toast.makeText(activity,
                            R.string.listen_server_error,
                            Toast.LENGTH_SHORT);
                    t.show();
                }

                // parse response, update map
                double lat = json.getDouble("latitude");
                double lon = json.getDouble("longitude");
                double accuracy = json.getDouble("accuracy");
                long epochTime = json.getLong("epoch_time");
                map.updateMap(lat, lon, accuracy, epochTime);
            } catch (ClientProtocolException e) {
                Toast.makeText(activity,
                        R.string.listen_http_exception,
                        Toast.LENGTH_SHORT).show();
                DebugLog.log(TAG, "ClientProtocolException: " + e.getLocalizedMessage());
            } catch (IOException e) {
                Toast.makeText(activity,
                        R.string.listen_io_exception,
                        Toast.LENGTH_SHORT).show();
                DebugLog.log(TAG, "IOException: " + e.getLocalizedMessage());
            } catch (JSONException e) {
                Toast.makeText(activity,
                        R.string.listen_json_exception,
                        Toast.LENGTH_SHORT).show();
                DebugLog.log(TAG, "JSONException: " + e.getLocalizedMessage());
            }
        }
    };

    public PositionListener(Activity a, MapSystem m) {
        activity = a;
        map = m;
        timer = new Timer();

        username = "stevehb";

        CheckBox checkBox = (CheckBox)(activity.findViewById(R.id.listen_checkbox));
        checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton button, boolean isChecked) {
                if(isChecked)
                    startListening();
                else
                    stopListening();
            }
        });
        if(checkBox.isChecked())
            startListening();
        else
            stopListening();
    }

    void startListening() {
        DebugLog.log(TAG, "starting listening");
        timer.scheduleAtFixedRate(new ListenTask(), 0, UPDATE_INTERVAL);
    }

    void stopListening() {
        DebugLog.log(TAG, "stopping listening");
        timer.cancel();
        timer.purge();
        timer = new Timer();
    }

}
