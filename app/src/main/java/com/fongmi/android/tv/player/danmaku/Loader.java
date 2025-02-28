package com.fongmi.android.tv.player.danmaku;

import com.fongmi.android.tv.utils.UrlUtil;
import com.github.catvod.net.OkHttp;

import java.io.InputStream;

import master.flame.danmaku.danmaku.loader.ILoader;
import master.flame.danmaku.danmaku.loader.IllegalDataException;
import master.flame.danmaku.danmaku.parser.android.AndroidFileSource;

public class Loader implements ILoader {

    private AndroidFileSource dataSource;

    public Loader(String url) {
        try {
            load(url);
        } catch (Exception ignored) {
        }
    }

    @Override
    public void load(String url) throws IllegalDataException {
        try {
            OkHttp.cancel("danmaku");
            dataSource = new AndroidFileSource(OkHttp.newCall(UrlUtil.convert(url), "danmaku").execute().body().byteStream());
        } catch (Exception e) {
            throw new IllegalDataException();
        }
    }

    @Override
    public void load(InputStream stream) throws IllegalDataException {
    }

    @Override
    public AndroidFileSource getDataSource() {
        return dataSource;
    }
}