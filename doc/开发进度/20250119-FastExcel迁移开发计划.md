# FastExcel迁移开发计划

> 制定时间：2025-01-19  
> 项目：CC小记 Excel功能迁移  
> 目标：从Apache POI迁移到FastExcel  
> 预计工期：10个工作日

## 一、项目背景

### 1.1 迁移原因
- **性能问题**：POI处理50MB文件内存占用高，易OOM
- **依赖问题**：POI Android Port 3.17版本老旧，存在兼容性问题
- **包体积**：POI占用8.2MB，严重影响APK大小

### 1.2 迁移目标
- **性能提升**：内存占用降低67%，处理速度提升6倍
- **包体积优化**：APK减少7MB
- **代码简化**：移除复杂的兼容层，降低维护成本

### 1.3 核心原则
- Excel仅作为数据导入导出工具，不追求复杂样式
- 保证数据准确性和完整性
- 用户体验优先，性能优先

## 二、开发计划概览

### 2.1 时间安排
- **总工期**：10个工作日（2025-01-20 至 2025-01-31）
- **每日工作时间**：8小时
- **缓冲时间**：预留20%用于处理意外情况

### 2.2 里程碑
1. **第3天**：完成导出功能迁移
2. **第7天**：完成导入功能迁移
3. **第9天**：完成测试和优化
4. **第10天**：完成文档和发布准备

## 三、详细任务分解

### 第一阶段：准备工作（Day 1）

#### 任务1.1：环境准备和依赖配置
**时间**：2小时  
**负责人**：开发者  
**具体内容**：
```kotlin
// 1. 在app/build.gradle.kts中添加FastExcel依赖
dependencies {
    // 移除POI依赖
    // implementation("com.github.SUPERCILEX.poi-android:poi:3.17")
    
    // 添加FastExcel
    implementation("com.github.dhatim:fastexcel:0.17.0")
    implementation("com.github.dhatim:fastexcel-reader:0.17.0")
}

// 2. 同步项目，确保依赖下载成功
// 3. 运行一个简单的FastExcel示例验证环境
```

**验收标准**：
- [ ] FastExcel依赖成功添加
- [ ] 编译无错误
- [ ] 示例代码运行成功

#### 任务1.2：代码结构分析和备份
**时间**：2小时  
**具体内容**：
1. 备份现有Excel相关代码
2. 分析POI使用点，创建迁移清单
3. 创建feature分支：`feature/fastexcel-migration`

**交付物**：
- 迁移清单文档
- Git分支创建

#### 任务1.3：创建FastExcel工具类框架
**时间**：4小时  
**具体内容**：
```kotlin
// 创建新的包结构
package com.ccxiaoji.app.data.excel.fastexcel

// FastExcelWriter.kt
class FastExcelWriter {
    fun exportToExcel(
        outputStream: OutputStream,
        data: ExcelExportData,
        onProgress: (Float) -> Unit = {}
    ): ExportResult {
        // TODO: 实现
    }
}

// FastExcelReader.kt
class FastExcelReader {
    fun importFromExcel(
        inputStream: InputStream,
        config: ImportConfig,
        onProgress: (Float) -> Unit = {}
    ): ImportResult {
        // TODO: 实现
    }
}

// FastExcelStyleHelper.kt
object FastExcelStyleHelper {
    fun createHeaderStyle(): Style {
        // TODO: 实现
    }
    
    fun createDataStyle(): Style {
        // TODO: 实现
    }
}
```

**验收标准**：
- [ ] 基础类结构创建完成
- [ ] 编译通过

### 第二阶段：导出功能迁移（Day 2-3）

#### 任务2.1：实现基础导出功能
**时间**：8小时  
**具体内容**：

```kotlin
// 1. 实现账单导出
class FastExcelWriter {
    fun exportTransactions(
        transactions: List<Transaction>,
        outputStream: OutputStream
    ) {
        Workbook(outputStream, "CC小记", "1.0").use { workbook ->
            val sheet = workbook.newWorksheet("交易记录")
            
            // 写入标题行
            writeTransactionHeaders(sheet)
            
            // 写入数据
            writeTransactionData(sheet, transactions)
            
            // 设置列宽
            setColumnWidths(sheet)
        }
    }
    
    private fun writeTransactionHeaders(sheet: Worksheet) {
        val headerStyle = StyleSetter()
            .fillColor("4472C4")
            .fontColor("FFFFFF")
            .bold()
            .build()
            
        val headers = listOf(
            "日期", "时间", "类型", "分类", 
            "金额", "账户", "备注", "标签"
        )
        
        headers.forEachIndexed { col, header ->
            sheet.value(0, col, header, headerStyle)
        }
    }
}

// 2. 实现任务导出
// 3. 实现习惯导出
// 4. 实现其他模块导出
```

