package net.cruciblesoftware.homingbacon.clients.views;

import net.cruciblesoftware.homingbacon.R;
import net.cruciblesoftware.homingbacon.clients.models.AppModel;
import net.cruciblesoftware.homingbacon.clients.pojos.Message;
import net.cruciblesoftware.homingbacon.clients.utils.DebugLog;
import net.cruciblesoftware.homingbacon.clients.utils.PostOffice;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;

public class MainView extends RelativeLayout implements Message.Listener {
    private static final String TAG = "HB: " + MainView.class.getSimpleName();

    private PostOffice post;
    private AppModel model;

    private CheckBox listenCheckBox, transmitCheckBox;
    private FriendMapView friendMapView;
    private FriendListSpinner friendListSpinner;

    public MainView(Context context, AttributeSet attrs) {
        super(context, attrs);
        DebugLog.log(TAG, "creating MainView");
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        DebugLog.log(TAG, "finished inflating MainView");
        post = PostOffice.getInstance();
        post.registerListener(this);
        model = AppModel.getInstance();

        listenCheckBox = (CheckBox)findViewById(R.id.listen_checkbox);
        if(listenCheckBox != null) {
            listenCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    Message.Type type;
                    type = (isChecked ? Message.Type.CONTROL_TOGGLE_LISTEN_ON : Message.Type.CONTROL_TOGGLE_LISTEN_OFF);
                    post.dispatchMessage(new Message(type, null));
                }
            });
            listenCheckBox.setChecked(false);
            listenCheckBox.setEnabled(false);
        }

        transmitCheckBox = (CheckBox)findViewById(R.id.transmit_checkbox);
        if(transmitCheckBox != null) {
            transmitCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    Message.Type type;
                    type = (isChecked ? Message.Type.CONTROL_TOGGLE_TRANSMIT_ON: Message.Type.CONTROL_TOGGLE_TRANSMIT_OFF);
                    post.dispatchMessage(new Message(type, null));
                }
            });
            transmitCheckBox.setChecked(false);
            transmitCheckBox.setEnabled(false);
        }

        friendListSpinner = (FriendListSpinner)findViewById(R.id.friend_list_spinner);
        if(friendListSpinner != null) {
            friendListSpinner.setEnabled(false);
        }

        friendMapView = (FriendMapView)findViewById(R.id.map);
    }

    @Override
    public void receiveMessage(Message msg) {
        switch(msg.type) {
        case ON_RESUME:
        case MODEL_CREATE_USER:
            if(model.hasUser()) {
                listenCheckBox.setEnabled(true);
                transmitCheckBox.setEnabled(true);
                friendListSpinner.setEnabled(true);
            }
            break;
        case CONTROL_TOGGLE_LISTEN_OFF:
            listenCheckBox.setChecked(false);
            break;
        case CONTROL_TOGGLE_TRANSMIT_OFF:
            transmitCheckBox.setChecked(false);
            break;
        }
    }
}
