package com.qigsaw.share

import com.dhy.qigsawbundle.apkmd5.md5

interface SplitHashItem<T : SplitHashItem.SplitApkInfo> {
    val splitName: String
    val apks: List<T>

    interface SplitApkInfo {
        val abi: String
        val md5: String
    }
}

/**
 * 仅根据apk本身计算md5，不因apkURL变化。以便同时支持Split内置和外置。
 * */
fun SplitDetails.md5(): String {
    return splits.splitDetailsMd5()
}

fun Collection<SplitHashItem<*>>.splitDetailsMd5(): String {
    val hashList: MutableList<String> = mutableListOf()
    forEach { splitInfo ->
        val splitName = splitInfo.splitName
        splitInfo.apks.forEach { apk ->
            hashList.add("${splitName}/${apk.abi}/${apk.md5}")
        }
    }
    hashList.sort()
    return hashList.joinToString(",").md5()
}