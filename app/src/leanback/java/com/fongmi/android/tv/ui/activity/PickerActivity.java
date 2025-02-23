package com.fongmi.android.tv.ui.activity;

import android.content.Intent;
import android.net.Uri;

import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.ItemBridgeAdapter;
import androidx.viewbinding.ViewBinding;

import com.fongmi.android.tv.databinding.ActivityPickerBinding;
import com.fongmi.android.tv.ui.base.BaseActivity;
import com.fongmi.android.tv.ui.presenter.PickPresenter;
import com.fongmi.android.tv.utils.ResUtil;
import com.github.catvod.utils.Path;

import java.io.File;

public class PickerActivity extends BaseActivity implements PickPresenter.OnClickListener {

    private ActivityPickerBinding mBinding;
    private ArrayObjectAdapter mAdapter;
    private File dir;

    private boolean isRoot() {
        return Path.root().equals(dir);
    }

    @Override
    protected ViewBinding getBinding() {
        return mBinding = ActivityPickerBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initView() {
        setRecyclerView();
        update(Path.root());
    }

    private void setRecyclerView() {
        mBinding.recycler.setHasFixedSize(true);
        mBinding.recycler.setVerticalSpacing(ResUtil.dp2px(16));
        mBinding.recycler.setAdapter(new ItemBridgeAdapter(mAdapter = new ArrayObjectAdapter(new PickPresenter(this))));
    }

    private void update(File dir) {
        mBinding.recycler.setSelectedPosition(0);
        mAdapter.setItems(Path.list(this.dir = dir), null);
    }

    @Override
    public void onItemClick(File file) {
        if (file.isDirectory()) {
            update(file);
        } else {
            setResult(RESULT_OK, new Intent().setData(Uri.fromFile(file)));
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        if (isRoot()) {
            super.onBackPressed();
        } else {
            update(dir.getParentFile());
        }
    }
}
