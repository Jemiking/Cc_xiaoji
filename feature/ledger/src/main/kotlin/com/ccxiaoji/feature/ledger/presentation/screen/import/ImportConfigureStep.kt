package com.ccxiaoji.feature.ledger.presentation.screen.import

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ccxiaoji.feature.ledger.domain.importer.ConflictStrategy
import com.ccxiaoji.feature.ledger.domain.importer.ImportConfig
import com.ccxiaoji.feature.ledger.presentation.viewmodel.ImportViewModel

/**
 * 配置步骤
 */
@Composable
fun ConfigureStep(
    config: ImportConfig,
    onConfigChange: (ImportConfig) -> Unit,
    onStartImport: () -> Unit,
    viewModel: ImportViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // 数据类型选择
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "选择要导入的数据",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                DataTypeCheckbox(
                    label = "账户",
                    checked = config.includeAccounts,
                    onCheckedChange = { checked ->
                        viewModel.updateDataTypeSelection(includeAccounts = checked)
                    }
                )
                
                DataTypeCheckbox(
                    label = "分类",
                    checked = config.includeCategories,
                    onCheckedChange = { checked ->
                        viewModel.updateDataTypeSelection(includeCategories = checked)
                    }
                )
                
                DataTypeCheckbox(
                    label = "交易记录",
                    checked = config.includeTransactions,
                    onCheckedChange = { checked ->
                        viewModel.updateDataTypeSelection(includeTransactions = checked)
                    }
                )
                
                DataTypeCheckbox(
                    label = "预算",
                    checked = config.includeBudgets,
                    onCheckedChange = { checked ->
                        viewModel.updateDataTypeSelection(includeBudgets = checked)
                    }
                )
                
                DataTypeCheckbox(
                    label = "储蓄目标",
                    checked = config.includeSavingsGoals,
                    onCheckedChange = { checked ->
                        viewModel.updateDataTypeSelection(includeSavingsGoals = checked)
                    }
                )
                
                DataTypeCheckbox(
                    label = "定期交易",
                    checked = config.includeRecurringTransactions,
                    onCheckedChange = { checked ->
                        viewModel.updateDataTypeSelection(includeRecurringTransactions = checked)
                    }
                )
                
                DataTypeCheckbox(
                    label = "信用卡账单",
                    checked = config.includeCreditCardBills,
                    onCheckedChange = { checked ->
                        viewModel.updateDataTypeSelection(includeCreditCardBills = checked)
                    }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 冲突处理策略
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "重复数据处理",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                ConflictStrategyOption(
                    strategy = ConflictStrategy.SKIP,
                    selected = config.conflictStrategy == ConflictStrategy.SKIP,
                    onSelect = {
                        viewModel.updateConflictStrategy(ConflictStrategy.SKIP)
                    }
                )
                
                ConflictStrategyOption(
                    strategy = ConflictStrategy.RENAME,
                    selected = config.conflictStrategy == ConflictStrategy.RENAME,
                    onSelect = {
                        viewModel.updateConflictStrategy(ConflictStrategy.RENAME)
                    }
                )
                
                ConflictStrategyOption(
                    strategy = ConflictStrategy.MERGE,
                    selected = config.conflictStrategy == ConflictStrategy.MERGE,
                    onSelect = {
                        viewModel.updateConflictStrategy(ConflictStrategy.MERGE)
                    }
                )
                
                ConflictStrategyOption(
                    strategy = ConflictStrategy.OVERWRITE,
                    selected = config.conflictStrategy == ConflictStrategy.OVERWRITE,
                    onSelect = {
                        viewModel.updateConflictStrategy(ConflictStrategy.OVERWRITE)
                    }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 高级选项
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "高级选项",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "允许部分导入",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "遇到错误时继续导入其他数据",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = config.allowPartialImport,
                        onCheckedChange = { checked ->
                            viewModel.updateAllowPartialImport(checked)
                        }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 开始导入按钮
        Button(
            onClick = onStartImport,
            modifier = Modifier.fillMaxWidth(),
            enabled = config.includeAccounts || config.includeCategories || 
                     config.includeTransactions || config.includeBudgets ||
                     config.includeSavingsGoals || config.includeRecurringTransactions ||
                     config.includeCreditCardBills
        ) {
            Text("开始导入")
        }
    }
}

@Composable
private fun DataTypeCheckbox(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun ConflictStrategyOption(
    strategy: ConflictStrategy,
    selected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onSelect
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = when (strategy) {
                    ConflictStrategy.SKIP -> "跳过重复数据"
                    ConflictStrategy.RENAME -> "自动重命名"
                    ConflictStrategy.MERGE -> "合并数据"
                    ConflictStrategy.OVERWRITE -> "覆盖现有数据"
                },
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = when (strategy) {
                    ConflictStrategy.SKIP -> "保留现有数据，跳过重复项"
                    ConflictStrategy.RENAME -> "为重复项添加后缀"
                    ConflictStrategy.MERGE -> "合并到现有数据中"
                    ConflictStrategy.OVERWRITE -> "用新数据替换现有数据"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}