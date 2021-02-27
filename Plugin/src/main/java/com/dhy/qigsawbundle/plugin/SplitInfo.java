package com.dhy.qigsawbundle.plugin;


import java.util.List;
import java.util.Set;

public class SplitInfo {
    public String splitName;
    public boolean builtIn;
    public boolean onDemand;
    public String applicationName;
    public String version;
    public int minSdkVersion;
    public int dexNumber;
    public Set<String> dependencies;
    public Set<String> workProcesses;
    public List<SplitApkData> apkData;
    public List<SplitLibData> libData;


    public static final class SplitApkData {
        public String abi;
        public String url;
        public String md5;
        public long size;
    }

    public static final class SplitLibData {
        public String abi;
        public List<Lib> jniLibs;

        public static final class Lib {
            public String name;
            public String md5;
            public long size;
        }
    }
}

