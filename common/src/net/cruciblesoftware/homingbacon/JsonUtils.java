package net.cruciblesoftware.homingbacon;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class JsonUtils {
    public static JsonObject getJsonObject(String json) {
        return new JsonParser().parse(json).getAsJsonObject();
    }
}
