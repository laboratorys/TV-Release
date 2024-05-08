package com.xunlei.downloadlib.android;

import java.security.SecureRandom;
import java.util.UUID;

public class XLUtil {

    public static String getMAC() {
        return random("ABCDEF0123456", 12).toUpperCase();
    }

    public static String getIMEI() {
        return random("0123456", 15);
    }

    public static String getPeerId() {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        uuid = uuid.substring(0, 12).toUpperCase() + "004V";
        return uuid;
    }

    private static String random(String base, int length) {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) sb.append(base.charAt(random.nextInt(base.length())));
        return sb.toString();
    }

    public static String getGuid() {
        return getIMEI() + "_" + getMAC();
    }
}
