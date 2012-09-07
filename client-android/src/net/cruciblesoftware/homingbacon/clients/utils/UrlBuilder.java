package net.cruciblesoftware.homingbacon.clients.utils;

import java.net.URLEncoder;

public class UrlBuilder {
    private static final String TAG = "HB: " + UrlBuilder.class.getSimpleName();
    private StringBuilder urlBuff;
    private boolean hasParam = false;

    public UrlBuilder() {
        urlBuff = new StringBuilder();
    }

    public UrlBuilder(String baseUrl) {
        urlBuff = new StringBuilder(baseUrl);
    }

    public UrlBuilder setHost(String host) {
        urlBuff = new StringBuilder(host);
        return this;
    }

    public UrlBuilder addParam(String key, String value) {
        if(!hasParam) {
            urlBuff.append("?");
            hasParam = true;
        } else {
            urlBuff.append("&");
        }
        urlBuff.append(URLEncoder.encode(key)).append("=").append(URLEncoder.encode(value));
        return this;
    }

    @Override
    public String toString() {
        return urlBuff.toString();
    }

}
