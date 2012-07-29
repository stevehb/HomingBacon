package net.cruciblesoftware.homingbacon.client;

import android.app.AlertDialog;
import android.content.Context;

class OpenAlertDialog extends AlertDialog {

    private boolean closeDialog;

    protected OpenAlertDialog(Context context) {
        super(context);
    }

    @Override
    public void dismiss() {
        if(closeDialog) {
            super.dismiss();
        }
    }

    boolean getCloseFlag() {
        return closeDialog;
    }

    void setCloseFlag(boolean flag) {
        closeDialog = flag;
    }
}
