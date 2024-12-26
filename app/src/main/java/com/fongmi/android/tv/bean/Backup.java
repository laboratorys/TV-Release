package com.fongmi.android.tv.bean;

import androidx.annotation.NonNull;

import com.fongmi.android.tv.App;
import com.fongmi.android.tv.db.AppDatabase;
import com.google.gson.annotations.SerializedName;

import java.util.Collections;
import java.util.List;

public class Backup {

    @SerializedName("site")
    private List<Site> site;
    @SerializedName("live")
    private List<Live> live;
    @SerializedName("keep")
    private List<Keep> keep;
    @SerializedName("config")
    private List<Config> config;
    @SerializedName("history")
    private List<History> history;

    public static Backup create() {
        Backup backup = new Backup();
        backup.setSite(AppDatabase.get().getSiteDao().findAll());
        backup.setLive(AppDatabase.get().getLiveDao().findAll());
        backup.setKeep(AppDatabase.get().getKeepDao().findAll());
        backup.setConfig(AppDatabase.get().getConfigDao().findAll());
        backup.setHistory(AppDatabase.get().getHistoryDao().findAll());
        return backup;
    }

    public void restore() {
        AppDatabase.get().clearAllTables();
        AppDatabase.get().getSiteDao().insertOrUpdate(getSite());
        AppDatabase.get().getLiveDao().insertOrUpdate(getLive());
        AppDatabase.get().getKeepDao().insertOrUpdate(getKeep());
        AppDatabase.get().getConfigDao().insertOrUpdate(getConfig());
        AppDatabase.get().getHistoryDao().insertOrUpdate(getHistory());
    }

    public static Backup objectFrom(String json) {
        try {
            Backup backup = App.gson().fromJson(json, Backup.class);
            return backup == null ? new Backup() : backup;
        } catch (Exception e) {
            return new Backup();
        }
    }

    public List<Site> getSite() {
        return site == null ? Collections.emptyList() : site;
    }

    public void setSite(List<Site> site) {
        this.site = site;
    }

    public List<Live> getLive() {
        return live == null ? Collections.emptyList() : live;
    }

    public void setLive(List<Live> live) {
        this.live = live;
    }

    public List<Keep> getKeep() {
        return keep == null ? Collections.emptyList() : keep;
    }

    public void setKeep(List<Keep> keep) {
        this.keep = keep;
    }

    public List<Config> getConfig() {
        return config == null ? Collections.emptyList() : config;
    }

    public void setConfig(List<Config> config) {
        this.config = config;
    }

    public List<History> getHistory() {
        return history == null ? Collections.emptyList() : history;
    }

    public void setHistory(List<History> history) {
        this.history = history;
    }

    @NonNull
    @Override
    public String toString() {
        return App.gson().toJson(this);
    }
}
