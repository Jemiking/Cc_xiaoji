package com.ccxiaoji.feature.plan.presentation.screen.detail.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ccxiaoji.feature.plan.domain.model.Milestone
import com.ccxiaoji.feature.plan.presentation.viewmodel.PlanDetailViewModel
import com.ccxiaoji.ui.components.ModernCard
import com.ccxiaoji.ui.theme.DesignTokens

/**
 * 里程碑卡片 - 扁平化设计
 */
@Composable
fun MilestonesCard(
    milestones: List<Milestone>,
    onMilestoneToggle: (String) -> Unit,
    onAddMilestone: () -> Unit,
    viewModel: PlanDetailViewModel,
    onEditMilestone: (String) -> Unit = {},
    onDeleteMilestone: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    ModernCard(
        modifier = modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colorScheme.surface,
        borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DesignTokens.Spacing.medium)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "里程碑",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                TextButton(
                    onClick = onAddMilestone,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(DesignTokens.Spacing.xs))
                    Text("添加")
                }
            }
            
            if (milestones.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = DesignTokens.Spacing.large),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "暂无里程碑",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(DesignTokens.Spacing.small))
                Column(
                    verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.small)
                ) {
                    milestones.forEach { milestone ->
                        MilestoneItem(
                            milestone = milestone,
                            onToggle = { onMilestoneToggle(milestone.id) },
                            onEdit = { onEditMilestone(milestone.id) },
                            onDelete = { onDeleteMilestone(milestone.id) }
                        )
                    }
                }
            }
        }
    }
}