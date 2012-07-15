package net.cruciblesoftware.homingbacon.client;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

class FriendSpinnerControl implements OnItemSelectedListener {
    private static final String TAG = "HB: " + FriendSpinnerControl.class.getSimpleName();

    private UserSettings settings;
    private HomingBaconActivity activity;
    private Spinner friendSpinner;
    private ArrayAdapter<String> spinnerAdapter;

    private boolean hasUsers = false;

    class GetFriendsTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params) {
            String users = "";
            try {
                // get the username list from the server
                String urlReq = //
                        "http://homingbacon.appspot.com/homingbacon?" +
                        "action=getusers&username=" + settings.get(UserSettings.PREF_KEY_USERNAME);
                DebugLog.log(TAG, "using request url:\n\t" + urlReq);
                HttpClient client = new DefaultHttpClient();
                HttpResponse resp = client.execute(new HttpGet(urlReq));
                BufferedReader reader = new BufferedReader(new InputStreamReader(resp.getEntity().getContent()));
                String line = "";
                StringBuilder jsonBuff = new StringBuilder();
                while((line = reader.readLine()) != null) { jsonBuff.append(line); }

                // find the list and return it
                JSONObject json = new JSONObject(jsonBuff.toString());
                if(json.getString("status").equalsIgnoreCase("success")) {
                    hasUsers = true;
                    users = json.getString("user_list");
                } else {
                    hasUsers = false;
                    users = "N/A";
                }
            } catch (Exception e) {
                String msg = "could not get usernames: "+ e.getLocalizedMessage();
                DebugLog.log(TAG, msg);
                hasUsers = false;
            }
            return users;
        }

        @Override
        protected void onPostExecute(String result) {
            String[] names = result.split(",");
            spinnerAdapter = new ArrayAdapter<String>(activity, android.R.layout.simple_spinner_item, names);
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            friendSpinner.setAdapter(spinnerAdapter);
        }
    }

    FriendSpinnerControl(HomingBaconActivity a) {
        activity = a;
        settings = UserSettings.getInstance();
        friendSpinner = (Spinner)activity.findViewById(R.id.username_spinner);
        friendSpinner.setOnItemSelectedListener(this);
        new GetFriendsTask().execute((Void[])null);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        String friendName = (String) parent.getItemAtPosition(pos);
        DebugLog.log(TAG, "item selected: pos=" + pos + ", itemAt=" + friendName);
        settings.set(UserSettings.PREF_KEY_FRIEND_NAME,  friendName);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        DebugLog.log(TAG, "nothing selected, nothing handled");
    }


}
