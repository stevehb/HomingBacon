package net.cruciblesoftware.homingbacon.clients.activities;

import net.cruciblesoftware.homingbacon.JsonKeys;
import net.cruciblesoftware.homingbacon.JsonUtils;
import net.cruciblesoftware.homingbacon.R;
import net.cruciblesoftware.homingbacon.clients.daos.ServerConnection;
import net.cruciblesoftware.homingbacon.clients.models.UserAccount;
import net.cruciblesoftware.homingbacon.clients.pojos.Message;
import net.cruciblesoftware.homingbacon.clients.pojos.PreferenceKeys;
import net.cruciblesoftware.homingbacon.clients.utils.DebugLog;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.widget.Toast;

import com.google.gson.JsonObject;

public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener, Message.Listener {
    private static final String TAG = "HB: " + SettingsActivity.class.getSimpleName();

    private boolean firstRun;
    private boolean hasAccount, hasUsername;
    private boolean checkTaskCancelled;
    private String origUsername;

    private ProgressDialog dialog;
    private EditTextPreference usernamePref;
    private ListPreference accountPref;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        PreferenceScreen screen = getPreferenceScreen();
        usernamePref = (EditTextPreference)screen.findPreference(PreferenceKeys.USERNAME);
        accountPref = (ListPreference)screen.findPreference(PreferenceKeys.ACCOUNT_NAME);

        // if first run, then show info message
        String pkgName = PreferenceKeys.class.getPackage().getName();
        String key = pkgName + "." + PreferenceKeys.FIRST_RUN;
        firstRun = getIntent().getExtras().getBoolean(key);
        if(firstRun) {
            hasAccount = false;
            hasUsername = false;
            origUsername = "";
        } else {
            hasAccount = true;
            hasUsername = true;
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            origUsername = prefs.getString(PreferenceKeys.USERNAME, "");
        }
        DebugLog.log(TAG, "got extra '" + key + "'=" + firstRun);
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(this);

        // populate the list preference
        AccountManager acctMgr = AccountManager.get(getBaseContext());
        Account[] accts = acctMgr.getAccountsByType(UserAccount.ACCOUNT_TYPE);
        String[] entries = new String[accts.length];
        for(int i = 0; i < accts.length; i++) {
            entries[i] = accts[i].name;
        }
        accountPref.setEntries(entries);
        accountPref.setEntryValues(entries);

        // if we have values for account or username, show them in their preference
        String accountName = prefs.getString(PreferenceKeys.ACCOUNT_NAME, "");
        if(!accountName.isEmpty()) {
            accountPref.setSummary(accountName);
        }
        String username = prefs.getString(PreferenceKeys.USERNAME, "");
        if(!username.isEmpty()) {
            usernamePref.setSummary(username);
        }

        // show welcome dialog if first run
        if(firstRun) {
            DebugLog.log(TAG, "creating & showing welcome dialog: " + firstRun);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.pref_first_run_message);
            builder.setCancelable(true);
            builder.setPositiveButton(R.string.button_ok, null);
            builder.show();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        SharedPreferences prefs = getPreferenceScreen().getSharedPreferences();
        prefs.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        DebugLog.log(TAG, "getting pref change for key=" + key);
        Editor editor = prefs.edit();

        if(key.equals(PreferenceKeys.ACCOUNT_NAME)) {
            // update display with account name
            String accountName = prefs.getString(key, "");
            accountPref.setSummary(accountName);
            hasAccount = true;

            /* TODO: add code to set default value for usernamePref
             * The difficulty is in setting the text already in the box, but
             * without triggering a preference change. setDefaultValue() does
             * not work. setText() triggers a pref change.
             * 
             * Existing code:
            // guess at the username
            int atIdx = accountName.indexOf("@");
            if(atIdx > 0 && !hasUsername) {
                String guess = accountName.substring(0, atIdx);
                DebugLog.log(TAG, "guessing username: " + guess);
                usernamePref.setDefaultValue(guess);
            }
             */
        } else if(key.equals(PreferenceKeys.USERNAME)) {
            // trim the username and check
            String username = prefs.getString(key, "");
            username = username.trim();
            if(username.isEmpty()) {
                if(hasUsername) {
                    editor.putString(PreferenceKeys.USERNAME, origUsername);
                    editor.commit();
                    usernamePref.setSummary(username);
                }
            } else {
                checkUsername(username);
            }
        } else if(key.equals(PreferenceKeys.FIRST_RUN)) {
            return;
        }
        finishFirstRun();
    }

    @Override
    public void receiveMessage(Message msg) {
        // server communication is finished, close dialog, handle cancellation
        dialog.dismiss();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Editor editor = prefs.edit();
        String newUsername = prefs.getString(PreferenceKeys.USERNAME, "");
        if(checkTaskCancelled) {
            if(hasUsername) {
                // no check, but has old username, so revert to original
                DebugLog.log(TAG, "cancelled check on " + newUsername + ", reverting to old username " + origUsername);
                editor.putString(PreferenceKeys.USERNAME, origUsername);
                editor.commit();
                usernamePref.setSummary(origUsername);
                return;
            } else {
                // no check, no username, so clear the unchecked username from prefs
                DebugLog.log(TAG, "cancelled check, on " + newUsername + ", clearing shared pref");
                editor.putString(PreferenceKeys.USERNAME, "");
                editor.commit();
                return;
            }
        }

        // parse the server response, get result
        boolean userExists = true;
        switch(msg.type) {
        case SERVER_RESPONSE:
            // server error
            String errMsg = JsonUtils.getServerErrorMessage(msg.data);
            if(errMsg != null) {
                DebugLog.err(TAG, "ERROR: checking whether username exists: server reports: " + errMsg);
                return;
            }
            // get actual response
            JsonObject jsonObj = JsonUtils.getJsonObject(msg.data);
            userExists = jsonObj.get(JsonKeys.HAS_USER).getAsBoolean();
            break;
        default:
            break;
        }

        // handle server response
        if(userExists) {
            // too bad, name already taken; notify user
            DebugLog.log(TAG, "username " + newUsername + " already exists");
            Toast.makeText(SettingsActivity.this, R.string.toast_username_already_exists_error, Toast.LENGTH_LONG).show();
        } else {
            // if there is no such user, then set the model
            DebugLog.log(TAG, "username accepted " + newUsername);
            Toast.makeText(SettingsActivity.this, R.string.toast_username_accepted, Toast.LENGTH_LONG).show();
            origUsername = newUsername;
            hasUsername = true;
            finishFirstRun();
        }
    }

    private void finishFirstRun() {
        if(firstRun && hasAccount && hasUsername) {
            DebugLog.log(TAG, "finishing first run");
            // set the first run flag and quit SettingsActivity
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            Editor editor = prefs.edit();
            firstRun = false;
            editor.putBoolean(PreferenceKeys.FIRST_RUN, false);
            editor.commit();
            finish();
        }
    }

    private void checkUsername(String newUsername) {
        // set up the progress dialog
        dialog = ProgressDialog.show(SettingsActivity.this, "", getString(R.string.dialog_check_username_message), true, true, new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                checkTaskCancelled = true;
            }
        });

        // contact the server
        ServerConnection server = new ServerConnection();
        server.hasUser(newUsername, this);
    }
}
