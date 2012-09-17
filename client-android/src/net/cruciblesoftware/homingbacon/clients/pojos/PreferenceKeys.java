package net.cruciblesoftware.homingbacon.clients.pojos;

import net.cruciblesoftware.homingbacon.R;
import android.content.Context;

public class PreferenceKeys {
    public static String FIRST_RUN;
    public static String UUID;
    public static String ACCOUNT_NAME;
    public static String USERNAME;
    public static String APP_DATA;

    public static void init(Context c) {
        // TODO: user reflection to get preference keys from R.string

        FIRST_RUN = c.getString(R.string.pref_key_first_run);
        UUID = c.getString(R.string.pref_key_uuid);
        ACCOUNT_NAME = c.getString(R.string.pref_key_account_name);
        USERNAME = c.getString(R.string.pref_key_username);
        APP_DATA = c.getString(R.string.pref_key_app_data);
    }
}