**验收标准**：
- [ ] 各模块导出功能实现
- [ ] 数据格式正确
- [ ] 基础样式应用成功

#### 任务2.2：样式和格式处理
**时间**：4小时  
**具体内容**：
```kotlin
object FastExcelStyleHelper {
    // 数字格式
    fun createMoneyStyle(): Style {
        return StyleSetter()
            .format("#,##0.00")
            .build()
    }
    
    // 日期格式
    fun createDateStyle(): Style {
        return StyleSetter()
            .format("yyyy-MM-dd")
            .build()
    }
    
    // 收入样式（绿色）
    fun createIncomeStyle(): Style {
        return StyleSetter()
            .fontColor("00B050")
            .format("#,##0.00")
            .build()
    }
    
    // 支出样式（红色）
    fun createExpenseStyle(): Style {
        return StyleSetter()
            .fontColor("FF0000")
            .format("#,##0.00")
            .build()
    }
}
```

**验收标准**：
- [x] 日期格式正确显示
- [x] 货币格式带千分位
- [x] 收支颜色区分

#### 任务2.3：性能优化和进度反馈
**时间**：4小时  
**具体内容**：
```kotlin
// 批量写入优化
class FastExcelWriter {
    fun exportLargeDataset(
        data: List<Any>,
        outputStream: OutputStream,
        onProgress: (Float) -> Unit
    ) {
        val totalRows = data.size
        var processedRows = 0
        
        Workbook(outputStream, "CC小记", "1.0").use { workbook ->
            val sheet = workbook.newWorksheet("数据")
            
            // 分批写入
            data.chunked(1000).forEach { chunk ->
                writeChunk(sheet, chunk, processedRows)
                processedRows += chunk.size
                
                // 报告进度
                val progress = processedRows.toFloat() / totalRows
                onProgress(progress)
            }
        }
    }
}
```

**验收标准**：
- [x] 50MB文件导出不OOM
- [x] 进度回调正常工作
- [x] 导出时间<30秒

### 第三阶段：导入功能迁移（Day 4-7）

#### 任务3.1：实现基础导入功能
**时间**：8小时  
**具体内容**：
```kotlin
class FastExcelReader {
    fun readExcel(inputStream: InputStream): List<Map<String, Any?>> {
        val result = mutableListOf<Map<String, Any?>>()
        
        ReadableWorkbook(inputStream).use { workbook ->
            val sheet = workbook.getFirstSheet()
            
            // 读取标题行
            val headers = readHeaders(sheet)
            
            // 读取数据行
            sheet.openStream().use { rows ->
                rows.forEach { row ->
                    if (row.rowNum > 0) {
                        val rowData = readRowData(row, headers)
                        result.add(rowData)
                    }
                }
            }
        }
        
        return result
    }
    
    private fun readHeaders(sheet: Sheet): List<String> {
        // 读取第一行作为标题
        return sheet.openStream().use { rows ->
            rows.firstOrNull()?.let { firstRow ->
                (0 until firstRow.cellCount).map { col ->
                    firstRow.getCellText(col) ?: ""
                }
            } ?: emptyList()
        }
    }
}
```

**验收标准**：
- [x] 正确读取Excel数据
- [x] 支持多种数据类型
- [x] 处理空值情况

#### 任务3.2：数据解析和验证
**时间**：8小时  
**具体内容**：
```kotlin
// 数据解析器
class ExcelDataParser {
    fun parseTransaction(row: Map<String, Any?>): Transaction? {
        return try {
            Transaction(
                date = parseDate(row["日期"]),
                category = row["分类"] as? String ?: "",
                amount = parseAmount(row["金额"]),
                account = row["账户"] as? String ?: "",
                note = row["备注"] as? String ?: ""
            )
        } catch (e: Exception) {
            null // 记录错误，返回null
        }
    }
    
    private fun parseDate(value: Any?): LocalDate {
        return when (value) {
            is LocalDate -> value
            is String -> LocalDate.parse(value)
            is Number -> // Excel日期序列号转换
                LocalDate.of(1900, 1, 1).plusDays(value.toLong() - 2)
            else -> throw IllegalArgumentException("无法解析日期: $value")
        }
    }
    
    private fun parseAmount(value: Any?): Double {
        return when (value) {
            is Number -> value.toDouble()
            is String -> value.replace(",", "")
                .replace("￥", "")
                .replace("¥", "")
                .toDoubleOrNull() ?: 0.0
            else -> 0.0
        }
    }
}
```

