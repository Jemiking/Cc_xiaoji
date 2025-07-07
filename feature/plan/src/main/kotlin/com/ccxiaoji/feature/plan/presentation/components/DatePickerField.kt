package com.ccxiaoji.feature.plan.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.navigation.NavController
import kotlinx.datetime.*

/**
 * 可重用的日期选择器字段 - 使用导航而非对话框
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(
    label: String,
    date: LocalDate?,
    onDateChange: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    supportingText: (@Composable () -> Unit)? = null,
    navController: NavController? = null
) {
    // 处理导航返回结果
    navController?.currentBackStackEntry?.savedStateHandle?.let { savedStateHandle ->
        val lifecycleOwner = LocalLifecycleOwner.current
        DisposableEffect(lifecycleOwner) {
            val observer = androidx.lifecycle.Observer<String> { selectedDateStr ->
                selectedDateStr?.let { dateStr ->
                    if (dateStr.isEmpty()) {
                        // 清除日期
                        onDateChange(LocalDate(2000, 1, 1)) // 使用一个特殊值表示清除
                        // 然后在调用方检测这个特殊值并将其转换为null
                    } else {
                        try {
                            val selectedDate = LocalDate.parse(dateStr)
                            onDateChange(selectedDate)
                        } catch (e: Exception) {
                            // 忽略解析错误
                        }
                    }
                    savedStateHandle.remove<String>("selected_date")
                }
            }
            savedStateHandle.getLiveData<String>("selected_date").observe(lifecycleOwner, observer)
            onDispose {
                savedStateHandle.getLiveData<String>("selected_date").removeObserver(observer)
            }
        }
    }
    
    OutlinedTextField(
        value = date?.toString() ?: "",
        onValueChange = { },
        label = { Text(label) },
        placeholder = { Text("选择日期") },
        modifier = modifier,
        readOnly = true,
        trailingIcon = {
            IconButton(onClick = { 
                if (navController != null) {
                    val dateStr = date?.toString() ?: "null"
                    navController.navigate("date_picker/$dateStr")
                }
            }) {
                Icon(Icons.Default.DateRange, contentDescription = "选择日期")
            }
        },
        isError = isError,
        supportingText = supportingText,
        singleLine = true
    )
}

