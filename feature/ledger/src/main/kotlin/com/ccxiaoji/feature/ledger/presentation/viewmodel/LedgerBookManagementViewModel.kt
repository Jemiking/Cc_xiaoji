package com.ccxiaoji.feature.ledger.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.common.base.BaseResult
import com.ccxiaoji.feature.ledger.domain.model.Ledger
import com.ccxiaoji.feature.ledger.domain.usecase.ManageLedgerUseCase
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
    private val manageLedgerUseCase: ManageLedgerUseCase
) : ViewModel() {

    data class UiState(
        val isLoading: Boolean = false,
        val ledgers: List<Ledger> = emptyList(),
        val error: String? = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    // TODO: 暂时使用默认用户ID，后续通过参数传递
    private val currentUserId = "default_user"

    /**
     * 加载所有记账簿
     */
    fun loadLedgers() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            manageLedgerUseCase.getUserLedgers(currentUserId)
                .catch { throwable ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            error = throwable.message ?: "加载失败"
                        ) 
                    }
                }
                .collect { ledgers ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            ledgers = ledgers,
                            error = null
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

}