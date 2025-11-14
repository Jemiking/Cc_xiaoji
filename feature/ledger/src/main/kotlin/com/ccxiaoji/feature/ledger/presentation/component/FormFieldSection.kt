package com.ccxiaoji.feature.ledger.presentation.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 通用表单字段区域容器组件
 *
 * 为表单输入控件提供统一的布局结构，包括标题、描述和错误提示。
 * 该组件遵循Material 3设计规范，确保表单在整个应用中的一致性。
 *
 * 特性：
 * - 统一的标题、描述、错误文本布局
 * - 支持前置图标增强视觉引导
 * - 通过配置对象灵活调整样式
 * - 无状态设计，便于在ViewModel中管理
 *
 * @param title 字段标题（必需）
 * @param modifier 外部修饰符
 * @param description 字段描述文本（可选）
 * @param error 错误提示文本（可选）
 * @param leadingIcon 标题前的图标组件（可选）
 * @param config 布局和样式配置
 * @param content 实际的表单控件内容
 */
@Composable
fun FormFieldSection(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    error: String? = null,
    leadingIcon: (@Composable (() -> Unit))? = null,
    config: FormFieldSectionConfig = FormFieldSectionConfig.Default,
    content: @Composable ColumnScope.() -> Unit
) {
    val hasError = !error.isNullOrBlank()

    Column(
        modifier = modifier.padding(
            horizontal = config.horizontalPadding,
            vertical = config.verticalPadding
        )
    ) {
        // 标题行
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            leadingIcon?.let {
                it()
                Spacer(modifier = Modifier.size(config.iconTitleSpacing))
            }

            Text(
                text = title,
                style = config.titleTextStyle ?: MaterialTheme.typography.titleSmall,
                color = if (hasError) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
        }

        // 描述文本
        description?.let { desc ->
            if (desc.isNotBlank()) {
                Spacer(modifier = Modifier.height(config.titleDescriptionSpacing))
                Text(
                    text = desc,
                    style = config.descriptionTextStyle ?: MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 内容区域
        Spacer(modifier = Modifier.height(config.headerContentSpacing))
        content()

        // 错误提示
        if (hasError) {
            Spacer(modifier = Modifier.height(config.contentErrorSpacing))
            Text(
                text = error.orEmpty(),
                style = config.errorTextStyle ?: MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

/**
 * 表单字段区域的布局和样式配置
 *
 * 通过配置对象模式，允许在不修改组件代码的情况下
 * 调整视觉样式，适应不同页面的设计需求。
 */
data class FormFieldSectionConfig(
    /** 水平内边距 */
    val horizontalPadding: Dp = 16.dp,
    /** 垂直内边距 */
    val verticalPadding: Dp = 8.dp,
    /** 图标与标题的间距 */
    val iconTitleSpacing: Dp = 8.dp,
    /** 标题与描述的间距 */
    val titleDescriptionSpacing: Dp = 4.dp,
    /** 头部与内容的间距 */
    val headerContentSpacing: Dp = 8.dp,
    /** 内容与错误提示的间距 */
    val contentErrorSpacing: Dp = 4.dp,
    /** 标题文本样式 */
    val titleTextStyle: TextStyle? = null,
    /** 描述文本样式 */
    val descriptionTextStyle: TextStyle? = null,
    /** 错误文本样式 */
    val errorTextStyle: TextStyle? = null
) {
    companion object {
        /**
         * 默认配置
         *
         * 使用Material 3推荐的间距和文本样式
         */
        val Default = FormFieldSectionConfig()

        /**
         * 紧凑配置
         *
         * 适用于空间有限的场景
         */
        val Compact = FormFieldSectionConfig(
            horizontalPadding = 12.dp,
            verticalPadding = 4.dp,
            titleDescriptionSpacing = 2.dp,
            headerContentSpacing = 4.dp,
            contentErrorSpacing = 2.dp
        )

        /**
         * 宽松配置
         *
         * 适用于需要更多呼吸感的场景
         */
        val Relaxed = FormFieldSectionConfig(
            horizontalPadding = 20.dp,
            verticalPadding = 12.dp,
            titleDescriptionSpacing = 8.dp,
            headerContentSpacing = 12.dp,
            contentErrorSpacing = 8.dp
        )
    }
}