package com.fongmi.android.tv.ui.adapter.diff;

import androidx.annotation.NonNull;
import androidx.leanback.widget.DiffCallback;
import androidx.recyclerview.widget.DiffUtil;

public abstract class BaseDiffCallback<T> extends DiffCallback<T> {

    @Override
    public abstract boolean areItemsTheSame(@NonNull T oldItem, @NonNull T newItem);

    @Override
    public abstract boolean areContentsTheSame(@NonNull T oldItem, @NonNull T newItem);

    public final DiffUtil.ItemCallback<T> asItemCallback() {
        return new DiffUtil.ItemCallback<T>() {
            @Override
            public boolean areItemsTheSame(@NonNull T oldItem, @NonNull T newItem) {
                return BaseDiffCallback.this.areItemsTheSame(oldItem, newItem);
            }

            @Override
            public boolean areContentsTheSame(@NonNull T oldItem, @NonNull T newItem) {
                return BaseDiffCallback.this.areContentsTheSame(oldItem, newItem);
            }
        };
    }
}