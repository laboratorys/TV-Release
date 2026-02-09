package com.fongmi.android.tv.extra;

public class Github {

    public static final String URL = "https://gh.noki.icu/github/laboratorys/TV-Release/releases/latest/download";

    private static String getUrl(String name) {
        return URL + "/" + name;
    }

    public static String getJson(String name) {
        return getUrl(name + ".json");
    }

    public static String getApk(String name) {
        return getUrl(name + ".apk");
    }
}
