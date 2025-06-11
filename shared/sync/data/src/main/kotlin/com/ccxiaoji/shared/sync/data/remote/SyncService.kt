package com.ccxiaoji.shared.sync.data.remote

import com.ccxiaoji.shared.sync.domain.model.SyncChange
import com.ccxiaoji.shared.sync.domain.model.SyncUploadItem
import com.ccxiaoji.shared.sync.domain.model.SyncUploadResponse
import retrofit2.Response
import retrofit2.http.*

/**
 * 同步服务的网络API接口
 * 用于与服务器进行数据同步
 */
interface SyncService {
    
    /**
     * 获取指定时间之后的变更
     * @param since 上次同步时间戳
     * @return 变更列表
     */
    @GET("v1/sync")
    suspend fun getChanges(@Query("since") since: Long): Response<List<SyncChange>>
    
    /**
     * 上传本地变更到服务器
     * @param changes 要上传的变更列表
     * @return 上传响应结果
     */
    @POST("v1/sync/upload")
    suspend fun uploadChanges(@Body changes: List<SyncUploadItem>): Response<SyncUploadResponse>
}