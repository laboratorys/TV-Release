package com.fongmi.quickjs.utils;

import android.util.Base64;

import com.github.catvod.net.OkHttp;
import com.github.catvod.utils.Asset;
import com.google.common.net.HttpHeaders;

import java.util.concurrent.ConcurrentHashMap;

import okhttp3.Headers;

public class Module {

    private final ConcurrentHashMap<String, String> cache;

    private static class Loader {
        static volatile Module INSTANCE = new Module();
    }

    public static Module get() {
        return Loader.INSTANCE;
    }

    public Module() {
        this.cache = new ConcurrentHashMap<>();
    }

    public String fetch(String name) {
        if (cache.contains(name)) return cache.get(name);
        if (name.startsWith("http")) cache.put(name, request(name));
        if (name.startsWith("assets")) cache.put(name, Asset.read(name));
        if (name.startsWith("lib/")) cache.put(name, Asset.read("js/" + name));
        return cache.get(name);
    }

    private String request(String url) {
        try {
            return OkHttp.newCall(url, Headers.of(HttpHeaders.USER_AGENT, "Mozilla/5.0")).execute().body().string();
        } catch (Exception e) {
            return "";
        }
    }

    public byte[] bb(String content) {
        byte[] bytes = Base64.decode(content.substring(4), Base64.DEFAULT);
        byte[] newBytes = new byte[bytes.length - 4];
        newBytes[0] = 1;
        System.arraycopy(bytes, 5, newBytes, 1, bytes.length - 5);
        return newBytes;
    }
}
