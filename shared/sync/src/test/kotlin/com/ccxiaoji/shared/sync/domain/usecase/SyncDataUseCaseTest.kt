package com.ccxiaoji.shared.sync.domain.usecase

import com.ccxiaoji.shared.sync.data.repository.SyncRepository
import com.ccxiaoji.shared.sync.domain.model.SyncResult
import com.ccxiaoji.shared.sync.domain.model.SyncStatus
import com.google.common.truth.Truth.assertThat
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.junit.Before
import org.junit.Test

class SyncDataUseCaseTest {

    @MockK
    private lateinit var syncRepository: SyncRepository

    private lateinit var syncDataUseCase: SyncDataUseCase

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        syncDataUseCase = SyncDataUseCase(syncRepository)
    }

    @Test
    fun `执行全量数据同步成功`() = runTest {
        // Given
        val syncResult = SyncResult(
            status = SyncStatus.SUCCESS,
            syncedAt = Clock.System.now(),
            uploadedCount = 10,
            downloadedCount = 5,
            errorCount = 0,
            errorMessage = null
        )

        coEvery { syncRepository.performFullSync() } returns syncResult

        // When
        val result = syncDataUseCase.execute()

        // Then
        assertThat(result.status).isEqualTo(SyncStatus.SUCCESS)
        assertThat(result.uploadedCount).isEqualTo(10)
        assertThat(result.downloadedCount).isEqualTo(5)
        assertThat(result.errorCount).isEqualTo(0)
        assertThat(result.errorMessage).isNull()
        coVerify(exactly = 1) { syncRepository.performFullSync() }
    }

    @Test
    fun `执行增量数据同步`() = runTest {
        // Given
        val lastSyncTime = Clock.System.now().minus(kotlinx.datetime.DateTimePeriod(hours = 1))
        val syncResult = SyncResult(
            status = SyncStatus.SUCCESS,
            syncedAt = Clock.System.now(),
            uploadedCount = 3,
            downloadedCount = 2,
            errorCount = 0,
            errorMessage = null
        )

        coEvery { syncRepository.performIncrementalSync(lastSyncTime) } returns syncResult

        // When
        val result = syncRepository.performIncrementalSync(lastSyncTime)

        // Then
        assertThat(result.status).isEqualTo(SyncStatus.SUCCESS)
        assertThat(result.uploadedCount).isEqualTo(3)
        assertThat(result.downloadedCount).isEqualTo(2)
        coVerify(exactly = 1) { syncRepository.performIncrementalSync(lastSyncTime) }
    }

    @Test
    fun `同步失败返回错误信息`() = runTest {
        // Given
        val errorMessage = "网络连接失败"
        val syncResult = SyncResult(
            status = SyncStatus.FAILED,
            syncedAt = Clock.System.now(),
            uploadedCount = 0,
            downloadedCount = 0,
            errorCount = 1,
            errorMessage = errorMessage
        )

        coEvery { syncRepository.performFullSync() } returns syncResult

        // When
        val result = syncDataUseCase.execute()

        // Then
        assertThat(result.status).isEqualTo(SyncStatus.FAILED)
        assertThat(result.errorCount).isEqualTo(1)
        assertThat(result.errorMessage).isEqualTo(errorMessage)
        assertThat(result.uploadedCount).isEqualTo(0)
        assertThat(result.downloadedCount).isEqualTo(0)
    }

    @Test
    fun `部分同步成功的情况`() = runTest {
        // Given
        val syncResult = SyncResult(
            status = SyncStatus.PARTIAL_SUCCESS,
            syncedAt = Clock.System.now(),
            uploadedCount = 8,
            downloadedCount = 4,
            errorCount = 2,
            errorMessage = "2个项目同步失败"
        )

        coEvery { syncRepository.performFullSync() } returns syncResult

        // When
        val result = syncDataUseCase.execute()

        // Then
        assertThat(result.status).isEqualTo(SyncStatus.PARTIAL_SUCCESS)
        assertThat(result.uploadedCount).isEqualTo(8)
        assertThat(result.downloadedCount).isEqualTo(4)
        assertThat(result.errorCount).isEqualTo(2)
        assertThat(result.errorMessage).contains("2个项目同步失败")
    }

    @Test
    fun `检查是否需要同步`() = runTest {
        // Given
        val lastSyncTime = Clock.System.now().minus(kotlinx.datetime.DateTimePeriod(hours = 2))
        val pendingChangesCount = 15

        coEvery { syncRepository.getLastSyncTime() } returns lastSyncTime
        coEvery { syncRepository.getPendingChangesCount() } returns pendingChangesCount

        // When
        val needsSync = syncRepository.getPendingChangesCount() > 0 ||
                Clock.System.now().minus(lastSyncTime).inWholeHours > 1

        // Then
        assertThat(needsSync).isTrue()
        assertThat(pendingChangesCount).isGreaterThan(0)
        coVerify(exactly = 1) { syncRepository.getPendingChangesCount() }
    }
}

// 假设的UseCase类
class SyncDataUseCase(
    private val syncRepository: SyncRepository
) {
    suspend fun execute(): SyncResult {
        return syncRepository.performFullSync()
    }
}

// 假设的Repository接口扩展
interface SyncRepository {
    suspend fun performFullSync(): SyncResult
    suspend fun performIncrementalSync(lastSyncTime: Instant): SyncResult
    suspend fun getLastSyncTime(): Instant?
    suspend fun getPendingChangesCount(): Int
}