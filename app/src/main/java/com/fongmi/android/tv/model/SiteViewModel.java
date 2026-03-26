package com.fongmi.android.tv.model;

import android.text.TextUtils;

import androidx.collection.ArrayMap;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.fongmi.android.tv.App;
import com.fongmi.android.tv.Constant;
import com.fongmi.android.tv.R;
import com.fongmi.android.tv.api.config.VodConfig;
import com.fongmi.android.tv.bean.Class;
import com.fongmi.android.tv.bean.Result;
import com.fongmi.android.tv.bean.Site;
import com.fongmi.android.tv.bean.Vod;
import com.fongmi.android.tv.exception.ExtractException;
import com.fongmi.android.tv.player.Source;
import com.fongmi.android.tv.utils.ResUtil;
import com.fongmi.android.tv.utils.Sniffer;
import com.fongmi.android.tv.utils.Task;
import com.github.catvod.crawler.Spider;
import com.github.catvod.crawler.SpiderDebug;
import com.github.catvod.net.OkHttp;
import com.github.catvod.utils.Prefers;
import com.github.catvod.utils.Util;
import com.google.common.util.concurrent.FluentFuture;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.Call;
import okhttp3.Response;

public class SiteViewModel extends ViewModel {

    private final MutableLiveData<Result> result;
    private final MutableLiveData<Result> player;
    private final MutableLiveData<Result> search;
    private final MutableLiveData<Result> action;

    private final Map<TaskType, ListenableFuture<?>> futures;
    private final Map<TaskType, AtomicInteger> taskIds;
    private final List<Future<?>> searchFuture;
    private final AtomicInteger searchEpoch;

    public SiteViewModel() {
        result = new MutableLiveData<>();
        player = new MutableLiveData<>();
        search = new MutableLiveData<>();
        action = new MutableLiveData<>();
        searchEpoch = new AtomicInteger(0);
        searchFuture = new CopyOnWriteArrayList<>();
        futures = new EnumMap<>(TaskType.class);
        taskIds = new EnumMap<>(TaskType.class);
        for (TaskType type : TaskType.values()) taskIds.put(type, new AtomicInteger(0));
    }

    public LiveData<Result> getResult() {
        return result;
    }

    public LiveData<Result> getPlayer() {
        return player;
    }

    public LiveData<Result> getSearch() {
        return search;
    }

    public LiveData<Result> getAction() {
        return action;
    }

    public SiteViewModel init() {
        search.setValue(null);
        result.setValue(null);
        player.setValue(null);
        action.setValue(null);
        return this;
    }

    public void homeContent() {
        execute(TaskType.RESULT, result, () -> {
            Site site = VodConfig.get().getHome();
            if (site.getType() == 3) {
                Spider spider = site.recent().spider();
                boolean crash = Prefers.getBoolean("crash");
                String homeContent = crash ? "" : spider.homeContent(true);
                String homeVideoContent = crash ? "" : spider.homeVideoContent();
                Prefers.put("crash", false);
                SpiderDebug.log("home", homeContent);
                SpiderDebug.log("homeVideo", homeVideoContent);
                Result res = Result.fromJson(homeContent);
                List<Vod> list = Result.fromJson(homeVideoContent).getList();
                if (!list.isEmpty()) res.setList(list);
                setTypes(site, res);
                return res;
            } else if (site.getType() == 4) {
                ArrayMap<String, String> params = new ArrayMap<>();
                params.put("filter", "true");
                String homeContent = call(site.fetchExt(), params);
                SpiderDebug.log("home", homeContent);
                Result res = Result.fromJson(homeContent);
                setTypes(site, res);
                return res;
            } else {
                try (Response response = OkHttp.newCall(site.getApi(), site.getHeader()).execute()) {
                    String homeContent = response.body().string();
                    SpiderDebug.log("home", homeContent);
                    Result res = Result.fromType(site.getType(), homeContent);
                    setTypes(site, res);
                    fetchPic(site, res);
                    return res;
                }
            }
        });
    }

