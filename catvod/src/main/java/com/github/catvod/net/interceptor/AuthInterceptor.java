package com.github.catvod.net.interceptor;


import androidx.annotation.NonNull;

import com.github.catvod.utils.Util;
import com.google.common.net.HttpHeaders;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {

    private final Map<String, String> authCache;

    public AuthInterceptor() {
        authCache = new ConcurrentHashMap<>();
    }

    @NonNull
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        String host = request.url().host();
        String user = request.url().uri().getUserInfo();

        if (authCache.containsKey(host)) {
            return chain.proceed(request.newBuilder().header(HttpHeaders.AUTHORIZATION, authCache.get(host)).build());
        }

        Response response = chain.proceed(request);
        if (response.code() != 401 || user == null) {
            return response;
        }

        String authValue;
        String authHeader = response.header(HttpHeaders.WWW_AUTHENTICATE);

        if (authHeader != null && authHeader.startsWith("Digest")) {
            authValue = Util.digest(authHeader, request);
        } else {
            authValue = Util.basic(user);
        }

        response.close();
        authCache.put(host, authValue);
        return chain.proceed(request.newBuilder().header(HttpHeaders.AUTHORIZATION, authValue).build());
    }
}
