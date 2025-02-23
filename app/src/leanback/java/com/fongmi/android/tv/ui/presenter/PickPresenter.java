package com.fongmi.android.tv.ui.presenter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.leanback.widget.Presenter;

import com.fongmi.android.tv.R;
import com.fongmi.android.tv.databinding.AdapterPickerBinding;

import java.io.File;

public class PickPresenter extends Presenter {

    private final OnClickListener mListener;

    public PickPresenter(OnClickListener listener) {
        this.mListener = listener;
    }

    public interface OnClickListener {

        void onItemClick(File file);
    }

    @Override
    public Presenter.ViewHolder onCreateViewHolder(ViewGroup parent) {
        return new ViewHolder(AdapterPickerBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(Presenter.ViewHolder viewHolder, Object object) {
        File file = (File) object;
        ViewHolder holder = (ViewHolder) viewHolder;
        holder.binding.name.setText(file.getName());
        holder.binding.getRoot().setOnClickListener(v -> mListener.onItemClick(file));
        holder.binding.image.setImageResource(file.isDirectory() ? R.drawable.ic_picker_folder : R.drawable.ic_picker_file);
    }

    @Override
    public void onUnbindViewHolder(Presenter.ViewHolder viewHolder) {
    }

    public static class ViewHolder extends Presenter.ViewHolder {

        private final AdapterPickerBinding binding;

        public ViewHolder(@NonNull AdapterPickerBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}