package com.ccxiaoji.app.data.backup.model

import kotlinx.datetime.Instant

/**
 * 备份文件元数据
 * 包含了备份文件的基本信息和统计数据
 */
data class BackupMetadata(
    val fileType: String = "CC小记备份文件",
    val fileVersion: String = "2.0",
    val exportTime: Instant,
    val appVersion: String,
    val deviceInfo: String,
    val dataRange: DataRange,
    val statistics: BackupStatistics,
    val files: Map<String, String>,
    val checksum: ChecksumInfo
)

/**
 * 数据范围
 */
data class DataRange(
    val startDate: String?,
    val endDate: String?
)

/**
 * 备份统计信息
 */
data class BackupStatistics(
    val transactionCount: Int = 0,
    val todoCount: Int = 0,
    val habitRecordCount: Int = 0,
    val scheduleCount: Int = 0,
    val planCount: Int = 0
)

/**
 * 校验和信息
 */
data class ChecksumInfo(
    val algorithm: String = "SHA-256",
    val value: String
)