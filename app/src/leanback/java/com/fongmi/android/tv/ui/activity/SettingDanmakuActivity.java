package com.fongmi.android.tv.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.viewbinding.ViewBinding;

import com.fongmi.android.tv.R;
import com.fongmi.android.tv.databinding.ActivitySettingDanmakuBinding;
import com.fongmi.android.tv.impl.DanmakuCallback;
import com.fongmi.android.tv.setting.DanmakuSetting;
import com.fongmi.android.tv.ui.base.BaseActivity;
import com.fongmi.android.tv.ui.dialog.DanmakuApiDialog;

public class SettingDanmakuActivity extends BaseActivity implements DanmakuCallback {

    private ActivitySettingDanmakuBinding mBinding;

    public static void start(Activity activity) {
        activity.startActivity(new Intent(activity, SettingDanmakuActivity.class));
    }

    private String getSwitch(boolean value) {
        return getString(value ? R.string.setting_on : R.string.setting_off);
    }

    @Override
    protected ViewBinding getBinding() {
        return mBinding = ActivitySettingDanmakuBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        mBinding.danmakuLoad.requestFocus();
        mBinding.danmakuApiText.setText(DanmakuSetting.getEffectiveApiUrl());
        mBinding.danmakuAutoText.setText(getSwitch(DanmakuSetting.isAuto()));
        mBinding.danmakuLoadText.setText(getSwitch(DanmakuSetting.isLoad()));
        updateApiVisibility();
    }

    @Override
    protected void initEvent() {
        mBinding.danmakuApi.setOnClickListener(this::onDanmakuApi);
        mBinding.danmakuAuto.setOnClickListener(this::setDanmakuAuto);
        mBinding.danmakuLoad.setOnClickListener(this::setDanmakuLoad);
    }

    private void setDanmakuLoad(View view) {
        DanmakuSetting.putLoad(!DanmakuSetting.isLoad());
        mBinding.danmakuLoadText.setText(getSwitch(DanmakuSetting.isLoad()));
        updateApiVisibility();
    }

    private void updateApiVisibility() {
        boolean load = DanmakuSetting.isLoad();
        mBinding.danmakuApi.setVisibility(load ? View.VISIBLE : View.GONE);
        updateAutoVisibility();
    }

    private void updateAutoVisibility() {
        boolean show = DanmakuSetting.isLoad() && !TextUtils.isEmpty(DanmakuSetting.getEffectiveApiUrl());
        mBinding.danmakuAuto.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void onDanmakuApi(View view) {
        DanmakuApiDialog.create(this).show();
    }

    @Override
    public void setDanmakuApi(String url) {
        DanmakuSetting.putApiUrl(url);
        mBinding.danmakuApiText.setText(DanmakuSetting.getEffectiveApiUrl());
        updateAutoVisibility();
    }

    private void setDanmakuAuto(View view) {
        DanmakuSetting.putAuto(!DanmakuSetting.isAuto());
        mBinding.danmakuAutoText.setText(getSwitch(DanmakuSetting.isAuto()));
    }
}
