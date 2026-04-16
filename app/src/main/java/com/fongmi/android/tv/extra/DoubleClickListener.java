package com.fongmi.android.tv.extra;

import android.os.Handler;
import android.os.Looper;
import android.view.View;

public class DoubleClickListener implements View.OnClickListener{
    private final View.OnClickListener singleClick;
    private final View.OnClickListener doubleClick;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private int clickCount = 0;

    public static void bind(View view, View.OnClickListener single, View.OnClickListener doubleClick) {
        view.setOnClickListener(new DoubleClickListener(single, doubleClick));
    }

    private DoubleClickListener(View.OnClickListener single, View.OnClickListener doubleClick) {
        this.singleClick = single;
        this.doubleClick = doubleClick;
    }

    @Override
    public void onClick(View v) {
        clickCount++;
        if (clickCount == 1) {
            // 第一次点击，发送延时任务
            // 等待间隔
            int interval = 400;
            handler.postDelayed(() -> {
                if (clickCount == 1 && singleClick != null) {
                    singleClick.onClick(v);
                }
                clickCount = 0;
            }, interval);
        } else if (clickCount == 2) {
            // 第二次点击，取消延时任务，直接跑双击
            handler.removeCallbacksAndMessages(null);
            if (doubleClick != null) doubleClick.onClick(v);
            clickCount = 0;
        }
    }
}
