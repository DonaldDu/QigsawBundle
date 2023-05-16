package com.dhy.qigsawbundle.plugin

import com.google.gson.Gson
import com.qigsaw.share.SplitDetails
import com.qigsaw.share.md5
import org.junit.Assert
import org.junit.Test
import java.io.File

class ExtTest {
    @Test
    fun toCmdParams() {
        val params: MutableMap<String, Any?> = mutableMapOf()
        params["param1"] = "dir"
        params["param2"] = true
        params["param3"] = false
        params["param4"] = null
        params["param5"] = "/c 2/a"
        params["param6"] = "\"/c 3/a\""
        val cmd = params.toCmdString("--")
        val kvSplit = "\\s+".toRegex()
        val kvPlaceHolder = "_*_"
        params["param5"] = "\"/c 2/a\""//update for compare
        cmd.trim().split("--").forEach {
            val item = it.trim().replaceFirst(kvSplit, kvPlaceHolder)
            if (item.isNotEmpty()) {
                val kv = item.split(kvPlaceHolder)
                val k = kv.first()
                val v = if (kv.size == 2) kv.last() else null
                Assert.assertEquals(params[k]?.toString(), v)
                params.remove(k)
            }
        }
        Assert.assertTrue(params.isEmpty())
    }

    @Test
    fun splitDetailsMd5() {
        val folder = File(projectRootDir, "tests/splits")
        val jsonFile = folder.listFiles()!!.find { it.name.endsWith(".json") }!!
        val json = jsonFile.readText()
        val details = Gson().fromJson(json, SplitDetails::class.java)
        println(details.md5())
    }
}