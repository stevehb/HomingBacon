package net.cruciblesoftware.homingbacon;

import android.os.Bundle;

import com.google.android.maps.MapActivity;

public class HomingBaconActivity extends MapActivity {
    private static final String TAG = HomingBaconActivity.class.getSimpleName();

    private PositionTransmitter transmitter;
    private PositionListener listener;
    private MapSystem map;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        DebugLog.log(TAG, "creating logs and stuff");

        map = new MapSystem(this);
        transmitter = new PositionTransmitter(this);
        listener = new PositionListener(this, map);

    }

    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }
}