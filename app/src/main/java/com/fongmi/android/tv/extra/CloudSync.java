package com.fongmi.android.tv.extra;

import android.text.TextUtils;
import android.util.Log;
import androidx.annotation.NonNull;
import com.fongmi.android.tv.App;
import com.fongmi.android.tv.api.config.VodConfig;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import okhttp3.*;

public abstract class CloudSync<T> {

    protected final String TAG = getClass().getSimpleName();
    protected final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    protected final Map<String, Boolean> supportCache = new ConcurrentHashMap<>();
    protected final OkHttpClient client = new OkHttpClient.Builder().connectTimeout(5, TimeUnit.SECONDS).build();

    protected abstract String getPath();

    protected abstract void onPulled(String json) throws IOException;

    protected String getApiUrl(String action) {
        String configUrl = VodConfig.getUrl();
        if (TextUtils.isEmpty(configUrl) || !Boolean.TRUE.equals(supportCache.get(configUrl))) return null;

        String baseUrl = configUrl;
        String query = "";
        if (configUrl.contains("?")) {
            int index = configUrl.indexOf("?");
            baseUrl = configUrl.substring(0, index);
            query = configUrl.substring(index);
        }
        return baseUrl + action + query;
    }

    public void checkSupport(String configUrl) {
        String baseUrl = configUrl.contains("?") ? configUrl.substring(0, configUrl.indexOf("?")) : configUrl;
        String query = configUrl.contains("?") ? configUrl.substring(configUrl.indexOf("?")) : "";
        String url = baseUrl + "/check" + query;

        Log.d(TAG, "Checking support: " + url);
        client.newCall(new Request.Builder().url(url).get().build()).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Check Support Failed: " + e.getMessage());
                supportCache.put(configUrl, false);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {


                boolean supported = response.isSuccessful();
                boolean checkResult = false;
                try{
                    String body = response.body().string();
                    JsonObject jsonObject = JsonParser.parseString(body).getAsJsonObject();
                    if (jsonObject.has("OK") && !jsonObject.get("OK").isJsonNull()) {
                        checkResult= jsonObject.get("OK").getAsBoolean();
                    }
                }catch (Exception e){
                    Log.d(TAG, "Check Error: " + e.getMessage());
                }
                supportCache.put(configUrl, (supported && checkResult));
                Log.d(TAG, "Support Result: " + (supported && checkResult));
                if(supported && checkResult) pull();
                response.close();
            }
        });
    }

    public void push(T item, String param) {
        String url = getApiUrl(getPath());
        if (url == null || item == null) return;
        JsonElement element = App.gson().toJsonTree(item);
        if (!element.isJsonObject()) return;
        JsonObject jsonObject = element.getAsJsonObject();
        if(param != null){
            jsonObject.addProperty("ext_param", param);
        }
        Log.d(TAG, "Pushing data to: " + url + "\nPayload: " + jsonObject.toString());
        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(jsonObject.toString(), JSON))
                .build();
        enqueue(request, "Push");
    }

    public void push(T item) {
        this.push(item, null);
    }

    public void pull() {
        String url = getApiUrl(getPath());
        if (url == null) return;
        String finalUrl = url + (url.contains("?") ? "&" : "?") + "cid=" + VodConfig.getCid();
        Log.d(TAG, "Pulling data from: " + finalUrl);
        client.newCall(new Request.Builder().url(finalUrl).get().build()).enqueue(new Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String body = response.body().string();
                    Log.d(TAG, "Pull Success. Data: " + body);
                    onPulled(body);
                } else {
                    Log.w(TAG, "Pull Failed. Code: " + response.code());
                }
                response.close();
            }
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Pull Network Error: " + e.getMessage());
            }
        });
    }

    public void delete(String key) {
        String url = getApiUrl(getPath());
        if (url == null) return;
        Log.d(TAG, "Deleting key: " + key + " at " + url);
        String json = String.format("{\"key\":\"%s\"}", key);
        enqueue(new Request.Builder().url(url).delete(RequestBody.create(json, JSON)).build(), "Delete");
    }

    protected void enqueue(Request request, String action) {
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, action + " Network Error: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                Log.d(TAG, action + " Response Code: " + response.code());
                response.close();
            }
        });
    }
}