package com.dhy.qigsawbundle.plugin

import com.dhy.qigsawbundle.apkmd5.apkMd5
import com.dhy.qigsawbundle.apkmd5.md5
import com.google.gson.Gson
import com.qigsaw.share.SplitDetails
import com.qigsaw.share.SplitInfo
import com.qigsaw.share.md5
import net.dongliu.apk.parser.ApkFile
import net.dongliu.apk.parser.bean.ApkMeta
import org.apache.commons.io.FileUtils
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.util.zip.ZipFile

object BundleApksUtil {
    private var splitVersion = ""

    @JvmStatic
    fun bundleApks(bundleOption: BundleOption, apks: File, baseApks: File) {
        val splitsFolder = File(apks.parent, "splits")
        apks.unzipSplits(splitsFolder)
        val universalApk = baseApks.unzipUniversalApk()
        val app = ApkFile(universalApk).meta

        clearSplitApks(bundleOption.keepLanguageConfigApks, splitsFolder)
        splitVersion = "${app.versionName}@${app.versionCode}"
        val fileNameParams: MutableMap<String, String> = mutableMapOf()
        fileNameParams["appId"] = app.packageName
        fileNameParams["split"] = ""
        fileNameParams["abi"] = ""
        fileNameParams["version"] = splitVersion
        fileNameParams["type"] = bundleOption.type ?: ""
        fileNameParams["split"] = "base"
        fileNameParams["md5"] = universalApk.apkMd5()
        bundleOption.fileNameParams = fileNameParams
        bundleOption.apkFileHost = bundleOption.apkFileHost.trimTailSeparator()
        val baseApk = File(splitsFolder, bundleOption.format() + ".apk")
        universalApk.renameTo(baseApk)

        val splitApks = splitsFolder.listFiles()?.filter { it.name.endsWith(".apk") && it.name != baseApk.name }
        val splitDetails = genSplitInfo(bundleOption, app.versionName, splitApks)

        fileNameParams["split"] = ""
        fileNameParams["abi"] = ""
        fileNameParams["version"] = splitVersion
        fileNameParams["md5"] = splitDetails.md5()
        val newInfoJsonFile = File(splitsFolder, bundleOption.format() + ".json")
        newInfoJsonFile.writeJson(splitDetails)

        if (splitsFolder.exists() && bundleOption.copyToDirectory != null) {
            copySplits(splitsFolder, File(bundleOption.copyToDirectory))
        }
        if (bundleOption.publish) publishSplits(bundleOption, splitsFolder)
        println("splits dir ${splitsFolder.absolutePath}")
    }

    @JvmStatic
    fun publishSplits(bundleOption: BundleOption, splits: File) {
        val publish = bundleOption.publishTool
        if (publish != null) {
            val publishOption = PublishOption().apply {
                forceUpdate = false
                dir = splits.absolutePath
                release = bundleOption.isRelease
                uploadBaseApk = bundleOption.uploadBaseApk
            }

            if (File(publish.toString()).exists()) {//JAR
                //quote path for which has EMPTY_CHAR, like 'C:\Program Files\test'
                runCommand("java -jar $publish ${publishOption.toCmdString()}")
            } else {//ClassName
                publishSplitsWithMain(publish.toString(), publishOption)
            }
        }
    }

    private fun publishSplitsWithMain(className: String, publishOption: PublishOption) {
        try {
            val publish = Class.forName(className)
            val m = publish.getDeclaredMethod("main", Array<String>::class.java)
            m.invoke(null, publishOption.toCmdArray())
        } catch (e: ClassNotFoundException) {
            println("publishTool must be JAR_PATH OR CLASS_NAME: $className")
        }
    }

    private fun copySplits(sourceFolder: File, destinationFolder: File) {
        sourceFolder.listFiles()?.forEach {
            FileUtils.copyFile(it, File(destinationFolder, it.name))
        }
    }

    private fun clearSplitApks(keepLanguageConfigApks: Boolean, splits: File) {
        splits.listFiles()?.forEach {
            if (it.isEmptyDpiApk()) it.delete()
            if (it.name.endsWith("_2.apk")) it.delete()
            if (it.name.startsWith("base-")) it.delete()
            if (!keepLanguageConfigApks && it.name.contains("-[a-z]{2}\\.apk".toRegex())) it.delete()
        }
    }

