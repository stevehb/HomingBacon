package net.cruciblesoftware.homingbacon.clients.views;

import net.cruciblesoftware.homingbacon.JsonKeys;
import net.cruciblesoftware.homingbacon.JsonUtils;
import net.cruciblesoftware.homingbacon.R;
import net.cruciblesoftware.homingbacon.clients.daos.ServerConnection;
import net.cruciblesoftware.homingbacon.clients.models.AppModel;
import net.cruciblesoftware.homingbacon.clients.pojos.Message;
import net.cruciblesoftware.homingbacon.clients.utils.DebugLog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.Toast;

import com.google.gson.JsonObject;

public class CheckUsernameProgressDialog extends ProgressDialog implements Message.Listener, DialogInterface.OnCancelListener {
    private static final String TAG = "HB: " + CheckUsernameProgressDialog.class.getSimpleName();

    private AppModel model;
    private String newUsername;
    private ServerConnection server;
    private boolean isCancelled;

    public CheckUsernameProgressDialog(Context context, String newUsername) {
        super(context);
        this.newUsername = newUsername;
        server = new ServerConnection();
        isCancelled = false;
        model = AppModel.getInstance();

        setProgressStyle(STYLE_SPINNER);
        setMessage(context.getText(R.string.dialog_check_username_message));
        setCancelable(true);
        setOnCancelListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        server.hasUser(newUsername, this);
    }

    @Override
    public void receiveMessage(Message msg) {
        if(isCancelled) {
            dismiss();
            return;
        }

        switch(msg.type) {
        case SERVER_RESPONSE:
            DebugLog.log(TAG, "message back from server: " + msg.data);

            // check for error
            String errMsg = JsonUtils.getServerErrorMessage(msg.data);
            if(errMsg != null) {
                DebugLog.err(TAG, "ERROR: checking newUsername: server reports: " + errMsg);
                dismiss();
            }

            JsonObject jsonObj = JsonUtils.getJsonObject(msg.data);
            boolean hasUsername = jsonObj.get(JsonKeys.HAS_USER).getAsBoolean();
            if(!hasUsername) {
                // if there is no such user, then set the model
                Toast.makeText(getContext(), R.string.toast_username_accepted, Toast.LENGTH_LONG).show();
                DebugLog.log(TAG, "setting username to '" + newUsername + "'");
                model.setUsername(newUsername);
            } else {
                // otherwise display the first dialog again
                Toast.makeText(getContext(), R.string.toast_username_already_exists_error, Toast.LENGTH_LONG).show();
                DebugLog.log(TAG, "username already exists");
                GetUsernameDialog dialog = new GetUsernameDialog(getContext());
                dialog.show();
            }
            break;
        default:
            break;
        }
        dismiss();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        isCancelled = true;
        if(model.hasUser() && model.getUser().getUsername().isEmpty()) {
            Toast.makeText(getContext(), R.string.toast_cancel_no_username, Toast.LENGTH_LONG).show();
        }
    }
}
