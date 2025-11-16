package com.fongmi.android.tv.player.danmaku;

import com.fongmi.android.tv.Constant;
import com.fongmi.android.tv.bean.Danmaku;
import com.github.catvod.net.OkHttp;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import master.flame.danmaku.danmaku.loader.ILoader;
import master.flame.danmaku.danmaku.loader.IllegalDataException;
import master.flame.danmaku.danmaku.parser.android.AndroidFileSource;
import okhttp3.OkHttpClient;
import okhttp3.Response;

public class Loader implements ILoader {

    private static final int MAX_CACHE_SIZE = 10;
    private final Map<String, byte[]> cache;
    private final OkHttpClient client;
    private AndroidFileSource source;

    public Loader() {
        client = OkHttp.client(Constant.TIMEOUT_DANMAKU);
        cache = Collections.synchronizedMap(new LinkedHashMap<>(MAX_CACHE_SIZE, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Entry<String, byte[]> eldest) {
                return size() > MAX_CACHE_SIZE;
            }
        });
    }

    public Loader load(Danmaku item) {
        try {
            OkHttp.cancel("danmaku");
            load(item.getRealUrl());
            return this;
        } catch (IllegalDataException e) {
            return this;
        }
    }

    @Override
    public void load(String url) throws IllegalDataException {
        if (cache.containsKey(url)) {
            load(cache.get(url));
        } else try (Response res = OkHttp.newCall(client, url, "danmaku").execute()) {
            byte[] data = res.body().bytes();
            if (data.length > 0) load(url, data);
        } catch (IOException ignored) {
        }
    }

    public void load(String key, byte[] bytes) throws IllegalDataException {
        cache.put(key, bytes);
        load(bytes);
    }

    public void load(byte[] bytes) throws IllegalDataException {
        load(new ByteArrayInputStream(bytes));
    }

    @Override
    public void load(InputStream stream) throws IllegalDataException {
        source = new AndroidFileSource(stream);
    }

    @Override
    public AndroidFileSource getDataSource() {
        return source;
    }

    public void clear() {
        cache.clear();
    }
}