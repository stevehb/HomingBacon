package net.cruciblesoftware.homingbacon.client;

import net.cruciblesoftware.homingbacon.JsonKeys;
import net.cruciblesoftware.homingbacon.JsonUtils;
import net.cruciblesoftware.homingbacon.JsonValues;
import net.cruciblesoftware.homingbacon.PreferenceKeys;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.google.gson.JsonObject;

class FriendSpinnerControl extends BaseControl implements OnItemSelectedListener, Message.Listener {
    private static final String TAG = "HB: " + FriendSpinnerControl.class.getSimpleName();

    private HomingBaconActivity activity;
    private Spinner friendSpinner;
    private ArrayAdapter<String> spinnerAdapter;

    private Message.Listener usernameListener = new Message.Listener() {
        @Override
        public void receiveMessage(Message msg) {
            switch(msg.type) {
            case NEW_USERNAME:
                DebugLog.log(TAG, "setting new username: " + msg.data);

                break;
            case ON_PAUSE:
                break;
            case ON_RESUME:
                break;
            case SERVER_RESPONSE:
                DebugLog.log(TAG, "message back from server: " + msg.data);
                JsonObject json = JsonUtils.getJsonObject(msg.data);
                if(json.get(JsonKeys.STATUS).getAsString().equals(JsonValues.ERROR)) {
                    setSpinnerValues(new String[] { "N/A" });
                    return;
                }
                String nameList = json.get(JsonKeys.FRIEND_LIST).getAsString();
                String[] names = nameList.split(",");
                setSpinnerValues(names);
                userData.set(PreferenceKeys.FRIEND_LIST, nameList);
                break;
            default:
                break;

            }
        }
    };

    FriendSpinnerControl(HomingBaconActivity a) {
        activity = a;
        friendSpinner = (Spinner)activity.findViewById(R.id.username_spinner);
        friendSpinner.setOnItemSelectedListener(this);
        String oldFriendList = userData.get(PreferenceKeys.FRIEND_LIST);
        if(!oldFriendList.isEmpty()) {
            setSpinnerValues(oldFriendList.split(","));
        }

        server.getFriends(userData.get(PreferenceKeys.USERNAME), usernameListener);
    }

    @Override
    public void receiveMessage(Message msg) {
        switch(msg.type) {
        case NEW_USERNAME:

        }
    }

    private void setSpinnerValues(String[] names) {
        spinnerAdapter = new ArrayAdapter<String>(activity, android.R.layout.simple_spinner_item, names);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        friendSpinner.setAdapter(spinnerAdapter);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        String friendName = (String) parent.getItemAtPosition(pos);
        DebugLog.log(TAG, "item selected: pos=" + pos + ", itemAt=" + friendName);
        userData.set(PreferenceKeys.FRIEND_NAME,  friendName);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        DebugLog.log(TAG, "nothing selected, nothing handled");
    }
}
