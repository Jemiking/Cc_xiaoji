package com.ccxiaoji.feature.plan.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.platform.LocalDensity

/**
 * 标签输入组件
 * 支持添加、删除标签，以及推荐标签功能
 */
@Composable
fun TagInput(
    tags: List<String>,
    onTagsChange: (List<String>) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "标签",
    placeholder: String = "输入标签后按回车",
    maxTags: Int = 10,
    recommendedTags: List<String> = emptyList(),
    enabled: Boolean = true
) {
    var inputText by remember { mutableStateOf("") }
    var showAddField by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    
    Column(modifier = modifier) {
        // 标题和添加按钮
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            if (tags.size < maxTags && enabled) {
                IconButton(
                    onClick = {
                        showAddField = true
                        // 延迟请求焦点，确保组件已经显示
                        
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "添加标签",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        
        // 标签列表
        if (tags.isNotEmpty() || showAddField) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    // 已添加的标签
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        tags.forEach { tag ->
                            TagChip(
                                tag = tag,
                                onRemove = if (enabled) {
                                    { onTagsChange(tags - tag) }
                                } else null
                            )
                        }
                        
                        // 添加标签输入框
                        if (showAddField && enabled) {
                            TagInputField(
                                value = inputText,
                                onValueChange = { inputText = it },
                                onDone = {
                                    if (inputText.isNotBlank() && !tags.contains(inputText.trim())) {
                                        onTagsChange(tags + inputText.trim())
                                        inputText = ""
                                    }
                                    showAddField = false
                                    focusManager.clearFocus()
                                },
                                onCancel = {
                                    inputText = ""
                                    showAddField = false
                                    focusManager.clearFocus()
                                },
                                focusRequester = focusRequester
                            )
                            
                            LaunchedEffect(showAddField) {
                                if (showAddField) {
                                    focusRequester.requestFocus()
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // 推荐标签
        if (recommendedTags.isNotEmpty() && enabled) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "推荐标签",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(recommendedTags.filter { !tags.contains(it) }) { tag ->
                    SuggestionChip(
                        onClick = {
                            if (tags.size < maxTags) {
                                onTagsChange(tags + tag)
                            }
                        },
                        label = { Text(tag) }
                    )
                }
            }
        }
    }
}

/**
 * 标签芯片组件
 */
@Composable
private fun TagChip(
    tag: String,
    onRemove: (() -> Unit)? = null
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = tag,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            if (onRemove != null) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "删除标签",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .clickable { onRemove() }
                )
            }
        }
    }
}

/**
 * 标签输入框
 */
@Composable
private fun TagInputField(
    value: String,
    onValueChange: (String) -> Unit,
    onDone: () -> Unit,
    onCancel: () -> Unit,
    focusRequester: FocusRequester
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .height(32.dp)
            .widthIn(min = 100.dp)
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(16.dp)
            )
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 12.dp)
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .weight(1f)
                .focusRequester(focusRequester),
            textStyle = TextStyle(
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 14.sp
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onDone() }),
            singleLine = true
        )
        
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "取消",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .clickable { onCancel() }
        )
    }
}

/**
 * 流式布局组件
 * 用于自动换行显示标签
 */
@Composable
private fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    Layout(
        modifier = modifier,
        content = content
    ) { measurables, constraints ->
        val rows = mutableListOf<List<Placeable>>()
        var currentRow = mutableListOf<Placeable>()
        var currentRowWidth = 0
        val spacing = with(density) { horizontalArrangement.spacing.roundToPx() }
        val verticalSpacing = with(density) { verticalArrangement.spacing.roundToPx() }
        
        measurables.forEach { measurable ->
            val placeable = measurable.measure(constraints)
            
            if (currentRowWidth + placeable.width + spacing > constraints.maxWidth && currentRow.isNotEmpty()) {
                rows.add(currentRow.toList())
                currentRow = mutableListOf(placeable)
                currentRowWidth = placeable.width
            } else {
                currentRow.add(placeable)
                currentRowWidth += placeable.width + if (currentRow.size > 1) spacing else 0
            }
        }
        
        if (currentRow.isNotEmpty()) {
            rows.add(currentRow)
        }
        
        val height = if (rows.isNotEmpty()) {
            rows.sumOf { row ->
                row.maxOf { placeable -> placeable.height }
            } + (rows.size - 1) * verticalSpacing
        } else 0
        
        layout(constraints.maxWidth, height) {
            var y = 0
            rows.forEach { row ->
                var x = 0
                val rowHeight = row.maxOf { placeable -> placeable.height }
                
                row.forEach { placeable ->
                    placeable.placeRelative(x, y)
                    x += placeable.width + spacing
                }
                
                y += rowHeight + verticalSpacing
            }
        }
    }
}

// 为Arrangement添加扩展属性
private val Arrangement.Horizontal.spacing: Dp
    get() = when (this) {
        Arrangement.Start -> 0.dp
        Arrangement.End -> 0.dp
        Arrangement.Center -> 0.dp
        else -> 8.dp // 默认间距
    }

private val Arrangement.Vertical.spacing: Dp
    get() = when (this) {
        Arrangement.Top -> 0.dp
        Arrangement.Bottom -> 0.dp
        Arrangement.Center -> 0.dp
        else -> 8.dp // 默认间距
    }