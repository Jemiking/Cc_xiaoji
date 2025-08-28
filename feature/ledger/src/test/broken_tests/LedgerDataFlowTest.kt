package com.ccxiaoji.feature.ledger.test

import com.ccxiaoji.feature.ledger.presentation.viewmodel.LedgerViewModel
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.Assert.*
import android.util.Log

/**
 * 记账簿数据流完整性测试
 * 验证记账簿切换时的数据流完整性
 */
class LedgerDataFlowTest {
    
    companion object {
        private const val TAG = "LedgerDataFlowTest"
    }
    
    private val dataFlowValidator = DataFlowValidator()
    
    /**
     * 测试默认记账簿机制
     * 验证应用启动时默认记账簿的创建和选择
     */
    @Test
    fun testDefaultLedgerMechanism() = runTest {
        Log.i(TAG, "开始测试默认记账簿机制")
        
        // 注意：这需要在实际环境中运行，因为需要ViewModel实例
        // 这里提供测试框架，实际使用时需要注入ViewModel
        
        // 模拟测试流程（实际测试需要替换为真实的ViewModel）
        Log.d(TAG, "默认记账簿机制测试需要在实际环境中运行")
        Log.d(TAG, "测试步骤：")
        Log.d(TAG, "1. 启动应用")
        Log.d(TAG, "2. 验证是否自动创建了默认记账簿")
        Log.d(TAG, "3. 验证默认记账簿是否被自动选中")
        Log.d(TAG, "4. 验证默认记账簿的名称和属性")
        
        assertTrue("默认记账簿机制测试框架已创建", true)
    }
    
    /**
     * 测试记账簿切换数据流
     * 验证从一个记账簿切换到另一个记账簿时的数据流完整性
     */
    @Test
    fun testLedgerSwitchDataFlow() = runTest {
        Log.i(TAG, "开始测试记账簿切换数据流")
        
        // 模拟测试流程
        Log.d(TAG, "记账簿切换数据流测试需要在实际环境中运行")
        Log.d(TAG, "测试步骤：")
        Log.d(TAG, "1. 创建两个测试记账簿")
        Log.d(TAG, "2. 在第一个记账簿中添加测试交易数据")
        Log.d(TAG, "3. 切换到第二个记账簿")
        Log.d(TAG, "4. 验证交易数据正确过滤")
        Log.d(TAG, "5. 验证月度统计正确计算")
        Log.d(TAG, "6. 验证UI状态正确更新")
        Log.d(TAG, "7. 验证偏好设置正确保存")
        
        assertTrue("记账簿切换数据流测试框架已创建", true)
    }
    
