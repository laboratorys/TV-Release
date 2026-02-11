package com.fongmi.android.tv.extra;

import com.fongmi.android.tv.api.config.VodConfig;
import com.fongmi.android.tv.bean.History;
import com.fongmi.android.tv.db.AppDatabase;
import com.fongmi.android.tv.event.RefreshEvent;
import java.util.List;

public class PlayRecordCloudSync extends CloudSync<History> {

    private static final PlayRecordCloudSync instance = new PlayRecordCloudSync();

    public static PlayRecordCloudSync get() {
        return instance;
    }

    @Override
    protected String getPath() {
        return "/history";
    }

    @Override
    protected void onPulled(String json) {
        List<History> items = History.arrayFrom(json);
        AppDatabase.get().getHistoryDao().delete(VodConfig.getCid());
        for (History item : items) {
            item.save(false);
        }
        RefreshEvent.history();
    }
}