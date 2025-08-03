package com.ccxiaoji.app.data.backup.model

/**
 * 备份错误类型
 */
sealed class BackupError(
    val message: String,
    val cause: Throwable? = null
) {
    class FileSystemError(message: String, cause: Throwable? = null) : BackupError(message, cause)
    class DatabaseError(message: String, cause: Throwable? = null) : BackupError(message, cause)
    class SerializationError(message: String, cause: Throwable? = null) : BackupError(message, cause)
    class PermissionError(message: String, cause: Throwable? = null) : BackupError(message, cause)
    class ValidationError(message: String, cause: Throwable? = null) : BackupError(message, cause)
    class UnknownError(message: String, cause: Throwable? = null) : BackupError(message, cause)
}