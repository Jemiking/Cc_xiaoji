package com.ccxiaoji.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ccxiaoji.ui.theme.DesignTokens

/**
 * 扁平化底部抽屉组件
 * 遵循极简扁平化设计（方案A）
 * 
 * @param onDismissRequest 关闭抽屉的回调
 * @param sheetState 抽屉状态
 * @param modifier 修饰符
 * @param containerColor 容器颜色
 * @param contentColor 内容颜色
 * @param dragHandle 拖动手柄
 * @param content 内容
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlatBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(),
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    dragHandle: @Composable (() -> Unit)? = { FlatBottomSheetDefaults.DragHandle() },
    content: @Composable ColumnScope.() -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        modifier = modifier,
        shape = RoundedCornerShape(
            topStart = DesignTokens.BorderRadius.large,
            topEnd = DesignTokens.BorderRadius.large,
            bottomStart = 0.dp,
            bottomEnd = 0.dp
        ),
        containerColor = containerColor,
        contentColor = contentColor,
        tonalElevation = 0.dp,
        dragHandle = dragHandle,
        content = content
    )
}

/**
 * 扁平化底部抽屉默认值
 */
object FlatBottomSheetDefaults {
    
    /**
     * 默认拖动手柄
     */
    @Composable
    fun DragHandle(
        modifier: Modifier = Modifier,
        width: Dp = 32.dp,
        height: Dp = 4.dp,
        color: Color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
    ) {
        Box(
            modifier = modifier
                .padding(vertical = DesignTokens.Spacing.small)
                .size(width = width, height = height)
                .clip(RoundedCornerShape(50))
                .background(color)
        )
    }
}

/**
 * 扁平化选择底部抽屉
 * 用于选项列表展示
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> FlatSelectionBottomSheet(
    title: String,
    items: List<T>,
    selectedItem: T?,
    onItemSelected: (T) -> Unit,
    onDismissRequest: () -> Unit,
    itemContent: @Composable (T) -> String,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState()
) {
    FlatBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = DesignTokens.Spacing.medium)
        ) {
            // 标题
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(
                    horizontal = DesignTokens.Spacing.medium,
                    vertical = DesignTokens.Spacing.small
                )
            )
            
            Divider(
                modifier = Modifier.padding(vertical = DesignTokens.Spacing.small),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
            )
            
            // 选项列表
            items.forEach { item ->
                FlatSelectionItem(
                    text = itemContent(item),
                    selected = item == selectedItem,
                    onClick = {
                        onItemSelected(item)
                        onDismissRequest()
                    }
                )
            }
        }
    }
}

/**
 * 扁平化选择项
 */
@Composable
private fun FlatSelectionItem(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        color = if (selected) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
        } else {
            Color.Transparent
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = DesignTokens.Spacing.medium,
                    vertical = DesignTokens.Spacing.medium
                ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = if (selected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
            
            if (selected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}