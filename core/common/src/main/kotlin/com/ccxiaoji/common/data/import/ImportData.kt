package com.ccxiaoji.common.data.import

import com.google.gson.annotations.SerializedName

/**
 * 导入数据的根结构
 */
data class ImportData(
    val metadata: ImportMetadata,
    val ledger: LedgerData? = null,
    val tasks: List<TaskData>? = null,
    val habits: HabitsData? = null,
    val others: OthersData? = null
)

/**
 * 导入元数据
 */
data class ImportMetadata(
    val exportDate: Long,
    val appVersion: String,
    val dataVersion: String,
    val deviceInfo: String? = null
)

/**
 * 记账数据
 */
data class LedgerData(
    val accounts: List<AccountData>? = null,
    val categories: List<CategoryData>? = null,
    val transactions: List<TransactionData>? = null,
    val budgets: List<BudgetData>? = null,
    val savingsGoals: List<SavingsGoalData>? = null
)

/**
 * 账户数据
 */
data class AccountData(
    val id: String,
    val userId: String,
    val name: String,
    val accountType: String,
    val balance: Double,
    val currency: String,
    val icon: String?,
    val color: String?,
    val isDefault: Boolean,
    val isArchived: Boolean,
    val displayOrder: Int,
    val createdAt: Long,
    val updatedAt: Long
)

/**
 * 分类数据
 */
data class CategoryData(
    val id: String,
    val userId: String,
    val name: String,
    val categoryType: String,
    val parentId: String?,
    val icon: String?,
    val color: String?,
    val displayOrder: Int,
    val isSystem: Boolean,
    val isArchived: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)

/**
 * 交易数据
 */
data class TransactionData(
    val id: String,
    val userId: String,
    val accountId: String,
    val categoryId: String,
    val transactionType: String,
    val amount: Double,
    val date: Long,
    val description: String?,
    val note: String?,
    val tags: List<String>?,
    val isRecurring: Boolean,
    val recurringId: String?,
    val createdAt: Long,
    val updatedAt: Long
)

/**
 * 预算数据
 */
data class BudgetData(
    val id: String,
    val userId: String,
    val name: String,
    val amount: Double,
    val period: String,
    val categoryIds: List<String>,
    val startDate: Long,
    val endDate: Long?,
    val isActive: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)

/**
 * 储蓄目标数据
 */
data class SavingsGoalData(
    val id: String,
    val userId: String,
    val name: String,
    val targetAmount: Double,
    val currentAmount: Double,
    val targetDate: Long?,
    val accountId: String?,
    val icon: String?,
    val color: String?,
    val isCompleted: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)

/**
 * 任务数据
 */
data class TaskData(
    val id: String,
    val userId: String,
    val title: String,
    val description: String?,
    val isCompleted: Boolean,
    val dueDate: Long?,
    val priority: Int,
    val tags: List<String>?,
    val createdAt: Long,
    val updatedAt: Long
)

/**
 * 习惯数据
 */
data class HabitsData(
    val habits: List<HabitData>? = null,
    val records: List<HabitRecordData>? = null
)

/**
 * 习惯数据
 */
data class HabitData(
    val id: String,
    val userId: String,
    val name: String,
    val description: String?,
    val icon: String?,
    val color: String?,
    val frequency: String,
    val targetDays: Int,
    val reminderTime: String?,
    val isArchived: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)

/**
 * 习惯记录数据
 */
data class HabitRecordData(
    val id: String,
    val habitId: String,
    val date: Long,
    val isCompleted: Boolean,
    val note: String?,
    val createdAt: Long
)

/**
 * 其他数据
 */
data class OthersData(
    val countdowns: List<CountdownData>? = null,
    val users: List<UserData>? = null
)

/**
 * 倒计时数据
 */
data class CountdownData(
    val id: String,
    val userId: String,
    val title: String,
    val targetDate: Long,
    val repeatType: String?,
    val isNotificationEnabled: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)

/**
 * 用户数据
 */
data class UserData(
    val id: String,
    val username: String,
    val email: String?,
    val avatar: String?,
    val createdAt: Long,
    val updatedAt: Long
)