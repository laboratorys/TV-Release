package com.fongmi.android.tv.api.config;

import android.net.Uri;
import android.text.TextUtils;

import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.ExoPlayer;

import com.fongmi.android.tv.App;
import com.fongmi.android.tv.R;
import com.fongmi.android.tv.Setting;
import com.fongmi.android.tv.bean.Config;
import com.fongmi.android.tv.event.RefreshEvent;
import com.fongmi.android.tv.impl.Callback;
import com.fongmi.android.tv.utils.FileUtil;
import com.fongmi.android.tv.utils.Notify;
import com.fongmi.android.tv.utils.UrlUtil;
import com.github.catvod.net.OkHttp;
import com.github.catvod.utils.Path;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WallConfig {

    private Config config;
    private ExoPlayer player;
    private ExecutorService executor;

    private boolean sync;

    private static class Loader {
        static volatile WallConfig INSTANCE = new WallConfig();
    }

    public static WallConfig get() {
        return Loader.INSTANCE;
    }

    public static String getUrl() {
        return get().getConfig().getUrl();
    }

    public static String getDesc() {
        return get().getConfig().getDesc();
    }

    public static void load(Config config, Callback callback) {
        get().clear().config(config).load(callback);
    }

    public WallConfig init() {
        createPlayer();
        return config(Config.wall());
    }

    public WallConfig config(Config config) {
        this.config = config;
        if (config.getUrl() == null) return this;
        this.sync = config.getUrl().equals(VodConfig.get().getWall());
        return this;
    }

    public WallConfig clear() {
        getPlayer().clearMediaItems();
        this.config = null;
        return this;
    }

    public Config getConfig() {
        return config == null ? Config.wall() : config;
    }

    public ExoPlayer getPlayer() {
        return player == null ? createPlayer() : player;
    }

    public void load(Callback callback) {
        if (executor != null) executor.shutdownNow();
        executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> loadConfig(callback));
    }

    private void loadConfig(Callback callback) {
        try {
            byte[] data = OkHttp.bytes(UrlUtil.convert(getUrl()));
            if (data.length == 0) throw new RuntimeException();
            Path.write(FileUtil.getWall(0), data);
            App.post(callback::success);
            config.update();
            refresh(0);
        } catch (Throwable e) {
            if (TextUtils.isEmpty(config.getUrl())) App.post(() -> callback.error(""));
            else App.post(() -> callback.error(Notify.getError(R.string.error_config_get, e)));
            e.printStackTrace();
        }
    }

    public boolean needSync(String url) {
        return sync || TextUtils.isEmpty(config.getUrl()) || url.equals(config.getUrl());
    }

    private ExoPlayer createPlayer() {
        player = new ExoPlayer.Builder(App.get()).build();
        player.setRepeatMode(ExoPlayer.REPEAT_MODE_ALL);
        player.setPlayWhenReady(true);
        player.setVolume(0);
        return player;
    }

    public static void load(File file) {
        if (get().getPlayer().getMediaItemCount() > 0) return;
        get().getPlayer().setMediaItem(MediaItem.fromUri(Uri.fromFile(file)));
        get().getPlayer().prepare();
    }

    public static void refresh(int index) {
        Setting.putWall(index);
        RefreshEvent.wall();
    }
}
