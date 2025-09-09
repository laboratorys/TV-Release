package com.fongmi.android.tv.impl;

import com.permissionx.guolindev.request.ExplainScope;
import com.permissionx.guolindev.request.ForwardScope;

import java.util.List;

public class ChainTask implements com.permissionx.guolindev.request.ChainTask {

    @Override
    public ExplainScope getExplainScope() {
        return null;
    }

    @Override
    public ForwardScope getForwardScope() {
        return null;
    }

    @Override
    public void request() {
    }

    @Override
    public void requestAgain(List<String> permissions) {
    }

    @Override
    public void finish() {
    }
}