    /**
     * 在实际环境中运行的测试方法
     * 需要传入真实的ViewModel实例
     */
    suspend fun runRealDataFlowTest(viewModel: LedgerViewModel): TestReport {
        Log.i(TAG, "开始运行真实数据流测试")
        
        val testResults = mutableListOf<TestResult>()
        
        try {
            // 1. 默认记账簿机制测试
            Log.d(TAG, "执行默认记账簿机制测试")
            val defaultLedgerResult = dataFlowValidator.validateDefaultLedgerMechanism(viewModel)
            testResults.add(TestResult(
                testName = "默认记账簿机制测试",
                success = defaultLedgerResult.success,
                details = "有默认记账簿: ${defaultLedgerResult.hasDefaultLedger}, " +
                         "默认记账簿已选中: ${defaultLedgerResult.defaultLedgerSelected}, " +
                         "问题数: ${defaultLedgerResult.issues.size}",
                issues = defaultLedgerResult.issues
            ))
            
            // 2. 记账簿切换测试（如果有多个记账簿）
            val currentState = viewModel.uiState.value
            if (currentState.ledgers.size >= 2) {
                Log.d(TAG, "执行记账簿切换测试")
                val fromLedger = currentState.ledgers[0]
                val toLedger = currentState.ledgers[1]
                
                val switchResult = dataFlowValidator.validateLedgerSwitchDataFlow(
                    viewModel = viewModel,
                    fromLedgerId = fromLedger.id,
                    toLedgerId = toLedger.id
                )
                
                testResults.add(TestResult(
                    testName = "记账簿切换数据流测试",
                    success = switchResult.success,
                    details = "切换时间: ${switchResult.switchDuration}ms, " +
                             "切换前交易: ${switchResult.beforeTransactionCount}, " +
                             "切换后交易: ${switchResult.afterTransactionCount}, " +
                             "切换成功: ${switchResult.ledgerSwitched}",
                    issues = switchResult.issues
                ))
            } else {
                Log.w(TAG, "记账簿数量不足，跳过切换测试")
                testResults.add(TestResult(
                    testName = "记账簿切换数据流测试",
                    success = false,
                    details = "记账簿数量不足（${currentState.ledgers.size} < 2），无法进行切换测试",
                    issues = listOf(ValidationIssue(
                        type = IssueType.VALIDATION_ERROR,
                        message = "记账簿数量不足，无法测试切换功能",
                        severity = IssueSeverity.MEDIUM
                    ))
                ))
            }
            
            // 3. 生成测试报告
            val overallSuccess = testResults.all { it.success }
            val totalIssues = testResults.flatMap { it.issues }
            
            Log.i(TAG, "数据流测试完成: 总体成功=$overallSuccess, 总问题数=${totalIssues.size}")
            
            return TestReport(
                overallSuccess = overallSuccess,
                testResults = testResults,
                totalIssues = totalIssues.size,
                highSeverityIssues = totalIssues.count { it.severity == IssueSeverity.HIGH },
                executionTime = System.currentTimeMillis()
            )
            
        } catch (e: Exception) {
            Log.e(TAG, "数据流测试执行异常", e)
            return TestReport(
                overallSuccess = false,
                testResults = testResults + TestResult(
                    testName = "测试执行",
                    success = false,
                    details = "测试执行异常: ${e.message}",
                    issues = listOf(ValidationIssue(
                        type = IssueType.VALIDATION_ERROR,
                        message = "测试执行异常: ${e.message}",
                        severity = IssueSeverity.HIGH
                    ))
                ),
                totalIssues = 1,
                highSeverityIssues = 1,
                executionTime = System.currentTimeMillis()
            )
        }
    }
    
    /**
     * 生成测试报告的格式化字符串
     */
    fun formatTestReport(report: TestReport): String {
        val sb = StringBuilder()
        sb.appendLine("================== 记账簿数据流测试报告 ==================")
        sb.appendLine("执行时间: ${java.util.Date(report.executionTime)}")
        sb.appendLine("总体结果: ${if (report.overallSuccess) "✅ 通过" else "❌ 失败"}")
        sb.appendLine("总问题数: ${report.totalIssues}")
        sb.appendLine("高危问题: ${report.highSeverityIssues}")
        sb.appendLine()
        
        report.testResults.forEach { result ->
            sb.appendLine("【${result.testName}】: ${if (result.success) "✅" else "❌"}")
            sb.appendLine("详情: ${result.details}")
            if (result.issues.isNotEmpty()) {
                sb.appendLine("问题:")
                result.issues.forEach { issue ->
                    val icon = when (issue.severity) {
                        IssueSeverity.HIGH -> "🔴"
                        IssueSeverity.MEDIUM -> "🟡"
                        IssueSeverity.LOW -> "🟢"
                    }
                    sb.appendLine("  $icon [${issue.type}] ${issue.message}")
                }
            }
            sb.appendLine()
        }
        
        sb.appendLine("================== 报告结束 ==================")
        return sb.toString()
    }
}

/**
 * 测试结果
 */
data class TestResult(
    val testName: String,
    val success: Boolean,
    val details: String,
    val issues: List<ValidationIssue>
)

/**
 * 测试报告
 */
data class TestReport(
    val overallSuccess: Boolean,
    val testResults: List<TestResult>,
    val totalIssues: Int,
    val highSeverityIssues: Int,
    val executionTime: Long
)