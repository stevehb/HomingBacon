package net.cruciblesoftware.homingbacon.clients.daos;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import net.cruciblesoftware.homingbacon.JsonKeys;
import net.cruciblesoftware.homingbacon.JsonUtils;
import net.cruciblesoftware.homingbacon.JsonValues;
import net.cruciblesoftware.homingbacon.ServerActions;
import net.cruciblesoftware.homingbacon.UrlParameters;
import net.cruciblesoftware.homingbacon.clients.pojos.Message;
import net.cruciblesoftware.homingbacon.clients.utils.DebugLog;
import net.cruciblesoftware.homingbacon.clients.utils.UrlBuilder;
import android.os.AsyncTask;

import com.google.gson.JsonObject;

public class ServerConnection {
    private static final String TAG = "HB: " + ServerConnection.class.getSimpleName();
    private static final String BASE_URL = "http://homingbacon.appspot.com/homingbacon";

    private boolean isNetAvail;

    private class ServerRequest {
        public String url;
        public Message.Listener listener;
        public String resp;

        public ServerRequest(String url, Message.Listener listener) {
            this.url = url;
            this.listener = listener;
        }
    }

    private class ServerRequestTask extends AsyncTask<ServerRequest, Void, ServerRequest> {
        @Override
        protected ServerRequest doInBackground(ServerRequest... params) {
            if(isNetAvail == false) {
                return null;
            }

            ServerRequest req = params[0];
            HttpURLConnection conn = null;

            DebugLog.log(TAG, "requesting: " + req.url);

            try {
                URL url = new URL(req.url);
                conn = (HttpURLConnection)url.openConnection();
                req.resp = getString(conn.getInputStream());
            } catch(Exception e) {
                DebugLog.err(TAG, "ERROR: tried url=" + req.url + "\n" + e.getLocalizedMessage(), e);
                JsonObject json = JsonUtils.createJson(new Object[] {
                        JsonKeys.STATUS, JsonValues.ERROR,
                        JsonKeys.MESSAGE, "Exception: " + e.getLocalizedMessage()});
                req.resp = json.toString();
            } finally {
                if(conn != null) {
                    conn.disconnect();
                }
            }
            return req;
        }

        @Override
        protected void onPostExecute(ServerRequest req) {
            if(req != null && req.listener != null) {
                req.listener.receiveMessage(new Message(Message.Type.SERVER_RESPONSE, req.resp));
            }
        }
    }

    public ServerConnection() {
        isNetAvail = true;
    }

    public void hasUser(String username, Message.Listener listener) {
        String strUrl = new UrlBuilder().setHost(BASE_URL)
                .addParam(UrlParameters.ACTION, ServerActions.HAS_USER)
                .addParam(UrlParameters.USERNAME, username)
                .toString();
        ServerRequest req = new ServerRequest(strUrl, listener);
        new ServerRequestTask().execute(req);
    }

    public void addUser(String username, Message.Listener listener) {
        String strUrl = new UrlBuilder().setHost(BASE_URL)
                .addParam(UrlParameters.ACTION, ServerActions.ADD_USER)
                .addParam(UrlParameters.USERNAME, username)
                .toString();
        ServerRequest req = new ServerRequest(strUrl, listener);
        new ServerRequestTask().execute(req);
    }

    public void changeUsername(String username, String newUsername, Message.Listener listener) {
        String strUrl = new UrlBuilder().setHost(BASE_URL)
                .addParam(UrlParameters.ACTION, ServerActions.ADD_USER)
                .addParam(UrlParameters.USERNAME, username)
                .addParam(UrlParameters.NEW_USERNAME, newUsername)
                .toString();
        ServerRequest req = new ServerRequest(strUrl, listener);
        new ServerRequestTask().execute(req);
    }

    public void getFriends(String username, Message.Listener listener) {
        String strUrl = new UrlBuilder().setHost(BASE_URL)
                .addParam(UrlParameters.ACTION, ServerActions.GET_FRIEND_LIST)
                .addParam(UrlParameters.USERNAME, username)
                .toString();
        ServerRequest req = new ServerRequest(strUrl, listener);
        new ServerRequestTask().execute(req);
    }

    public void getLocation(String username, String friend, Message.Listener listener) {
        String strUrl = new UrlBuilder().setHost(BASE_URL)
                .addParam(UrlParameters.ACTION, ServerActions.GET_POSITION)
                .addParam(UrlParameters.USERNAME, username)
                .addParam(UrlParameters.FRIEND, friend)
                .toString();
        ServerRequest req = new ServerRequest(strUrl, listener);
        new ServerRequestTask().execute(req);
    }

    public void setLocation(String username,
            double latitude, double longitude,
            double accuracy, long time, Message.Listener listener) {
        String strUrl = new UrlBuilder().setHost(BASE_URL)
                .addParam(UrlParameters.ACTION, ServerActions.SET_POSITION)
                .addParam(UrlParameters.USERNAME, username)
                .addParam(UrlParameters.LATITUDE, Double.toString(latitude))
                .addParam(UrlParameters.LONGITUDE, Double.toString(longitude))
                .addParam(UrlParameters.ACCURACY, Double.toString(accuracy))
                .addParam(UrlParameters.EPOCH_TIME, Long.toString(time))
                .toString();
        ServerRequest req = new ServerRequest(strUrl, listener);
        new ServerRequestTask().execute(req);
    }

    private String getString(InputStream is)
            throws IOException {
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        StringBuilder buff = new StringBuilder();
        String line;
        while((line = br.readLine()) != null) {
            buff.append(line);
        }
        return buff.toString();
    }

    /*
    private boolean isOnline() {
        // borrowed from http://stackoverflow.com/a/4009133/324625
        ConnectivityManager cm;
        cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if(netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }
     */
}
