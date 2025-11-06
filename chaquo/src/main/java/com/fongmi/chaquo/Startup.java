package com.fongmi.chaquo;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.startup.Initializer;

import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

import java.util.Collections;
import java.util.List;

public class Startup implements Initializer<Void> {

    @NonNull
    @Override
    public Void create(@NonNull Context context) {
        Python.start(new AndroidPlatform(context));
        return null;
    }

    @NonNull
    @Override
    public List<Class<? extends Initializer<?>>> dependencies() {
        return Collections.emptyList();
    }
}
