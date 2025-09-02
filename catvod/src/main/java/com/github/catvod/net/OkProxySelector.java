package com.github.catvod.net;

import com.github.catvod.bean.Proxy;
import com.github.catvod.utils.Util;

import java.io.IOException;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class OkProxySelector extends ProxySelector {

    private final List<Proxy> proxy;
    private final ProxySelector system;

    public OkProxySelector() {
        proxy = new ArrayList<>();
        system = ProxySelector.getDefault();
    }

    public synchronized void addAll(List<Proxy> items) {
        for (Proxy item : items) item.init();
        proxy.addAll(items);
        Proxy.sort(proxy);
    }

    public void clear() {
        proxy.clear();
    }

    @Override
    public List<java.net.Proxy> select(URI uri) {
        if (proxy.isEmpty() || uri.getHost() == null || "127.0.0.1".equals(uri.getHost())) return system.select(uri);
        for (Proxy item : proxy) for (String host : item.getHosts()) if (Util.containOrMatch(uri.getHost(), host)) return item.getProxies().isEmpty() ? system.select(uri) : item.getProxies();
        return system.select(uri);
    }

    @Override
    public void connectFailed(URI uri, SocketAddress socketAddress, IOException e) {
        system.connectFailed(uri, socketAddress, e);
    }
}
