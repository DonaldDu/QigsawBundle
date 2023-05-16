package com.qigsaw.share

import com.google.gson.Gson
import org.junit.Assert
import org.junit.Test
import java.io.File

class SplitDetailsMd5Test {
    @Test
    fun testMe() {
        val jsonFile = File(projectRootDir, "tests/splits").listFiles()?.find { it.name.endsWith(".json") }
        if (jsonFile?.exists() == true) {
            val json = jsonFile.readText()
            val gson = Gson()
            val details = gson.fromJson(json, SplitDetails::class.java)
            println(details.md5())
            val md5 = ("[a-zA-Z0-9]{32}").toRegex().find(jsonFile.name)?.value
            Assert.assertEquals(md5, details.md5())
        }
    }
}

val projectRootDir: File by lazy {
    File(File("").absolutePath).parentFile
}