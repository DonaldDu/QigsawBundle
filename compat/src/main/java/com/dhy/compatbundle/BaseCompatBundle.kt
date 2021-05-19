package com.dhy.compatbundle

import android.content.Context
import com.dhy.qigsawbundle.apkmd5.DataSizeUnit
import com.dhy.qigsawbundle.apkmd5.apkMd5
import com.dhy.qigsawbundle.apkmd5.md5
import com.google.gson.Gson
import com.iqiyi.android.qigsaw.core.Qigsaw
import com.iqiyi.android.qigsaw.core.common.ICompatBundle
import java.io.File
import java.io.InputStream

abstract class BaseCompatBundle : ICompatBundle {
    override fun readDefaultSplitVersionContent(context: Context, fileName: String): String? {
        val info = context.defaultQigsawSplitVersionFile
        return if (info.exists()) info.readText() else null
    }

    override fun getMD5(file: File): String {
        return if (file.name.endsWith(".apk")) file.apkMd5()
        else file.md5()
    }

    private val bufferSize = DataSizeUnit.MEBIBYTES.toBytes(2).toInt()
    override fun getMD5(inputStream: InputStream): String {
        return if (inputStream.available() <= bufferSize) inputStream.md5()
        else inputStream.md5(ByteArray(bufferSize))
    }

    override fun injectActivityResource(): Boolean = true
    override fun disableComponentInfoManager(): Boolean = true
}

val Context.defaultQigsawSplitVersionFile: File
    get() {
        return File(filesDir, "defaultQigsawSplitVersion.json")
    }

private fun setDefaultSplitVersion(context: Context, file: File) {
    val info = context.defaultQigsawSplitVersionFile
    if (!info.exists()) info.createNewFile()
    info.writeBytes(file.readBytes())
    file.delete()
}

fun Context.updateQigsawSplits(splitVersionInfo: File) {
    val defaultFile = defaultQigsawSplitVersionFile
    if (defaultFile.exists()) {
        clearUpdate(defaultFile, splitVersionInfo)
        val v = System.currentTimeMillis().toString()
        Qigsaw.updateSplits(this, v, splitVersionInfo.absolutePath)
    } else {
        setDefaultSplitVersion(this, splitVersionInfo)
    }
}

/**
 * keep only updated split in SplitDetails.updateSplits[]
 * */
internal fun clearUpdate(oldF: File, newF: File) {
    val gson = Gson()
    val old = gson.fromJson(oldF.readText(), SplitDetails::class.java)
    val new = gson.fromJson(newF.readText(), SplitDetails::class.java)
    val updateSplits: MutableList<String> = mutableListOf()
    new.updateSplits.forEach { splitName ->
        val oldSplit = old.splits.find { it.splitName == splitName }
        val newSplit = new.splits.find { it.splitName == splitName }
        if (oldSplit != null && newSplit != null) {
            val oldMd5 = oldSplit.apkData.map { it.md5 }
            val newMd5 = newSplit.apkData.map { it.md5 }
            if (!oldMd5.containsAll(newMd5)) updateSplits.add(splitName)
        } else updateSplits.add(splitName)
    }
    if (new.updateSplits != updateSplits) {
        new.updateSplits = updateSplits
        newF.writeBytes(gson.toJson(new).toByteArray())
    }
    newF.copyTo(oldF, true)
}