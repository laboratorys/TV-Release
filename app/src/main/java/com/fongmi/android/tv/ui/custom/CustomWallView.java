package com.fongmi.android.tv.ui.custom;

import static android.widget.ImageView.ScaleType.CENTER_CROP;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.media3.ui.AspectRatioFrameLayout;
import androidx.media3.ui.PlayerView;

import com.fongmi.android.tv.Setting;
import com.fongmi.android.tv.api.config.WallConfig;
import com.fongmi.android.tv.event.RefreshEvent;
import com.fongmi.android.tv.utils.FileUtil;
import com.fongmi.android.tv.utils.ImgUtil;
import com.fongmi.android.tv.utils.ResUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;

public class CustomWallView extends FrameLayout {

    private ImageView image;
    private PlayerView video;

    public CustomWallView(@NonNull Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        image = new ImageView(context);
        image.setScaleType(CENTER_CROP);
        video = new PlayerView(context);
        video.setUseController(false);
        video.setKeepContentOnPlayerReset(true);
        video.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM);
        addView(image, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        addView(video, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        refresh();
    }

    private boolean isVideo(File file) {
        try (MediaMetadataRetriever retriever = new MediaMetadataRetriever()) {
            retriever.setDataSource(file.getAbsolutePath());
            return "yes".equalsIgnoreCase(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_VIDEO));
        } catch (Exception e) {
            return false;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRefreshEvent(RefreshEvent event) {
        if (event.getType() == RefreshEvent.Type.WALL) refresh();
    }

    private void refresh() {
        load(FileUtil.getWall(Setting.getWall()));
    }

    private void load(File file) {
        if (!file.getName().endsWith("0")) loadRes(ResUtil.getDrawable(file.getName()));
        else if (isVideo(file)) loadVideo(file);
        else loadImage(file);
    }

    private void loadRes(int resId) {
        video.setPlayer(null);
        video.setVisibility(GONE);
        image.setVisibility(VISIBLE);
        image.setImageResource(resId);
        WallConfig.get().getPlayer().clearMediaItems();
    }

    private void loadImage(File file) {
        video.setPlayer(null);
        video.setVisibility(GONE);
        ImgUtil.load(file, image);
        image.setVisibility(VISIBLE);
        WallConfig.get().getPlayer().clearMediaItems();
    }

    private void loadVideo(File file) {
        WallConfig.load(file);
        image.setVisibility(GONE);
        video.setVisibility(VISIBLE);
        image.setImageDrawable(null);
        video.setPlayer(WallConfig.get().getPlayer());
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        EventBus.getDefault().unregister(this);
    }
}