    public void categoryContent(String key, String tid, String page, boolean filter, HashMap<String, String> extend) {
        execute(TaskType.RESULT, result, () -> {
            Site site = VodConfig.get().getSite(key);
            SpiderDebug.log("category", "key=%s,tid=%s,page=%s,filter=%s,extend=%s", key, tid, page, filter, extend);
            if (site.getType() == 3) {
                String categoryContent = site.recent().spider().categoryContent(tid, page, filter, extend);
                SpiderDebug.log("category", categoryContent);
                return Result.fromJson(categoryContent);
            } else {
                ArrayMap<String, String> params = new ArrayMap<>();
                if (site.getType() == 1 && !extend.isEmpty()) params.put("f", App.gson().toJson(extend));
                if (site.getType() == 4) params.put("ext", Util.base64(App.gson().toJson(extend), Util.URL_SAFE));
                params.put("ac", site.getType() == 0 ? "videolist" : "detail");
                params.put("t", tid);
                params.put("pg", page);
                String categoryContent = call(site, params);
                SpiderDebug.log("category", categoryContent);
                return Result.fromType(site.getType(), categoryContent);
            }
        });
    }

    public void detailContent(String key, String id) {
        execute(TaskType.RESULT, result, () -> {
            Site site = VodConfig.get().getSite(key);
            SpiderDebug.log("detail", "key=%s,id=%s", key, id);
            if (site.isEmpty() && "push_agent".equals(key)) {
                Vod vod = new Vod();
                vod.setId(id);
                vod.setName(id);
                vod.setPlayUrl(id);
                vod.setPlayFrom(ResUtil.getString(R.string.push));
                vod.setPic(ResUtil.getString(R.string.push_image));
                Source.get().parse(vod.setFlags());
                return Result.vod(vod);
            } else if (site.getType() == 3) {
                String detailContent = site.recent().spider().detailContent(Arrays.asList(id));
                SpiderDebug.log("detail", detailContent);
                Result res = Result.fromJson(detailContent);
                Source.get().parse(res.getVod().setFlags());
                return res;
            } else {
                ArrayMap<String, String> params = new ArrayMap<>();
                params.put("ac", site.getType() == 0 ? "videolist" : "detail");
                params.put("ids", id);
                String detailContent = call(site, params);
                SpiderDebug.log("detail", detailContent);
                Result res = Result.fromType(site.getType(), detailContent);
                Source.get().parse(res.getVod().setFlags());
                return res;
            }
        });
    }

    public void playerContent(String key, String flag, String id) {
        execute(TaskType.PLAYER, player, () -> {
            Source.get().stop();
            Site site = VodConfig.get().getSite(key);
            SpiderDebug.log("player", "key=%s,flag=%s,id=%s", key, flag, id);
            if (site.getType() == 3) {
                String playerContent = site.recent().spider().playerContent(flag, id, VodConfig.get().getFlags());
                SpiderDebug.log("player", playerContent);
                Result res = Result.fromJson(playerContent);
                if (res.getFlag().isEmpty()) res.setFlag(flag);
                res.setUrl(Source.get().fetch(res));
                res.setHeader(site.getHeader());
                res.setKey(key);
                return res;
            } else if (site.getType() == 4) {
                ArrayMap<String, String> params = new ArrayMap<>();
                params.put("play", id);
                params.put("flag", flag);
                String playerContent = call(site, params);
                SpiderDebug.log("player", playerContent);
                Result res = Result.fromJson(playerContent);
                if (res.getFlag().isEmpty()) res.setFlag(flag);
                res.setUrl(Source.get().fetch(res));
                res.setHeader(site.getHeader());
                return res;
            } else if (site.isEmpty() && "push_agent".equals(key)) {
                Result res = new Result();
                res.setUrl(id);
                res.setParse(0);
                res.setFlag(flag);
                res.setUrl(Source.get().fetch(res));
                SpiderDebug.log("player", res.toString());
                return res;
            } else {
                Result res = new Result();
                res.setUrl(id);
                res.setFlag(flag);
                res.setHeader(site.getHeader());
                res.setPlayUrl(site.getPlayUrl());
                res.setParse(Sniffer.isVideoFormat(id) && res.getPlayUrl().isEmpty() ? 0 : 1);
                res.setUrl(Source.get().fetch(res));
                SpiderDebug.log("player", res.toString());
                return res;
            }
        });
    }

