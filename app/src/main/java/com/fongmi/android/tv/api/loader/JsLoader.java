package com.fongmi.android.tv.api.loader;

import com.fongmi.android.tv.App;
import com.fongmi.android.tv.api.config.LiveConfig;
import com.fongmi.android.tv.api.config.VodConfig;
import com.github.catvod.crawler.Spider;
import com.github.catvod.crawler.SpiderNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import dalvik.system.DexClassLoader;

public class JsLoader {

    private final ConcurrentHashMap<String, Spider> spiders;
    private final JarLoader jarLoader;
    private String recent;

    public JsLoader() {
        jarLoader = new JarLoader();
        spiders = new ConcurrentHashMap<>();
    }

    public void clear() {
        for (Spider spider : spiders.values()) App.execute(spider::destroy);
        jarLoader.clear();
        spiders.clear();
    }

    public void setRecent(String recent) {
        this.recent = recent;
    }

    private DexClassLoader dex(String key, String jar) {
        try {
            return jar.isEmpty() ? null : jarLoader.getLoader(key, jar);
        } catch (Throwable e) {
            return null;
        }
    }

    public Spider getSpider(String key, String api, String ext, String jar) {
        try {
            if (spiders.containsKey(key)) return spiders.get(key);
            Spider spider = new com.fongmi.quickjs.crawler.Spider(key, api, dex(key, jar));
            spider.init(App.get(), ext);
            spiders.put(key, spider);
            return spider;
        } catch (Throwable e) {
            e.printStackTrace();
            return new SpiderNull();
        }
    }

    private Spider find(Map<String, String> params) {
        if (!params.containsKey("siteKey")) return spiders.get(recent);
        boolean live = params.containsKey("live") && "true".equals(params.get("live"));
        boolean vod = !params.containsKey("live") || "false".equals(params.get("live"));
        if (vod) return VodConfig.get().getSpider(VodConfig.get().getSite(params.get("siteKey")));
        if (live) return LiveConfig.get().getSpider(LiveConfig.get().getLive(params.get("siteKey")));
        return new SpiderNull();
    }

    public Object[] proxyInvoke(Map<String, String> params) {
        try {
            return find(params).proxyLocal(params);
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }
}
