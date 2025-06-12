package com.ccxiaoji.core.common.extensions

import java.security.MessageDigest

/**
 * 字符串相关扩展函数
 */

/**
 * 判断字符串是否为空或只包含空白字符
 */
fun String?.isNotNullOrBlank(): Boolean = !this.isNullOrBlank()

/**
 * 将字符串转换为MD5
 */
fun String.toMD5(): String {
    val md = MessageDigest.getInstance("MD5")
    val digest = md.digest(this.toByteArray())
    return digest.joinToString("") { "%02x".format(it) }
}

/**
 * 移除字符串中的所有空白字符
 */
fun String.removeAllSpaces(): String = this.replace("\\s".toRegex(), "")

/**
 * 限制字符串长度，超出部分用省略号替代
 */
fun String.ellipsize(maxLength: Int): String {
    return if (this.length <= maxLength) {
        this
    } else {
        this.take(maxLength - 3) + "..."
    }
}

/**
 * 将字符串的首字母大写
 */
fun String.capitalizeFirst(): String {
    return if (isEmpty()) {
        this
    } else {
        this[0].uppercaseChar() + substring(1)
    }
}