package net.cruciblesoftware.homingbacon.server;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.cruciblesoftware.homingbacon.JsonKeys;
import net.cruciblesoftware.homingbacon.JsonValues;
import net.cruciblesoftware.homingbacon.ServerActions;
import net.cruciblesoftware.homingbacon.UrlParameters;

import com.google.gson.JsonObject;

public class HomingBaconServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        doGet(req, resp);
    }

    /*
     * 
    // UserService userServ = UserServiceFactory.getUserService();
    // User user = userServ.getCurrentUser();
     * if(user == null) {
     * resp.sendRedirect(userServ.createLoginURL(req.getRequestURI())); }
     * else { writer.println("user nickname=" + user.getNickname());
     * writer.println("user id=" + user.getUserId());
     * writer.println("user email=" + user.getEmail()); }
     */

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        // set up server's response
        resp.setContentType("text/plain");
        JsonObject json = new JsonObject();

        // basic GET params
        String paramAction = null;
        String paramUsername = null;

        try {
            // check basic params
            paramAction = getUrlParam(req, UrlParameters.ACTION);
            paramUsername = getUrlParam(req, UrlParameters.USERNAME);

            // parse action
            DataProvider data = new DataProvider();
            if(paramAction.equalsIgnoreCase(ServerActions.HAS_USER)) {
                boolean hasUser = data.hasUser(paramUsername);
                json.addProperty(JsonKeys.STATUS, JsonValues.SUCCESS);
                json.addProperty(JsonKeys.MESSAGE, hasUser);
            } else if(paramAction.equalsIgnoreCase(ServerActions.ADD_USER)) {
                data.addUser(paramUsername);
                json.addProperty(JsonKeys.STATUS, JsonValues.SUCCESS);
            } else if(paramAction.equalsIgnoreCase(ServerActions.GET_USERS)) {
                String users = data.getUserList(paramUsername);
                json.addProperty(JsonKeys.USER_LIST, users);
                json.addProperty(JsonKeys.STATUS, JsonValues.SUCCESS);
            } else if(paramAction.equalsIgnoreCase(ServerActions.GET_FRIEND_LIST)) {
                String friends = data.getFriendList(paramUsername);
                json.addProperty(JsonKeys.FRIEND_LIST, friends);
                json.addProperty(JsonKeys.STATUS, JsonValues.SUCCESS);
            } else if(paramAction.equalsIgnoreCase(ServerActions.ADD_FRIEND)) {
                String paramFriend = getUrlParam(req, UrlParameters.FRIEND);
                data.addFriend(paramUsername, paramFriend);
                json.addProperty(JsonKeys.STATUS, JsonValues.SUCCESS);
            } else if(paramAction.equalsIgnoreCase(ServerActions.GET_POSITION)) {
                String paramFriend = getUrlParam(req, UrlParameters.FRIEND);
                LastKnownPosition pos = data.getLastKnownPosition(paramFriend);
                json.addProperty(JsonKeys.LATITUDE, pos.latitude);
                json.addProperty(JsonKeys.LONGITUDE, pos.longitude);
                json.addProperty(JsonKeys.ACCURACY, pos.accuracy);
                json.addProperty(JsonKeys.EPOCH_TIME, pos.epochTime);
                json.addProperty(JsonKeys.STATUS, JsonValues.SUCCESS);
            } else if(paramAction.equalsIgnoreCase(ServerActions.SET_POSITION)) {
                // first get and check the URL parameters
                String paramLat = getUrlParam(req, UrlParameters.LATITUDE);
                String paramLon = getUrlParam(req, UrlParameters.LONGITUDE);
                String paramAccuracy = getUrlParam(req, UrlParameters.ACCURACY);
                String paramTime = getUrlParam(req, UrlParameters.EPOCH_TIME);

                // create a position and set it
                LastKnownPosition pos = new LastKnownPosition();
                pos.latitude = Double.parseDouble(paramLat);
                pos.longitude = Double.parseDouble(paramLon);
                pos.accuracy = Double.parseDouble(paramAccuracy);
                pos.epochTime = Long.parseLong(paramTime);
                data.setLastKnownPosition(paramUsername, pos);
                json.addProperty(JsonKeys.STATUS, JsonValues.SUCCESS);
            }
        } catch(NumberFormatException e) {
            json.addProperty(JsonKeys.STATUS, JsonValues.ERROR);
            json.addProperty(JsonKeys.MESSAGE, "could not format number: " + e.getLocalizedMessage());
        } catch(IllegalArgumentException e) {
            json.addProperty(JsonKeys.STATUS, JsonValues.ERROR);
            json.addProperty(JsonKeys.MESSAGE, "bad argument: " + e.getLocalizedMessage());
        } catch(Exception e) {
            json.addProperty(JsonKeys.STATUS, JsonValues.ERROR);
            json.addProperty(JsonKeys.MESSAGE, "unknown error: " + e.getLocalizedMessage());
            e.printStackTrace(System.err);
        }

        resp.getWriter().println(json.toString());
    }

    private String getUrlParam(HttpServletRequest req, String name)
            throws IllegalArgumentException {
        String val = req.getParameter(name);
        if(val == null) {
            throw new IllegalArgumentException(String.format(JsonValues.MISSING_PARAM, name));
        }
        return val;
    }
}
