package net.cruciblesoftware.homingbacon.client;

import android.os.Bundle;
import android.view.Menu;

import com.google.android.maps.MapActivity;

public class HomingBaconActivity extends MapActivity {
    private static final String TAG = "HB: " + HomingBaconActivity.class.getSimpleName();

    private UserSettings settings;
    private ListenControl listenControl;
    private TransmitControl transmitControl;
    private FriendSpinnerControl friendSpinnerControl;
    private MapSystem map;

    private String username = "stevehb";
    private String friendname = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homing_bacon);

        settings = UserSettings.getInstance();
        settings.setActivity(this);

        listenControl = new ListenControl(this);
        transmitControl = new TransmitControl(this);
        friendSpinnerControl = new FriendSpinnerControl(this);
        map = new MapSystem(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!settings.hasUsername()) {
            settings.getUsernameFromUser();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_homing_bacon, menu);
        return true;
    }

    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }
}
