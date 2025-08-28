package com.ccxiaoji.feature.ledger.domain.model

/**
 * 记账设置数据模型
 */
data class LedgerSettings(
    // 基础设置
    val basicSettings: BasicSettings = BasicSettings(),
    // 高级设置
    val advancedSettings: AdvancedSettings = AdvancedSettings(),
    // 自动化设置
    val automationSettings: AutomationSettings = AutomationSettings()
)

/**
 * 基础设置
 */
data class BasicSettings(
    // 默认账户ID
    val defaultAccountId: Long? = null,
    // 默认币种
    val defaultCurrency: String = "CNY",
    // 时间记录开关
    val enableTimeRecording: Boolean = false,
    // 首页显示设置
    val homeDisplaySettings: HomeDisplaySettings = HomeDisplaySettings(),
    // 记账提醒设置
    val reminderSettings: ReminderSettings = ReminderSettings()
)

/**
 * 首页显示设置
 */
data class HomeDisplaySettings(
    // 显示今日支出
    val showTodayExpense: Boolean = true,
    // 显示今日收入
    val showTodayIncome: Boolean = true,
    // 显示本月支出
    val showMonthExpense: Boolean = true,
    // 显示本月收入
    val showMonthIncome: Boolean = true,
    // 显示账户余额
    val showAccountBalance: Boolean = true,
    // 显示预算进度
    val showBudgetProgress: Boolean = true,
    // 显示最近交易
    val showRecentTransactions: Boolean = true,
    // 最近交易显示数量
    val recentTransactionCount: Int = 5
)

/**
 * 记账提醒设置
 */
data class ReminderSettings(
    // 是否启用每日记账提醒
    val enableDailyReminder: Boolean = false,
    // 每日提醒时间（格式：HH:mm）
    val dailyReminderTime: String = "20:00",
    // 是否启用周末提醒
    val enableWeekendReminder: Boolean = true,
    // 是否启用月末提醒
    val enableMonthEndReminder: Boolean = false,
    // 月末提醒提前天数
    val monthEndReminderDays: Int = 2
)

/**
 * 高级设置
 */
data class AdvancedSettings(
    // 显示小数位数
    val decimalPlaces: Int = 2,
    // 是否启用分类图标
    val enableCategoryIcons: Boolean = true,
    // 是否启用账户图标
    val enableAccountIcons: Boolean = true,
    // 是否显示已删除记录
    val showDeletedRecords: Boolean = false,
    // 默认日期选择（0: 今天, 1: 昨天, 2: 自定义）
    val defaultDateSelection: Int = 0
)

/**
 * 自动化设置
 */
data class AutomationSettings(
    // 是否启用智能分类
    val enableSmartCategorization: Boolean = false,
    // 智能分类置信度阈值（0.0-1.0）
    val smartCategorizationThreshold: Float = 0.8f,
    // 是否启用智能记账建议
    val enableSmartSuggestions: Boolean = true,
    // 是否自动创建定期交易
    val enableAutoRecurring: Boolean = true,
    // 支付成功后快速记账（无障碍命中时弹半屏面板）
    val enableQuickAddOnPaymentSuccess: Boolean = true,
    // 自动分类规则
    val autoCategorizationRules: List<AutoCategorizationRule> = emptyList()
)

/**
 * 自动分类规则
 */
data class AutoCategorizationRule(
    val id: Long = 0,
    // 规则名称
    val name: String,
    // 是否启用
    val enabled: Boolean = true,
    // 匹配条件
    val conditions: List<RuleCondition>,
    // 目标分类ID
    val targetCategoryId: Long,
    // 优先级（数字越大优先级越高）
    val priority: Int = 0
)

/**
 * 规则条件
 */
data class RuleCondition(
    // 条件类型（描述、金额、账户等）
    val type: ConditionType,
    // 操作符（包含、等于、大于等）
    val operator: ConditionOperator,
    // 条件值
    val value: String
)

/**
 * 条件类型
 */
enum class ConditionType {
    DESCRIPTION,    // 描述
    AMOUNT,        // 金额
    ACCOUNT,       // 账户
    NOTE           // 备注
}

/**
 * 条件操作符
 */
enum class ConditionOperator {
    CONTAINS,      // 包含
    EQUALS,        // 等于
    STARTS_WITH,   // 开始于
    ENDS_WITH,     // 结束于
    GREATER_THAN,  // 大于
    LESS_THAN,     // 小于
    BETWEEN        // 介于
}
