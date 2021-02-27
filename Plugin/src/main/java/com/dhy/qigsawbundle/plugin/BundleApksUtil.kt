package com.dhy.qigsawbundle.plugin

import com.dhy.qigsawbundle.apkmd5.apkMd5
import com.dhy.qigsawbundle.apkmd5.md5
import com.google.gson.Gson
import net.dongliu.apk.parser.ApkFile
import net.dongliu.apk.parser.bean.ApkMeta
import org.apache.commons.io.FileUtils
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.util.zip.ZipFile

object BundleApksUtil {
    private var splitVersionCode = 0L

    @JvmStatic
    fun bundleApks(apkFileHost: String, apks: File, keepLanguageConfigApks: Boolean, copyToDirectory: String?) {
        splitVersionCode = System.currentTimeMillis() / 1000
        val splits = File(apks.parent, "splits")
        apks.unzipSplits(splits)
        val app = getBaseApkMeta(splits)
        clearSplitApks(keepLanguageConfigApks, splits)
        genSplitInfo(apkFileHost.replace("/+$".toRegex(), ""), app.versionName, splits, File(splits, app.versionInfoJson()))
        if (splits.exists() && copyToDirectory != null) {
            copySplits(splits, File(copyToDirectory))
        }
    }

    private fun copySplits(sourceFolder: File, destinationFolder: File) {
        sourceFolder.listFiles()?.forEach {
            FileUtils.copyFile(it, File(destinationFolder, it.name))
        }
    }

    private fun ApkMeta.versionInfoJson(): String {
        return "${packageName}-v${versionName}@${versionCode}.json"
    }

    private fun getBaseApkMeta(splits: File): ApkMeta {
        val base = File(splits, "base-master.apk")
        return ApkFile(base).meta
    }

    private fun clearSplitApks(keepLanguageConfigApks: Boolean, splits: File) {
        splits.listFiles()?.forEach {
            if (it.isEmptyDpiApk()) it.delete()
            if (it.name.endsWith("_2.apk")) it.delete()
            if (it.name.startsWith("base-")) it.delete()
            if (!keepLanguageConfigApks && it.name.contains("-[a-z]{2}\\.apk".toRegex())) it.delete()
        }
    }

    private fun genSplitInfo(apkFileHost: String, appVersionName: String, splits: File, infoJson: File) {
        val details = SplitDetails()
        details.qigsawId = appVersionName
        details.appVersionName = appVersionName
        details.splits = mutableListOf()

        splits.listFiles()?.forEach {
            if (it.name.endsWith(".apk")) {
                showInfo(apkFileHost, details, it)
            }
        }
        details.updateSplits = details.splits.map { it.splitName }
        if (infoJson.exists()) infoJson.exists()
        FileUtils.writeByteArrayToFile(infoJson, gson.toJson(details).toByteArray())
    }

    private fun showInfo(apkFileHost: String, details: SplitDetails, apk: File) {
        val apkFile = ApkFile(apk)
        val info = apkFile.meta
        val splitInfo = details.splits.find { it.splitName == info.splitName } ?: SplitInfo()
        if (splitInfo.splitName == null) {//new
            details.splits.add(splitInfo)
            splitInfo.splitName = info.splitName
            splitInfo.builtIn = false
            splitInfo.onDemand = true
            splitInfo.apkData = mutableListOf()
            //用bundletool打包出来的，Split版本信息都是base的，所以用打包时间作为Split版本号，以保证更新。
            //运行时会根据md5来判断是否更新，如果未更新，本地为使用旧版本号（自动降低特定组件版本号）。
            splitInfo.version = splitVersionCode.toString()
        }
        val moduleVersion = apk.moduleVersion(info.splitName)
        if (moduleVersion != null) splitInfo.version = moduleVersion
        splitInfo.initUseSplits(apkFile.manifestXml)
        splitInfo.dexNumber += apk.dexNumber()
        if (info.minSdkVersion != null) splitInfo.minSdkVersion = info.minSdkVersion.toInt()

        val splitApkData = SplitInfo.SplitApkData()
        splitInfo.apkData.add(splitApkData)
        splitApkData.size = apk.length()
        splitApkData.md5 = apk.apkMd5()
        splitApkData.abi = info.abi
        if (info.isSO()) {
            if (splitInfo.libData == null) splitInfo.libData = mutableListOf()
            val splitLibData = apk.splitLibData()
            if (splitLibData != null) splitInfo.libData.add(splitLibData)
        }

        val newApkName = apk.newName(splitInfo.version, splitApkData.md5)
        splitApkData.url = "$apkFileHost/$newApkName"
        apk.renameTo(File(apk.parent, newApkName))
    }

