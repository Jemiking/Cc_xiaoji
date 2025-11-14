package com.ccxiaoji.feature.ledger.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ccxiaoji.feature.ledger.R
import com.ccxiaoji.ui.theme.DesignTokens

/**
 * 颜色选择对话框
 *
 * 提供预设颜色选择和自定义颜色输入功能。
 * 适用于分类、标签等需要颜色标识的场景。
 *
 * 特性：
 * - Material 3预设颜色网格
 * - 自定义十六进制颜色输入
 * - 实时颜色预览
 * - 颜色格式验证
 *
 * @param onColorSelected 选择颜色后的回调，返回Color对象
 * @param onDismissRequest 关闭对话框的回调
 * @param modifier 外部修饰符
 * @param initialColor 初始选中的颜色
 * @param title 对话框标题
 */
@Composable
fun ColorPickerDialog(
    onColorSelected: (Color) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    initialColor: Color? = null,
    title: String = stringResource(R.string.select_color)
) {
    val defaultColors = remember { ColorPickerDefaults.presetColors() }

    val initialHex = remember(initialColor) {
        initialColor?.toHexString() ?: "#66BB6A"
    }

    var hexInput by rememberSaveable { mutableStateOf(initialHex) }
    var selectedColor by remember { mutableStateOf(initialColor ?: parseHexColor(initialHex)) }

    // 验证十六进制颜色格式
    val isValidHex = remember(hexInput) {
        isValidHexColor(hexInput)
    }

    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f)
            ) {
                // 颜色预览
                ColorPreview(
                    color = selectedColor,
                    hexString = selectedColor.toHexString()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 预设颜色网格
                Text(
                    text = stringResource(R.string.preset_colors),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                ColorGrid(
                    colors = defaultColors,
                    selectedColor = selectedColor,
                    onColorClick = { color ->
                        selectedColor = color
                        hexInput = color.toHexString()
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 自定义颜色输入
                Text(
                    text = stringResource(R.string.custom_color),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    value = hexInput,
                    onValueChange = { input ->
                        hexInput = input
                        if (isValidHexColor(input)) {
                            selectedColor = parseHexColor(input)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("#RRGGBB") },
                    isError = !isValidHex && hexInput.isNotEmpty(),
                    supportingText = {
                        if (!isValidHex && hexInput.isNotEmpty()) {
                            Text(
                                text = stringResource(R.string.invalid_color_format),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onColorSelected(selectedColor)
                },
                enabled = isValidHex
            ) {
                Text(stringResource(R.string.confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

/**
 * 颜色预览组件
 */
@Composable
private fun ColorPreview(
    color: Color,
    hexString: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(color)
                    .border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = CircleShape
                    )
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = stringResource(R.string.current_color),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = hexString,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

/**
 * 颜色网格展示
 */
@Composable
private fun ColorGrid(
    colors: List<ColorOption>,
    selectedColor: Color,
    onColorClick: (Color) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(6),
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = colors,
            key = { it.name }
        ) { colorOption ->
            ColorItem(
                color = colorOption.color,
                isSelected = colorOption.color.toHexString() == selectedColor.toHexString(),
                onClick = { onColorClick(colorOption.color) }
            )
        }
    }
}

/**
 * 单个颜色项
 */
@Composable
private fun ColorItem(
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(color)
            .border(
                width = if (isSelected) 3.dp else 1.dp,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                },
                shape = CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = Color.White
            )
        }
    }
}

/**
 * 颜色选项数据模型
 */
data class ColorOption(
    val name: String,
    val color: Color
)

/**
 * 默认颜色集合
 */
object ColorPickerDefaults {
    fun presetColors(): List<ColorOption> = listOf(
        // 品牌色
        ColorOption("记账绿", DesignTokens.BrandColors.Ledger),
        ColorOption("待办蓝", DesignTokens.BrandColors.Todo),
        ColorOption("习惯紫", DesignTokens.BrandColors.Habit),
        ColorOption("排班橙", DesignTokens.BrandColors.Schedule),
        ColorOption("计划棕", DesignTokens.BrandColors.Plan),

        // 功能色
        ColorOption("成功绿", DesignTokens.BrandColors.Success),
        ColorOption("警告橙", DesignTokens.BrandColors.Warning),
        ColorOption("错误红", DesignTokens.BrandColors.Error),
        ColorOption("信息蓝", DesignTokens.BrandColors.Info),

        // Material 3标准色
        ColorOption("紫色", Color(0xFF6750A4)),
        ColorOption("蓝色", Color(0xFF0061A4)),
        ColorOption("青色", Color(0xFF006874)),
        ColorOption("绿色", Color(0xFF006E26)),
        ColorOption("黄色", Color(0xFF7C5800)),
        ColorOption("橙色", Color(0xFF8B5000)),
        ColorOption("红色", Color(0xFFBA1A1A)),
        ColorOption("粉色", Color(0xFF984061)),

        // 中性色
        ColorOption("灰色1", Color(0xFF605D62)),
        ColorOption("灰色2", Color(0xFF79747E)),
        ColorOption("灰色3", Color(0xFF938F99)),
        ColorOption("灰色4", Color(0xFFCAC4D0)),

        // 额外颜色
        ColorOption("深蓝", Color(0xFF1E3A8A)),
        ColorOption("天蓝", Color(0xFF0EA5E9)),
        ColorOption("青绿", Color(0xFF14B8A6)),
        ColorOption("草绿", Color(0xFF84CC16)),
        ColorOption("琥珀", Color(0xFFF59E0B)),
        ColorOption("深橙", Color(0xFFEA580C)),
        ColorOption("玫红", Color(0xFFE11D48)),
        ColorOption("深紫", Color(0xFF9333EA))
    )
}

/**
 * 验证十六进制颜色格式
 */
private fun isValidHexColor(hex: String): Boolean {
    val cleanHex = hex.trim()
    val regex = "^#?([A-Fa-f0-9]{6}|[A-Fa-f0-9]{8})$".toRegex()
    return regex.matches(cleanHex)
}

/**
 * 解析十六进制颜色字符串
 */
private fun parseHexColor(hex: String): Color {
    val cleanHex = hex.trim().removePrefix("#")
    return try {
        when (cleanHex.length) {
            6 -> {
                val r = cleanHex.substring(0, 2).toInt(16)
                val g = cleanHex.substring(2, 4).toInt(16)
                val b = cleanHex.substring(4, 6).toInt(16)
                Color(r, g, b)
            }
            8 -> {
                val a = cleanHex.substring(0, 2).toInt(16)
                val r = cleanHex.substring(2, 4).toInt(16)
                val g = cleanHex.substring(4, 6).toInt(16)
                val b = cleanHex.substring(6, 8).toInt(16)
                Color(r, g, b, a)
            }
            else -> DesignTokens.BrandColors.Ledger
        }
    } catch (e: Exception) {
        DesignTokens.BrandColors.Ledger
    }
}

/**
 * 将Color转换为十六进制字符串
 */
private fun Color.toHexString(): String {
    val r = (red * 255).toInt()
    val g = (green * 255).toInt()
    val b = (blue * 255).toInt()
    return String.format("#%02X%02X%02X", r, g, b)
}