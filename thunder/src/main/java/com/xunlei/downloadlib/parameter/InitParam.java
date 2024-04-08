package com.xunlei.downloadlib.parameter;

public class InitParam {

    public String mGuid;
    public String mAppVersion;
    public String mLogSavePath;
    public String mStatSavePath;
    public String mStatCfgSavePath;
    public int mPermissionLevel;

    public InitParam(String path) {
        this.mGuid = "000000000000000000";
        this.mAppVersion = "1.18.2";
        this.mLogSavePath = path;
        this.mStatSavePath = path;
        this.mStatCfgSavePath = path;
        this.mPermissionLevel = 3;
    }
}
