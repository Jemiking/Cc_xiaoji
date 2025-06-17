package com.ccxiaoji.app.data.remote.api

import com.ccxiaoji.app.data.sync.SyncChange
import com.ccxiaoji.app.data.sync.SyncUploadItem
import retrofit2.Response
import retrofit2.http.*

interface SyncApi {
    @GET("v1/sync")
    suspend fun getChanges(@Query("since") since: Long): Response<List<SyncChange>>
    
    @POST("v1/sync/upload")
    suspend fun uploadChanges(@Body changes: List<SyncUploadItem>): Response<SyncUploadResponse>
}

data class SyncUploadResponse(
    val serverTime: Long,
    val processedCount: Int,
    val conflicts: List<ConflictItem>?
)

data class ConflictItem(
    val table: String,
    val rowId: String,
    val resolution: String
)