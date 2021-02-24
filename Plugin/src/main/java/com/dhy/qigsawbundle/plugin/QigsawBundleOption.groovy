package com.dhy.qigsawbundle.plugin

class QigsawBundleOption {
    boolean log
    /**
     * [
     * '--ks=5658.keystore',
     * '--ks-pass=pass:android',
     * '--ks-key-alias=androiddebugkey',
     * '--key-pass=pass:android'
     * ]
     * */
    List<String> options
    boolean keepLanguageConfigApks = false
    /**
     *{host}/{apkName}-{md5}.apk -> "http://192.168.80.15/dynamicfeature-master-v1.1@2-2d8797caaf8f74426ac071d73e75b93a.apk"
     * */
    String apkFileHost
    String copyToDirectory
    /**
     * aabFolder = buildDir+"/outputs/bundle/${baseVariant.name}"
     * */
    Closure<String> aabFolder = { baseVariant ->
        return "outputs/bundle/${baseVariant.name}"
    }
}