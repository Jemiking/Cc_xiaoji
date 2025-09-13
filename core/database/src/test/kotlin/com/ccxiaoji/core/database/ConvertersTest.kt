package com.ccxiaoji.core.database

import com.ccxiaoji.common.model.SyncStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class ConvertersTest {
    private val converters = Converters()

    @Test
    fun syncStatus_roundTrip() {
        val values = listOf(
            SyncStatus.SYNCED,
            SyncStatus.PENDING,
            SyncStatus.PENDING_SYNC,
            SyncStatus.MODIFIED,
            SyncStatus.FAILED
        )
        values.forEach { status ->
            val stored = converters.fromSyncStatus(status)
            val restored = converters.toSyncStatus(stored)
            assertEquals(status, restored)
        }
    }
}