    private fun SplitInfo.initUseSplits(manifestXml: String) {
        val useSplits = manifestXml.parseUseSplitsFromManifestXml()
        if (useSplits.isNotEmpty()) {
            if (dependencies == null) dependencies = mutableSetOf()
            dependencies.addAll(useSplits)
        }
    }

    private fun File.newName(version: String, md5: String): String {
        val v = if (version.contains("@")) {
            "-v${version}-${md5}.apk"
        } else "-${md5}.apk"
        return name.replace(".apk", v).replace('_', '-')
    }


    private fun ApkMeta.isSO(): Boolean {
        return split.contains(".")
    }

    private val ApkFile.meta: ApkMeta
        get() {
            val data = apkMeta
            close()
            return data
        }

    private val gson = Gson()

    private fun File.isEmptyDpiApk(): Boolean {
        var empty = false
        if (name.contains("-\\w+dpi.apk".toRegex())) {
            val apk = ZipFile(this)
            empty = apk.getEntry("res") == null
            apk.close()
        }
        return empty
    }

    private fun File.dexNumber(): Int {
        val zip = ZipFile(this)
        val files = zip.entries()
        var count = 0
        while (files.hasMoreElements()) {
            val e = files.nextElement()
            if (!e.name.contains("/") && e.name.endsWith(".dex")) count++
        }
        zip.close()
        return count
    }

    private fun File.splitLibData(): SplitInfo.SplitLibData? {
        val splitLibData = SplitInfo.SplitLibData()
        splitLibData.jniLibs = mutableListOf()
        val zip = ZipFile(this)
        val files = zip.entries()
        while (files.hasMoreElements()) {
            val e = files.nextElement()
            if (e.name.startsWith("lib/") && e.name.endsWith(".so")) {
                val lib = SplitInfo.SplitLibData.Lib()
                if (splitLibData.abi == null) splitLibData.abi = e.name.abi
                lib.name = e.name.substring(e.name.lastIndexOf('/') + 1)
                lib.md5 = zip.getInputStream(e).md5()
                lib.size = e.size
                splitLibData.jniLibs.add(lib)
            }
        }
        zip.close()
        return if (splitLibData.jniLibs.isNotEmpty()) splitLibData else null
    }

    private val String.abi: String
        get() {
            return substring(indexOf('/') + 1, lastIndexOf('/'))
        }

    fun runCommand(cmd: String) {
        val process = Runtime.getRuntime().exec(cmd)
        val code = process.waitFor()
        val inputStream = if (code == 0) process.inputStream else process.errorStream
        val reader = BufferedReader(InputStreamReader(inputStream))
        val sb = StringBuilder()
        var line: String? = reader.readLine()
        while (line != null) {
            if (code == 0) println(line)
            else sb.append(line).append("\n")
            line = reader.readLine()
        }
        reader.close()
        inputStream.close()
        if (code != 0) throw Exception(sb.toString())
    }
}

fun File.readEntry(name: String): String? {
    val file = ZipFile(this)
    val e = file.getEntry(name)
    if (e == null) {
        file.close()
        return null
    }
    val inputStream = file.getInputStream(e)
    val text = inputStream.readBytes().toString(Charset.defaultCharset())
    inputStream.close()
    file.close()
    return text
}

fun File.moduleVersion(splitName: String): String? {
    val name = "META-INF/module_version_$splitName"
    return readEntry(name)//1.1@2
}

fun File.unzipSplits(folder: File) {
    folder.deleteAll()
    val zip = ZipFile(this)
    val files = zip.entries()
    while (files.hasMoreElements()) {
        val e = files.nextElement()
        if (e.name.endsWith(".apk")) {
            val inputStream = zip.getInputStream(e)
            val apk = File(folder, e.name.substring(e.name.indexOf('/') + 1))
            FileUtils.copyInputStreamToFile(inputStream, apk)
            inputStream.close()
        }
    }
    zip.close()
}

fun File.deleteAll() {
    if (isDirectory) {
        listFiles()?.forEach {
            if (it.isDirectory) it.deleteAll()
            else it.delete()
        }
    }
    delete()
}

fun String.parseUseSplitsFromManifestXml(): Set<String> {
    val splits: MutableSet<String> = mutableSetOf()
    val usesSplit = "<uses-split android:name=\"([^\"]+)".toRegex()
    var result = usesSplit.find(this)
    while (result != null) {
        splits.add(result.groupValues[1])
        result = result.next()
    }
    return splits
}