package com.dhy.qigsawbundle.plugin;

import java.util.Map;

public class BundleOption {
    public String bundleTool = "bundletool.bat";
    /**
     * {host}/{apkName}.apk -> "http://192.168.80.15/dynamicfeature-master-v1.1@2.apk"
     */
    public String apkFileHost = "http://www.apk.com";
    public String fileNameFormat = "{appId}-{split}-{abi}-{type}-v{version}-{md5}";
    public boolean keepLanguageConfigApks = false;

    transient String type;
    public String debugType = "debug";
    public String releaseType = "release";
    /**
     * JAR file or className, invoke with "--uploadBaseApk {true/false} --dir $splitsFolder --release {true:release, false:debug}"
     */
    public Object publishTool;
    /**
     * uploadBaseApk or not
     */
    public boolean uploadBaseApk = true;
    transient boolean publish;
    public String copyToDirectory;

    transient Map<String, String> fileNameParams;

    public String format() {
        return BundleApksUtilKt.format(fileNameFormat, fileNameParams);
    }

    public boolean isRelease() {
        return type != null && type.equals(releaseType);
    }
}
