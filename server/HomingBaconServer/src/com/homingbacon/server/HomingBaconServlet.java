package com.homingbacon.server;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

@SuppressWarnings("serial")
public class HomingBaconServlet extends HttpServlet {
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        UserService userServ = UserServiceFactory.getUserService();
        User user = userServ.getCurrentUser();
        resp.setContentType("text/plain");

        PrintWriter writer = new PrintWriter(resp.getWriter());
        String action = req.getParameter("action");
        String username = req.getParameter("username");
        writer.println("action=" + action + ", username=" + username);

        if(action == null || username == null) {
            return;
        }

        try {
            DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
            if(action.equals("set")) {
                Key key = KeyFactory.createKey("UserLocation", username);
                Entity userEntity = new Entity(key);
                writer.println("created key=" + key.toString());
                userEntity.setProperty("latitude", 37.422005);
                userEntity.setProperty("longitude", -122.084095);
                userEntity.setProperty("accuracy", 10);
                userEntity.setProperty("time", 2147483647);
                datastore.put(userEntity);
            } else if(action.equals("get")) {
                Key key = KeyFactory.createKey("UserLocation", username);
                Entity userEntity = datastore.get(key);
                writer.println("latitude=" + userEntity.getProperty("latitude"));
                writer.println("longitude=" + userEntity.getProperty("longitude"));
                writer.println("accuracy=" + userEntity.getProperty("accuracy"));
                writer.println("location_time=" + userEntity.getProperty("location_time"));
            }
        } catch (EntityNotFoundException e) {
            writer.println("Ecountered exception: " + e.getLocalizedMessage());
        }

        if(user == null) {
            resp.sendRedirect(userServ.createLoginURL(req.getRequestURI()));
        } else {
            writer.println("user nickname=" + user.getNickname());
            writer.println("user id=" + user.getUserId());
            writer.println("user email=" + user.getEmail());
        }
    }
}
