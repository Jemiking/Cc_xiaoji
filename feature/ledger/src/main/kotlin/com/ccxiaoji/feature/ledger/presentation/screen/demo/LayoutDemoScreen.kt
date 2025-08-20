package com.ccxiaoji.feature.ledger.presentation.screen.demo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ccxiaoji.ui.theme.DesignTokens

data class LayoutDemoItem(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val route: String,
    val pros: List<String>,
    val cons: List<String>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LayoutDemoScreen(
    navController: NavController
) {
    val demoItems = listOf(
        LayoutDemoItem(
            title = "方案一：紧凑型布局",
            description = "减少间距，合并相关组件，保持单列布局",
            icon = compressIcon,
            route = "add_transaction_compact",
            pros = listOf("减少50%垂直空间", "保持熟悉操作流程", "适配各种屏幕"),
            cons = listOf("横向空间可能拥挤", "部分组件功能受限")
        ),
        LayoutDemoItem(
            title = "方案二：卡片分组布局",
            description = "按业务逻辑分组，使用卡片容器",
            icon = Icons.Default.Dashboard,
            route = "add_transaction_cards",
            pros = listOf("信息层次清晰", "功能分组合理", "视觉组织性强"),
            cons = listOf("卡片边框增加复杂度", "可能仍然较高")
        ),
        LayoutDemoItem(
            title = "方案三：分步填写布局",
            description = "核心信息优先，详细信息折叠",
            icon = Icons.Default.ExpandMore,
            route = "add_transaction_stepped",
            pros = listOf("核心操作极简化", "高级功能按需展开", "适应不同场景"),
            cons = listOf("需要额外点击操作", "功能发现性降低")
        ),
        LayoutDemoItem(
            title = "方案四：网格布局",
            description = "充分利用横向空间，网格化布局",
            icon = gridViewIcon,
            route = "add_transaction_grid",
            pros = listOf("最大化屏幕利用率", "信息密度最高", "现代化设计"),
            cons = listOf("小屏幕可能拥挤", "触控目标偏小")
        ),
        LayoutDemoItem(
            title = "方案五：浮动操作布局",
            description = "核心字段固定，次要功能浮动",
            icon = touchAppIcon,
            route = "add_transaction_floating",
            pros = listOf("核心操作突出", "界面简洁清爽", "操作手势友好"),
            cons = listOf("功能可见性差", "学习成本较高")
        ),
        LayoutDemoItem(
            title = "方案六：分类优先布局",
            description = "基于参考界面，分类网格+数字键盘",
            icon = Icons.Default.GridView,
            route = "add_transaction_category_first",
            pros = listOf("分类选择直观", "数字输入快速", "符合使用习惯"),
            cons = listOf("界面元素较多", "需要滚动操作")
        ),
        LayoutDemoItem(
            title = "方案七：简化网格布局",
            description = "精选分类，大按钮，流畅操作",
            icon = Icons.Default.Apps,
            route = "add_transaction_simplified_grid",
            pros = listOf("操作极其简单", "视觉层次清晰", "颜色引导直观"),
            cons = listOf("分类数量有限", "扩展性受限")
        ),
        LayoutDemoItem(
            title = "🛠 布局调节器",
            description = "实时调节方案六的所有布局参数",
            icon = Icons.Default.Tune,
            route = "add_transaction_layout_adjuster",
            pros = listOf("实时预览效果", "精确参数控制", "自定义布局"),
            cons = listOf("调节过程较复杂", "需要反复测试")
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "记账页面布局方案Demo",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Medium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = "返回"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(DesignTokens.Spacing.medium),
            verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.medium)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(DesignTokens.Spacing.large)
                    ) {
                        Text(
                            text = "📋 布局方案比较",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(DesignTokens.Spacing.small))
                        Text(
                            text = "点击下方任意方案查看实际效果，体验不同的布局设计和操作流程。",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            items(demoItems) { item ->
                DemoItemCard(
                    item = item,
                    onClick = { navController.navigate(item.route) }
                )
            }
        }
    }
}

@Composable
private fun DemoItemCard(
    item: LayoutDemoItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(DesignTokens.Spacing.large)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = DesignTokens.BrandColors.Ledger
                )
                Spacer(modifier = Modifier.width(DesignTokens.Spacing.medium))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = item.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = chevronRightIcon,
                    contentDescription = "查看详情"
                )
            }
            
            Spacer(modifier = Modifier.height(DesignTokens.Spacing.medium))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // 优点
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = DesignTokens.BrandColors.Success
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "优点",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            color = DesignTokens.BrandColors.Success
                        )
                    }
                    item.pros.take(2).forEach { pro ->
                        Text(
                            text = "• $pro",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(DesignTokens.Spacing.medium))
                
                // 缺点
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = DesignTokens.BrandColors.Error
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "缺点",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            color = DesignTokens.BrandColors.Error
                        )
                    }
                    item.cons.take(2).forEach { con ->
                        Text(
                            text = "• $con",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

// 图标映射：使用现有图标替代不存在的图标
private val compressIcon: ImageVector = Icons.Default.UnfoldLess
private val gridViewIcon: ImageVector = Icons.Default.Apps  
private val touchAppIcon: ImageVector = Icons.Default.PanTool
private val chevronRightIcon: ImageVector = Icons.Default.ExpandMore