package com.dhy.qigsawbundle.apkmd5

import org.junit.Assert
import org.junit.Test
import java.io.File

class MD5Test {
    @Test
    fun md5TestMaster() {
        val c1 = File(projectRootDir, "tests/test-md5-master-c1.apk")
        val c4 = File(projectRootDir, "tests/test-md5-master-c4.apk")
        val md5 = c4.apkMd5()
        Assert.assertEquals(c1.apkMd5(), md5)
        println(md5)
    }

    @Test
    fun md5TestSO() {
        val c1 = File(projectRootDir, "tests/test-md5-so-c1.apk")
        val c4 = File(projectRootDir, "tests/test-md5-so-c4.apk")
        val md5 = c4.apkMd5()
        Assert.assertEquals(c1.apkMd5(), md5)
        println(md5)
    }
}

val projectRootDir: File by lazy {
    File(File("").absolutePath).parentFile
}