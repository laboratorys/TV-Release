package com.fongmi.android.tv.bean;

import androidx.annotation.NonNull;

import com.fongmi.android.tv.App;
import com.fongmi.android.tv.db.AppDatabase;
import com.google.gson.annotations.SerializedName;

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
        backup.setKeep(AppDatabase.get().getKeepDao().findAll());
        backup.setLive(AppDatabase.get().getLiveDao().findAll());
        backup.setSite(AppDatabase.get().getSiteDao().findAll());
        backup.setConfig(AppDatabase.get().getConfigDao().findAll());
        backup.setHistory(AppDatabase.get().getHistoryDao().findAll());
        return backup;
    }

    public void restore() {
        AppDatabase.get().clearAllTables();
        for (History item : getHistory()) item.save();
        for (Config item : getConfig()) item.save();
        for (Site item : getSite()) item.save();
        for (Live item : getLive()) item.save();
        for (Keep item : getKeep()) item.save();
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
        return site;
    }

    public void setSite(List<Site> site) {
        this.site = site;
    }

    public List<Live> getLive() {
        return live;
    }

    public void setLive(List<Live> live) {
        this.live = live;
    }

    public List<Keep> getKeep() {
        return keep;
    }

    public void setKeep(List<Keep> keep) {
        this.keep = keep;
    }

    public List<Config> getConfig() {
        return config;
    }

    public void setConfig(List<Config> config) {
        this.config = config;
    }

    public List<History> getHistory() {
        return history;
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