**验收标准**：
- [ ] 日期解析正确
- [ ] 金额解析处理各种格式
- [ ] 错误数据有合理处理

#### 任务3.3：批量导入和错误处理
**时间**：8小时  
**具体内容**：
```kotlin
class BatchImportProcessor {
    fun processBatch(
        data: List<Map<String, Any?>>,
        onProgress: (Float) -> Unit,
        onError: (ImportError) -> Unit
    ): ImportResult {
        val total = data.size
        var processed = 0
        var success = 0
        val errors = mutableListOf<ImportError>()
        
        data.forEachIndexed { index, row ->
            try {
                // 解析和验证
                val transaction = parseAndValidate(row)
                
                // 保存到数据库
                saveTransaction(transaction)
                
                success++
            } catch (e: Exception) {
                val error = ImportError(
                    row = index + 1,
                    message = e.message ?: "未知错误",
                    data = row
                )
                errors.add(error)
                onError(error)
            }
            
            processed++
            onProgress(processed.toFloat() / total)
        }
        
        return ImportResult(
            total = total,
            success = success,
            errors = errors
        )
    }
}
```

**验收标准**：
- [ ] 批量导入事务处理
- [ ] 详细的错误报告
- [ ] 进度反馈准确

#### 任务3.4：余额验证（特定于记账模块）
**时间**：4小时  
**具体内容**：
```kotlin
class BalanceValidator {
    fun validateTransactions(
        transactions: List<Transaction>,
        accounts: Map<String, Account>
    ): ValidationResult {
        val balanceMap = mutableMapOf<String, Double>()
        val errors = mutableListOf<BalanceError>()
        
        // 初始化账户余额
        accounts.forEach { (name, account) ->
            balanceMap[name] = account.initialBalance
        }
        
        // 逐笔验证
        transactions.sortedBy { it.date }.forEach { transaction ->
            val account = transaction.account
            val currentBalance = balanceMap[account] ?: 0.0
            
            // 更新余额
            val newBalance = when (transaction.type) {
                TransactionType.INCOME -> currentBalance + transaction.amount
                TransactionType.EXPENSE -> currentBalance - transaction.amount
                TransactionType.TRANSFER -> currentBalance // 转账需特殊处理
            }
            
            balanceMap[account] = newBalance
            
            // 检查余额是否匹配
            if (transaction.balance != null && 
                abs(transaction.balance - newBalance) > 0.01) {
                errors.add(BalanceError(
                    transaction = transaction,
                    expectedBalance = newBalance,
                    actualBalance = transaction.balance
                ))
            }
        }
        
        return ValidationResult(errors)
    }
}
```

**验收标准**：
- [ ] 余额计算准确
- [ ] 误差在0.01以内
- [ ] 清晰的错误提示

### 第四阶段：集成和测试（Day 8-9）

#### 任务4.1：替换现有POI调用
**时间**：8小时  
**具体内容**：
```kotlin
// 修改ExcelManager.kt
class ExcelManager @Inject constructor(
    private val context: Context,
    private val ledgerApi: LedgerApi,
    private val todoApi: TodoApi,
    private val habitApi: HabitApi
) {
    // 使用FastExcel替代POI
    private val writer = FastExcelWriter()
    private val reader = FastExcelReader()
    
    suspend fun exportToExcel(
        uri: Uri,
        config: ExcelExportConfig,
        onProgress: (Float) -> Unit = {}
    ): ExportResult {
        return withContext(Dispatchers.IO) {
            context.contentResolver.openOutputStream(uri)?.use { output ->
                when (config.dataType) {
                    DataType.TRANSACTION -> 
                        writer.exportTransactions(getTransactions(), output)
                    DataType.TODO -> 
                        writer.exportTodos(getTodos(), output)
                    DataType.HABIT -> 
                        writer.exportHabits(getHabits(), output)
                }
            } ?: throw IOException("无法打开输出流")
        }
    }
}
```

**验收标准**：
- [ ] 所有POI引用已替换
- [ ] 编译无错误
- [ ] 功能正常工作

