package com.github.catvod.utils;

import java.util.HashMap;
import java.util.Map;

public class ZhuToPin {

    private static final Map<String, String> map = new HashMap<>();

    static {
        map.put("ㄅ", "b");
        map.put("ㄆ", "p");
        map.put("ㄇ", "m");
        map.put("ㄈ", "f");
        map.put("ㄉ", "d");
        map.put("ㄊ", "t");
        map.put("ㄋ", "n");
        map.put("ㄌ", "l");
        map.put("ㄍ", "g");
        map.put("ㄎ", "k");
        map.put("ㄏ", "h");
        map.put("ㄐ", "j");
        map.put("ㄑ", "q");
        map.put("ㄒ", "x");
        map.put("ㄓ", "zh");
        map.put("ㄔ", "ch");
        map.put("ㄕ", "sh");
        map.put("ㄖ", "r");
        map.put("ㄗ", "z");
        map.put("ㄘ", "c");
        map.put("ㄙ", "s");
        map.put("ㄧ", "yi");
        map.put("ㄨ", "wu");
        map.put("ㄩ", "yu");
        map.put("ㄚ", "a");
        map.put("ㄛ", "o");
        map.put("ㄜ", "e");
        map.put("ㄝ", "eh");
        map.put("ㄞ", "ai");
        map.put("ㄟ", "ei");
        map.put("ㄠ", "ao");
        map.put("ㄡ", "ou");
        map.put("ㄢ", "an");
        map.put("ㄣ", "en");
        map.put("ㄤ", "ang");
        map.put("ㄥ", "eng");
        map.put("ㄦ", "er");
    }

    public static String get(String text) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            String zhuyin = String.valueOf(text.charAt(i));
            String pinyin = map.get(zhuyin);
            sb.append(pinyin != null ? pinyin.charAt(0) : zhuyin);
        }
        return sb.toString();
    }
}
