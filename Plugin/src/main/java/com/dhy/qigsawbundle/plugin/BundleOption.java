package com.dhy.qigsawbundle.plugin;

import java.util.Map;

public class BundleOption {
    /**
     * {host}/{apkName}.apk -> "http://192.168.80.15/dynamicfeature-master-v1.1@2.apk"
     */
    public String apkFileHost;
    public String fileNameFormat = "{appId}-{split}-{abi}-{type}-v{version}-{md5}";
    public boolean keepLanguageConfigApks = false;

    transient String type;
    public String debugType;
    public String releaseType;
    public String copyToDirectory;

    transient Map<String, String> fileNameParams;

    public String format() {
        return BundleApksUtilKt.format(fileNameFormat, fileNameParams);
    }
}
