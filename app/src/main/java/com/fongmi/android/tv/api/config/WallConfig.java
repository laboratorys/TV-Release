package com.fongmi.android.tv.api.config;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.text.TextUtils;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.fongmi.android.tv.App;
import com.fongmi.android.tv.R;
import com.fongmi.android.tv.Setting;
import com.fongmi.android.tv.bean.Config;
import com.fongmi.android.tv.event.RefreshEvent;
import com.fongmi.android.tv.impl.Callback;
import com.fongmi.android.tv.utils.Download;
import com.fongmi.android.tv.utils.FileUtil;
import com.fongmi.android.tv.utils.Notify;
import com.fongmi.android.tv.utils.ResUtil;
import com.fongmi.android.tv.utils.UrlUtil;
import com.github.catvod.net.OkHttp;
import com.github.catvod.utils.Path;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class WallConfig extends BaseConfig {

    private static final String TAG = WallConfig.class.getSimpleName();

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
        get().config(config).load(callback);
    }

    public WallConfig init() {
        return config(Config.wall());
    }

    public WallConfig config(Config config) {
        this.config = config;
        if (config.isEmpty()) return this;
        this.sync = config.getUrl().equals(VodConfig.get().getWall());
        return this;
    }

    public void load() {
        if (sync) return;
        load(new Callback());
    }

    @Override
    protected String getTag() {
        return TAG;
    }

    @Override
    protected Config defaultConfig() {
        return Config.wall();
    }

    @Override
    protected void doLoad(int id, Config config, Callback callback) throws Throwable {
        download(id, config.getUrl(), callback);
    }

    @Override
    protected void loadConfig(int id, Config config, Callback callback) {
        try {
            OkHttp.cancel(TAG);
            doLoad(id, config, callback);
            if (getTaskId() == id && config.equals(this.config)) config.update();
        } catch (Throwable e) {
            e.printStackTrace();
            if (isCanceled(e)) return;
            if (getTaskId() != id) return;
            Setting.putWall(1);
            RefreshEvent.wall();
            if (TextUtils.isEmpty(config.getUrl())) App.post(() -> callback.error(""));
            else App.post(() -> callback.error(Notify.getError(R.string.error_config_get, e)));
        }
    }

    private void download(int id, String url, Callback callback) throws Throwable {
        File file = FileUtil.getWall(0);
        if (url.startsWith("file")) Path.copy(Path.local(url), file);
        else Download.create(UrlUtil.convert(url), file).tag(TAG).get();
        if (!Path.exists(file)) throw new FileNotFoundException();
        if (getTaskId() != id) return;
        process(file);
        RefreshEvent.wall();
        App.post(callback::success);
    }

    private static void process(File file) throws Throwable {
        setWallType(file);
        setSnapshot(file);
    }

    private static void setWallType(File file) {
        Setting.putWallType(0);
        if (isGif(file)) Setting.putWallType(1);
        else if (isVideo(file)) Setting.putWallType(2);
    }

    private static void setSnapshot(File file) throws Throwable {
        Bitmap bitmap = Glide.with(App.get()).asBitmap().frame(0).load(file).override(ResUtil.getScreenWidth(), ResUtil.getScreenHeight()).skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE).submit().get();
        try (FileOutputStream fos = new FileOutputStream(FileUtil.getWallCache())) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        }
    }

    private static boolean isVideo(File file) {
        try (MediaMetadataRetriever retriever = new MediaMetadataRetriever()) {
            retriever.setDataSource(file.getAbsolutePath());
            return "yes".equalsIgnoreCase(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_VIDEO));
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean isGif(File file) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(file.getAbsolutePath(), options);
            return "image/gif".equals(options.outMimeType);
        } catch (Exception e) {
            return false;
        }
    }
}
