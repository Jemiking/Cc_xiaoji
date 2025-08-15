package com.ccxiaoji.feature.ledger.data.importer.validator

import com.ccxiaoji.feature.ledger.data.local.entity.*
import com.ccxiaoji.feature.ledger.domain.importer.ImportError
import javax.inject.Inject

/**
 * 数据验证器
 */
class DataValidator @Inject constructor() {
    
    /**
     * 验证账户数据
     */
    fun validateAccount(account: AccountEntity): ValidationResult {
        val errors = mutableListOf<String>()
        
        // 验证账户名称
        if (account.name.isBlank()) {
            errors.add("账户名称不能为空")
        }
        if (account.name.length > 50) {
            errors.add("账户名称不能超过50个字符")
        }
        
        // 验证信用卡特有字段
        if (account.type == "CREDIT_CARD") {
            if (account.billingDay != null && (account.billingDay < 1 || account.billingDay > 31)) {
                errors.add("账单日必须在1-31之间")
            }
            if (account.paymentDueDay != null && (account.paymentDueDay < 1 || account.paymentDueDay > 31)) {
                errors.add("还款日必须在1-31之间")
            }
        }
        
        return if (errors.isEmpty()) {
            ValidationResult.Success
        } else {
            ValidationResult.Failed(errors)
        }
    }
    
    /**
     * 验证分类数据
     */
    fun validateCategory(category: CategoryEntity): ValidationResult {
        val errors = mutableListOf<String>()
        
        // 验证分类名称
        if (category.name.isBlank()) {
            errors.add("分类名称不能为空")
        }
        if (category.name.length > 20) {
            errors.add("分类名称不能超过20个字符")
        }
        
        // 验证颜色格式
        category.color?.let { color ->
            if (!color.matches(Regex("^#[0-9A-Fa-f]{6}$"))) {
                errors.add("颜色格式错误，应为#RRGGBB格式")
            }
        }
        
        return if (errors.isEmpty()) {
            ValidationResult.Success
        } else {
            ValidationResult.Failed(errors)
        }
    }
    
    /**
     * 验证交易数据
     */
    fun validateTransaction(transaction: TransactionEntity): ValidationResult {
        val errors = mutableListOf<String>()
        
        // 验证金额
        if (transaction.amountCents == 0) {
            errors.add("交易金额不能为0")
        }
        
        // 验证日期
        if (transaction.createdAt > System.currentTimeMillis()) {
            errors.add("交易日期不能是未来时间")
        }
        
        // 验证备注长度
        transaction.note?.let { note ->
            if (note.length > 200) {
                errors.add("备注不能超过200个字符")
            }
        }
        
        return if (errors.isEmpty()) {
            ValidationResult.Success
        } else {
            ValidationResult.Failed(errors)
        }
    }
    
    /**
     * 验证预算数据
     */
    fun validateBudget(budget: BudgetEntity): ValidationResult {
        val errors = mutableListOf<String>()
        
        // 验证年月
        if (budget.year < 2000 || budget.year > 3000) {
            errors.add("年份必须在2000-3000之间")
        }
        if (budget.month < 1 || budget.month > 12) {
            errors.add("月份必须在1-12之间")
        }
        
        // 验证预算金额
        if (budget.budgetAmountCents <= 0) {
            errors.add("预算金额必须大于0")
        }
        
        return if (errors.isEmpty()) {
            ValidationResult.Success
        } else {
            ValidationResult.Failed(errors)
        }
    }
    
    /**
     * 验证储蓄目标数据
     */
    fun validateSavingsGoal(savingsGoal: SavingsGoalEntity): ValidationResult {
        val errors = mutableListOf<String>()
        
        // 验证目标名称
        if (savingsGoal.name.isBlank()) {
            errors.add("目标名称不能为空")
        }
        if (savingsGoal.name.length > 50) {
            errors.add("目标名称不能超过50个字符")
        }
        
        // 验证金额
        if (savingsGoal.targetAmount <= 0) {
            errors.add("目标金额必须大于0")
        }
        if (savingsGoal.currentAmount < 0) {
            errors.add("当前金额不能小于0")
        }
        
        // 验证截止日期
        savingsGoal.targetDate?.let { targetDate ->
            if (targetDate.isBefore(java.time.LocalDate.now())) {
                errors.add("截止日期不能是过去时间")
            }
        }
        
        return if (errors.isEmpty()) {
            ValidationResult.Success
        } else {
            ValidationResult.Failed(errors)
        }
    }
}

/**
 * 验证结果
 */
sealed class ValidationResult {
    object Success : ValidationResult()
    data class Failed(val errors: List<String>) : ValidationResult()
}