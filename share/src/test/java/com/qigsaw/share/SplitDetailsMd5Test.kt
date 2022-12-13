package com.qigsaw.share

import com.google.gson.Gson
import org.junit.Assert
import org.junit.Test
import java.io.File

class SplitDetailsMd5Test {
    @Test
    fun testMe() {
        val md5 = "550c51b0b4390ee5b8bfbcc2124f6bea"
        val jsonFile = File(projectRootDir, "tests/splits/acom.dhy.qigsaw2test-t0-v1.0@1-$md5.json")
        val json = jsonFile.readText()
        val gson = Gson()
        val details = gson.fromJson(json, SplitDetails::class.java)
        println(details.md5())
        Assert.assertEquals(md5, details.md5())
    }
}

val projectRootDir: File by lazy {
    File(File("").absolutePath).parentFile
}