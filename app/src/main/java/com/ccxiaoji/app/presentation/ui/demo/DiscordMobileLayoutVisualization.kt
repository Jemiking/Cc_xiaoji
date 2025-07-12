package com.ccxiaoji.app.presentation.ui.demo

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ccxiaoji.ui.theme.DiscordColors

/**
 * Discord移动端布局可视化展示
 */
@Composable
fun DiscordMobileLayoutVisualization() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DiscordColors.Dark.BackgroundDeepest)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // 标题
        Text(
            "Discord风格移动端布局设计",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = DiscordColors.Dark.TextPrimary
        )
        
        // 布局展示
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 默认状态
            LayoutState(
                title = "默认状态",
                modifier = Modifier.weight(1f)
            ) {
                DefaultStateLayout()
            }
            
            // 抽屉打开状态
            LayoutState(
                title = "抽屉打开",
                modifier = Modifier.weight(1f)
            ) {
                DrawerOpenLayout()
            }
            
            // 频道展开状态
            LayoutState(
                title = "频道展开",
                modifier = Modifier.weight(1f)
            ) {
                ChannelExpandedLayout()
            }
        }
        
        // 布局说明
        LayoutExplanation()
    }
}

@Composable
private fun LayoutState(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = DiscordColors.Dark.TextNormal
        )
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(9f / 16f)
                .clip(RoundedCornerShape(8.dp))
                .background(DiscordColors.Dark.BackgroundPrimary)
        ) {
            content()
        }
    }
}

@Composable
private fun DefaultStateLayout() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        
        // 背景
        drawRoundRect(
            color = DiscordColors.Dark.BackgroundPrimary,
            size = size,
            cornerRadius = CornerRadius(8.dp.toPx())
        )
        
        // 顶部栏
        drawRect(
            color = DiscordColors.Dark.BackgroundSidebar,
            size = Size(width, 56.dp.toPx())
        )
        
        // 菜单图标
        drawCircle(
            color = DiscordColors.Dark.TextMuted,
            radius = 12.dp.toPx(),
            center = Offset(24.dp.toPx(), 28.dp.toPx())
        )
        
        // 模块图标和标题
        drawRoundRect(
            color = DiscordColors.Blurple,
            topLeft = Offset(60.dp.toPx(), 12.dp.toPx()),
            size = Size(32.dp.toPx(), 32.dp.toPx()),
            cornerRadius = CornerRadius(8.dp.toPx())
        )
        
        // 搜索和更多图标
        drawCircle(
            color = DiscordColors.Dark.TextMuted,
            radius = 12.dp.toPx(),
            center = Offset(width - 60.dp.toPx(), 28.dp.toPx())
        )
        drawCircle(
            color = DiscordColors.Dark.TextMuted,
            radius = 12.dp.toPx(),
            center = Offset(width - 24.dp.toPx(), 28.dp.toPx())
        )
        
        // 内容区域 - 消息列表
        val messageY = 80.dp.toPx()
        repeat(4) { index ->
            val y = messageY + (index * 80.dp.toPx())
            
            // 头像
            drawCircle(
                color = when(index) {
                    0 -> DiscordColors.Blurple
                    1 -> DiscordColors.Green
                    2 -> DiscordColors.Yellow
                    else -> DiscordColors.Fuchsia
                },
                radius = 20.dp.toPx(),
                center = Offset(24.dp.toPx(), y + 20.dp.toPx())
            )
            
            // 消息内容占位
            drawRoundRect(
                color = DiscordColors.Dark.SurfaceDefault,
                topLeft = Offset(60.dp.toPx(), y),
                size = Size(width - 84.dp.toPx(), 40.dp.toPx()),
                cornerRadius = CornerRadius(4.dp.toPx())
            )
        }
        
        // 底部频道栏
        val bottomBarY = height - 56.dp.toPx()
        drawRoundRect(
            color = DiscordColors.Dark.BackgroundSidebar,
            topLeft = Offset(0f, bottomBarY),
            size = Size(width, 56.dp.toPx()),
            cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx())
        )
        
        // 底部频道标签
        val chipWidth = 80.dp.toPx()
        repeat(3) { index ->
            val x = 16.dp.toPx() + (index * (chipWidth + 12.dp.toPx()))
            drawRoundRect(
                color = if (index == 0) DiscordColors.Dark.SurfaceSelected 
                       else DiscordColors.Dark.SurfaceDefault,
                topLeft = Offset(x, bottomBarY + 12.dp.toPx()),
                size = Size(chipWidth, 32.dp.toPx()),
                cornerRadius = CornerRadius(16.dp.toPx())
            )
        }
    }
}

