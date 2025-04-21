package com.github.catvod.net.interceptor;

import androidx.annotation.NonNull;

import com.github.catvod.net.OkCookieJar;
import com.github.catvod.utils.Json;
import com.google.common.net.HttpHeaders;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class RequestInterceptor implements Interceptor {

    private ConcurrentHashMap<String, String> auth;
    private ConcurrentHashMap<String, JsonObject> header;

    private ConcurrentHashMap<String, String> auth() {
        return auth == null ? auth = new ConcurrentHashMap<>() : auth;
    }

    private ConcurrentHashMap<String, JsonObject> header() {
        return header == null ? header = new ConcurrentHashMap<>() : header;
    }

    public synchronized void setHeaders(List<JsonElement> items) {
        for (JsonElement item : items) {
            JsonObject object = Json.safeObject(item);
            header().put(object.get("host").getAsString(), object.get("header").getAsJsonObject());
        }
    }

    public void clear() {
        auth().clear();
        header().clear();
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request request = chain.request();
        HttpUrl url = request.url();
        Request.Builder builder = request.newBuilder();
        checkHeader(url, builder);
        checkAuthUser(url, builder);
        OkCookieJar.sync(url.toString(), request.header(HttpHeaders.COOKIE));
        return chain.proceed(builder.build());
    }

    private void checkHeader(HttpUrl url, Request.Builder builder) {
        if (!header().containsKey(url.host())) return;
        for (Map.Entry<String, JsonElement> entry : header().get(url.host()).entrySet()) {
            builder.header(entry.getKey(), entry.getValue().getAsString());
        }
    }

    private void checkAuthUser(HttpUrl url, Request.Builder builder) {
        String auth = url.queryParameter("auth");
        if (auth != null) auth().put(url.host(), auth);
        if (auth().containsKey(url.host()) && auth == null) builder.url(url.newBuilder().addQueryParameter("auth", auth().get(url.host())).build());
    }
}
