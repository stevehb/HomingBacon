package net.cruciblesoftware.homingbacon.client;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.widget.EditText;

class UserSettings {
    private static final String TAG = "HB: " + UserSettings.class.getSimpleName();

    static final String PREF_KEY_USERNAME = "username";
    static final String PREF_KEY_FRIEND_NAME = "friend_name";

    private Activity activity;
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    private static UserSettings instance;
    private UserSettings() { }
    static UserSettings getInstance() {
        if(instance == null) {
            instance = new UserSettings();
        }
        return instance;
    }

    void setActivity(Activity a) {
        activity = a;
        prefs = activity.getPreferences(Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    boolean hasUsername() {
        String username = prefs.getString(PREF_KEY_USERNAME, "");
        return !username.isEmpty();
    }

    void getUsernameFromUser() {
        AlertDialog.Builder alert = new AlertDialog.Builder(activity);
        alert.setTitle("Choose Username");
        alert.setMessage("Username:");
        alert.setCancelable(false);

        final EditText input = new EditText(activity);
        alert.setView(input);
        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                String username = input.getText().toString();
                DebugLog.log(TAG, "saving username '" + username + "'");
                editor.putString(PREF_KEY_USERNAME, username);
                editor.apply();
                return;
            }
        });
        alert.show();
    }

    String get(String key) {
        return prefs.getString(key, "");
    }

    void set(String key, String val) {
        editor.putString(key, val);
        editor.apply();
    }
}
