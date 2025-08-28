package com.ccxiaoji.feature.ledger.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.common.base.BaseResult
import com.ccxiaoji.feature.ledger.domain.model.Ledger
import com.ccxiaoji.feature.ledger.domain.model.LedgerLink
import com.ccxiaoji.feature.ledger.domain.model.SyncMode
import com.ccxiaoji.feature.ledger.domain.usecase.ManageLedgerUseCase
import com.ccxiaoji.feature.ledger.domain.usecase.ManageLedgerLinkUseCase
import com.ccxiaoji.feature.ledger.data.local.dao.LedgerDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import javax.inject.Inject

/**
 * 记账簿管理ViewModel
 * 负责记账簿的创建、读取、更新、删除操作
 */
@HiltViewModel
class LedgerBookManagementViewModel @Inject constructor(
    private val manageLedgerUseCase: ManageLedgerUseCase,
    private val manageLedgerLinkUseCase: ManageLedgerLinkUseCase,
    private val ledgerDao: LedgerDao
) : ViewModel() {

    data class UiState(
        val isLoading: Boolean = false,
        val ledgers: List<Ledger> = emptyList(),
        val error: String? = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    // TODO: 暂时使用默认用户ID，后续通过参数传递
    private val currentUserId = "current_user_id"

    /**
     * 加载所有记账簿
     */
    fun loadLedgers() {
        viewModelScope.launch {
            android.util.Log.d("LEDGER_MANAGEMENT_DEBUG", "🚀 开始加载记账簿...")
            android.util.Log.d("LEDGER_MANAGEMENT_DEBUG", "👤 当前用户ID: $currentUserId")
            
            // 验证依赖注入
            android.util.Log.d("LEDGER_MANAGEMENT_DEBUG", "🔧 验证依赖注入:")
            android.util.Log.d("LEDGER_MANAGEMENT_DEBUG", "  manageLedgerUseCase: ${manageLedgerUseCase::class.java.simpleName}")
            android.util.Log.d("LEDGER_MANAGEMENT_DEBUG", "  manageLedgerUseCase hashCode: ${manageLedgerUseCase.hashCode()}")
            
            // 直接查询数据库进行调试
            debugDatabaseContent()
            
            _uiState.update { it.copy(isLoading = true, error = null) }
            android.util.Log.d("LEDGER_MANAGEMENT_DEBUG", "⏳ UI状态已设置为加载中")
            
            try {
                android.util.Log.d("LEDGER_MANAGEMENT_DEBUG", "🎯 准备调用 manageLedgerUseCase.getUserLedgers($currentUserId)")
                
                val ledgersFlow = manageLedgerUseCase.getUserLedgers(currentUserId)
                android.util.Log.d("LEDGER_MANAGEMENT_DEBUG", "📡 Flow已获取: ${ledgersFlow::class.java.simpleName}")
                
                android.util.Log.d("LEDGER_MANAGEMENT_DEBUG", "🔄 开始收集Flow数据...")
                
                ledgersFlow
                    .catch { throwable ->
                        android.util.Log.e("LEDGER_MANAGEMENT_DEBUG", "❌ Flow收集过程中出现异常: ${throwable.message}", throwable)
                        android.util.Log.e("LEDGER_MANAGEMENT_DEBUG", "❌ 异常类型: ${throwable::class.java.simpleName}")
                        android.util.Log.e("LEDGER_MANAGEMENT_DEBUG", "❌ 异常堆栈: ${throwable.stackTraceToString()}")
                        _uiState.update { 
                            it.copy(
                                isLoading = false, 
                                error = "Flow异常: ${throwable.message}"
                            ) 
                        }
                    }
                    .collect { ledgers ->
                        android.util.Log.d("LEDGER_MANAGEMENT_DEBUG", "🎉 Flow emit了数据!")
                        android.util.Log.d("LEDGER_MANAGEMENT_DEBUG", "📋 收到记账簿数据: ${ledgers.size} 个")
                        
                        if (ledgers.isEmpty()) {
                            android.util.Log.w("LEDGER_MANAGEMENT_DEBUG", "⚠️ 收到空的记账簿列表")
                        } else {
                            ledgers.forEachIndexed { index, ledger ->
                                android.util.Log.d("LEDGER_MANAGEMENT_DEBUG", "  [$index] ID:${ledger.id}, 名称:${ledger.name}, 默认:${ledger.isDefault}, 激活:${ledger.isActive}")
                            }
                        }
                        
                        val defaultLedgers = ledgers.filter { it.isDefault }
                        android.util.Log.d("LEDGER_MANAGEMENT_DEBUG", "🎯 默认记账簿数量: ${defaultLedgers.size}")
                        defaultLedgers.forEach { ledger ->
                            android.util.Log.d("LEDGER_MANAGEMENT_DEBUG", "  默认记账簿: ${ledger.name} (ID: ${ledger.id})")
                        }
                        
                        android.util.Log.d("LEDGER_MANAGEMENT_DEBUG", "🔄 准备更新UI状态...")
                        _uiState.update { 
                            it.copy(
                                isLoading = false, 
                                ledgers = ledgers,
                                error = null
                            ) 
                        }
                        
                        android.util.Log.d("LEDGER_MANAGEMENT_DEBUG", "✅ UI状态已更新，记账簿数量: ${ledgers.size}")
                        
                        // 验证UI状态
                        val currentState = _uiState.value
                        android.util.Log.d("LEDGER_MANAGEMENT_DEBUG", "🔍 当前UI状态验证:")
                        android.util.Log.d("LEDGER_MANAGEMENT_DEBUG", "  isLoading: ${currentState.isLoading}")
                        android.util.Log.d("LEDGER_MANAGEMENT_DEBUG", "  ledgers.size: ${currentState.ledgers.size}")
                        android.util.Log.d("LEDGER_MANAGEMENT_DEBUG", "  error: ${currentState.error}")
                    }
                    
            } catch (e: Exception) {
                android.util.Log.e("LEDGER_MANAGEMENT_DEBUG", "💥 loadLedgers整体异常: ${e.message}", e)
                android.util.Log.e("LEDGER_MANAGEMENT_DEBUG", "💥 异常类型: ${e::class.java.simpleName}")
                android.util.Log.e("LEDGER_MANAGEMENT_DEBUG", "💥 异常堆栈: ${e.stackTraceToString()}")
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        error = "整体异常: ${e.message}"
                    ) 
                }
            }
        }
    }

    /**
     * 创建新记账簿
     */
    suspend fun createLedger(
        name: String,
        description: String?,
        color: String,
        icon: String
    ): Result<String> {
        return try {
            when (val result = manageLedgerUseCase.createLedger(
                userId = currentUserId,
                name = name,
                description = description,
                color = color,
                icon = icon
            )) {
                is BaseResult.Success -> {
                    loadLedgers() // 重新加载列表
                    Result.success(result.data.id)
                }
                is BaseResult.Error -> {
                    Result.failure(result.exception)
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 更新记账簿信息
     */
    suspend fun updateLedger(
        ledgerId: String,
        name: String,
        description: String?,
        color: String,
        icon: String
    ): Result<Unit> {
        return try {
            // 首先获取当前记账簿
            val currentLedger = _uiState.value.ledgers.find { it.id == ledgerId }
                ?: return Result.failure(Exception("记账簿不存在"))
            
            when (val result = manageLedgerUseCase.updateLedger(
                ledger = currentLedger,
                name = name,
                description = description,
                color = color,
                icon = icon
            )) {
                is BaseResult.Success -> {
                    loadLedgers() // 重新加载列表
                    Result.success(Unit)
                }
                is BaseResult.Error -> {
                    Result.failure(result.exception)
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 删除记账簿
     */
    suspend fun deleteLedger(ledgerId: String): Result<Unit> {
        return try {
            // 检查是否为默认记账簿
            val currentLedger = _uiState.value.ledgers.find { it.id == ledgerId }
            if (currentLedger?.isDefault == true) {
                return Result.failure(Exception("不能删除默认记账簿，请先设置其他记账簿为默认"))
            }

            when (val result = manageLedgerUseCase.deleteLedger(ledgerId, currentUserId)) {
                is BaseResult.Success -> {
                    loadLedgers() // 重新加载列表
                    Result.success(Unit)
                }
                is BaseResult.Error -> {
                    Result.failure(result.exception)
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 设置默认记账簿
     */
    suspend fun setDefaultLedger(ledgerId: String): Result<Unit> {
        return try {
            when (val result = manageLedgerUseCase.setDefaultLedger(currentUserId, ledgerId)) {
                is BaseResult.Success -> {
                    loadLedgers() // 重新加载列表
                    Result.success(Unit)
                }
                is BaseResult.Error -> {
                    Result.failure(result.exception)
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 更新记账簿排序
     */
    suspend fun updateLedgerOrder(ledgers: List<Ledger>): Result<Unit> {
        return try {
            val ledgerOrders = ledgers.mapIndexed { index, ledger ->
                ledger.id to index
            }
            
            when (val result = manageLedgerUseCase.reorderLedgers(ledgerOrders)) {
                is BaseResult.Success -> {
                    loadLedgers() // 重新加载列表
                    Result.success(Unit)
                }
                is BaseResult.Error -> {
                    Result.failure(result.exception)
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 调试数据库内容
     */
    private suspend fun debugDatabaseContent() {
        try {
            android.util.Log.d("DATABASE_CONTENT_DEBUG", "🗄️ 开始直接查询数据库...")
            
            // 查询所有用户
            val allUsers = try {
                // 这里我们需要手动查询，因为没有直接的方法
                android.util.Log.d("DATABASE_CONTENT_DEBUG", "🔍 尝试查询用户信息...")
                "用户查询需要UserDao"
            } catch (e: Exception) {
                android.util.Log.e("DATABASE_CONTENT_DEBUG", "❌ 查询用户失败: ${e.message}")
                "查询失败"
            }
            
            // 查询特定用户的记账簿（只获取一次数据，不阻塞后续流程）
            val userLedgers = ledgerDao.getUserLedgers(currentUserId)
            val entities = userLedgers.first() // 只获取第一次emit的数据
            
            android.util.Log.d("DATABASE_CONTENT_DEBUG", "📋 直接DAO查询结果: ${entities.size} 个实体")
            entities.forEachIndexed { index, entity ->
                android.util.Log.d("DATABASE_CONTENT_DEBUG", "  [$index] 实体详情:")
                android.util.Log.d("DATABASE_CONTENT_DEBUG", "    ID: ${entity.id}")
                android.util.Log.d("DATABASE_CONTENT_DEBUG", "    用户ID: ${entity.userId}")
                android.util.Log.d("DATABASE_CONTENT_DEBUG", "    名称: ${entity.name}")
                android.util.Log.d("DATABASE_CONTENT_DEBUG", "    描述: ${entity.description}")
                android.util.Log.d("DATABASE_CONTENT_DEBUG", "    是否默认: ${entity.isDefault}")
                android.util.Log.d("DATABASE_CONTENT_DEBUG", "    是否激活: ${entity.isActive}")
                android.util.Log.d("DATABASE_CONTENT_DEBUG", "    显示顺序: ${entity.displayOrder}")
                android.util.Log.d("DATABASE_CONTENT_DEBUG", "    创建时间: ${entity.createdAt}")
            }
            
            // 检查默认记账簿
            val hasDefault = ledgerDao.hasDefaultLedger(currentUserId)
            android.util.Log.d("DATABASE_CONTENT_DEBUG", "🎯 是否有默认记账簿: $hasDefault")
            
            if (hasDefault) {
                val defaultLedger = ledgerDao.getDefaultLedger(currentUserId)
                if (defaultLedger != null) {
                    android.util.Log.d("DATABASE_CONTENT_DEBUG", "✅ 默认记账簿: ${defaultLedger.name} (ID: ${defaultLedger.id})")
                } else {
                    android.util.Log.e("DATABASE_CONTENT_DEBUG", "❌ hasDefaultLedger返回true但getDefaultLedger返回null")
                }
            } else {
                android.util.Log.w("DATABASE_CONTENT_DEBUG", "⚠️ 没有找到默认记账簿")
            }
            
            android.util.Log.d("DATABASE_CONTENT_DEBUG", "🔚 数据库调试完成，继续正常流程...")
            
        } catch (e: Exception) {
            android.util.Log.e("DATABASE_CONTENT_DEBUG", "❌ 调试数据库内容失败: ${e.message}", e)
        }
    }

    // =============================================================================
    // 联动关系管理方法
    // =============================================================================
    
    /**
     * 获取指定记账簿的联动关系
     */
    fun getLedgerLinks(ledgerId: String): Flow<List<LedgerLink>> {
        return manageLedgerLinkUseCase.getLedgerLinks(ledgerId)
    }
    
    /**
     * 创建联动关系
     */
    suspend fun createLedgerLink(
        currentLedgerId: String,
        targetLedgerId: String,
        syncMode: SyncMode
    ): Result<Unit> {
        return try {
            when (val result = manageLedgerLinkUseCase.createLedgerLink(
                parentLedgerId = currentLedgerId,
                childLedgerId = targetLedgerId,
                syncMode = syncMode,
                autoSyncEnabled = true
            )) {
                is BaseResult.Success -> {
                    Result.success(Unit)
                }
                is BaseResult.Error -> {
                    Result.failure(result.exception)
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 删除联动关系
     */
    suspend fun deleteLedgerLink(linkId: String): Result<Unit> {
        return try {
            when (val result = manageLedgerLinkUseCase.deleteLedgerLink(linkId)) {
                is BaseResult.Success -> {
                    Result.success(Unit)
                }
                is BaseResult.Error -> {
                    Result.failure(result.exception)
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 更新同步模式
     */
    suspend fun updateSyncMode(linkId: String, syncMode: SyncMode): Result<Unit> {
        return try {
            when (val result = manageLedgerLinkUseCase.updateSyncMode(linkId, syncMode)) {
                is BaseResult.Success -> {
                    Result.success(Unit)
                }
                is BaseResult.Error -> {
                    Result.failure(result.exception)
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 切换自动同步开关
     */
    suspend fun toggleAutoSync(linkId: String, enabled: Boolean): Result<Unit> {
        return try {
            when (val result = manageLedgerLinkUseCase.setAutoSyncEnabled(linkId, enabled)) {
                is BaseResult.Success -> {
                    Result.success(Unit)
                }
                is BaseResult.Error -> {
                    Result.failure(result.exception)
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 获取可用于联动的记账簿列表（排除已联动的记账簿）
     */
    fun getAvailableLedgersForLink(currentLedgerId: String, existingLinks: List<LedgerLink>): List<Ledger> {
        val currentLedgers = _uiState.value.ledgers
        val linkedLedgerIds = existingLinks.flatMap { link ->
            listOf(link.parentLedgerId, link.childLedgerId)
        }.toSet()
        
        return currentLedgers.filter { ledger ->
            ledger.id != currentLedgerId && 
            ledger.id !in linkedLedgerIds &&
            ledger.isActive
        }
    }

}