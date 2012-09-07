package net.cruciblesoftware.homingbacon.clients.views;

import net.cruciblesoftware.homingbacon.clients.models.AppModel;
import net.cruciblesoftware.homingbacon.clients.pojos.Message;
import net.cruciblesoftware.homingbacon.clients.pojos.UserPosition;
import net.cruciblesoftware.homingbacon.clients.utils.DebugLog;
import net.cruciblesoftware.homingbacon.clients.utils.PostOffice;
import android.content.Context;
import android.util.AttributeSet;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;

public class FriendMapView extends MapView implements Message.Listener {
    private static final String TAG = "HB: " + FriendMapView.class.getSimpleName();

    private AppModel model;
    private PostOffice post;
    private MapController mapController;

    public FriendMapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        DebugLog.log(TAG, "creating FriendMapView");
        model = AppModel.getInstance();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        DebugLog.log(TAG, "finished inflating FriendMapView");
        post = PostOffice.getInstance();
        post.registerListener(this);

        setBuiltInZoomControls(true);
        mapController = getController();
        mapController.setZoom(16);
    }

    @Override
    public void receiveMessage(Message msg) {
        switch(msg.type) {
        case MODEL_UPDATE_FRIEND_POSITION:
            UserPosition f = model.getFriend();
            updateMap(f.getLatitude(), f.getLongitude(), f.getAccuracy(), f.getEpochTime());
            break;
        default:
            break;
        }
    }

    private void updateMap(double lat, double lon, double accuracy, long time) {
        DebugLog.log(TAG, "moving map to lat=" + lat + ", lon=" + lon + ", accuracy=" + accuracy);
        mapController.setCenter(new GeoPoint((int) (lat * 1000000), (int) (lon * 1000000)));
        // TODO: add circle based on accuracy?
        // TODO: add point balloon with extra information (address, username, etc)
    }
}
