package com.fongmi.android.tv.ui.adapter.diff;

import com.fongmi.android.tv.bean.Keep;

import org.jspecify.annotations.NonNull;

public class KeepDiffCallback extends BaseDiffCallback<Keep> {

    @Override
    public boolean areItemsTheSame(@NonNull Keep oldItem, @NonNull Keep newItem) {
        return oldItem.getKey().equals(newItem.getKey());
    }

    @Override
    public boolean areContentsTheSame(@NonNull Keep oldItem, @NonNull Keep newItem) {
        return oldItem.equals(newItem);
    }
}
