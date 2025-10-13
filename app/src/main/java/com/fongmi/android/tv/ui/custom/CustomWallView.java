package com.fongmi.android.tv.ui.custom;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import androidx.activity.ComponentActivity;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.ExoPlayer;

import com.fongmi.android.tv.R;
import com.fongmi.android.tv.Setting;
import com.fongmi.android.tv.databinding.ViewWallBinding;
import com.fongmi.android.tv.event.RefreshEvent;
import com.fongmi.android.tv.utils.FileUtil;
import com.fongmi.android.tv.utils.ResUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.IOException;

import pl.droidsonroids.gif.GifDrawable;

public class CustomWallView extends FrameLayout implements DefaultLifecycleObserver {

    private ViewWallBinding binding;
    private GifDrawable drawable;
    private ExoPlayer player;
    private Drawable cache;

    public CustomWallView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!isInEditMode()) init();
    }

    private void init() {
        binding = ViewWallBinding.inflate(LayoutInflater.from(getContext()), this, true);
        ((ComponentActivity) getContext()).getLifecycle().addObserver(this);
        createPlayer();
        refresh();
    }

    private void createPlayer() {
        player = new ExoPlayer.Builder(getContext()).build();
        player.setRepeatMode(ExoPlayer.REPEAT_MODE_ALL);
        player.setPlayWhenReady(true);
        player.setVolume(0);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRefreshEvent(RefreshEvent event) {
        if (event.getType() == RefreshEvent.Type.WALL) refresh();
    }

    private void refresh() {
        cache = Drawable.createFromPath(FileUtil.getWallCache().getAbsolutePath());
        load(FileUtil.getWall(Setting.getWall()));
    }

    private void load(File file) {
        if (!file.getName().endsWith("0")) loadRes(ResUtil.getDrawable(file.getName()));
        else if (Setting.getWallType() == 2) loadVideo(file);
        else if (Setting.getWallType() == 1) loadGif(file);
        else loadImage();
    }

    private void loadRes(int resId) {
        player.clearMediaItems();
        binding.video.setPlayer(null);
        binding.video.setVisibility(GONE);
        binding.image.setImageResource(resId);
    }

    private void loadVideo(File file) {
        binding.video.setPlayer(player);
        binding.video.setVisibility(VISIBLE);
        binding.image.setImageDrawable(cache);
        player.setMediaItem(MediaItem.fromUri(Uri.fromFile(file)));
        player.prepare();
    }

    private void loadGif(File file) {
        player.clearMediaItems();
        binding.video.setPlayer(null);
        binding.video.setVisibility(GONE);
        binding.image.setImageDrawable(cache);
        if (drawable != null) drawable.recycle();
        binding.image.setImageDrawable(drawable = gif(file));
    }

    private void loadImage() {
        player.clearMediaItems();
        binding.video.setPlayer(null);
        binding.video.setVisibility(GONE);
        if (cache != null) binding.image.setImageDrawable(cache);
        else binding.image.setImageResource(R.drawable.wallpaper_1);
    }

    private GifDrawable gif(File file) {
        try {
            return new GifDrawable(file);
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public void onCreate(@NonNull LifecycleOwner owner) {
        EventBus.getDefault().register(this);
    }

    @Override
    public void onResume(@NonNull LifecycleOwner owner) {
        if (drawable != null) drawable.start();
        if (binding.video.getVisibility() != VISIBLE || player == null || player.getMediaItemCount() == 0) return;
        binding.video.setPlayer(player);
        player.play();
    }

    @Override
    public void onPause(@NonNull LifecycleOwner owner) {
        if (drawable != null) drawable.pause();
        if (binding.video.getVisibility() != VISIBLE || player == null || player.getMediaItemCount() == 0) return;
        binding.video.setPlayer(null);
        player.pause();
    }

    @Override
    public void onDestroy(@NonNull LifecycleOwner owner) {
        EventBus.getDefault().unregister(this);
        if (drawable != null) drawable.recycle();
        binding.video.setPlayer(null);
        player.release();
        drawable = null;
        binding = null;
        player = null;
        cache = null;
    }
}