    private fun File.writeJson(data: Any) {
        writeBytes(gson.toJson(data).toByteArray())
    }

    private fun genSplitInfo(bundleOption: BundleOption, appVersionName: String, splitApks: List<File>?): SplitDetails {
        val details = SplitDetails()
        details.qigsawId = appVersionName
        details.appVersionName = appVersionName
        details.splits = mutableListOf()

        splitApks?.forEach {
            showInfo(bundleOption, details, it)
        }
        if (renameTasks.isNotEmpty()) renameTasks.values.forEach { it?.rename(true) }
        renameTasks.clear()
        details.updateSplits = details.splits.map { it.splitName }
        return details
    }

    private fun showInfo(bundleOption: BundleOption, details: SplitDetails, apk: File) {
        val apkFile = ApkFile(apk)
        val info = apkFile.apkMeta
        val splitInfo = details.splits.find { it.splitName == info.splitName } ?: SplitInfo()
        if (splitInfo.splitName == null) {//new
            details.splits.add(splitInfo)
            splitInfo.splitName = info.splitName
            splitInfo.builtIn = false
            splitInfo.onDemand = true
            splitInfo.apkDataList = mutableListOf()
            //用bundletool打包出来的，Split版本信息都是base的
            splitInfo.version = splitVersion
        }

        apkFile.close()

        splitInfo.initUseSplits(apkFile.manifestXml)
        splitInfo.dexNumber += apk.dexNumber()
        if (info.minSdkVersion != null) splitInfo.minSdkVersion = info.minSdkVersion.toInt()

        val splitApkData = SplitInfo.ApkData()
        splitInfo.apkDataList.add(splitApkData)
        splitApkData.size = apk.length()
        splitApkData.md5 = apk.apkMd5()
        splitApkData.abi = info.abi
        if (info.isSO()) {
            if (splitInfo.libDataList == null) splitInfo.libDataList = mutableListOf()
            val splitLibData = apk.splitLibData()
            if (splitLibData != null) splitInfo.libDataList.add(splitLibData)
        }

        RenameTask(apk, splitInfo, splitApkData, bundleOption).rename(false)
    }

    private val renameTasks: MutableMap<File, RenameTask?> = mutableMapOf()

    private class RenameTask(
        val apk: File,
        val splitInfo: SplitInfo,
        val splitApkData: SplitInfo.ApkData,
        bundleOption: BundleOption
    ) {
        private val apkFileHost = bundleOption.apkFileHost
        private val apkNameFormat = bundleOption.fileNameFormat
        private val params: MutableMap<String, String> = mutableMapOf()

        init {
            this.params.putAll(bundleOption.fileNameParams)
        }

        fun rename(force: Boolean) {
            if (force || splitInfo.version.contains("@")) {
                params["split"] = splitInfo.splitName
                params["version"] = splitInfo.version
                params["abi"] = splitApkData.abi
                params["md5"] = splitApkData.md5

                val newApkName = apkNameFormat.format(params) + ".apk"
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

    private fun File.splitLibData(): SplitInfo.LibData? {
        val splitLibData = SplitInfo.LibData()
        splitLibData.jniLibs = mutableListOf()
        val zip = ZipFile(this)
        val files = zip.entries()
        while (files.hasMoreElements()) {
            val e = files.nextElement()
            if (e.name.startsWith("lib/") && e.name.endsWith(".so")) {
                val lib = SplitInfo.LibData.Lib()
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

    fun runCommand(cmd: String, dir: File? = null) {
        val process = Runtime.getRuntime().exec(cmd, null, dir)
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

fun String.format(params: Map<String, String>): String {
    var s = this
    params.forEach {
        s = s.replace("{${it.key}}", it.value)
    }
    return s.replace("[-_]+".toRegex(), "-")
        .replace("(^-)|(-$)".toRegex(), "")
}

fun String.trimTailSeparator(): String {
    return replace("/+$".toRegex(), "")
}