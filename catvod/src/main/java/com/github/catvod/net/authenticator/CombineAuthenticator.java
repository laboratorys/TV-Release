package com.github.catvod.net.authenticator;

import com.github.catvod.utils.Util;
import com.google.common.net.HttpHeaders;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import okhttp3.Authenticator;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

public class CombineAuthenticator implements Authenticator {

    private final Map<String, String> authCache;

    public CombineAuthenticator() {
        authCache = new ConcurrentHashMap<>();
    }

    @Override
    public Request authenticate(Route route, Response response) {
        if (response.code() != 401) return null;

        HttpUrl url = response.request().url();
        String userInfo = url.uri().getUserInfo();
        String host = response.request().url().host();
        String authHeader = response.header(HttpHeaders.WWW_AUTHENTICATE);

        if (authCache.containsKey(host)) {
            return response.request().newBuilder().header(HttpHeaders.AUTHORIZATION, authCache.get(host)).build();
        }

        if (userInfo == null) {
            return null;
        }

        if (authHeader.startsWith("Digest")) {
            Map<String, String> params = parseAuthHeader(authHeader.substring(7));
            String[] parts = userInfo.split(":", 2);
            String username = parts[0];
            String password = parts[1];
            String uri = url.encodedPath();
            String qop = params.get("qop");
            String realm = params.get("realm");
            String nonce = params.get("nonce");
            String opaque = params.get("opaque");
            String method = response.request().method();
            String digestAuth = getDigestAuth(username, password, realm, nonce, qop, opaque, method, uri);
            authCache.put(host, digestAuth);
            return response.request().newBuilder().header(HttpHeaders.AUTHORIZATION, digestAuth).build();
        } else {
            String basicAuth = Util.basic(userInfo);
            authCache.put(host, basicAuth);
            return response.request().newBuilder().header(HttpHeaders.AUTHORIZATION, basicAuth).build();
        }
    }

    private String getDigestAuth(String username, String password, String realm, String nonce, String qop, String opaque, String method, String uri) {
        String nc = "00000001";
        String hash2 = Util.md5(method + ":" + uri);
        String cnonce = Long.toHexString(System.currentTimeMillis());
        String hash1 = Util.md5(username + ":" + realm + ":" + password);
        String response = Util.md5(hash1 + ":" + nonce + ":" + nc + ":" + cnonce + ":" + (qop != null ? qop : "") + ":" + hash2);
        StringBuilder authValue = new StringBuilder("Digest ");
        authValue.append("username=\"").append(username).append("\", ");
        if (realm != null) authValue.append("realm=\"").append(realm).append("\", ");
        if (nonce != null) authValue.append("nonce=\"").append(nonce).append("\", ");
        authValue.append("uri=\"").append(uri).append("\", ");
        authValue.append("cnonce=\"").append(cnonce).append("\", ");
        authValue.append("nc=").append(nc).append(", ");
        if (qop != null) authValue.append("qop=\"").append(qop).append("\", ");
        authValue.append("response=\"").append(response).append("\"");
        if (opaque != null) authValue.append(", opaque=\"").append(opaque).append("\"");
        return authValue.toString();
    }

    private Map<String, String> parseAuthHeader(String header) {
        Map<String, String> params = new HashMap<>();
        for (String part : header.split(",\\s*")) {
            String[] kv = part.split("=", 2);
            if (kv.length == 2) params.put(kv[0].trim(), kv[1].trim().replace("\"", ""));
        }
        return params;
    }
}
