package com.fongmi.android.tv;

import java.util.concurrent.TimeUnit;

public class Constant {

    //快進時間單位
    public static final long INTERVAL_SEEK = TimeUnit.SECONDS.toMillis(10);
    //控件隱藏時間
    public static final long INTERVAL_HIDE = TimeUnit.SECONDS.toMillis(5);
    //網路偵測間隔
    public static final long INTERVAL_TRAFFIC = TimeUnit.SECONDS.toMillis(1);
    //點播爬蟲時間
    public static final long TIMEOUT_VOD = TimeUnit.SECONDS.toMillis(30);
    //直播爬蟲時間
    public static final long TIMEOUT_LIVE = TimeUnit.SECONDS.toMillis(30);
    //節目爬蟲時間
    public static final long TIMEOUT_EPG = TimeUnit.SECONDS.toMillis(5);
    //節目爬蟲時間
    public static final long TIMEOUT_XML = TimeUnit.SECONDS.toMillis(15);
    //播放超時時間
    public static final long TIMEOUT_PLAY = TimeUnit.SECONDS.toMillis(15);
    //彈幕超時時間
    public static final long TIMEOUT_DANMAKU = TimeUnit.SECONDS.toMillis(30);
    //解析預設時間
    public static final long TIMEOUT_PARSE_DEF = TimeUnit.SECONDS.toMillis(15);
    //嗅探超時時間
    public static final long TIMEOUT_PARSE_WEB = TimeUnit.SECONDS.toMillis(15);
    //直播解析時間
    public static final long TIMEOUT_PARSE_LIVE = TimeUnit.SECONDS.toMillis(10);
    //同步超時時間
    public static final long TIMEOUT_SYNC = TimeUnit.SECONDS.toMillis(2);
    //主要線程數量
    public static final int THREAD_POOL = 10;

}
