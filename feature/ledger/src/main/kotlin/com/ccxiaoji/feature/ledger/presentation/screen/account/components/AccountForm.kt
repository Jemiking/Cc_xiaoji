package com.ccxiaoji.feature.ledger.presentation.screen.account.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.ccxiaoji.feature.ledger.R
import com.ccxiaoji.feature.ledger.domain.model.AccountType

/**
 * 账户表单组件
 *
 * 统一的账户信息输入表单，支持新增和编辑模式
 *
 * @param name 账户名称
 * @param type 账户类型
 * @param balance 账户余额
 * @param onNameChange 名称变更回调
 * @param onTypeChange 类型变更回调
 * @param onBalanceChange 余额变更回调
 * @param modifier 修饰符
 * @param isEditMode 是否为编辑模式
 * @param typeReadOnly 类型是否只读
 * @param nameError 名称错误消息
 * @param balanceError 余额错误消息
 * @param creditCardFields 信用卡特殊字段配置
 * @param enabled 是否启用表单
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountForm(
    name: String,
    type: AccountType,
    balance: String,
    onNameChange: (String) -> Unit,
    onTypeChange: (AccountType) -> Unit,
    onBalanceChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    isEditMode: Boolean = false,
    typeReadOnly: Boolean = false,
    nameError: String? = null,
    balanceError: String? = null,
    creditCardFields: CreditCardFields? = null,
    enabled: Boolean = true
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 账户名称
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("账户名称") },
            placeholder = { Text("请输入账户名称") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.AccountBalance,
                    contentDescription = null
                )
            },
            isError = nameError != null,
            supportingText = if (nameError != null) {
                { Text(nameError, color = MaterialTheme.colorScheme.error) }
            } else null,
            enabled = enabled,
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier.fillMaxWidth()
        )

        // 账户类型
        if (!isEditMode || !typeReadOnly) {
            AccountTypeSelector(
                selectedType = type,
                onTypeSelect = onTypeChange,
                enabled = enabled && !typeReadOnly,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            // 编辑模式下只显示类型，不允许修改
            OutlinedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = getAccountTypeIcon(type),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "账户类型",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = getAccountTypeName(type),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }

        // 账户余额
        OutlinedTextField(
            value = balance,
            onValueChange = onBalanceChange,
            label = { Text(if (isEditMode) "当前余额" else "初始余额") },
            placeholder = { Text("0.00") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.AttachMoney,
                    contentDescription = null
                )
            },
            isError = balanceError != null,
            supportingText = if (balanceError != null) {
                { Text(balanceError, color = MaterialTheme.colorScheme.error) }
            } else null,
            enabled = enabled,
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = if (creditCardFields != null) ImeAction.Next else ImeAction.Done
            ),
            modifier = Modifier.fillMaxWidth()
        )

        // 信用卡特殊字段
        if (type == AccountType.CREDIT_CARD && creditCardFields != null) {
            CreditCardFieldsSection(
                fields = creditCardFields,
                enabled = enabled
            )
        }
    }
}

/**
 * 账户类型选择器
 */
@Composable
private fun AccountTypeSelector(
    selectedType: AccountType,
    onTypeSelect: (AccountType) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    val types = remember {
        listOf(
            AccountType.BANK,
            AccountType.CASH,
            AccountType.CREDIT_CARD,
            AccountType.ALIPAY,
            AccountType.WECHAT,
            AccountType.OTHER
        )
    }

    Column(modifier = modifier) {
        Text(
            text = "选择账户类型",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        types.chunked(3).forEach { rowTypes ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowTypes.forEach { type ->
                    FilterChip(
                        selected = selectedType == type,
                        onClick = { if (enabled) onTypeSelect(type) },
                        label = { Text(getAccountTypeName(type)) },
                        leadingIcon = {
                            Icon(
                                imageVector = getAccountTypeIcon(type),
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        enabled = enabled,
                        modifier = Modifier.weight(1f)
                    )
                }
                // 填充空白
                repeat(3 - rowTypes.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

/**
 * 信用卡特殊字段区域
 */
@Composable
private fun CreditCardFieldsSection(
    fields: CreditCardFields,
    enabled: Boolean
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 信用额度
        OutlinedTextField(
            value = fields.creditLimit,
            onValueChange = fields.onCreditLimitChange,
            label = { Text("信用额度") },
            placeholder = { Text("0.00") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.CreditCard,
                    contentDescription = null
                )
            },
            enabled = enabled,
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 账单日
            OutlinedTextField(
                value = fields.billingDay,
                onValueChange = fields.onBillingDayChange,
                label = { Text("账单日") },
                placeholder = { Text("1-31") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null
                    )
                },
                enabled = enabled,
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.weight(1f)
            )

            // 还款日
            OutlinedTextField(
                value = fields.dueDay,
                onValueChange = fields.onDueDayChange,
                label = { Text("还款日") },
                placeholder = { Text("1-31") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Event,
                        contentDescription = null
                    )
                },
                enabled = enabled,
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * 信用卡特殊字段数据类
 */
data class CreditCardFields(
    val creditLimit: String,
    val billingDay: String,
    val dueDay: String,
    val onCreditLimitChange: (String) -> Unit,
    val onBillingDayChange: (String) -> Unit,
    val onDueDayChange: (String) -> Unit
)

/**
 * 获取账户类型图标
 */
private fun getAccountTypeIcon(type: AccountType) = when (type) {
    AccountType.BANK -> Icons.Default.AccountBalance
    AccountType.CASH -> Icons.Default.Money
    AccountType.CREDIT_CARD -> Icons.Default.CreditCard
    AccountType.ALIPAY -> Icons.Default.PhoneAndroid
    AccountType.WECHAT -> Icons.Default.Message
    AccountType.OTHER -> Icons.Default.MoreHoriz
}

/**
 * 获取账户类型名称
 * 使用AccountType自带的displayName属性
 */
private fun getAccountTypeName(type: AccountType) = type.displayName