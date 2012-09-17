package net.cruciblesoftware.homingbacon.clients.views;

import java.util.ArrayList;
import java.util.Arrays;

import net.cruciblesoftware.homingbacon.clients.models.AppModel;
import net.cruciblesoftware.homingbacon.clients.pojos.Message;
import net.cruciblesoftware.homingbacon.clients.utils.DebugLog;
import net.cruciblesoftware.homingbacon.clients.utils.PostOffice;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class FriendListSpinner extends Spinner implements Message.Listener, OnItemSelectedListener {
    private static final String TAG = "HB: " + FriendListSpinner.class.getSimpleName();

    private AppModel model;
    private PostOffice post;
    private Context context;

    public FriendListSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
        DebugLog.log(TAG, "creating FriendListSpinner");
        this.context = context;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        DebugLog.log(TAG, "finished inflating FriendListSpinner");
        setOnItemSelectedListener(this);

        model = AppModel.getInstance();
        post = PostOffice.getInstance();
        post.registerListener(this);
    }

    @Override
    public void receiveMessage(Message msg) {
        switch(msg.type) {
        case MODEL_UPDATE_FRIEND_LIST:
            String friendStr = model.getFriendList();
            String[] friendArray = friendStr.split(",");
            ArrayList<String> friendList = new ArrayList<String>(Arrays.asList(friendArray));
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, friendList);
            setAdapter(adapter);
            break;
        default:
            break;
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String friendUsername = (String)parent.getItemAtPosition(position);
        if(friendUsername == null || friendUsername.isEmpty()) {
            DebugLog.log(TAG, "bad friend username: '" + friendUsername + "'; not storing");
        } else {
            DebugLog.log(TAG, "selected friend '" + friendUsername + "'");
            post.dispatchMessage(new Message(Message.Type.CONTROL_SPINNER_FRIEND_CHOSEN, friendUsername));
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        DebugLog.log(TAG, "nothing selected");
    }
}
