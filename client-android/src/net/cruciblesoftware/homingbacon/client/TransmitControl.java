package net.cruciblesoftware.homingbacon.client;

import net.cruciblesoftware.homingbacon.JsonKeys;
import net.cruciblesoftware.homingbacon.JsonValues;
import net.cruciblesoftware.homingbacon.PreferenceKeys;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

class TransmitControl extends BaseControl implements Message.Listener {
    private static final String TAG = "HB: " + TransmitControl.class.getSimpleName();

    private HomingBaconActivity activity;
    private CheckBox transmitCheckbox;
    private GpsListener gpsListener;

    private Message.Listener errorReporter = new Message.Listener() {
        @Override
        public void receiveMessage(Message msg) {
            // sanity check on message type
            if(msg.type != Message.Type.SERVER_RESPONSE) {
                DebugLog.log(TAG, "ERROR: expecting message of type " +
                        Message.Type.SERVER_RESPONSE + ", got message of type " +
                        msg.type + ", data=" + msg.data);
                return;
            }
            // check server's response
            JsonObject json = new JsonParser().parse(msg.data).getAsJsonObject();
            String status = json.get(JsonKeys.STATUS).getAsString();
            if(status.equals(JsonValues.ERROR)) {
                DebugLog.log(TAG, "ERROR: server reported error: " +
                        json.get(JsonKeys.MESSAGE).getAsString());
                return;
            }
        }
    };

    TransmitControl(HomingBaconActivity a) {
        activity = a;
        gpsListener = new GpsListener(activity, post);
        transmitCheckbox = (CheckBox)activity.findViewById(R.id.transmit_checkbox);
        transmitCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    gpsListener.activiate();
                } else {
                    gpsListener.deactivate();
                }
            }
        });
    }

    @Override
    public void receiveMessage(Message msg) {
        switch(msg.type) {
        case NEW_USER_LOCATION:
            if(transmitCheckbox.isChecked()) {
                JsonParser parser = new JsonParser();
                JsonObject json = parser.parse(msg.data).getAsJsonObject();
                server.setLocation(userData.get(PreferenceKeys.USERNAME),
                        json.get(JsonKeys.LATITUDE).getAsDouble(),
                        json.get(JsonKeys.LONGITUDE).getAsDouble(),
                        json.get(JsonKeys.ACCURACY).getAsDouble(),
                        json.get(JsonKeys.EPOCH_TIME).getAsLong(),
                        errorReporter);
            }
            break;
        default:
            break;
        }
    }
}
