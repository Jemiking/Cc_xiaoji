package com.ccxiaoji.core.common.utils

/**
 * 类型安全指南
 * 
 * 这个文件包含了在处理不同数值类型时的最佳实践，
 * 以避免类型不匹配导致的编译错误。
 * 
 * ## 常见问题和解决方案
 * 
 * ### 1. Long vs Int 比较
 * 问题：Kotlin不允许直接比较Long和Int类型
 * 
 * 解决方案：
 * - 如果数值范围在Int内，转换为Int：`longValue.toInt()`
 * - 如果需要保持Long精度，使用Long字面量：`1L, 60L, 365L`
 * 
 * ### 2. Duration API
 * kotlinx.datetime的Duration API返回Long类型：
 * - `duration.inWholeMinutes` 返回 Long
 * - `duration.inWholeHours` 返回 Long
 * - `duration.inWholeDays` 返回 Long
 * 
 * 比较时使用Long字面量：
 * ```kotlin
 * when {
 *     duration.inWholeMinutes < 60L -> "..."
 *     duration.inWholeHours < 24L -> "..."
 * }
 * ```
 * 
 * ### 3. LocalDate.toEpochDays()
 * 返回Long类型，表示从Unix纪元开始的天数。
 * 
 * 计算天数差时，如果确定在Int范围内，可以安全转换：
 * ```kotlin
 * val daysDiff = (date1.toEpochDays() - date2.toEpochDays()).toInt()
 * ```
 * 
 * ### 4. 类型安全检查清单
 * - [ ] 所有数值比较使用相同类型
 * - [ ] Long类型比较使用L后缀
 * - [ ] 必要时进行显式类型转换
 * - [ ] 考虑数值范围，避免溢出
 */
@Suppress("unused")
private object TypeSafetyGuideline