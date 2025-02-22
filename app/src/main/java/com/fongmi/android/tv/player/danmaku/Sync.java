package com.fongmi.android.tv.player.danmaku;

import com.fongmi.android.tv.player.Players;

import master.flame.danmaku.danmaku.model.AbsDanmakuSync;

public class Sync extends AbsDanmakuSync {

    private final Players players;
    private long time;

    public Sync(Players players) {
        this.players = players;
        this.time = System.currentTimeMillis();
    }

    @Override
    public long getUptimeMillis() {
        return players == null ? 0 : players.getPosition();
    }

    @Override
    public int getSyncState() {
        if (players == null) return SYNC_STATE_HALT;
        long current = System.currentTimeMillis();
        if (current - time < 1000) return SYNC_STATE_HALT;
        time = current;
        return players.isPlaying() ? SYNC_STATE_PLAYING : SYNC_STATE_HALT;
    }

    @Override
    public long getThresholdTimeMills() {
        return 1000L;
    }
}
