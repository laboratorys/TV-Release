package com.fongmi.chaquo;

import androidx.annotation.Keep;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.github.catvod.utils.Path;

public class Loader {

    private final PyObject app;

    public Loader() {
        app = Python.getInstance().getModule("app");
    }

    @Keep
    public Spider spider(String api) {
        PyObject obj = app.callAttr("spider", Path.py().getAbsolutePath(), api);
        return new Spider(app, obj, api);
    }
}