@Composable
private fun DrawerOpenLayout() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val drawerWidth = width * 0.75f
        val contentOffset = drawerWidth * 0.8f
        
        // 主内容（偏移并缩小）
        drawRoundRect(
            color = DiscordColors.Dark.BackgroundPrimary,
            topLeft = Offset(contentOffset, 0f),
            size = Size(width - contentOffset, height),
            cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx())
        )
        
        // 半透明遮罩
        drawRect(
            color = Color.Black.copy(alpha = 0.3f),
            topLeft = Offset(contentOffset, 0f),
            size = Size(width - contentOffset, height)
        )
        
        // 左侧抽屉
        drawRect(
            color = DiscordColors.Dark.BackgroundDeepest,
            size = Size(drawerWidth, height)
        )
        
        // 用户面板
        drawRect(
            color = DiscordColors.Dark.BackgroundSidebar,
            topLeft = Offset(0f, 48.dp.toPx()),
            size = Size(drawerWidth, 52.dp.toPx())
        )
        
        // 模块列表
        val moduleY = 120.dp.toPx()
        val moduleColors = listOf(
            DiscordColors.Blurple,
            DiscordColors.Green,
            DiscordColors.Yellow,
            DiscordColors.Fuchsia,
            DiscordColors.Red
        )
        
        repeat(5) { index ->
            val y = moduleY + (index * 60.dp.toPx())
            
            // 模块项背景
            if (index == 0) {
                drawRoundRect(
                    color = DiscordColors.Dark.SurfaceHover,
                    topLeft = Offset(12.dp.toPx(), y - 8.dp.toPx()),
                    size = Size(drawerWidth - 24.dp.toPx(), 56.dp.toPx()),
                    cornerRadius = CornerRadius(8.dp.toPx())
                )
            }
            
            // 模块图标
            drawRoundRect(
                color = moduleColors[index],
                topLeft = Offset(24.dp.toPx(), y),
                size = Size(40.dp.toPx(), 40.dp.toPx()),
                cornerRadius = CornerRadius(12.dp.toPx())
            )
            
            // 文字占位
            drawRoundRect(
                color = DiscordColors.Dark.TextMuted.copy(alpha = 0.3f),
                topLeft = Offset(76.dp.toPx(), y + 8.dp.toPx()),
                size = Size(120.dp.toPx(), 24.dp.toPx()),
                cornerRadius = CornerRadius(4.dp.toPx())
            )
        }
    }
}

@Composable
private fun ChannelExpandedLayout() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val expandedHeight = height * 0.6f
        
        // 背景
        drawRoundRect(
            color = DiscordColors.Dark.BackgroundPrimary,
            size = size,
            cornerRadius = CornerRadius(8.dp.toPx())
        )
        
        // 顶部栏
        drawRect(
            color = DiscordColors.Dark.BackgroundSidebar,
            size = Size(width, 56.dp.toPx())
        )
        
        // 内容区域（变暗）
        drawRect(
            color = Color.Black.copy(alpha = 0.3f),
            topLeft = Offset(0f, 56.dp.toPx()),
            size = Size(width, height - 56.dp.toPx() - expandedHeight)
        )
        
        // 展开的频道面板
        val panelY = height - expandedHeight
        drawRoundRect(
            color = DiscordColors.Dark.BackgroundSidebar,
            topLeft = Offset(0f, panelY),
            size = Size(width, expandedHeight),
            cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx()),
            style = Fill
        )
        
        // 拖动手柄
        drawRoundRect(
            color = DiscordColors.Dark.TextMuted.copy(alpha = 0.3f),
            topLeft = Offset(width / 2 - 20.dp.toPx(), panelY + 8.dp.toPx()),
            size = Size(40.dp.toPx(), 4.dp.toPx()),
            cornerRadius = CornerRadius(2.dp.toPx())
        )
        
        // 频道分类
        drawRect(
            color = DiscordColors.Dark.TextMuted.copy(alpha = 0.2f),
            topLeft = Offset(16.dp.toPx(), panelY + 40.dp.toPx()),
            size = Size(100.dp.toPx(), 20.dp.toPx())
        )
        
        // 频道列表
        val channelY = panelY + 80.dp.toPx()
        repeat(4) { index ->
            val y = channelY + (index * 40.dp.toPx())
            
            // 频道项背景
            if (index == 0) {
                drawRoundRect(
                    color = DiscordColors.Dark.SurfaceSelected.copy(alpha = 0.3f),
                    topLeft = Offset(8.dp.toPx(), y - 4.dp.toPx()),
                    size = Size(width - 16.dp.toPx(), 32.dp.toPx()),
                    cornerRadius = CornerRadius(4.dp.toPx())
                )
            }
            
            // 频道图标
            drawCircle(
                color = DiscordColors.Dark.TextMuted,
                radius = 8.dp.toPx(),
                center = Offset(24.dp.toPx(), y + 12.dp.toPx())
            )
            
            // 频道名称占位
            drawRoundRect(
                color = DiscordColors.Dark.TextMuted.copy(alpha = 0.3f),
                topLeft = Offset(44.dp.toPx(), y + 4.dp.toPx()),
                size = Size(80.dp.toPx(), 16.dp.toPx()),
                cornerRadius = CornerRadius(4.dp.toPx())
            )
        }
    }
}

