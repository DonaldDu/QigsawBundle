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
    val bytes = readZipEntryBytes("META-INF/MANIFEST.MF")

    val mf = String(bytes)
    var name = ""
    val keyName = "Name: "
    val keyDigest = "SHA"// SHA1-Digest, SHA-256-Digest
    mf.split("\r\n")
        .filter { it.isNotEmpty() }
        .forEach { line ->
            if (line.startsWith(keyName)) {
                name = line.substring(keyName.length)
            } else if (line.startsWith(keyDigest)) {
                details[name] = line.substring(line.indexOf(":") + 2)// 'SHA-256-Digest: '
            }
        }
    details["AndroidManifest.xml"] = manifestXmlWithoutVersion().md5()
    return details
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

fun File.readZipEntryBytes(name: String): ByteArray {//  lib/x86/libhello-jni.so
    return ZipFile(this).use { zip ->
        zip.getInputStream(zip.getEntry(name)).use { it.readBytes() }
    }
}