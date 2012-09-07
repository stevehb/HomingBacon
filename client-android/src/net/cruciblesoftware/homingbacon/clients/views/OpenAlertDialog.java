package net.cruciblesoftware.homingbacon.clients.views;

import net.cruciblesoftware.homingbacon.clients.utils.DebugLog;
import android.app.AlertDialog;
import android.content.Context;

public class OpenAlertDialog extends AlertDialog {
    private static final String TAG = "HB: " + OpenAlertDialog.class.getSimpleName();

    private boolean closeDialog;

    public OpenAlertDialog(Context context) {
        super(context);
    }

    @Override
    public void dismiss() {
        if(closeDialog) {
            DebugLog.log(TAG, "close flag is set: closing dialog");
            super.dismiss();
        }
    }

    public boolean getCloseOnDismiss() {
        return closeDialog;
    }

    public void setCloseOnDismiss(boolean flag) {
        closeDialog = flag;
    }
}
