package com.fongmi.android.tv.player.danmaku;

import master.flame.danmaku.danmaku.loader.ILoader;
import master.flame.danmaku.danmaku.loader.android.BiliDanmakuLoader;
import master.flame.danmaku.danmaku.loader.android.DanmakuLoaderFactory;

public class Loader extends DanmakuLoaderFactory {

    public static ILoader create() {
        return BiliDanmakuLoader.instance();
    }
}