    public void searchContent(List<Site> sites, String keyword, boolean quick) {
        int epoch = stopSearch();
        sites.forEach(site -> {
            FluentFuture<Result> future = FluentFuture.from(Task.largeExecutor().submit(SearchTask.create(this, site, keyword, quick))).withTimeout(Constant.TIMEOUT_SEARCH, TimeUnit.MILLISECONDS, Task.scheduler());
            searchFuture.add(future);
            future.addCallback(Task.callback(
                    result -> {
                        if (searchEpoch.get() == epoch) search.postValue(result);
                    },
                    error -> {
                    }
            ), MoreExecutors.directExecutor());
        });
    }

    public void searchContent(Site site, String keyword, boolean quick, String page) {
        execute(TaskType.RESULT, result, SearchTask.create(this, site, keyword, quick, page));
    }

    public void action(String key, String action) {
        execute(TaskType.ACTION, this.action, () -> {
            Site site = VodConfig.get().getSite(key);
            SpiderDebug.log("action", "key=%s,action=%s", key, action);
            if (site.getType() == 3) return Result.fromJson(site.recent().spider().action(action));
            if (site.getType() == 4) return Result.fromJson(OkHttp.string(action));
            return Result.empty();
        });
    }

    String call(Site site, ArrayMap<String, String> params) throws IOException {
        if (!site.getExt().isEmpty()) params.put("extend", site.getExt());
        Call call = site.getExt().length() <= 1000 ? OkHttp.newCall(site.getApi(), site.getHeader(), params) : OkHttp.newCall(site.getApi(), site.getHeader(), OkHttp.toBody(params));
        try (Response response = call.execute()) {
            return response.body().string();
        }
    }

    Result fetchPic(Site site, Result result) throws Exception {
        if (site.getType() > 2 || result.getList().isEmpty() || !result.getVod().getPic().isEmpty()) return result;
        ArrayList<String> ids = new ArrayList<>();
        boolean empty = site.getCategories().isEmpty();
        for (Vod item : result.getList()) if (empty || site.getCategories().contains(item.getTypeName())) ids.add(item.getId());
        if (ids.isEmpty()) return result.clear();
        ArrayMap<String, String> params = new ArrayMap<>();
        params.put("ac", site.getType() == 0 ? "videolist" : "detail");
        params.put("ids", TextUtils.join(",", ids));
        try (Response response = OkHttp.newCall(site.getApi(), site.getHeader(), params).execute()) {
            result.setList(Result.fromType(site.getType(), response.body().string()).getList());
            return result;
        }
    }

    private void setTypes(Site site, Result result) {
        result.getTypes().stream().filter(type -> result.getFilters().containsKey(type.getTypeId())).forEach(type -> type.setFilters(result.getFilters().get(type.getTypeId())));
        if (site.getCategories().isEmpty()) return;
        Map<String, Class> typeByName = new HashMap<>();
        result.getTypes().forEach(type -> typeByName.put(type.getTypeName(), type));
        List<Class> types = site.getCategories().stream().map(typeByName::get).filter(Objects::nonNull).toList();
        if (!types.isEmpty()) result.setTypes(types);
    }

    private void execute(TaskType type, MutableLiveData<Result> liveData, Callable<Result> callable) {
        AtomicInteger taskId = Objects.requireNonNull(taskIds.get(type));
        int currentId = taskId.incrementAndGet();
        ListenableFuture<?> old = futures.get(type);
        if (old != null) old.cancel(true);
        FluentFuture<Result> future = FluentFuture.from(Task.executor().submit(callable)).withTimeout(Constant.TIMEOUT_VOD, TimeUnit.MILLISECONDS, Task.scheduler());
        futures.put(type, future);
        future.addCallback(Task.callback(
                result -> {
                    if (taskId.get() == currentId) liveData.postValue(result);
                },
                error -> {
                    if (taskId.get() != currentId) return;
                    if (error instanceof CancellationException) return;
                    if (error instanceof ExtractException) liveData.postValue(Result.error(error.getMessage()));
                    else liveData.postValue(Result.empty());
                    error.printStackTrace();
                }
        ), MoreExecutors.directExecutor());
    }

    public int stopSearch() {
        int epoch = searchEpoch.incrementAndGet();
        searchFuture.forEach(future -> future.cancel(true));
        searchFuture.clear();
        return epoch;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        stopSearch();
        futures.values().forEach(future -> future.cancel(true));
    }

    private enum TaskType {RESULT, PLAYER, ACTION}
}
