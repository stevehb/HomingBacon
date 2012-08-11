package net.cruciblesoftware.homingbacon.client;

import net.cruciblesoftware.homingbacon.PreferenceKeys;
import android.os.Handler;
import android.widget.CheckBox;
import android.widget.CompoundButton;

class ListenControl extends BaseControl {
    private static final String TAG = "HB: " + ListenControl.class.getSimpleName();
    private static final long LISTERN_DELAY = 1000;

    private HomingBaconActivity activity;
    private CheckBox listenCheckbox;


    Handler listener = new Handler();
    Runnable responder = new Runnable() {
        Message.Listener msgListener = new Message.Listener() {
            @Override
            public void receiveMessage(Message msg) {
                post.dispatchMessage(new Message(Message.Type.NEW_FRIEND_LOCATION, msg.data));
            }
        };

        @Override
        public void run() {
            if(!listenCheckbox.isChecked()) {
                DebugLog.log(TAG, "listener disabled, stopping callbacks");
                return;
            }
            server.getLocation(userData.get(PreferenceKeys.USERNAME),
                    userData.get(PreferenceKeys.FRIEND_NAME),
                    msgListener);
            if(listenCheckbox.isChecked()) {
                listener.postDelayed(responder, LISTERN_DELAY);
            }
        }
    };

    ListenControl(HomingBaconActivity a) {
        activity = a;
        listenCheckbox = (CheckBox)activity.findViewById(R.id.listen_checkbox);
        listenCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    DebugLog.log(TAG, "starting listener with delay=" + LISTERN_DELAY + "ms");
                    listener.postDelayed(responder, LISTERN_DELAY);
                }
            }
        });
    }
}
