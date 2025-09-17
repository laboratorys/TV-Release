package com.fongmi.android.tv.ui.adapter.diff;

import com.fongmi.android.tv.bean.History;

import org.jspecify.annotations.NonNull;

public class HistoryDiffCallback extends BaseDiffCallback<History> {

    @Override
    public boolean areItemsTheSame(@NonNull History oldItem, @NonNull History newItem) {
        return oldItem.getKey().equals(newItem.getKey());
    }

    @Override
    public boolean areContentsTheSame(@NonNull History oldItem, @NonNull History newItem) {
        return oldItem.equals(newItem);
    }
}