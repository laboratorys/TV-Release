package com.fongmi.android.tv.player;

import com.fongmi.android.tv.bean.Channel;
import com.fongmi.android.tv.bean.Result;
import com.fongmi.android.tv.utils.UrlUtil;

import java.util.Map;

import tv.danmaku.ijk.media.player.MediaSource;

public class IjkUtil {

    public static MediaSource getSource(Result result) {
        return getSource(result.getHeaders(), result.getRealUrl());
    }

    public static MediaSource getSource(Channel channel) {
        return getSource(channel.getHeaders(), channel.getUrl());
    }

    public static MediaSource getSource(Map<String, String> headers, String url) {
        return new MediaSource(Players.checkUa(headers), UrlUtil.uri(url));
    }
}
