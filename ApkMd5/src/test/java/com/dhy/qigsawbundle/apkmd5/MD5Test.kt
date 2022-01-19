package com.dhy.qigsawbundle.apkmd5

import org.junit.Assert
import org.junit.Test
import java.io.File
import java.security.MessageDigest
import java.util.*

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

    @Test
    fun md5SHA() {
        val c1 = File(projectRootDir, "tests/test-md5-so-c1.apk")
        val data = c1.readZipEntryBytes("lib/x86/libhello-jni.so")
        val digest = MessageDigest.getInstance("SHA")
        digest.update(data)
        val hex = String(Base64.getEncoder().encode(digest.digest()))
        Assert.assertEquals("B4llqtwOcNTHuXsnHGWQYBTcrq8=", hex)
    }
}

val projectRootDir: File by lazy {
    File(File("").absolutePath).parentFile
}