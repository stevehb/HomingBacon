package net.cruciblesoftware.homingbacon.client;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.maps.MapActivity;

public class HomingBaconActivity extends MapActivity {
    private static final String TAG = "HB: " + HomingBaconActivity.class.getSimpleName();

    private ListenControl listenControl;
    private TransmitControl transmitControl;
    private FriendSpinnerControl friendSpinnerControl;
    private MapSystem map;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homing_bacon);

        BaseControl.userData.setActivity(this);

        // create supporting objects: transmitter, listener, spinner, map
        listenControl = new ListenControl(this);
        transmitControl = new TransmitControl(this);
        friendSpinnerControl = new FriendSpinnerControl(this);
        map = new MapSystem(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        BaseControl.post.dispatchMessage(new Message(Message.Type.ON_RESUME, ""));
        if(!BaseControl.userData.hasUsername()) {
            BaseControl.userData.getUsernameFromUser();
        }
    }

    @Override
    protected void onPause() {
        BaseControl.post.dispatchMessage(new Message(Message.Type.ON_PAUSE, ""));
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        DebugLog.log(TAG, "creating menu");
        getMenuInflater().inflate(R.menu.activity_homing_bacon, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_settings:
            return true;
        case R.id.menu_set_username:
            BaseControl.userData.getUsernameFromUser();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }
}
