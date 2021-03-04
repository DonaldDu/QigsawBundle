package com.dhy.qigsawbundle.plugin

import org.junit.Test
import java.io.File

class BundleApksUtilTest {
    @Test
    fun test() {
        val folder = "D:\\Donald\\Qigsaw2Test\\app\\build\\outputs\\bundle\\debug"
        val apks = File(folder, "app.apks")
        val baseApks = File(folder, "base.apks")
        if (apks.exists() && baseApks.exists()) {
            val host = "http://www.qigsaw.com/"
            BundleApksUtil.bundleApks(host, apks, baseApks, false, null)
        }
    }
}