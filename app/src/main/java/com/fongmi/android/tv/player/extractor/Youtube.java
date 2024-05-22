package com.fongmi.android.tv.player.extractor;

import android.text.TextUtils;
import android.util.Base64;

import com.fongmi.android.tv.impl.NewPipeImpl;
import com.fongmi.android.tv.player.Source;
import com.github.catvod.net.OkHttp;
import com.github.catvod.utils.Json;
import com.github.catvod.utils.Util;
import com.google.common.net.HttpHeaders;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.services.youtube.YoutubeJavaScriptPlayerManager;
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeStreamLinkHandlerFactory;
import org.schabi.newpipe.extractor.utils.Parser;

import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Headers;

public class Youtube implements Source.Extractor {

    private static final String MPD = "<MPD xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xmlns='urn:mpeg:dash:schema:mpd:2011' xsi:schemaLocation='urn:mpeg:dash:schema:mpd:2011 DASH-MPD.xsd' type='static' mediaPresentationDuration='%s' minBufferTime='PT1.500S' profiles='urn:mpeg:dash:profile:isoff-on-demand:2011'>\n" + "<Period duration='%s' start='PT0S'>\n" + "%s\n" + "%s\n" + "</Period>\n" + "</MPD>";
    private static final String ADAPTATION_SET = "<AdaptationSet lang='chi'>\n" + "<ContentComponent contentType='%s'/>\n" + "<Representation id='%s' bandwidth='%d' codecs='%s' mimeType='%s' %s maxPlayoutRate='1' startWithSAP='1'>\n" + "<BaseURL>%s</BaseURL>\n" + "<SegmentBase indexRange='%s'>\n" + "<Initialization range='%s'/>\n" + "</SegmentBase>\n" + "</Representation>\n" + "</AdaptationSet>";

    @Override
    public boolean match(String scheme, String host) {
        return host.contains("youtube.com") || host.contains("youtu.be");
    }

    public Youtube() {
        NewPipe.init(new NewPipeImpl());
    }

    @Override
    public String fetch(String url) throws Exception {
        String id = YoutubeStreamLinkHandlerFactory.getInstance().getId(url);
        String html = OkHttp.newCall(url, Headers.of(HttpHeaders.USER_AGENT, Util.CHROME)).execute().body().string();
        Matcher matcher = Pattern.compile("var ytInitialPlayerResponse =(.*?\\});").matcher(html);
        if (!matcher.find()) return "";
        JsonObject streamingData = Json.parse(matcher.group(1)).getAsJsonObject().get("streamingData").getAsJsonObject();
        if (streamingData.has("hlsManifestUrl")) return getHlsManifestUrl(streamingData);
        if (streamingData.has("adaptiveFormats")) return getMpdWithBase64(streamingData, id);
        return url;
    }

    private String getHlsManifestUrl(JsonObject streamingData) {
        JsonElement hlsManifestUrl = streamingData.get("hlsManifestUrl");
        if (hlsManifestUrl.isJsonArray()) return hlsManifestUrl.getAsJsonArray().get(0).getAsString();
        return hlsManifestUrl.getAsString();
    }

    private String getMpdWithBase64(JsonObject streamingData, String videoId) {
        String approxDurationMs = "";
        StringBuilder video = new StringBuilder();
        StringBuilder audio = new StringBuilder();
        for (JsonElement element : streamingData.get("adaptiveFormats").getAsJsonArray()) {
            JsonObject adaptiveFormat = element.getAsJsonObject();
            String mimeType = adaptiveFormat.get("mimeType").getAsString();
            if (mimeType.contains("video")) video.append(getAdaptationSet(videoId, adaptiveFormat, "video", mimeType.split(";")));
            if (mimeType.contains("audio")) audio.append(getAdaptationSet(videoId, adaptiveFormat, "audio", mimeType.split(";")));
            if (TextUtils.isEmpty(approxDurationMs)) approxDurationMs = adaptiveFormat.get("approxDurationMs").getAsString();
        }
        String duration = String.format(Locale.getDefault(), "PT%.3fS", Integer.parseInt(approxDurationMs) / 1000.0);
        String finalMpd = String.format(Locale.getDefault(), MPD, duration, duration, video, audio);
        return "data:application/dash+xml;base64," + Base64.encodeToString(finalMpd.getBytes(), 0);
    }

    private String getAdaptationSet(String videoId, JsonObject adaptiveFormat, String contentType, String[] split) {
        String mediaParam = "";
        String mimeType = split[0];
        String baseUrl = getBaseUrl(videoId, adaptiveFormat);
        String iTag = adaptiveFormat.get("itag").getAsString();
        int bitrate = adaptiveFormat.get("bitrate").getAsInt();
        String codecs = split[1].split("=")[1].replace("\"", "");
        JsonObject initRange = adaptiveFormat.get("initRange").getAsJsonObject();
        JsonObject indexRange = adaptiveFormat.get("indexRange").getAsJsonObject();
        String initParam = initRange.get("start").getAsString() + "-" + initRange.get("end").getAsString();
        String indexParam = indexRange.get("start").getAsString() + "-" + indexRange.get("end").getAsString();

        if (mimeType.contains("video")) {
            int fps = adaptiveFormat.get("fps").getAsInt();
            int width = adaptiveFormat.get("width").getAsInt();
            int height = adaptiveFormat.get("height").getAsInt();
            mediaParam = String.format(Locale.getDefault(), "height='%d' width='%d' frameRate='%d'", height, width, fps);
        }

        if (mimeType.contains("audio")) {
            int audioSamplingRate = adaptiveFormat.get("audioSampleRate").getAsInt();
            mediaParam = String.format(Locale.getDefault(), "subsegmentAlignment='true' audioSamplingRate='%d'", audioSamplingRate);
        }

        return String.format(Locale.getDefault(), ADAPTATION_SET, contentType, iTag, bitrate, codecs, mimeType, mediaParam, baseUrl, indexParam, initParam);
    }

    private String getBaseUrl(String videoId, JsonObject adaptiveFormat) {
        String baseUrl;
        if (adaptiveFormat.has("url")) baseUrl = adaptiveFormat.get("url").getAsString();
        else baseUrl = decodeCipher(videoId, adaptiveFormat);
        return baseUrl.replace("&", "&amp;");
    }

    private String decodeCipher(String videoId, JsonObject adaptiveFormat) {
        try {
            String cipherString = adaptiveFormat.has("cipher") ? adaptiveFormat.get("cipher").getAsString() : adaptiveFormat.get("signatureCipher").getAsString();
            Map<String, String> cipher = Parser.compatParseMap(cipherString);
            return cipher.get("url") + "&" + cipher.get("sp") + "=" + YoutubeJavaScriptPlayerManager.deobfuscateSignature(videoId, cipher.get("s"));
        } catch (Exception e) {
            return "";
        }
    }

    @Override
    public void stop() {
    }

    @Override
    public void exit() {
    }
}
