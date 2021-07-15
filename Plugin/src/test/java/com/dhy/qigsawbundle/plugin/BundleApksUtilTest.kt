package com.dhy.qigsawbundle.plugin

import org.junit.Assert
import org.junit.Test
import java.io.File

class BundleApksUtilTest {
    @Test
    fun test() {
        val folder = File(projectRootDir, "tests")
        val apks = File(folder, "app.apks")
        val baseApks = File(folder, "base.apks")
        if (apks.exists() && baseApks.exists()) {
            val bundleOption = BundleOption()
            bundleOption.type = "0"
            bundleOption.fileNameFormat = "{appId}-{split}-{abi}-t{type}-v{version}-{md5}"
            bundleOption.apkFileHost = "http://www.qigsaw.com/"
//            bundleOption.publishTool = "D:\\Donald\\Qigsaw2Test\\publish\\build\\libs\\publish-all.jar"
//            bundleOption.publishTool = "com.dhy.qigsawbundle.plugin.DemoPublish"
            BundleApksUtil.bundleApks(bundleOption, apks, baseApks)

            val splits = File(folder, "splits")
            Assert.assertEquals(true, splits.listFiles()?.find { it.name.contains("-base-") }?.exists())
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