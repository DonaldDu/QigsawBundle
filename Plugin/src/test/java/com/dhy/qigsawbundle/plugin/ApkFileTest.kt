package com.dhy.qigsawbundle.plugin

import net.dongliu.apk.parser.ApkFile
import org.junit.Test
import java.io.File

class ApkFileTest {

    @Test
    fun showUseSplit() {
        val apk = File("C:\\datas\\Qigsaw2Test\\app\\build\\outputs\\bundle\\debug\\splits\\assets-master-v1.0@2-b44f5457e6d0782ece2f10dcaf733b52.apk")
        if (apk.exists()) {
            val apkFile = ApkFile(apk)
            println(apkFile.manifestXml.parseUseSplitsFromManifestXml())
            apkFile.close()
        }
    }

    @Test
    fun parseModuleVersion() {
        val apk = File("C:\\datas\\Qigsaw2Test\\java\\build\\outputs\\apk\\debug\\java-debug.apk")
        if (apk.exists()) {
            val apkFile = ApkFile(apk)
            println(apkFile.parseModuleVersion())
            apkFile.close()
        }
    }
}