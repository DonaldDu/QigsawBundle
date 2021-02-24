package com.dhy.qigsawbundle.apkmd5

import java.io.File
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

fun File.zipDetails(): MutableMap<String, String> {
    val details: MutableMap<String, String> = mutableMapOf()
    val zip = ZipFile(this)
    val fs = zip.entries()
    var buffer: ByteArray? = null
    val bufferSize = 1024 * 1024 * 2//2MB
    while (fs.hasMoreElements()) {
        val e = fs.nextElement()
        if (!e.name.endsWith("/")) {
            if (e.size <= bufferSize) {
                details[e.name] = zip.getInputStream(e).md5()
            } else {
                if (buffer == null) buffer = ByteArray(bufferSize)
                details[e.name] = zip.getInputStream(e).md5(buffer)
            }
        }
    }
    zip.close()
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

fun ByteArray.toHex(): String {
    val hex = BigInteger(1, this).toString(16)
    val sb = StringBuilder(hex)
    while (sb.length < size * 2) sb.insert(0, '0')
    return sb.toString()
}