package com.fongmi.android.tv.server;

import com.fongmi.android.tv.player.Players;
import com.github.catvod.Proxy;
import com.github.catvod.utils.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Server {

    private final List<Nano> items;
    private Players player;

    private static class Loader {
        static volatile Server INSTANCE = new Server();
    }

    public static Server get() {
        return Loader.INSTANCE;
    }

    public Server() {
        this.items = new ArrayList<>();
    }

    public Players getPlayer() {
        return player;
    }

    public void setPlayer(Players player) {
        this.player = player;
    }

    public String getAddress() {
        return getAddress(false);
    }

    public String getAddress(int tab) {
        return getAddress(false) + "?tab=" + tab;
    }

    public String getAddress(String path) {
        return getAddress(true) + path;
    }

    public String getAddress(boolean local) {
        return "http://" + (local ? "127.0.0.1" : Util.getIp()) + ":" + getPort();
    }

    public int getPort() {
        for (Nano item : items) if (item.getListeningPort() == 8964) return 8964;
        for (Nano item : items) if (item.getListeningPort() == 9978) return 9978;
        return -1;
    }

    public void start() {
        if (!items.isEmpty()) return;
        for (int port : Arrays.asList(9978, 8964)) {
            try {
                Nano nano = new Nano(port);
                nano.start(500, false);
                Proxy.set(port);
                items.add(nano);
            } catch (Throwable ignored) {
            }
        }
    }

    public void stop() {
        for (Nano item : items) item.stop();
    }
}
