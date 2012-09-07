package net.cruciblesoftware.homingbacon.clients.daos;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class AppPrefs {
    private static final String TAG = "HB: " + AppPrefs.class.getSimpleName();

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    public AppPrefs(Activity activity) {
        prefs = activity.getPreferences(Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public String getString(String key) {
        return prefs.getString(key, "");
    }

    public boolean getBoolean(String key) {
        return getBoolean(key, false);
    }

    public boolean getBoolean(String key, boolean value) {
        return prefs.getBoolean(key, value);
    }

    public void set(String key, String value) {
        editor.putString(key, value);
        editor.commit();
    }

    public void set(String key, boolean value) {
        editor.putBoolean(key, value);
        editor.commit();
    }
}
