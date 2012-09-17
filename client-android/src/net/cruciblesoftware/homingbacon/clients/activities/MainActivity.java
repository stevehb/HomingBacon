package net.cruciblesoftware.homingbacon.clients.activities;

import net.cruciblesoftware.homingbacon.R;
import net.cruciblesoftware.homingbacon.clients.controllers.FriendListSpinnerController;
import net.cruciblesoftware.homingbacon.clients.controllers.ListenController;
import net.cruciblesoftware.homingbacon.clients.controllers.TransmitController;
import net.cruciblesoftware.homingbacon.clients.controllers.UserDataController;
import net.cruciblesoftware.homingbacon.clients.daos.AppPrefs;
import net.cruciblesoftware.homingbacon.clients.models.AppModel;
import net.cruciblesoftware.homingbacon.clients.pojos.Message;
import net.cruciblesoftware.homingbacon.clients.pojos.PreferenceKeys;
import net.cruciblesoftware.homingbacon.clients.utils.DebugLog;
import net.cruciblesoftware.homingbacon.clients.utils.PostOffice;
import net.cruciblesoftware.homingbacon.clients.views.GetUsernameDialog;
import net.cruciblesoftware.homingbacon.clients.views.MainView;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

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

        PreferenceKeys.init(this);
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
        String username = prefs.getString(PreferenceKeys.USERNAME);
        String accountName = prefs.getString(PreferenceKeys.ACCOUNT_NAME);
        boolean needSettings = firstRun && (username.isEmpty() || accountName.isEmpty());

        // either the first run, or we need more info, send user to settings
        if(needSettings) {
            DebugLog.log(TAG, "need settings: firstRun=" + firstRun + ", username='" + username + "', account='" + accountName + "'");
            launchSettings(true);
            return;
        }

        // if user has entered info but we haven't created a user, then do that
        if(!model.hasUser()) {
            model.setUsername(username);
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
            launchSettings(false);
            return true;
        case R.id.menu_add_friend:
            Toast.makeText(this, "Coming Soon", Toast.LENGTH_LONG).show();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void receiveMessage(Message msg) {
        switch(msg.type) {
        case MODEL_CREATE_USER:
            break;
        default:
            break;
        }
    }

    private void launchSettings(boolean firstRun) {
        Intent i = new Intent(this, SettingsActivity.class);
        String pkgName = PreferenceKeys.class.getPackage().getName();
        String key = pkgName + "." + PreferenceKeys.FIRST_RUN;
        DebugLog.log(TAG, "setting extra '" + key + "'=" + firstRun);
        i.putExtra(key, firstRun);
        this.startActivity(i);
    }

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
