package com.fongmi.android.tv.extra;

import android.util.Log;

import com.fongmi.android.tv.App;
import com.fongmi.android.tv.Setting;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class KeywordCloudSync extends CloudSync<Object> {

    private static final KeywordCloudSync instance = new KeywordCloudSync();

    public static KeywordCloudSync get() {
        return instance;
    }

    @Override
    protected String getPath() {
        return "/keyword";
    }

    @Override
    protected void onPulled(String json) {
        List<String> remoteItems = App.gson().fromJson(json, new TypeToken<List<String>>() {}.getType());
        if (remoteItems == null || remoteItems.isEmpty()) return;
        List<String> localItems = App.gson().fromJson(Setting.getKeyword(), new TypeToken<List<String>>() {}.getType());
        if (localItems == null) localItems = new ArrayList<>();
        for (String item : remoteItems) {
            if (!localItems.contains(item)) {
                localItems.add(item);
            }
        }
        if (localItems.size() > 10) {
            localItems = localItems.subList(0, 10);
        }
        Setting.putKeyword(App.gson().toJson(localItems));
    }

    public void push(String text) {
        this.push(Map.of("keyword", text));
    }

}