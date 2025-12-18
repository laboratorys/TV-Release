package com.github.catvod.net;

import androidx.annotation.NonNull;

import com.github.catvod.bean.Doh;
import com.github.catvod.utils.Util;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import kotlin._Assertions;
import okhttp3.Dns;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.dnsoverhttps.DnsOverHttps;

public class OkDns implements Dns {

    private final ConcurrentHashMap<String, String> map;
    private DnsOverHttps doh;

    public OkDns() {
        this.map = new ConcurrentHashMap<>();
    }

    public void setDoh(Doh item) {
        if (item.getUrl().isEmpty()) return;
        this.doh = new DnsOverHttps.Builder().client(new OkHttpClient()).url(HttpUrl.get(item.getUrl())).bootstrapDnsHosts(item.getHosts()).build();
    }

    public void clear() {
        map.clear();
    }

    public void addAll(List<String> hosts) {
        hosts.stream().map(host -> host.split("=", 2)).filter(splits -> splits.length == 2).forEach(splits -> map.put(splits[0], splits[1]));
    }

    @NonNull
    @Override
    public List<InetAddress> lookup(@NonNull String hostname) throws UnknownHostException {
        for (Map.Entry<String, String> entry : map.entrySet()) if (Util.containOrMatch(hostname, entry.getKey())) hostname = entry.getValue();
        return (doh != null ? doh : Dns.SYSTEM).lookup(hostname);
    }
}
