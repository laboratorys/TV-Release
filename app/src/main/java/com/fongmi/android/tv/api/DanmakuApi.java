package com.fongmi.android.tv.api;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.fongmi.android.tv.App;
import com.fongmi.android.tv.api.config.VodConfig;
import com.fongmi.android.tv.bean.Danmaku;
import com.fongmi.android.tv.bean.Result;
import com.fongmi.android.tv.impl.Callback;
import com.fongmi.android.tv.setting.DanmakuSetting;
import com.github.catvod.net.OkHttp;
import com.github.catvod.utils.Trans;

import java.util.function.Consumer;

import okhttp3.Call;
import okhttp3.Response;

public class DanmakuApi {

    private static final String TAG = DanmakuApi.class.getSimpleName();

    public static boolean canSearch(Result result) {
        return result.getDanmaku().isEmpty() && DanmakuSetting.isLoad() && !TextUtils.isEmpty(VodConfig.get().getConfig().getDanmaku());
    }

    public static Call newCall(String name, String episode) {
        return OkHttp.newCall(VodConfig.get().getConfig().getDanmaku().replace("{name}", Trans.t2s(name)).replace("{episode}", Trans.t2s(episode)), TAG);
    }

    public static void search(String name, String episode, Consumer<Danmaku> found) {
        newCall(name, episode).enqueue(new Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                try {
                    Danmaku.arrayFrom(response.body().string()).stream().findFirst().ifPresent(item -> App.post(() -> found.accept(item)));
                } catch (Exception ignored) {
                }
            }
        });
    }

    public static void cancel() {
        OkHttp.cancel(TAG);
    }
}
