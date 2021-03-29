package com.dhy.qigsawbundle.apkmd5

import net.dongliu.apk.parser.ApkFile
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.math.BigInteger
import java.security.MessageDigest
import java.util.zip.ZipFile

fun File.apkMd5(): String {
    val fs = zipDetails()
    val names = fs.keys.sorted().iterator()
    val digest = MessageDigest.getInstance("MD5")
    while (names.hasNext()) {
        val name = names.next()
        digest.update(name.toByteArray())
        digest.update(fs.getValue(name).toByteArray())
    }
    return digest.digest().toHex()
}

private val BUFFER_SIZE = DataSizeUnit.MEBIBYTES.toBytes(2).toInt()

fun File.md5(): String {
    val digest = MessageDigest.getInstance("MD5")
    if (length() > BUFFER_SIZE) {
        val buffer = ByteArray(BUFFER_SIZE)
        val inputStream = FileInputStream(this)
        while (true) {
            val size = inputStream.read(buffer)
            if (size > 0) digest.update(buffer, 0, size)
            else break
        }
        inputStream.close()
    } else {
        digest.update(readBytes())
    }
    return digest.digest().toHex()
}

private fun File.manifestXmlWithoutVersion(): String {
    val apkFile = ApkFile(this)
    var manifestXml = apkFile.manifestXml
    apkFile.close()
    manifestXml = manifestXml.replace("android:versionCode=\"\\d+\"".toRegex(), "")
    manifestXml = manifestXml.replace("android:versionName=\"[^\"]+\"".toRegex(), "")
    return manifestXml
}

private fun File.zipDetails(): MutableMap<String, String> {
    val details: MutableMap<String, String> = mutableMapOf()
    details["AndroidManifest.xml"] = manifestXmlWithoutVersion().md5()
    val zip = ZipFile(this)
    val fs = zip.entries()
    var buffer: ByteArray? = null
    while (fs.hasMoreElements()) {
        val e = fs.nextElement()
        if (e.name.isValidApkEntry()) {
            if (e.size <= BUFFER_SIZE) {
                details[e.name] = zip.getInputStream(e).md5()
            } else {
                if (buffer == null) buffer = ByteArray(BUFFER_SIZE)
                details[e.name] = zip.getInputStream(e).md5(buffer)
            }
        }
    }
    zip.close()
    return details
}

private fun String.isValidApkEntry(): Boolean {
    return this != "AndroidManifest.xml"
            && this != "META-INF/BNDLTOOL.RSA"
            && this != "META-INF/BNDLTOOL.SF"
            && this != "META-INF/MANIFEST.MF"
            && !endsWith("/")
}

fun InputStream.md5(buffer: ByteArray): String {
    val digest = MessageDigest.getInstance("MD5")
    while (true) {
        val length = read(buffer)
        if (length > 0) digest.update(buffer, 0, length)
        else break
    }
    close()
    return digest.digest().toHex()
}

fun InputStream.md5(): String {
    val digest = MessageDigest.getInstance("MD5")
    digest.update(readBytes())
    close()
    return digest.digest().toHex()
}

fun String.md5(): String {
    val digest = MessageDigest.getInstance("MD5")
    digest.update(toByteArray())
    return digest.digest().toHex()
}

fun ByteArray.toHex(): String {
    val hex = BigInteger(1, this).toString(16)
    return hex.padStart(size * 2, '0')
}