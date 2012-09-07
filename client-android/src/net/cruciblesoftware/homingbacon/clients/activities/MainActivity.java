package net.cruciblesoftware.homingbacon.clients.activities;

import net.cruciblesoftware.homingbacon.PreferenceKeys;
import net.cruciblesoftware.homingbacon.R;
import net.cruciblesoftware.homingbacon.clients.controllers.FriendListSpinnerController;
import net.cruciblesoftware.homingbacon.clients.controllers.ListenController;
import net.cruciblesoftware.homingbacon.clients.controllers.TransmitController;
import net.cruciblesoftware.homingbacon.clients.controllers.UserDataController;
import net.cruciblesoftware.homingbacon.clients.daos.AppPrefs;
import net.cruciblesoftware.homingbacon.clients.models.AppModel;
import net.cruciblesoftware.homingbacon.clients.pojos.Message;
import net.cruciblesoftware.homingbacon.clients.utils.DebugLog;
import net.cruciblesoftware.homingbacon.clients.utils.PostOffice;
import net.cruciblesoftware.homingbacon.clients.views.GetUsernameDialog;
import net.cruciblesoftware.homingbacon.clients.views.MainView;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.maps.MapActivity;

public class MainActivity extends MapActivity implements Message.Listener {
    private static final String TAG = "HB: " + MainActivity.class.getSimpleName();

    private AppModel model;
    private MainView view;
    private ListenController listenController;
    private TransmitController transmitController;
    private FriendListSpinnerController friendListController;
    private UserDataController userDataController;

    private PostOffice post;
    private AppPrefs prefs;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        post = PostOffice.getInstance();
        prefs = new AppPrefs(this);

        DebugLog.log(TAG, "creating model, view, and controllers");
        model = AppModel.getInstance();
        view = (MainView)View.inflate(this, R.layout.activity_main, null);
        setContentView(view);
        listenController = new ListenController();
        transmitController = new TransmitController(this);
        friendListController = new FriendListSpinnerController();
        userDataController = new UserDataController();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadModel();
        post.dispatchMessage(new Message(Message.Type.ON_RESUME, null));

        // check first run
        boolean firstRun = prefs.getBoolean(PreferenceKeys.FIRST_RUN, true);
        boolean hasUser = model.hasUser();
        DebugLog.log(TAG, "first run check: firstRun=" + firstRun + ", hasUser=" + hasUser);
        if(firstRun || !hasUser) {
            DebugLog.log(TAG, "detected no username...getting username");
            getUserName();
            DebugLog.log(TAG, "turning off first run flag");
            prefs.set(PreferenceKeys.FIRST_RUN, false);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveModel();
        post.dispatchMessage(new Message(Message.Type.ON_PAUSE, null));
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
            // go to menu screen
            // TODO: build a menu screen
            return true;
        case R.id.menu_set_username:
            getUserName();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void receiveMessage(Message msg) { }

    private void loadModel() {
        String jsonStr = prefs.getString(PreferenceKeys.APP_DATA);
        DebugLog.log(TAG, "loading app data from preference store: " + jsonStr);
        if(!jsonStr.isEmpty()) {
            model.loadFromJson(jsonStr);
        }
    }

    private void saveModel() {
        String jsonStr = model.serializeToJson();
        DebugLog.log(TAG, "saving app data to preference store: " + jsonStr);
        prefs.set(PreferenceKeys.APP_DATA, jsonStr);
    }

    private void getUserName() {
        GetUsernameDialog dialog = new GetUsernameDialog(this);
        dialog.show();
    }

    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }
}
