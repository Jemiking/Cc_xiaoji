package com.ccxiaoji.shared.sync.data.remote.api

import com.ccxiaoji.shared.sync.data.remote.dto.SyncChange
import com.ccxiaoji.shared.sync.data.remote.dto.SyncUploadItem
import com.ccxiaoji.shared.sync.data.remote.dto.SyncUploadResponse
import retrofit2.Response
import retrofit2.http.*

interface SyncService {
    @GET("v1/sync")
    suspend fun getChanges(@Query("since") since: Long): Response<List<SyncChange>>
    
    @POST("v1/sync/upload")
    suspend fun uploadChanges(@Body changes: List<SyncUploadItem>): Response<SyncUploadResponse>
}