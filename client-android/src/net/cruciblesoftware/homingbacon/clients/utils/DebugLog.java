package net.cruciblesoftware.homingbacon.clients.utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

import android.os.Environment;
import android.util.Log;

public class DebugLog {
    private static final String TAG = "HB: " + DebugLog.class.getSimpleName();
    private static BufferedWriter writer;
    static boolean writeToFile = false;
    static boolean muzzle = false;

    static {
        try {
            if(!muzzle) {
                Log.d(TAG, "-------->>>>>>>> creating HomingBacon log <<<<<<<<--------");
            }
            if(!muzzle && writeToFile) {
                String path = Environment.getExternalStorageDirectory() + "/homingbacon_log.txt";
                writer = new BufferedWriter(new FileWriter(path, true));
                writer.write("Starting HomingBacon log at " + (new Date()).toString() + "\n");
                writer.flush();
            }
        } catch(IOException e) {
            Log.e(TAG, "IOException on file creation: " + e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    public static void err(String tag, String msg) {
        err(tag, msg, new Throwable());
    }

    public static void err(String tag, String msg, Throwable e) {
        try {
            Log.e(tag, msg, e);
            if(writeToFile) {
                writer.write(tag + ": " + msg + "\n" +
                        "stack:\n" + Log.getStackTraceString(e) + "\n");
                writer.flush();
            }
        } catch(IOException ex) {
            Log.e(TAG, "IOException on file write: " + ex.getLocalizedMessage(), ex);
        }
    }

    public static void log(String tag, String msg) {
        try {
            if(!muzzle) {
                Log.d(tag, msg);
            }
            if(!muzzle && writeToFile) {
                writer.write(tag + ": " + msg + "\n");
                writer.flush();
            }
        } catch(IOException ex) {
            Log.e(TAG, "IOException on file write: " + ex.getLocalizedMessage(), ex);
        }
    }
}
