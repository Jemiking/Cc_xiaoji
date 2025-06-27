package com.ccxiaoji.shared.backup.domain.usecase

import com.ccxiaoji.shared.backup.data.repository.BackupRepository
import com.ccxiaoji.shared.backup.domain.model.BackupInfo
import com.ccxiaoji.shared.backup.domain.model.BackupResult
import com.ccxiaoji.shared.backup.domain.model.RestoreResult
import com.google.common.truth.Truth.assertThat
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import org.junit.Before
import org.junit.Test
import java.io.File

class BackupDataUseCaseTest {

    @MockK
    private lateinit var backupRepository: BackupRepository

    private lateinit var backupDataUseCase: BackupDataUseCase
    private lateinit var restoreDataUseCase: RestoreDataUseCase

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        backupDataUseCase = BackupDataUseCase(backupRepository)
        restoreDataUseCase = RestoreDataUseCase(backupRepository)
    }

    @Test
    fun `创建本地备份成功`() = runTest {
        // Given
        val backupPath = "/storage/emulated/0/CcXiaoji/backup_20240115_120000.zip"
        val backupResult = BackupResult(
            success = true,
            filePath = backupPath,
            fileSize = 1024 * 1024 * 5, // 5MB
            backupTime = Clock.System.now(),
            itemCount = 150,
            errorMessage = null
        )

        coEvery { backupRepository.createLocalBackup() } returns backupResult

        // When
        val result = backupDataUseCase.execute()

        // Then
        assertThat(result.success).isTrue()
        assertThat(result.filePath).isEqualTo(backupPath)
        assertThat(result.fileSize).isEqualTo(5 * 1024 * 1024)
        assertThat(result.itemCount).isEqualTo(150)
        assertThat(result.errorMessage).isNull()
        coVerify(exactly = 1) { backupRepository.createLocalBackup() }
    }

    @Test
    fun `创建备份失败`() = runTest {
        // Given
        val errorMessage = "存储空间不足"
        val backupResult = BackupResult(
            success = false,
            filePath = null,
            fileSize = 0,
            backupTime = Clock.System.now(),
            itemCount = 0,
            errorMessage = errorMessage
        )

        coEvery { backupRepository.createLocalBackup() } returns backupResult

        // When
        val result = backupDataUseCase.execute()

        // Then
        assertThat(result.success).isFalse()
        assertThat(result.filePath).isNull()
        assertThat(result.fileSize).isEqualTo(0)
        assertThat(result.errorMessage).isEqualTo(errorMessage)
    }

    @Test
    fun `从备份文件恢复数据`() = runTest {
        // Given
        val backupFile = File("/storage/emulated/0/CcXiaoji/backup_20240115_120000.zip")
        val restoreResult = RestoreResult(
            success = true,
            restoredItemCount = 145,
            skippedItemCount = 5,
            errorMessage = null,
            restoreTime = Clock.System.now()
        )

        coEvery { backupRepository.restoreFromBackup(backupFile) } returns restoreResult

        // When
        val result = restoreDataUseCase.execute(backupFile)

        // Then
        assertThat(result.success).isTrue()
        assertThat(result.restoredItemCount).isEqualTo(145)
        assertThat(result.skippedItemCount).isEqualTo(5)
        assertThat(result.errorMessage).isNull()
        coVerify(exactly = 1) { backupRepository.restoreFromBackup(backupFile) }
    }

    @Test
    fun `获取备份文件列表`() = runTest {
        // Given
        val now = Clock.System.now()
        val backupList = listOf(
            BackupInfo(
                fileName = "backup_20240115_120000.zip",
                filePath = "/storage/emulated/0/CcXiaoji/backup_20240115_120000.zip",
                fileSize = 5 * 1024 * 1024,
                createdAt = now.minus(kotlinx.datetime.DateTimePeriod(days = 1)),
                itemCount = 150
            ),
            BackupInfo(
                fileName = "backup_20240114_180000.zip",
                filePath = "/storage/emulated/0/CcXiaoji/backup_20240114_180000.zip",
                fileSize = 4 * 1024 * 1024,
                createdAt = now.minus(kotlinx.datetime.DateTimePeriod(days = 2)),
                itemCount = 140
            )
        )

        coEvery { backupRepository.getBackupList() } returns backupList

        // When
        val result = backupRepository.getBackupList()

        // Then
        assertThat(result).hasSize(2)
        assertThat(result[0].fileName).contains("backup_20240115")
        assertThat(result[0].fileSize).isEqualTo(5 * 1024 * 1024)
        assertThat(result[1].itemCount).isEqualTo(140)
        coVerify(exactly = 1) { backupRepository.getBackupList() }
    }

    @Test
    fun `自动备份功能`() = runTest {
        // Given
        val autoBackupEnabled = true
        val backupInterval = 7 // 天
        val lastBackupTime = Clock.System.now().minus(kotlinx.datetime.DateTimePeriod(days = 8))

        coEvery { backupRepository.isAutoBackupEnabled() } returns autoBackupEnabled
        coEvery { backupRepository.getBackupInterval() } returns backupInterval
        coEvery { backupRepository.getLastBackupTime() } returns lastBackupTime

        // When
        val shouldBackup = autoBackupEnabled && 
            Clock.System.now().minus(lastBackupTime).inWholeDays >= backupInterval

        // Then
        assertThat(shouldBackup).isTrue()
        coVerify { backupRepository.isAutoBackupEnabled() }
        coVerify { backupRepository.getLastBackupTime() }
    }
}

// 假设的UseCase类
class BackupDataUseCase(
    private val backupRepository: BackupRepository
) {
    suspend fun execute(): BackupResult {
        return backupRepository.createLocalBackup()
    }
}

class RestoreDataUseCase(
    private val backupRepository: BackupRepository
) {
    suspend fun execute(backupFile: File): RestoreResult {
        return backupRepository.restoreFromBackup(backupFile)
    }
}

// 假设的Repository接口扩展
interface BackupRepository {
    suspend fun createLocalBackup(): BackupResult
    suspend fun restoreFromBackup(file: File): RestoreResult
    suspend fun getBackupList(): List<BackupInfo>
    suspend fun isAutoBackupEnabled(): Boolean
    suspend fun getBackupInterval(): Int
    suspend fun getLastBackupTime(): kotlinx.datetime.Instant?
}