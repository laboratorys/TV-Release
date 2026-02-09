package com.fongmi.android.tv.extra;

import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.fongmi.android.tv.api.config.VodConfig;
import com.fongmi.android.tv.bean.History;
import com.fongmi.android.tv.event.RefreshEvent;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PlayRecordCloudSync {

    private static final String TAG = "PlayRecordCloudSync";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    // 缓存不同源的支持状态：Key 为完整的 configUrl (含参数)
    private static final Map<String, Boolean> supportCache = new ConcurrentHashMap<>();

    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build();

    private static long lastPullTime = 0;
    private static final long PULL_INTERVAL = TimeUnit.MINUTES.toMillis(2);

    /**
     * 获取API地址：直接在原始 URL 后拼接
     */
    private static String getApiUrl(String action) {
        String configUrl = VodConfig.getUrl();
        if (TextUtils.isEmpty(configUrl)) return null;

        // 如果该 URL 还没检测过
        if (!supportCache.containsKey(configUrl)) {
            supportCache.put(configUrl, false);
            checkSupport(configUrl);
            return null;
        }

        // 如果检测过且不支持
        if (Boolean.FALSE.equals(supportCache.get(configUrl))) {
            return null;
        }

        // 拼接逻辑：处理参数分离
        String baseUrl = configUrl;
        String query = "";
        if (configUrl.contains("?")) {
            int index = configUrl.indexOf("?");
            baseUrl = configUrl.substring(0, index);
            query = configUrl.substring(index);
        }

        // 直接在 baseUrl 后拼接 /action
        String finalAction = action.startsWith("/") ? action : "/" + action;
        return baseUrl + "/" + finalAction + query;
    }

    /**
     * 检测当前配置地址是否支持同步
     */
    public static void checkSupport(String configUrl) {
        // 构建检测地址：直接在原地址后拼接 /history/check
        String baseUrl = configUrl;
        String query = "";
        if (configUrl.contains("?")) {
            int index = configUrl.indexOf("?");
            baseUrl = configUrl.substring(0, index);
            query = configUrl.substring(index);
        }

        String url = baseUrl + "/check" + query;

        Request request = new Request.Builder().url(url).head().build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Sync Not Supported: " + configUrl);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                boolean supported = response.isSuccessful();
                supportCache.put(configUrl, supported);
                Log.d(TAG, "Sync Support Result: " + supported + " for " + configUrl);
                if (supported) pull();
                response.close();
            }
        });
    }

    private static String buildUrl(String url, String params) {
        if (url == null) return null;
        return url + (url.contains("?") ? "&" : "?") + params;
    }

    public static void push(History history) {
        String url = getApiUrl("/history");
        if (url == null || history == null) return;
        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(history.toString(), JSON))
                .build();
        enqueue(request, "Push");
    }

    public static void pull() {
        if (System.currentTimeMillis() - lastPullTime < PULL_INTERVAL) return;
        String url = getApiUrl("/history");
        if (url == null) return;
        String finalUrl = buildUrl(url, "cid=" + VodConfig.getCid());
        lastPullTime = System.currentTimeMillis();
        Request request = new Request.Builder().url(finalUrl).get().build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Pull Failed: " + e.getMessage());
            }
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    List<History> items = History.arrayFrom(response.body().string());
                    History.delete(VodConfig.getCid());
                    History.sync(items);
                    RefreshEvent.history();
                }
                response.close();
            }
        });
    }

    public static void delete(String key) {
        String url = getApiUrl("/history");
        if (url == null) return;
        String finalUrl = buildUrl(url, "key=" + key);
        String param = String.format("{\"key\":\"%s\"}", key);
        enqueue(new Request.Builder().url(finalUrl).delete(RequestBody.create(param, JSON)).build(), "Delete");
    }

    public static void clear() {
        delete("");
    }

    private static void enqueue(Request request, String action) {
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, action + " Network Error");
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                response.close();
            }
        });
    }
}