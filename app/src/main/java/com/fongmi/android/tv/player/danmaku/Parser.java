package com.fongmi.android.tv.player.danmaku;

import android.graphics.Color;

import com.fongmi.android.tv.bean.Danmaku;
import com.fongmi.android.tv.utils.UrlUtil;
import com.github.catvod.net.OkHttp;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import master.flame.danmaku.danmaku.model.BaseDanmaku;
import master.flame.danmaku.danmaku.model.IDanmakus;
import master.flame.danmaku.danmaku.model.android.Danmakus;
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser;
import master.flame.danmaku.danmaku.util.DanmakuUtils;

public class Parser extends BaseDanmakuParser {

    private final Map<String, String> charMap;
    private BaseDanmaku item;
    private Danmaku danmaku;
    private int index;

    public Parser(String path) {
        charMap = new HashMap<>();
        charMap.put("&amp;", "&");
        charMap.put("&quot;", "\"");
        charMap.put("&gt;", ">");
        charMap.put("&lt;", "<");
        fetch(path);
    }

    private void fetch(String path) {
        try {
            danmaku = Danmaku.objectFrom(OkHttp.newCall(UrlUtil.convert(path)).execute().body().byteStream());
        } catch (IOException e) {
            danmaku = new Danmaku();
        }
    }

    @Override
    protected Danmakus parse() {
        Danmakus result = new Danmakus(IDanmakus.ST_BY_TIME);
        for (Danmaku.Data data : danmaku.getData()) {
            String[] values = data.getParam().split(",");
            if (values.length < 4) continue;
            setParam(values);
            setText(data.getText());
            synchronized (result.obtainSynchronizer()) {
                result.addItem(item);
            }
        }
        return result;
    }

    private void setParam(String[] values) {
        int type = Integer.parseInt(values[1]);
        long time = (long) (Float.parseFloat(values[0]) * 1000);
        float size = Float.parseFloat(values[2]) * (mDispDensity - 0.6f);
        int color = (int) ((0x00000000ff000000L | Long.parseLong(values[3])) & 0x00000000ffffffffL);
        item = mContext.mDanmakuFactory.createDanmaku(type, mContext);
        item.index = index++;
        item.setTime(time);
        item.setTimer(mTimer);
        item.textSize = size;
        item.textColor = color;
        item.textShadowColor = color <= Color.BLACK ? Color.WHITE : Color.BLACK;
        item.flags = mContext.mGlobalFlagValues;
    }

    private void setText(String text) {
        for (Map.Entry<String, String> entry : charMap.entrySet()) text = text.replace(entry.getKey(), entry.getValue());
        DanmakuUtils.fillText(item, text);
    }
}