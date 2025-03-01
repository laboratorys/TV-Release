package com.fongmi.android.tv.bean;

import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

import java.io.File;
import java.util.List;

public class Danmaku {

    @SerializedName("name")
    private String name;
    @SerializedName("url")
    private String url;

    private boolean selected;

    public static List<Danmaku> from(String path) {
        if (path.startsWith("http")) {
            return http(path);
        } else {
            return file(path);
        }
    }

    public static List<Danmaku> http(String path) {
        Danmaku danmaku = new Danmaku();
        danmaku.setName(path);
        danmaku.setUrl(path);
        return List.of(danmaku);
    }

    public static List<Danmaku> file(String path) {
        Danmaku danmaku = new Danmaku();
        danmaku.setName(new File(path).getName());
        danmaku.setUrl("file:/" + path);
        return List.of(danmaku);
    }

    public static Danmaku empty() {
        return new Danmaku();
    }

    public String getName() {
        return TextUtils.isEmpty(name) ? getUrl() : name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return TextUtils.isEmpty(url) ? "" : url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isEmpty() {
        return getUrl().isEmpty();
    }
}