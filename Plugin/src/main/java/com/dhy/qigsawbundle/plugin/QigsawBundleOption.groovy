package com.dhy.qigsawbundle.plugin

class QigsawBundleOption extends BundleOption {
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

    /**
     * aabFolder = buildDir+"/outputs/bundle/${baseVariant.name}"
     * */
    Closure<String> aabFolder = { baseVariant ->
        return "outputs/bundle/${baseVariant.name}"
    }
}