package com.fongmi.android.tv.ui.dialog;

import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.inputmethod.EditorInfo;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.fongmi.android.tv.R;
import com.fongmi.android.tv.databinding.DialogUaBinding;
import com.fongmi.android.tv.impl.DanmakuCallback;
import com.fongmi.android.tv.setting.DanmakuSetting;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class DanmakuApiDialog {

    private final DialogUaBinding binding;
    private final DanmakuCallback callback;
    private AlertDialog dialog;

    public static DanmakuApiDialog create(Fragment fragment) {
        return new DanmakuApiDialog(fragment);
    }

    public DanmakuApiDialog(Fragment fragment) {
        this.callback = (DanmakuCallback) fragment;
        this.binding = DialogUaBinding.inflate(LayoutInflater.from(fragment.getContext()));
    }

    public void show() {
        initDialog();
        initView();
        initEvent();
    }

    private void initDialog() {
        dialog = new MaterialAlertDialogBuilder(binding.getRoot().getContext()).setTitle(R.string.danmaku_api).setView(binding.getRoot()).setPositiveButton(R.string.dialog_positive, this::onPositive).setNegativeButton(R.string.dialog_negative, null).create();
        dialog.getWindow().setDimAmount(0);
        dialog.show();
    }

    private void initView() {
        String text;
        binding.text.setText(text = DanmakuSetting.getEffectiveApiUrl());
        binding.text.setSelection(TextUtils.isEmpty(text) ? 0 : text.length());
    }

    private void initEvent() {
        binding.text.setOnEditorActionListener((textView, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) dialog.getButton(DialogInterface.BUTTON_POSITIVE).performClick();
            return true;
        });
    }

    private void onPositive(DialogInterface dialog, int which) {
        callback.setDanmakuApi(binding.text.getText().toString().trim());
    }
}
