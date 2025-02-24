package com.fongmi.android.tv.gson;

import com.fongmi.android.tv.App;
import com.fongmi.android.tv.bean.Danmaku;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.List;

public class DanmakuAdapter implements JsonDeserializer<List<Danmaku>> {

    @Override
    public List<Danmaku> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (json.isJsonPrimitive()) return Danmaku.from(json.getAsString());
        return App.gson().fromJson(json, typeOfT);
    }
}
