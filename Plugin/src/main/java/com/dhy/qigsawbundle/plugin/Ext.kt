package com.dhy.qigsawbundle.plugin

/**
 * @param keyHead eg: '-', '--'
 * */
fun Map<String, Any?>.toCmdString(keyHead: String): String {
    val sb = StringBuilder()
    forEach { (k, v) ->
        sb.append(" ").append(keyHead).append(k).append(" ")
        val value = v?.toString()
        if (value?.contains(' ') == true) {
            if (!value.contains("\"")) sb.append("\"$value\"")
            else sb.append(value)
        } else {
            sb.append(value ?: "")
        }
    }
    return sb.toString()
}