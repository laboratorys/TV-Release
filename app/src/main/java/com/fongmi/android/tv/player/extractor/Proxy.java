package com.fongmi.android.tv.player.extractor;

import com.fongmi.android.tv.player.Source;
import com.fongmi.android.tv.utils.UrlUtil;

public class Proxy implements Source.Extractor {

    @Override
    public boolean match(String scheme, String host) {
        return scheme.equals("proxy");
    }

    @Override
    public String fetch(String url) throws Exception {
        return UrlUtil.convert(url);
    }

    @Override
    public void stop() {
    }

    @Override
    public void exit() {
    }
}
