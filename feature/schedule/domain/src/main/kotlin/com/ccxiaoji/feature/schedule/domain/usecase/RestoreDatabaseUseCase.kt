package com.ccxiaoji.feature.schedule.domain.usecase

import android.net.Uri
import com.ccxiaoji.shared.backup.api.BackupApi
import javax.inject.Inject

/**
 * 数据库恢复用例
 */
class RestoreDatabaseUseCase @Inject constructor(
    private val backupApi: BackupApi
) {
    /**
     * 执行数据库恢复
     * @param backupUri 备份文件的URI
     * @return 恢复结果
     */
    suspend operator fun invoke(backupUri: Uri): Result<Unit> {
        return try {
            // 从 Uri 获取文件路径并执行恢复
            val success = backupApi.restoreBackup(backupUri.path ?: "")
            if (success) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("恢复失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}