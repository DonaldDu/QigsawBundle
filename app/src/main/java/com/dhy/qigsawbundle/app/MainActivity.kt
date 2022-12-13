package com.dhy.qigsawbundle.app

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.qigsaw.share.SplitDetails
import com.qigsaw.share.md5

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<View>(R.id.btTest).setOnClickListener {
            testMe()
        }
    }

    private fun testMe() {
        val md5 = "550c51b0b4390ee5b8bfbcc2124f6bea"
        val json = assets.open("acom.dhy.qigsaw2test-t0-v1.0@1-$md5.json").readBytes().decodeToString()
        val gson = Gson()
        val details = gson.fromJson(json, SplitDetails::class.java)
        println(details.md5())
        assert(md5 == details.md5())
    }
}