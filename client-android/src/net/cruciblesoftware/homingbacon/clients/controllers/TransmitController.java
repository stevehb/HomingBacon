package net.cruciblesoftware.homingbacon.clients.controllers;

import net.cruciblesoftware.homingbacon.JsonUtils;
import net.cruciblesoftware.homingbacon.Position;
import net.cruciblesoftware.homingbacon.clients.daos.GpsListener;
import net.cruciblesoftware.homingbacon.clients.daos.ServerConnection;
import net.cruciblesoftware.homingbacon.clients.models.AppModel;
import net.cruciblesoftware.homingbacon.clients.pojos.Message;
import net.cruciblesoftware.homingbacon.clients.pojos.UserPosition;
import net.cruciblesoftware.homingbacon.clients.utils.DebugLog;
import net.cruciblesoftware.homingbacon.clients.utils.PostOffice;
import android.app.Activity;

import com.google.gson.JsonObject;

public class TransmitController implements Message.Listener {
    private static final String TAG = "HB: " + TransmitController.class.getSimpleName();

    private AppModel model;
    private PostOffice post;

    private boolean isTransmitting;
    private GpsListener gpsListener;
    private ServerConnection server;

    public TransmitController(Activity activity) {
        DebugLog.log(TAG, "creating TransmitController");
        model = AppModel.getInstance();
        post = PostOffice.getInstance();
        post.registerListener(this);
        isTransmitting = false;
        gpsListener = new GpsListener(activity, this);
        server = new ServerConnection();
    }

    @Override
    public void receiveMessage(Message msg) {
        switch(msg.type) {
        case CONTROL_TOGGLE_TRANSMIT_ON:
            gpsListener.activate();
            isTransmitting = true;
            break;
        case CONTROL_TOGGLE_TRANSMIT_OFF:
            gpsListener.deactivate();
            isTransmitting = false;
            break;
        case GPS_RESPONSE:
            JsonObject jsonObj = JsonUtils.getJsonObject(msg.data);
            Position pos = JsonUtils.parsePosition(jsonObj);
            DebugLog.log(TAG, "updating model with new GPS position: " + pos.toString());
            model.setUserPosition(pos);
            break;
        case MODEL_UPDATE_USER_POSITION:
            DebugLog.log(TAG, "getting user position update message");
            if(isTransmitting) {
                UserPosition user = model.getUser();
                server.setLocation(user.getUsername(), user.getLatitude(), user.getLongitude(), user.getAccuracy(), user.getEpochTime(), this);
            }
            break;
        case SERVER_RESPONSE:
            String errMsg = JsonUtils.getServerErrorMessage(msg.data);
            if(errMsg != null) {
                DebugLog.err(TAG, "ERROR: transmitting position: server reports: " + errMsg);
            }
            break;
        default:
            break;
        }
    }
}
