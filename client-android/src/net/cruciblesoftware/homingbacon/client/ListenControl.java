package net.cruciblesoftware.homingbacon.client;

import android.widget.CheckBox;
import android.widget.Spinner;

class ListenControl {
    private HomingBaconActivity activity;
    private CheckBox listenCheckbox, transmitCheckbox;
    private Spinner usernameSpinner;

    ListenControl(HomingBaconActivity a) {
        activity = a;
        listenCheckbox = (CheckBox)activity.findViewById(R.id.listen_checkbox);
    }


}
