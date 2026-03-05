package com.fongmi.android.tv.extra;

import android.app.Activity;
import android.util.Log;

import com.fongmi.android.tv.App;
import com.fongmi.android.tv.api.config.VodConfig;
import com.fongmi.android.tv.bean.Config;
import com.fongmi.android.tv.event.RefreshEvent;
import com.fongmi.android.tv.impl.Callback;
import com.fongmi.android.tv.utils.Notify;

public class BootConfig {
    public static Config createDepot() {
        Config config = new Config();
        config.setName("🚀 内置多仓源");
        config.setUrl(Constant.DEPOT_URL);
        config.setType(0);
        return config;
    }

    public static void loadDepot(Activity activity){
        Config config = createDepot();
        VodConfig.load(config, new Callback() {
            @Override
            public void start() {
                if (activity != null) Notify.progress(activity);
            }

            @Override
            public void success() {
                Log.d("BootConfig", "🚀 内置多仓源已刷新");
                Notify.dismiss();
                RefreshEvent.live();
                RefreshEvent.home();
                App.post(() -> Notify.show("🚀 内置多仓源已刷新"));
            }

            @Override
            public void error(String msg) {
                Log.d("BootConfig", "❌ 刷新失败: " + msg);
                App.post(() -> Notify.show("❌ 刷新失败: " + msg));
            }
        });
    }
}
