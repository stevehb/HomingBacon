package net.cruciblesoftware.homingbacon.client;

import net.cruciblesoftware.homingbacon.PreferenceKeys;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.location.Location;
import android.widget.EditText;
import android.widget.Toast;

class UserData {
    private static final String TAG = "HB: " + UserData.class.getSimpleName();

    private Location location;

    private Activity activity;
    private PostOffice post;
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    UserData(PostOffice post) {
        this.post = post;
    }

    void setActivity(Activity a) {
        activity = a;
        prefs = activity.getPreferences(Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    Location getUserLocation() {
        return location;
    }

    void setUserLocation(Location l) {
        location = l;
    }

    boolean hasUsername() {
        String username = prefs.getString(PreferenceKeys.USERNAME, "");
        return !username.isEmpty();
    }

    void getUsernameFromUser() {
        final OpenAlertDialog dialog = new OpenAlertDialog(activity);
        dialog.setTitle(R.string.dialog_get_username_title);
        dialog.setMessage(activity.getText(R.string.dialog_get_username_message));
        dialog.setCancelable(false);

        final EditText input = new EditText(activity);
        dialog.setView(input);
        dialog.setButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int whichButton) {
                String username = input.getText().toString();
                // TODO: need to check with server to see whether username is taken
                boolean usernameOk = !username.isEmpty();

                if(usernameOk) {
                    Toast.makeText(activity, R.string.dialog_get_username_error,
                            Toast.LENGTH_LONG).show();
                    dialog.setCloseFlag(false);
                } else {
                    DebugLog.log(TAG, "saving username '" + username + "'");
                    // TODO: tell server add/change username
                    editor.putString(PreferenceKeys.USERNAME, username);
                    editor.apply();
                    post.dispatchMessage(new Message(Message.Type.NEW_USERNAME, username));
                    dialog.setCloseFlag(true);
                }
            }
        });
        dialog.show();
    }

    String get(String key) {
        return prefs.getString(key, "");
    }

    void set(String key, String val) {
        editor.putString(key, val);
        editor.apply();
    }
}