@Composable
private fun LayoutExplanation() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = DiscordColors.Dark.SurfaceDefault
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "移动端Discord布局特点",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = DiscordColors.Dark.TextPrimary
            )
            
            FeatureItem(
                icon = Icons.Default.Layers,
                title = "三层结构",
                description = "抽屉层、主内容层、底部频道层"
            )
            
            FeatureItem(
                icon = Icons.Default.SwipeRight,
                title = "手势操作",
                description = "右滑打开模块列表，左滑关闭"
            )
            
            FeatureItem(
                icon = Icons.Default.ExpandLess,
                title = "可展开频道栏",
                description = "底部栏上滑展开完整频道列表"
            )
            
            FeatureItem(
                icon = Icons.Default.Animation,
                title = "流畅动画",
                description = "所有切换都有平滑的过渡动画"
            )
            
            FeatureItem(
                icon = Icons.Default.DarkMode,
                title = "Discord配色",
                description = "深色主题，紫色品牌色，层次分明"
            )
        }
    }
}

@Composable
private fun FeatureItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = DiscordColors.Blurple,
            modifier = Modifier.size(24.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = DiscordColors.Dark.TextNormal
            )
            Text(
                description,
                fontSize = 12.sp,
                color = DiscordColors.Dark.TextMuted
            )
        }
    }
}

/**
 * ASCII艺术风格的布局展示
 */
@Composable
fun DiscordMobileLayoutASCII() {
    val asciiLayout = """
╔════════════════════════════════════════╗
║ Discord移动端布局结构                   ║
╠════════════════════════════════════════╣
║                                        ║
║  ┌─────────┬──────────────────┐       ║
║  │ 抽屉    │    主内容区       │       ║
║  │ (隐藏)  │                  │       ║
║  │         │  ┌──────────┐    │       ║
║  │ 📱首页  │  │ 顶部栏   │    │       ║
║  │ 💰记账  │  ├──────────┤    │       ║
║  │ ✅待办  │  │          │    │       ║
║  │ 💪习惯  │  │  消息    │    │       ║
║  │ 📅排班  │  │  列表    │    │       ║
║  │         │  │          │    │       ║
║  │         │  │          │    │       ║
║  │         │  ├──────────┤    │       ║
║  │         │  │ 频道栏   │    │       ║
║  └─────────┴──────────────────┘       ║
║                                        ║
║  手势操作:                             ║
║  → 右滑: 打开抽屉                      ║
║  ← 左滑: 关闭抽屉                      ║
║  ↑ 上滑: 展开频道                      ║
║  ↓ 下滑: 收起频道                      ║
╚════════════════════════════════════════╝
    """.trimIndent()
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        color = DiscordColors.Dark.BackgroundDeepest,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = asciiLayout,
            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
            fontSize = 12.sp,
            color = DiscordColors.Dark.TextNormal,
            modifier = Modifier.padding(16.dp)
        )
    }
}