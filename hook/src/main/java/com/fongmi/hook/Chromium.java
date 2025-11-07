package com.fongmi.hook;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Looper;

import java.util.Arrays;
import java.util.Set;

public class Chromium {

    private static final String SYSTEM_SETTINGS_PACKAGE = "com.android.settings";

    private static final Set<String> CHROMIUM_CLASS_NAMES = Set.of(
            "org.chromium.base.buildinfo",
            "org.chromium.base.apkinfo"
    );

    private static final Set<String> CHROMIUM_METHOD_NAMES = Set.of(
            "getall",
            "getpackagename",
            "<init>"
    );

    private static final Set<String> BROWSER_PACKAGES = Set.of(
            "com.android.chrome",
            "com.mi.globalbrowser",
            "com.huawei.browser",
            "com.heytap.browser",
            "com.vivo.browser"
    );

    private static boolean isInstalled(PackageManager pm, String pkg) {
        try {
            pm.getPackageInfo(pkg, PackageManager.GET_META_DATA);
            return true;
        } catch (PackageManager.NameNotFoundException ignored) {
            return false;
        }
    }

    public static boolean find() {
        try {
            return Arrays.stream(Looper.getMainLooper().getThread().getStackTrace()).anyMatch(trace -> CHROMIUM_CLASS_NAMES.contains(trace.getClassName().toLowerCase()) && CHROMIUM_METHOD_NAMES.contains(trace.getMethodName().toLowerCase()));
        } catch (Exception e) {
            return false;
        }
    }

    public static String spoofedPackageName(Context context) {
        PackageManager pm = context.getPackageManager();
        return BROWSER_PACKAGES.stream().filter(packageName -> isInstalled(pm, packageName)).findFirst().orElse(SYSTEM_SETTINGS_PACKAGE);
    }
}
