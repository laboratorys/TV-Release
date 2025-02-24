package com.fongmi.android.tv.bean;

import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Text;
import org.simpleframework.xml.core.Persister;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Root(name = "i", strict = false)
public class Danmaku {

    @ElementList(entry = "d", required = false, inline = true)
    private List<Data> data;

    @SerializedName("name")
    private String name;
    @SerializedName("url")
    private String url;

    public static List<Danmaku> from(String path) {
        Danmaku danmaku = new Danmaku();
        danmaku.setName(path);
        danmaku.setUrl(path);
        return List.of(danmaku);
    }

    public static Danmaku fromXml(InputStream is) {
        try {
            return new Persister().read(Danmaku.class, is);
        } catch (Exception e) {
            return fromText(is);
        }
    }

    //[0.000000,1,25,16777215]20250222
    public static Danmaku fromText(InputStream is) {
        Danmaku danmaku = new Danmaku();
        Pattern pattern = Pattern.compile("\\[(.*?)\\](.*)");
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            String line;
            while ((line = br.readLine()) != null) {
                Matcher matcher = pattern.matcher(line);
                if (matcher.matches() && matcher.groupCount() == 2) danmaku.getData().add(new Data(matcher));
            }
            return danmaku;
        } catch (Exception ignored) {
            return danmaku;
        }
    }

    public List<Data> getData() {
        return data = data == null ? new ArrayList<>() : data;
    }

    public String getUrl() {
        return TextUtils.isEmpty(url) ? "" : url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return TextUtils.isEmpty(name) ? getUrl() : name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static class Data {

        @Attribute(name = "p", required = false)
        public String param;

        @Text(required = false)
        public String text;

        public Data() {
        }

        public Data(Matcher matcher) {
            this.param = matcher.group(1);
            this.text = matcher.group(2);
        }

        public String getParam() {
            return TextUtils.isEmpty(param) ? "" : param;
        }

        public String getText() {
            return TextUtils.isEmpty(text) ? "" : text;
        }
    }
}