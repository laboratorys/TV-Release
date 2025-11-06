package com.fongmi.quickjs;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.startup.Initializer;

import com.whl.quickjs.android.QuickJSLoader;

import java.util.Collections;
import java.util.List;

public class Startup implements Initializer<Void> {

    @NonNull
    @Override
    public Void create(@NonNull Context context) {
        QuickJSLoader.init();
        return null;
    }

    @NonNull
    @Override
    public List<Class<? extends Initializer<?>>> dependencies() {
        return Collections.emptyList();
    }
}
