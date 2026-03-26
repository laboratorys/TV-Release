package com.fongmi.android.tv.model;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.fongmi.android.tv.Constant;
import com.fongmi.android.tv.R;
import com.fongmi.android.tv.api.EpgParser;
import com.fongmi.android.tv.api.LiveParser;
import com.fongmi.android.tv.api.config.LiveConfig;
import com.fongmi.android.tv.bean.Channel;
import com.fongmi.android.tv.bean.Epg;
import com.fongmi.android.tv.bean.EpgData;
import com.fongmi.android.tv.bean.Group;
import com.fongmi.android.tv.bean.Live;
import com.fongmi.android.tv.bean.Result;
import com.fongmi.android.tv.exception.ExtractException;
import com.fongmi.android.tv.player.Source;
import com.fongmi.android.tv.utils.Formatters;
import com.fongmi.android.tv.utils.Task;
import com.github.catvod.net.OkHttp;
import com.google.common.util.concurrent.FluentFuture;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class LiveViewModel extends ViewModel {

    private final MutableLiveData<Boolean> xml;
    private final MutableLiveData<Result> url;
    private final MutableLiveData<Live> live;
    private final MutableLiveData<Epg> epg;

    private final Map<TaskType, ListenableFuture<?>> futures;
    private final Map<TaskType, AtomicInteger> taskIds;
    private volatile FormatHolder formats;

    public LiveViewModel() {
        this.epg = new MutableLiveData<>();
        this.xml = new MutableLiveData<>();
        this.url = new MutableLiveData<>();
        this.live = new MutableLiveData<>();
        this.futures = new EnumMap<>(TaskType.class);
        this.taskIds = new EnumMap<>(TaskType.class);
        this.formats = new FormatHolder(ZoneId.systemDefault());
        for (TaskType type : TaskType.values()) taskIds.put(type, new AtomicInteger(0));
    }

    public LiveData<Result> url() {
        return url;
    }

    public LiveData<Boolean> xml() {
        return xml;
    }

    public LiveData<Epg> epg() {
        return epg;
    }

    public LiveData<Live> live() {
        return live;
    }

    public ZoneId getZoneId() {
        return formats.zoneId();
    }

    public void getLive(Live item) {
        execute(TaskType.LIVE, () -> {
            LiveParser.start(item.recent());
            setTimeZone(item);
            verify(item);
            return item;
        }, live::postValue, t -> {
            if (t instanceof ExtractException) url.postValue(Result.error(t.getMessage()));
            else live.postValue(new Live());
        });
    }

    public void getXml(Live item) {
        execute(TaskType.XML, () -> item.getEpgXml().stream().anyMatch(url -> parseXml(item, url)), xml::postValue, t -> xml.postValue(false));
    }

    private boolean parseXml(Live item, String url) {
        try {
            EpgParser.start(item, url);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    public void getEpg(Channel item) {
        FormatHolder holder = this.formats;
        String today = holder.formatDate(0);
        execute(TaskType.EPG, () -> {
            for (int offset : new int[]{-1, 0, 1}) fetchEpgDay(item, holder, offset);
            return item.getDataList().stream().filter(e -> e.equal(today)).findFirst().orElseGet(Epg::new).selected();
        }, epg::postValue, t -> epg.postValue(new Epg()));
    }

    private void fetchEpgDay(Channel item, FormatHolder holder, int offset) {
        String date = holder.formatDate(offset);
        String url = item.getEpg().replace("{date}", date);
        boolean need = url.startsWith("http") && item.getDataList().stream().noneMatch(e -> e.equal(date));
        if (need) item.setData(Epg.objectFrom(OkHttp.string(url), item.getTvgId(), holder.zoneId));
    }

    public void getUrl(Channel item) {
        execute(TaskType.URL, () -> {
            Source.get().stop();
            Result result = item.result();
            result.setUrl(Source.get().fetch(result));
            return result;
        }, url::postValue, this::handleUrlError);
    }

    public void getUrl(Channel item, EpgData data) {
        execute(TaskType.URL, () -> {
            Source.get().stop();
            Result result = item.result();
            if (item.isRtsp()) result.getHeader().put("rtsp_range", data.getRange());
            result.setUrl(item.getCatchup().format(Source.get().fetch(result), data));
            return result;
        }, url::postValue, this::handleUrlError);
    }

    private void handleUrlError(Throwable t) {
        if (t instanceof ExtractException) url.postValue(Result.error(t.getMessage()));
        else url.postValue(new Result());
    }

    private void setTimeZone(Live live) {
        try {
            ZoneId zoneId = live.getTimeZone().isEmpty() ? ZoneId.systemDefault() : ZoneId.of(live.getTimeZone());
            this.formats = new FormatHolder(zoneId);
        } catch (Exception ignored) {
        }
    }

    private void verify(Live item) {
        item.getGroups().removeIf(Group::isEmpty);
        if (item.getGroups().isEmpty() || item.getGroups().get(0).isKeep()) return;
        item.getGroups().add(0, Group.create(R.string.keep));
        LiveConfig.get().applyKeepsToGroups(item.getGroups());
    }

    private <T> void execute(TaskType type, Callable<T> callable, Consumer<T> onSuccess, Consumer<Throwable> onError) {
        AtomicInteger taskId = taskIds.get(type);
        int currentId = taskId.incrementAndGet();
        ListenableFuture<?> old = futures.get(type);
        if (old != null) old.cancel(true);
        FluentFuture<T> future = FluentFuture.from(Task.executor().submit(callable)).withTimeout(type.timeout, TimeUnit.MILLISECONDS, Task.scheduler());
        futures.put(type, future);
        future.addCallback(Task.callback(
                result -> {
                    if (taskId.get() == currentId) onSuccess.accept(result);
                },
                error -> {
                    if (error instanceof CancellationException) return;
                    if (taskId.get() != currentId) return;
                    onError.accept(error);
                }
        ), MoreExecutors.directExecutor());
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        futures.values().forEach(future -> future.cancel(true));
    }

    private enum TaskType {

        LIVE(Constant.TIMEOUT_LIVE),
        EPG(Constant.TIMEOUT_EPG),
        XML(Constant.TIMEOUT_XML),
        URL(Constant.TIMEOUT_PARSE_LIVE);

        final long timeout;

        TaskType(long timeout) {
            this.timeout = timeout;
        }
    }

    private record FormatHolder(ZoneId zoneId) {

        String formatDate(int offsetDays) {
            return LocalDate.now(zoneId).plusDays(offsetDays).format(Formatters.DATE);
        }
    }
}