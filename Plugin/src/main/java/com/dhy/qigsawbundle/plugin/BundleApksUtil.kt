package com.dhy.qigsawbundle.plugin

import com.dhy.qigsawbundle.apkmd5.apkMd5
import com.dhy.qigsawbundle.apkmd5.md5
import com.google.gson.Gson
import net.dongliu.apk.parser.ApkFile
import net.dongliu.apk.parser.bean.ApkMeta
import net.dongliu.apk.parser.parser.ResourceTableParser
import net.dongliu.apk.parser.struct.AndroidConstants
import org.apache.commons.io.FileUtils
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.nio.ByteBuffer
import java.util.zip.ZipFile

object BundleApksUtil {
    private var splitVersionCode = 0L

    @JvmStatic
    fun bundleApks(apkFileHost: String, apks: File, baseApks: File, keepLanguageConfigApks: Boolean, copyToDirectory: String?) {
        splitVersionCode = System.currentTimeMillis() / 1000
        val splits = File(apks.parent, "splits")
        apks.unzipSplits(splits)
        clearSplitApks(keepLanguageConfigApks, splits)

        val universalApk = baseApks.unzipUniversalApk()
        val app = ApkFile(universalApk).meta
        val baseApk = File(splits, "base-v${app.versionName}@${app.versionCode}.apk")
        universalApk.renameTo(baseApk)

        val splitApks = splits.listFiles()?.filter { it.name.endsWith(".apk") && it.name != baseApk.name }
        genSplitInfo(apkFileHost.replace("/+$".toRegex(), ""), app.versionName, splitApks, File(splits, app.versionInfoJson()))

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

    private fun clearSplitApks(keepLanguageConfigApks: Boolean, splits: File) {
        splits.listFiles()?.forEach {
            if (it.isEmptyDpiApk()) it.delete()
            if (it.name.endsWith("_2.apk")) it.delete()
            if (it.name.startsWith("base-")) it.delete()
            if (!keepLanguageConfigApks && it.name.contains("-[a-z]{2}\\.apk".toRegex())) it.delete()
        }
    }

    private fun genSplitInfo(apkFileHost: String, appVersionName: String, splitApks: List<File>?, infoJson: File) {
        val details = SplitDetails()
        details.qigsawId = appVersionName
        details.appVersionName = appVersionName
        details.splits = mutableListOf()

        splitApks?.forEach {
            showInfo(apkFileHost, details, it)
        }
        if (renameTasks.isNotEmpty()) renameTasks.values.forEach { it?.rename(true) }
        renameTasks.clear()
        details.updateSplits = details.splits.map { it.splitName }
        if (infoJson.exists()) infoJson.exists()
        FileUtils.writeByteArrayToFile(infoJson, gson.toJson(details).toByteArray())
    }

    private fun showInfo(apkFileHost: String, details: SplitDetails, apk: File) {
        val apkFile = ApkFile(apk)
        val info = apkFile.apkMeta
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
        val moduleVersion = apkFile.parseModuleVersion()
        if (moduleVersion != null) splitInfo.version = moduleVersion
        apkFile.close()

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

        RenameTask(apk, splitInfo, splitApkData, apkFileHost).rename(false)
    }

    private val renameTasks: MutableMap<File, RenameTask?> = mutableMapOf()

    private data class RenameTask(val apk: File, val splitInfo: SplitInfo, val splitApkData: SplitInfo.SplitApkData, val apkFileHost: String) {
        fun rename(force: Boolean) {
            if (force || splitInfo.version.contains("@")) {
                val newApkName = apk.newName(splitInfo.version, splitApkData.md5)
                splitApkData.url = "$apkFileHost/$newApkName"
                apk.renameTo(File(apk.parent, newApkName))
                renameTasks[apk] = null
            } else renameTasks[apk] = this
        }
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

fun ApkFile.parseModuleVersion(): String? {
    val data = getFileData(AndroidConstants.RESOURCE_FILE) ?: return null
    val buffer = ByteBuffer.wrap(data)
    val resourceTableParser = ResourceTableParser(buffer)
    resourceTableParser.parse()
    val stringPool = resourceTableParser.resourceTable.stringPool
    try {
        val moduleVersion = "module_version_"
        for (i in 0..Int.MAX_VALUE) {
            val v = stringPool[i]
            if (v.startsWith(moduleVersion)) return v.substring(moduleVersion.length)//module_version_1.1@2
        }
    } catch (e: Exception) {
    }
    return null
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

fun File.unzipUniversalApk(): File {//universal.apk
    val zip = ZipFile(this)
    val baseApk = File(parentFile, "base.apk")
    val e = zip.getEntry("universal.apk")
    val inputStream = zip.getInputStream(e)
    FileUtils.copyInputStreamToFile(inputStream, baseApk)
    inputStream.close()
    zip.close()
    return baseApk
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