package com.fongmi.android.tv.extra;

import com.fongmi.android.tv.api.config.VodConfig;
import com.fongmi.android.tv.bean.Keep;
import com.fongmi.android.tv.db.AppDatabase;
import com.fongmi.android.tv.event.RefreshEvent;
import java.util.List;

public class KeepCloudSync extends CloudSync<Keep> {

    private static final KeepCloudSync instance = new KeepCloudSync();

    public static KeepCloudSync get() {
        return instance;
    }

    @Override
    protected String getPath() {
        return "/keep";
    }

    @Override
    protected void onPulled(String json) {
        List<Keep> items = Keep.arrayFrom(json);
        if (items.isEmpty()) return;
        AppDatabase.get().getKeepDao().delete(VodConfig.getCid());
        for (Keep item : items) {
            item.saveLocal();
        }
        RefreshEvent.keep();
    }
}