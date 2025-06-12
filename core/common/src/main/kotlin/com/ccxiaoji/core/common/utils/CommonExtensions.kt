package com.ccxiaoji.core.common.utils

import android.content.Context
import android.widget.Toast

/**
 * 通用扩展函数
 */

/**
 * 显示Toast
 */
fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

/**
 * 字符串判空处理
 */
fun String?.orEmpty(): String = this ?: ""

/**
 * 字符串是否为空或只包含空白字符
 */
fun String?.isNullOrBlank(): Boolean = this == null || this.isBlank()

/**
 * 安全地将字符串转换为Int
 * @param defaultValue 转换失败时的默认值
 * @return 转换后的Int值
 */
fun String?.toIntOrDefault(defaultValue: Int = 0): Int {
    return this?.toIntOrNull() ?: defaultValue
}

/**
 * 安全地将字符串转换为Double
 * @param defaultValue 转换失败时的默认值
 * @return 转换后的Double值
 */
fun String?.toDoubleOrDefault(defaultValue: Double = 0.0): Double {
    return this?.toDoubleOrNull() ?: defaultValue
}

/**
 * 限制字符串长度，超过长度的部分用省略号代替
 * @param maxLength 最大长度
 * @param ellipsis 省略号，默认为"..."
 * @return 处理后的字符串
 */
fun String.truncate(maxLength: Int, ellipsis: String = "..."): String {
    return if (this.length <= maxLength) {
        this
    } else {
        this.take(maxLength - ellipsis.length) + ellipsis
    }
}

/**
 * 判断列表是否为空或null
 */
fun <T> List<T>?.isNullOrEmpty(): Boolean = this == null || this.isEmpty()

/**
 * 判断列表是否不为空且不为null
 */
fun <T> List<T>?.isNotNullOrEmpty(): Boolean = !this.isNullOrEmpty()

/**
 * 安全地获取列表中的元素
 * @param index 索引
 * @param defaultValue 默认值
 * @return 元素或默认值
 */
fun <T> List<T>.getOrElse(index: Int, defaultValue: T): T {
    return if (index in indices) this[index] else defaultValue
}

/**
 * 安全地获取列表中的元素
 * @param index 索引
 * @return 元素或null
 */
fun <T> List<T>.getOrNull(index: Int): T? {
    return if (index in indices) this[index] else null
}