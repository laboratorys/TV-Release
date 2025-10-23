package com.fongmi.android.tv.ui.dialog;

import android.app.Activity;
import android.view.LayoutInflater;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.fongmi.android.tv.bean.Config;
import com.fongmi.android.tv.databinding.DialogHistoryBinding;
import com.fongmi.android.tv.impl.ConfigCallback;
import com.fongmi.android.tv.ui.adapter.ConfigAdapter;
import com.fongmi.android.tv.ui.custom.SpaceItemDecoration;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class HistoryDialog implements ConfigAdapter.OnClickListener {

    private final DialogHistoryBinding binding;
    private final ConfigCallback callback;
    private final AlertDialog dialog;
    private ConfigAdapter adapter;
    private boolean readOnly;
    private int type;

    public static HistoryDialog create(Activity activity) {
        return new HistoryDialog(activity);
    }

    public static HistoryDialog create(Fragment fragment) {
        return new HistoryDialog(fragment);
    }

    public HistoryDialog type(int type) {
        this.type = type;
        return this;
    }

    public HistoryDialog(Activity activity) {
        this.callback = (ConfigCallback) activity;
        this.binding = DialogHistoryBinding.inflate(LayoutInflater.from(activity));
        this.dialog = new MaterialAlertDialogBuilder(activity).setView(binding.getRoot()).create();
    }

    public HistoryDialog(Fragment fragment) {
        this.callback = (ConfigCallback) fragment;
        this.binding = DialogHistoryBinding.inflate(LayoutInflater.from(fragment.getContext()));
        this.dialog = new MaterialAlertDialogBuilder(fragment.requireActivity()).setView(binding.getRoot()).create();
    }

    public HistoryDialog readOnly() {
        this.readOnly = true;
        return this;
    }

    public void show() {
        setRecyclerView();
        setDialog();
    }

    private void setRecyclerView() {
        adapter = new ConfigAdapter(this);
        binding.recycler.setItemAnimator(null);
        binding.recycler.setHasFixedSize(false);
        binding.recycler.addItemDecoration(new SpaceItemDecoration(1, 8));
        binding.recycler.setAdapter(adapter.readOnly(readOnly).addAll(type));
    }

    private void setDialog() {
        if (adapter.getItemCount() == 0) return;
        dialog.getWindow().setDimAmount(0);
        dialog.show();
    }

    @Override
    public void onTextClick(Config item) {
        callback.setConfig(item);
        dialog.dismiss();
    }

    @Override
    public void onDeleteClick(Config item) {
        if (adapter.remove(item) == 0) dialog.dismiss();
    }
}
