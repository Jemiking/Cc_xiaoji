package com.ccxiaoji.feature.plan.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

/**
 * 颜色选择器组件 - 使用导航而非对话框
 * 支持预设颜色选择和自定义颜色输入
 */
@Composable
fun ColorPicker(
    selectedColor: String,
    onColorSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "选择颜色",
    enabled: Boolean = true,
    navController: NavController? = null
) {
    // 处理导航返回结果
    navController?.currentBackStackEntry?.savedStateHandle?.let { savedStateHandle ->
        val lifecycleOwner = LocalLifecycleOwner.current
        DisposableEffect(lifecycleOwner) {
            val observer = androidx.lifecycle.Observer<String> { selectedColorStr ->
                selectedColorStr?.let { colorStr ->
                    onColorSelected(colorStr)
                    savedStateHandle.remove<String>("selected_color")
                }
            }
            savedStateHandle.getLiveData<String>("selected_color").observe(lifecycleOwner, observer)
            onDispose {
                savedStateHandle.getLiveData<String>("selected_color").removeObserver(observer)
            }
        }
    }
    
    val currentColor = remember(selectedColor) {
        try {
            Color(android.graphics.Color.parseColor(selectedColor))
        } catch (e: Exception) {
            Color(0xFF6650a4)
        }
    }
    
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // 颜色预览和选择按钮
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                .clickable(enabled = enabled) { 
                    if (navController != null) {
                        val colorStr = selectedColor.replace("#", "%23") // URL encode the # character
                        navController.navigate("color_picker/$colorStr")
                    }
                }
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 颜色预览
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(currentColor)
                    .border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = CircleShape
                    )
            )
            
            // 颜色值文本
            Text(
                text = selectedColor.uppercase(),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            Text(
                text = if (enabled) "点击选择" else "已禁用",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

