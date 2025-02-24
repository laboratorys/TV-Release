package com.fongmi.android.tv.bean;

import android.graphics.Color;
import android.text.TextUtils;

import java.util.regex.Matcher;

public class DanmakuData {

    private final String text;
    private int type;
    private int color;
    private int shadow;
    private long time;
    private float size;

    public DanmakuData(Matcher matcher) throws Exception {
        this.param(matcher.group(1));
        this.text = matcher.group(2);
    }

    private void param(String parm) throws Exception {
        String[] params = parm.split(",");
        if (params.length < 4) throw new Exception();
        this.type = Integer.parseInt(params[1]);
        this.size = Float.parseFloat(params[2]);
        this.time = (long) (Float.parseFloat(params[0]) * 1000);
        this.color = (int) (0xFF000000L | Long.parseLong(params[3]) & 0xFFFFFF);
        this.shadow = color == Color.BLACK ? Color.WHITE : Color.BLACK;
    }

    public int getType() {
        return type;
    }

    public int getShadow() {
        return shadow;
    }

    public int getColor() {
        return color;
    }

    public long getTime() {
        return time;
    }

    public float getSize() {
        return size;
    }

    public String getText() {
        return TextUtils.isEmpty(text) ? "" : text.replace("&amp;", "&").replace("&quot;", "\"").replace("&gt;", ">").replace("&lt;", "<");
    }
}
