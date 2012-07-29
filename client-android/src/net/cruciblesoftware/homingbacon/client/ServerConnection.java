package net.cruciblesoftware.homingbacon.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import net.cruciblesoftware.homingbacon.ServerActions;
import net.cruciblesoftware.homingbacon.UrlParameters;

class ServerConnection implements Message.Listener {
    private static final String TAG = "HB: " + ServerConnection.class.getSimpleName();
    private static final String BASE_URL = "http://homingbacon.appspot.com/homingbacon";

    private PostOffice post;

    private boolean isNetAvail;
    private URL url;
    private HttpURLConnection conn;

    ServerConnection(PostOffice p) {
        post = p;
        post.registerListener(this);
        isNetAvail = true;
    }

    void getFriends(String username, Message.Listener listener) {
        String strUrl = new UrlBuilder().setHost(BASE_URL)
                .addParam(UrlParameters.ACTION, ServerActions.GET_FRIEND_LIST)
                .addParam(UrlParameters.USERNAME, username)
                .toString();
        String resp = getServerResponse(strUrl);
        listener.receiveMessage(new Message(Message.Type.SERVER_RESPONSE, resp));
    }

    void getLocation(String username, String friend, Message.Listener listener) {
        String strUrl = new UrlBuilder().setHost(BASE_URL)
                .addParam(UrlParameters.ACTION, ServerActions.GET_POSITION)
                .addParam(UrlParameters.USERNAME, username)
                .addParam(UrlParameters.FRIEND, friend)
                .toString();
        String resp = getServerResponse(strUrl);
        listener.receiveMessage(new Message(Message.Type.SERVER_RESPONSE, resp));
    }

    void setLocation(String username,
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
        String resp = getServerResponse(strUrl);
        listener.receiveMessage(new Message(Message.Type.SERVER_RESPONSE, resp));
    }

    @Override
    public void receiveMessage(Message message) {
        switch(message.type) {
        case ON_RESUME:
            // test network status here
            isNetAvail = true;
            break;

        case ON_PAUSE:
            if(conn != null) {
                conn.disconnect();
            }
            break;

        default:
            break;
        }
    }

    private String getServerResponse(String strUrl) {
        String resp = "";
        url = null;
        conn = null;
        try {
            url = new URL(strUrl);
            conn = (HttpURLConnection)url.openConnection();
            resp = getString(conn.getInputStream());
        } catch(MalformedURLException e) {
            DebugLog.log(TAG, "ERROR: malformed URL: " + strUrl + "\n" + e.getLocalizedMessage());
            resp = "ERROR";
        } catch(IOException e) {
            DebugLog.log(TAG, "ERROR: I/O problem: " + strUrl + "\n" + e.getLocalizedMessage());
            resp = "ERROR";
        } finally {
            if(conn != null) {
                conn.disconnect();
            }
        }
        return resp;
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
}
