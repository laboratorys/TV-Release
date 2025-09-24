package com.fongmi.android.tv.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewbinding.ViewBinding;

import com.fongmi.android.tv.R;
import com.fongmi.android.tv.bean.Style;
import com.fongmi.android.tv.databinding.FragmentFolderBinding;
import com.fongmi.android.tv.ui.base.BaseFragment;

import java.util.HashMap;

public class TypeFragment extends BaseFragment {

    public static TypeFragment newInstance(String key, String typeId, Style style, HashMap<String, String> extend, boolean folder) {
        Bundle args = new Bundle();
        args.putString("key", key);
        args.putString("typeId", typeId);
        args.putBoolean("folder", folder);
        args.putParcelable("style", style);
        args.putSerializable("extend", extend);
        TypeFragment fragment = new TypeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private String getKey() {
        return getArguments().getString("key");
    }

    private String getTypeId() {
        return getArguments().getString("typeId");
    }

    private boolean getFolder() {
        return getArguments().getBoolean("folder");
    }

    private Style getStyle() {
        return getArguments().getParcelable("style");
    }

    private HashMap<String, String> getExtend() {
        return (HashMap<String, String>) getArguments().getSerializable("extend");
    }

    @Override
    protected ViewBinding getBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentFolderBinding.inflate(inflater, container, false);
    }

    @Override
    protected void initView() {
        getChildFragmentManager().beginTransaction().replace(R.id.container, VodFragment.newInstance(getKey(), getTypeId(), getStyle(), getExtend(), getFolder())).commit();
    }

    public void openFolder(String typeId) {
        VodFragment next = VodFragment.newInstance(getKey(), typeId, getStyle(), getExtend(), getFolder());
        VodFragment current = (VodFragment) getChildFragmentManager().findFragmentById(R.id.container);
        FragmentTransaction ft = getChildFragmentManager().beginTransaction();
        if (current != null) ft.hide(current);
        ft.add(R.id.container, next);
        ft.addToBackStack(null);
        ft.commit();
    }

    public void toggleFilter(boolean visible) {
        VodFragment current = (VodFragment) getChildFragmentManager().findFragmentById(R.id.container);
        if (current != null) current.toggleFilter(visible);
    }

    public void onRefresh() {
        VodFragment current = (VodFragment) getChildFragmentManager().findFragmentById(R.id.container);
        if (current != null) current.onRefresh();
    }

    public boolean canBack() {
        return getChildFragmentManager().getBackStackEntryCount() > 0;
    }

    public void goBack() {
        getChildFragmentManager().popBackStack();
    }
}
