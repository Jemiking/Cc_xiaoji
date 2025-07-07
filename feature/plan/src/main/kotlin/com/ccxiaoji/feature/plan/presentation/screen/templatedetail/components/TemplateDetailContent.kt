package com.ccxiaoji.feature.plan.presentation.screen.templatedetail.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ccxiaoji.feature.plan.domain.model.Template
import com.ccxiaoji.ui.theme.DesignTokens

/**
 * 模板详情内容 - 扁平化设计
 */
@Composable
fun TemplateDetailContent(
    template: Template,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(DesignTokens.Spacing.medium),
        verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.medium)
    ) {
        // 模板基本信息卡片
        item {
            TemplateInfoCard(template)
        }
        
        // 模板结构预览
        item {
            TemplateStructureCard(template)
        }
        
        // 使用统计卡片
        item {
            TemplateStatisticsCard(template)
        }
    }
}