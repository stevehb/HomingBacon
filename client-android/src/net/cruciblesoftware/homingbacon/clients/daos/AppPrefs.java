package net.cruciblesoftware.homingbacon.clients.daos;

import java.util.Map;
import java.util.Set;

import net.cruciblesoftware.homingbacon.clients.utils.DebugLog;
import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class AppPrefs {
    private static final String TAG = "HB: " + AppPrefs.class.getSimpleName();

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    public AppPrefs(Activity activity) {
        prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        editor = prefs.edit();
    }

    public void listPrefs() {
        Map<String, ?> map = prefs.getAll();
        Set<String> keys = map.keySet();
        DebugLog.log(TAG, "SharedPreferences:");
        for(String k : keys) {
            DebugLog.log(TAG, "\t" + k + "=" + map.get(k));
        }
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
