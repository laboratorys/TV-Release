package com.fongmi.android.tv.player.extractor;

import android.net.Uri;

import com.fongmi.android.tv.player.Source;
import com.fongmi.android.tv.utils.UrlUtil;
import com.github.catvod.net.OkHttp;
import com.github.catvod.utils.Path;

import java.io.File;

public class Strm implements Source.Extractor {

    @Override
    public boolean match(Uri uri) {
        return UrlUtil.path(uri).contains(".strm");
    }

    @Override
    public String fetch(String url) throws Exception {
        if (url.startsWith("file")) url = url.substring(7);
        if (url.startsWith("http")) return OkHttp.string(url).split("\\R", 2)[0];
        return Path.read(new File(url)).split("\\R", 2)[0];
    }

    @Override
    public void stop() {
    }

    @Override
    public void exit() {
    }
}
