package com.fongmi.android.tv.player.danmaku;

import com.fongmi.android.tv.bean.Danmaku;
import com.fongmi.android.tv.utils.Download;
import com.github.catvod.utils.Path;

import java.io.File;
import java.io.InputStream;

import master.flame.danmaku.danmaku.loader.ILoader;
import master.flame.danmaku.danmaku.parser.android.AndroidFileSource;

public class Loader implements ILoader {

    private AndroidFileSource source;

    public Loader load(Danmaku item) {
        try { load(item.getRealUrl()); } catch (Throwable ignored) {}
        return this;
    }

    @Override
    public void load(String url) {
        File file = Path.danmaku(url);
        if (!Path.exists(file)) Download.create(url, file).start();
        load(file);
    }

    public void load(File file) {
        source = new AndroidFileSource(file);
    }

    @Override
    public void load(InputStream stream) {
        source = new AndroidFileSource(stream);
    }

    @Override
    public AndroidFileSource getDataSource() {
        return source;
    }
}