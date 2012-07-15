package net.cruciblesoftware.homingbacon.server;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.JsonObject;

public class HomingBaconServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private static final String ADD_USER_ACTION = "adduser";
    private static final String ADD_FRIEND_ACTION = "addfriend";
    private static final String GET_FRIEND_LIST_ACTION = "getfriendlist";
    private static final String GET_POSITION_ACTION = "getposition";
    private static final String SET_POSITION_ACTION = "setposition";
    private static final String GET_USERS_ACTION = "getusers";

    private static final String JSON_KEY_STATUS = "status";
    private static final String JSON_KEY_MESSAGE = "message";
    private static final String JSON_KEY_LAT = "latitude";
    private static final String JSON_KEY_LON = "longitude";
    private static final String JSON_KEY_ACCURACY = "accuracy";
    private static final String JSON_KEY_TIME = "epoch_time";
    private static final String JSON_VALUE_SUCCESS = "success";
    private static final String JSON_VALUE_ERROR = "error";

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        doGet(req, resp);
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        // set up server's response
        resp.setContentType("text/plain");
        JsonObject json = new JsonObject();

        // basic GET params
        String paramAction = req.getParameter("action");
        String paramUsername = req.getParameter("username");

        try {
            // check basic params
            if(paramAction == null) {
                throw new IllegalArgumentException("missing 'action' parameter");
            }
            if(paramUsername == null) {
                throw new IllegalArgumentException("missing 'username' parameter");
            }

            // parse action
            DataProvider data = new DataProvider();
            if(paramAction.equalsIgnoreCase(ADD_USER_ACTION)) {
                data.addUser(paramUsername);
                json.addProperty(JSON_KEY_STATUS, JSON_VALUE_SUCCESS);
            } else if(paramAction.equalsIgnoreCase(GET_USERS_ACTION)) {
                String users = data.getUserList(paramUsername);
                json.addProperty("user_list", users);
                json.addProperty(JSON_KEY_STATUS, JSON_VALUE_SUCCESS);
            } else if(paramAction.equalsIgnoreCase(GET_FRIEND_LIST_ACTION)) {
                String friends = data.getFriendList(paramUsername);
                json.addProperty("friend_list", friends);
                json.addProperty(JSON_KEY_STATUS, JSON_VALUE_SUCCESS);
            } else if(paramAction.equalsIgnoreCase(ADD_FRIEND_ACTION)) {
                String paramFriend = req.getParameter("friend");
                if(paramFriend == null) {
                    throw new IllegalArgumentException("missing 'friend' parameter");
                }
                data.addFriend(paramUsername, paramFriend);
                json.addProperty(JSON_KEY_STATUS, JSON_VALUE_SUCCESS);
            } else if(paramAction.equalsIgnoreCase(GET_POSITION_ACTION)) {
                String paramFriend = req.getParameter("friend");
                if(paramFriend == null) {
                    throw new IllegalArgumentException("missing 'friend' parameter");
                }
                LastKnownPosition pos = data.getLastKnownPosition(paramFriend);
                json.addProperty(JSON_KEY_LAT, pos.latitude);
                json.addProperty(JSON_KEY_LON, pos.longitude);
                json.addProperty(JSON_KEY_ACCURACY, pos.accuracy);
                json.addProperty(JSON_KEY_TIME, pos.epochTime);
                json.addProperty(JSON_KEY_STATUS, JSON_VALUE_SUCCESS);
            } else if(paramAction.equalsIgnoreCase(SET_POSITION_ACTION)) {
                // first get and check the URL parameters
                String paramLat = req.getParameter("lat");
                if(paramLat == null) {
                    throw new IllegalArgumentException("missing 'lat' parameter");
                }
                String paramLon = req.getParameter("lon");
                if(paramLon == null) {
                    throw new IllegalArgumentException("missing 'lon' parameter");
                }
                String paramAccuracy = req.getParameter("accuracy");
                if(paramAccuracy == null) {
                    throw new IllegalArgumentException("missing 'accuracy' parameter");
                }
                String paramTime = req.getParameter("time");
                if(paramTime == null) {
                    throw new IllegalArgumentException("missing 'time' parameter");
                }

                // create a position and set it
                LastKnownPosition pos = new LastKnownPosition();
                pos.latitude = Double.parseDouble(paramLat);
                pos.longitude = Double.parseDouble(paramLon);
                pos.accuracy = Double.parseDouble(paramAccuracy);
                pos.epochTime = Long.parseLong(paramTime);
                data.setLastKnownPosition(paramUsername, pos);
                json.addProperty(JSON_KEY_STATUS, JSON_VALUE_SUCCESS);
            }
        } catch(NumberFormatException e) {
            json.addProperty(JSON_KEY_STATUS, JSON_VALUE_ERROR);
            json.addProperty(JSON_KEY_MESSAGE, "could not format number: " + e.getLocalizedMessage());
        } catch(IllegalArgumentException e) {
            json.addProperty(JSON_KEY_STATUS, JSON_VALUE_ERROR);
            json.addProperty(JSON_KEY_MESSAGE, "bad argument: " + e.getLocalizedMessage());
        } catch(Exception e) {
            json.addProperty(JSON_KEY_STATUS, JSON_VALUE_ERROR);
            json.addProperty(JSON_KEY_MESSAGE, "unknown error: " + e.getLocalizedMessage());
            e.printStackTrace(System.err);
        }

        resp.getWriter().println(json.toString());



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
    }

}
