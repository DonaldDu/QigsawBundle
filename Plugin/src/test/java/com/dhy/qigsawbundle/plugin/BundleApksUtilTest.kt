package com.dhy.qigsawbundle.plugin

import org.junit.Assert
import org.junit.Test
import java.io.File

class BundleApksUtilTest {
    @Test
    fun test() {
        val folder = "D:\\Donald\\Qigsaw2Test\\app\\build\\outputs\\bundle\\debug"
        val apks = File(folder, "app.apks")
        val baseApks = File(folder, "base.apks")
        if (apks.exists() && baseApks.exists()) {
            val bundleOption = BundleOption()
            bundleOption.type = "debug"
            bundleOption.apkFileHost = "http://www.qigsaw.com/"

            BundleApksUtil.bundleApks(bundleOption, apks, baseApks)
        }
    }

    @Test
    fun renameApk() {
        val params: MutableMap<String, String> = mutableMapOf()
        params["appId"] = "acom.dhy.qigsaw2test"
        params["split"] = "base"
        params["abi"] = "master"
        params["versionName"] = "1.2.5"
        params["versionCode"] = "12"
        params["type"] = "debug"
        params["md5"] = "f492337b8cf6fcd4c57e5f9c1650a4a6"

        val apkName = "{appId}-{split}-{abi}-{type}-v{versionName}@{versionCode}-{md5}"
        Assert.assertEquals("acom.dhy.qigsaw2test-base-master-debug-v1.2.5@12-f492337b8cf6fcd4c57e5f9c1650a4a6", apkName.format(params))
        params["appId"] = ""
        Assert.assertEquals("base-master-debug-v1.2.5@12-f492337b8cf6fcd4c57e5f9c1650a4a6", apkName.format(params))
        params["md5"] = ""
        Assert.assertEquals("base-master-debug-v1.2.5@12", apkName.format(params))
    }
}