#### 任务4.2：单元测试编写
**时间**：4小时  
**具体内容**：
```kotlin
class FastExcelWriterTest {
    @Test
    fun `test export transactions`() {
        // 准备测试数据
        val transactions = listOf(
            Transaction(
                date = LocalDate.now(),
                category = "餐饮",
                amount = 50.0,
                account = "现金"
            )
        )
        
        // 导出到内存
        val output = ByteArrayOutputStream()
        val writer = FastExcelWriter()
        writer.exportTransactions(transactions, output)
        
        // 验证输出
        val bytes = output.toByteArray()
        assertTrue(bytes.isNotEmpty())
        
        // 读取验证
        val input = ByteArrayInputStream(bytes)
        val reader = FastExcelReader()
        val imported = reader.readExcel(input)
        
        assertEquals(1, imported.size)
        assertEquals("餐饮", imported[0]["分类"])
    }
}
```

**验收标准**：
- [ ] 核心功能测试覆盖
- [ ] 边界条件测试
- [ ] 性能测试通过

#### 任务4.3：集成测试
**时间**：4小时  
**具体内容**：
1. 真机测试各模块导入导出
2. 测试大文件处理（10MB、50MB）
3. 测试错误处理和恢复
4. 测试不同Android版本兼容性

**测试清单**：
- [ ] 账单导出导入
- [ ] 任务导出导入
- [ ] 习惯导出导入
- [ ] 50MB文件测试
- [ ] 内存监控
- [ ] 性能对比

### 第五阶段：优化和收尾（Day 10）

#### 任务5.1：性能优化
**时间**：3小时  
**具体内容**：
1. 分析性能瓶颈
2. 优化内存使用
3. 优化导出速度

**优化项**：
- [ ] 减少对象创建
- [ ] 优化批处理大小
- [ ] 调整缓冲区大小

#### 任务5.2：文档更新
**时间**：3小时  
**具体内容**：
```markdown
# Excel导入导出使用说明

## 支持的格式
- 文件格式：XLSX（Excel 2007+）
- 编码：UTF-8
- 最大支持：100万行

## 导出格式说明
### 账单导出
| 列名 | 说明 | 格式 |
|-----|------|------|
| 日期 | 交易日期 | yyyy-MM-dd |
| 分类 | 交易分类 | 文本 |
| 金额 | 交易金额 | #,##0.00 |
| 账户 | 所属账户 | 文本 |
| 备注 | 备注信息 | 文本 |

## 导入要求
1. 第一行必须是标题行
2. 日期格式支持：yyyy-MM-dd、yyyy/MM/dd
3. 金额支持：带千分位、货币符号
```

**交付物**：
- [ ] 用户使用文档
- [ ] 开发者文档
- [ ] API变更说明

#### 任务5.3：发布准备
**时间**：2小时  
**具体内容**：
1. 代码审查
2. 清理调试代码
3. 更新版本号
4. 准备发布说明

**检查清单**：
- [ ] 移除所有TODO
- [ ] 移除调试日志
- [ ] 更新CHANGELOG
- [ ] APK大小对比

## 四、风险管理

### 4.1 技术风险
| 风险 | 概率 | 影响 | 应对措施 |
|------|------|------|----------|
| FastExcel bug | 低 | 高 | 保留POI分支，可快速回滚 |
| 性能不达标 | 低 | 中 | 提前性能测试，优化算法 |
| 兼容性问题 | 中 | 中 | 多版本Android测试 |

### 4.2 进度风险
| 风险 | 概率 | 影响 | 应对措施 |
|------|------|------|----------|
| 工期延误 | 低 | 中 | 预留20%缓冲时间 |
| 需求变更 | 低 | 高 | 冻结需求，变更走新版本 |

## 五、验收标准

### 5.1 功能验收
- [ ] 所有模块导入导出功能正常
- [ ] 数据准确性100%
- [ ] 错误处理完善

### 5.2 性能验收
- [ ] 50MB文件处理时间<30秒
- [ ] 内存峰值<100MB
- [ ] 无OOM崩溃

### 5.3 质量验收
- [ ] 单元测试覆盖率>80%
- [ ] 无编译警告
- [ ] 代码审查通过

## 六、项目交付物

1. **代码交付**
   - FastExcel实现代码
   - 单元测试代码
   - 集成测试代码

2. **文档交付**
   - 用户使用说明
   - API文档
   - 性能测试报告

3. **其他交付**
   - APK对比报告
   - 风险评估报告
   - 项目总结

## 七、后续计划

### 7.1 短期（1个月内）
- 监控线上表现
- 收集用户反馈
- 修复发现的问题

### 7.2 中期（3个月内）
- 优化导入性能
- 添加更多格式支持
- 完善错误提示

### 7.3 长期（6个月内）
- 考虑支持CSV格式
- 探索其他轻量级方案
- 持续性能优化

---

*本计划将根据实际开发情况动态调整，每日更新进度，确保项目按时交付。*