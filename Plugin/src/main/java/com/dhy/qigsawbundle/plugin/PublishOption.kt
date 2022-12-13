package com.dhy.qigsawbundle.plugin

class PublishOption {
    companion object {
        const val keyHead = "--"
    }

    /**
     * split apk data dir
     * */
    var dir: String? = null
    var uploadBaseApk = true
    var release: Boolean = false
    var forceUpdate: Boolean = false

    private fun toMap(): Map<String, Any?> {
        val map: MutableMap<String, Any?> = mutableMapOf()
        javaClass.declaredFields.forEach {
            map[it.name] = it.get(this)
        }
        return map
    }

    fun toCmdString(keyHead: String = PublishOption.keyHead): String {
        return toMap().toCmdString(keyHead)
    }

    fun toCmdArray(keyHead: String = PublishOption.keyHead): Array<String> {
        val list: MutableList<String> = mutableListOf()
        toMap().forEach {
            list.add("${keyHead}${it.key}")
            if (it.value != null) list.add(it.value.toString())
        }
        return list.toTypedArray()
    }
}