package net.cruciblesoftware.homingbacon.clients.views;

import net.cruciblesoftware.homingbacon.R;
import net.cruciblesoftware.homingbacon.clients.models.AppModel;
import net.cruciblesoftware.homingbacon.clients.utils.DebugLog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.EditText;
import android.widget.Toast;

public class GetUsernameDialog extends OpenAlertDialog implements DialogInterface.OnClickListener, DialogInterface.OnCancelListener {
    private static final String TAG = "HB: " + GetUsernameDialog.class.getSimpleName();

    private AppModel model;
    private EditText inputTextBox;
    private boolean hasUsername;
    private String currentUsername;

    public GetUsernameDialog(Context context) {
        super(context);
        model = AppModel.getInstance();
        setCloseOnDismiss(true);
        inputTextBox = new EditText(getContext());
        DebugLog.log(TAG, "creating GetUsernameDialog");

        setTitle(R.string.dialog_get_username_title);
        setMessage(getContext().getText(R.string.dialog_get_username_message));
        setCancelable(true);
        setOnCancelListener(this);
        setView(inputTextBox);
        setButton("OK", this);

        // put in the current username, if it exists, into input box
        currentUsername = "";
        hasUsername = model.hasUser() && !model.getUser().getUsername().isEmpty();
        if(hasUsername) {
            currentUsername = model.getUser().getUsername();
            inputTextBox.setText(currentUsername);
        }
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        String userInput = inputTextBox.getText().toString();
        DebugLog.log(TAG, "user input for username: " + userInput);

        setCloseOnDismiss(true);
        boolean usernameBlank = userInput.isEmpty();
        if(usernameBlank) {
            DebugLog.log(TAG, "new username is blank");
            if(!hasUsername) {
                DebugLog.log(TAG, "system does not have username: not closing");
                Toast.makeText(getContext(), R.string.toast_username_blank_error, Toast.LENGTH_LONG).show();
                setCloseOnDismiss(false);
            } else {
                DebugLog.log(TAG, "system does have username: allowing close");
                Toast.makeText(getContext(), R.string.toast_username_no_change, Toast.LENGTH_LONG).show();
            }
        } else {
            DebugLog.log(TAG, "new username is '" + userInput + "'");
            if(!userInput.equals(currentUsername)) {
                DebugLog.log(TAG, "does not match current username: allow close and check with server");
                CheckUsernameProgressDialog checkDialog = new CheckUsernameProgressDialog(getContext(), userInput);
                checkDialog.show();
            } else {
                DebugLog.log(TAG, "matches current username: allow close and ignore");
            }
        }
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        setCloseOnDismiss(true);
        DebugLog.log(TAG, "user cancelled: allowing close");
        if(!hasUsername) {
            DebugLog.log(TAG, "displaying warning toast message about no user");
            Toast.makeText(getContext(), R.string.toast_cancel_no_username, Toast.LENGTH_LONG).show();
        }
    }
}
