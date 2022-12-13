package com.qigsaw.share;


import com.google.gson.annotations.SerializedName;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

public class SplitInfo implements SplitHashItem<SplitInfo.ApkData> {
    public String splitName;
    public boolean builtIn;
    public boolean onDemand;
    public String applicationName;
    public String version;
    public int minSdkVersion;
    public int dexNumber;
    public Set<String> dependencies;
    public Set<String> workProcesses;
    @SerializedName("apkData")
    public List<ApkData> apkDataList;
    @SerializedName("libData")
    public List<LibData> libDataList;

    @NotNull
    @Override
    public String getSplitName() {
        return splitName;
    }

    @NotNull
    @Override
    public List<ApkData> getApks() {
        return apkDataList;
    }

    public static final class ApkData implements SplitHashItem.SplitApkInfo {
        public String abi;
        public String url;
        public String md5;
        public long size;

        @NotNull
        @Override
        public String getAbi() {
            return abi;
        }

        @NotNull
        @Override
        public String getMd5() {
            return md5;
        }
    }

    public static final class LibData {
        public String abi;
        public List<Lib> jniLibs;

        public static final class Lib {
            public String name;
            public String md5;
            public long size;
        }
    }
}