package com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Checkroom
import androidx.compose.material.icons.outlined.CleaningServices
import androidx.compose.material.icons.outlined.Coffee
import androidx.compose.material.icons.outlined.Cookie
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.ElectricBolt
import androidx.compose.material.icons.outlined.CardGiftcard
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Kitchen
import androidx.compose.material.icons.outlined.LocalCafe
import androidx.compose.material.icons.outlined.LocalGroceryStore
import androidx.compose.material.icons.outlined.MedicalServices
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import kotlin.math.cos
import kotlin.math.sin
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import kotlin.math.min
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.atan2
import kotlin.math.abs
import android.graphics.Paint
import androidx.compose.ui.unit.Dp
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.launch

enum class CategoryDimension { Expense, Income }

enum class CategoryIcon {
    Electronics,    // 电器数码
    Study,         // 学习
    Clothes,       // 衣服
    Restaurant,    // 下馆子
    Supermarket,   // 超市
    Groceries,     // 买菜
    Other,         // 其它
    Gift,          // 请客送礼
    Entertainment, // 娱乐
    Transport,     // 交通
    Medical,       // 医疗
    Daily,         // 日用品
    Snacks,        // 零食
    Breakfast,     // 早餐
    Phone,         // 话费网费
    Essentials,    // 柴米油盐
    Housing,       // 住房
    Drinks         // 饮料
}

data class CategoryEntry(
    val name: String,
    val amount: String,
    val percent: Float, // 0f..1f
    val color: Color,
    val icon: CategoryIcon = CategoryIcon.Other // 添加图标类型
)

data class CategoryState(
    val entries: List<CategoryEntry>
)

/**
 * 标签信息数据类 - 用于防重叠布局
 * 存储每个标签的位置和绘制信息
 */
data class LabelInfo(
    val entry: CategoryEntry,           // 原始分类数据
    val angle: Float,                   // 扇形中心角度
    val anchorX: Float,                 // 锚点X坐标（圆环上的连接点）
    val anchorY: Float,                 // 锚点Y坐标
    val originalY: Float,               // 原始Y位置（径向延伸后的位置）
    var adjustedY: Float,               // 调整后的Y位置（防重叠后）
    val horizontalEndX: Float,          // 水平线终点X坐标
    val isLeftSide: Boolean,           // 是否在左侧（用于文字对齐）
    val text: String                    // 显示文本（名称+百分比）
)

/**
 * 标签布局配置参数
 */
object LabelLayoutConfig {
    const val MIN_LABEL_SPACING = 16f          // 最小垂直间距（px）
    const val MAX_VERTICAL_ADJUSTMENT = 50f     // 最大垂直调整量（px）
    const val RADIAL_EXTENSION = 35f           // 径向延伸长度（px）
    const val MIN_HORIZONTAL_EXT = 25f         // 最小水平延伸（px）
    const val MAX_HORIZONTAL_EXT = 55f         // 最大水平延伸（px）
    const val LABEL_TEXT_SIZE = 10f            // 标签文字大小（sp）
    const val MERGE_THRESHOLD = 0.02f          // 合并阈值（2%）
    const val LINE_COLOR = 0xFFBBBBBB.toInt()  // 引导线颜色
    const val DOT_RADIUS = 2f                  // 圆点半径（px）
    const val TEXT_PADDING = 5f                // 文字与线的间距（px）
}

@Composable
fun CategoryDonutCard(
    state: CategoryState,
    dimension: CategoryDimension,
    onDimensionChange: (CategoryDimension) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = ReportTokens.Palette.Card),
        shape = RoundedCornerShape(ReportTokens.Metrics.CardCorner),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)  // 增加阴影
    ) {
        Column(modifier = Modifier.padding(ReportTokens.Metrics.CardPadding)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "分类报表",
                    color = ReportTokens.Palette.TextPrimary,
                    fontSize = ReportTokens.Type.Title,
                    fontWeight = FontWeight.Medium
                )
                Box(modifier = Modifier.size(1.dp))
            }

            Spacer(Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Donut(
                    entries = state.entries,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(ReportTokens.Metrics.DonutChartHeight)
                )
            }

            Spacer(Modifier.height(12.dp))

            // 说明文案
            Text(
                text = "可手指旋转甜甜圈查看，点击分类查看明细",
                color = ReportTokens.Palette.TextMuted,
                fontSize = ReportTokens.Type.Small,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(12.dp))

            Segmented2(
                labels = listOf("支出", "收入"),
                selected = if (dimension == CategoryDimension.Expense) 0 else 1,
                onSelected = { idx -> onDimensionChange(if (idx == 0) CategoryDimension.Expense else CategoryDimension.Income) }
            )

            Spacer(Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(ReportTokens.Metrics.CategoryItemGap)) {
                state.entries.forEach { entry ->
                    CategoryRow(entry)
                }
            }
        }
    }
}

@Composable
private fun Donut(entries: List<CategoryEntry>, modifier: Modifier = Modifier) {
    // 状态管理
    var rotationAngle by remember { mutableStateOf(0f) }
    val rotationAnimatable = remember { Animatable(0f) }
    var highlightedIndex by remember { mutableStateOf(0) }
    var isDragging by remember { mutableStateOf(false) }
    var previousAngle by remember { mutableStateOf(0f) }
    var velocity by remember { mutableStateOf(0f) }
    val scope = rememberCoroutineScope()

    Box(modifier, contentAlignment = Alignment.Center) {
        Canvas(
            modifier = Modifier
                .matchParentSize()
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            isDragging = true
                            val centerX = size.width / 2f
                            val centerY = size.height / 2f
                            previousAngle = atan2(
                                offset.y - centerY,
                                offset.x - centerX
                            )
                            velocity = 0f
                        },
                        onDragEnd = {
                            isDragging = false
                            // 启动惯性动画
                            if (abs(velocity) > 0.5f) {
                                scope.launch {
                                    rotationAnimatable.snapTo(rotationAngle)
                                    rotationAnimatable.animateDecay(
                                        initialVelocity = velocity * 100,
                                        animationSpec = exponentialDecay(frictionMultiplier = 2f)
                                    ) {
                                        rotationAngle = value
                                        // 检测高亮的分类
                                        highlightedIndex = detectHighlightedSegment(
                                            rotationAngle,
                                            entries
                                        )
                                    }
                                }
                            }
                        }
                    ) { change, _ ->
                        val centerX = size.width / 2f
                        val centerY = size.height / 2f

                        // 计算当前角度
                        val currentAngle = atan2(
                            change.position.y - centerY,
                            change.position.x - centerX
                        )

                        // 计算角度差
                        var deltaAngle = currentAngle - previousAngle

                        // 处理角度跨越问题（从359度到1度）
                        if (deltaAngle > Math.PI) {
                            deltaAngle -= 2 * Math.PI.toFloat()
                        } else if (deltaAngle < -Math.PI) {
                            deltaAngle += 2 * Math.PI.toFloat()
                        }

                        // 转换为度数并更新
                        val deltaDegrees = Math.toDegrees(deltaAngle.toDouble()).toFloat()
                        rotationAngle += deltaDegrees
                        velocity = deltaDegrees
                        previousAngle = currentAngle

                        // 实时更新高亮的分类
                        highlightedIndex = detectHighlightedSegment(rotationAngle, entries)
                    }
                }
        ) {
            val thickness = ReportTokens.Metrics.DonutThickness.toPx()
            val stroke = Stroke(width = thickness)

            // 为标注留出更多空间
            val labelSpace = 80f
            val donutSize = min(size.width - labelSpace * 2, size.height - labelSpace * 2)
            val centerX = size.width / 2f
            val centerY = size.height / 2f
            val radius = donutSize / 2f

            val arcSize = Size(donutSize - thickness, donutSize - thickness)
            val topLeft = Offset(centerX - arcSize.width / 2f, centerY - arcSize.height / 2f)

            val list = entries.takeIf { it.isNotEmpty() }
                ?: listOf(CategoryEntry("暂无数据", "¥0.00", 1f, ReportTokens.Palette.TotalGray))
            val sum = list.sumOf { it.percent.toDouble().coerceIn(0.0, 1.0) }.takeIf { it > 0.0 } ?: 1.0
            val normalized = list.map { (it.percent.toDouble().coerceIn(0.0, 1.0) / sum).toFloat() }

            // 1. 绘制扇形（应用旋转角度）
            var start = -90f + rotationAngle  // 应用旋转偏移
            var acc = 0f
            val angles = mutableListOf<Pair<Float, Float>>() // 记录每个扇形的起始和结束角度

            for (i in normalized.indices) {
                val isHighlighted = i == highlightedIndex
                val color = list[i].color
                val isLast = i == normalized.lastIndex
                val sweep = if (!isLast) (normalized[i] * 360f) else (360f - acc)
                val sweepClamped = sweep.coerceAtLeast(0f)

                angles.add(start to sweepClamped)

                // 高亮效果：增加厚度
                val strokeWidth = if (isHighlighted) thickness * 1.2f else thickness
                val highlightStroke = Stroke(width = strokeWidth)

                drawArc(
                    color = if (isHighlighted) color.copy(alpha = 1f) else color.copy(alpha = 0.9f),
                    startAngle = start,
                    sweepAngle = sweepClamped,
                    useCenter = false,
                    topLeft = if (isHighlighted) {
                        // 高亮扇形稍微放大
                        val scale = 1.05f
                        val scaledSize = Size(arcSize.width * scale, arcSize.height * scale)
                        Offset(centerX - scaledSize.width / 2f, centerY - scaledSize.height / 2f)
                    } else topLeft,
                    size = if (isHighlighted) {
                        val scale = 1.05f
                        Size(arcSize.width * scale, arcSize.height * scale)
                    } else arcSize,
                    style = highlightStroke
                )
                start += sweepClamped
                acc += sweepClamped
            }

            // 2. 使用防重叠标签系统绘制引导线和标注
            drawLabelsWithAntiOverlap(
                list = list,
                normalized = normalized,
                angles = angles,
                centerX = centerX,
                centerY = centerY,
                radius = radius
            )
        }

        // 动态显示当前选中的分类信息
        val highlightedEntry = entries.getOrNull(highlightedIndex)
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = highlightedEntry?.name ?: "支出比例",
                color = ReportTokens.Palette.TextPrimary,
                fontSize = ReportTokens.Type.Body,
                fontWeight = FontWeight.Medium
            )
            if (highlightedEntry != null) {
                Text(
                    text = highlightedEntry.amount,
                    color = ReportTokens.Palette.TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${(highlightedEntry.percent * 100).toInt()}%",
                    color = ReportTokens.Palette.TextMuted,
                    fontSize = ReportTokens.Type.Caption
                )
            }
        }
    }
}

// 检测高亮的扇形
private fun detectHighlightedSegment(
    rotationAngle: Float,
    entries: List<CategoryEntry>
): Int {
    if (entries.isEmpty()) return 0

    // 标准化角度到0-360范围
    val normalizedRotation = ((-rotationAngle) % 360 + 360) % 360

    // 累计角度
    var accumulatedAngle = 0f
    val total = entries.sumOf { it.percent.toDouble() }.toFloat()

    entries.forEachIndexed { index, entry ->
        val segmentAngle = (entry.percent / total) * 360f
        if (normalizedRotation >= accumulatedAngle &&
            normalizedRotation < accumulatedAngle + segmentAngle) {
            return index
        }
        accumulatedAngle += segmentAngle
    }

    return 0
}

@Composable
private fun Segmented2(labels: List<String>, selected: Int, onSelected: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .background(ReportTokens.Palette.ChipContainer, RoundedCornerShape(ReportTokens.Metrics.SegmentedRadius))
            .border(width = 1.dp, color = ReportTokens.Palette.Divider, shape = RoundedCornerShape(ReportTokens.Metrics.SegmentedRadius))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        labels.forEachIndexed { index, label ->
            val selectedState = index == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(28.dp)
                    .clip(RoundedCornerShape(ReportTokens.Metrics.SegmentedRadius))
                    .background(if (selectedState) Color.White else Color.Transparent)
                    .clickable { onSelected(index) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    color = if (selectedState) ReportTokens.Palette.TextPrimary else ReportTokens.Palette.TextSecondary,
                    fontSize = ReportTokens.Type.Body
                )
            }
        }
    }
}

@Composable
private fun CategoryRow(entry: CategoryEntry) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(ReportTokens.Metrics.CategoryRowHeight),
        horizontalArrangement = Arrangement.spacedBy(ReportTokens.Metrics.CategoryIconGap),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 图标
        CategoryIconView(
            icon = entry.icon,
            color = entry.color,
            modifier = Modifier.size(ReportTokens.Metrics.CategoryIcon)
        )

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 显示名称和百分比（保留2位小数）
                Text(
                    text = "${entry.name} (${String.format("%.2f", entry.percent * 100)}%)",
                    color = ReportTokens.Palette.TextPrimary,
                    fontSize = ReportTokens.Type.Body,
                    fontWeight = FontWeight.Normal
                )

                // 金额右对齐
                Text(
                    text = entry.amount,
                    color = ReportTokens.Palette.TextPrimary,
                    fontSize = ReportTokens.Type.Body,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(Modifier.height(4.dp))

            // 进度条（直角）
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(ReportTokens.Metrics.ProgressHeight)
                    .background(ReportTokens.Palette.ProgressTrack)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(entry.percent.coerceIn(0.02f, 1f))
                        .height(ReportTokens.Metrics.ProgressHeight)
                        .background(entry.color)
                )
            }
        }
    }
}

// 新增图标组件
@Composable
private fun CategoryIconView(
    icon: CategoryIcon,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = when (icon) {
                CategoryIcon.Electronics -> Icons.Outlined.ElectricBolt
                CategoryIcon.Study -> Icons.Outlined.MenuBook
                CategoryIcon.Clothes -> Icons.Outlined.Checkroom
                CategoryIcon.Restaurant -> Icons.Outlined.Restaurant
                CategoryIcon.Supermarket -> Icons.Outlined.ShoppingCart
                CategoryIcon.Groceries -> Icons.Outlined.LocalGroceryStore
                CategoryIcon.Gift -> Icons.Outlined.CardGiftcard
                CategoryIcon.Entertainment -> Icons.Outlined.SportsEsports
                CategoryIcon.Transport -> Icons.Outlined.DirectionsCar
                CategoryIcon.Medical -> Icons.Outlined.MedicalServices
                CategoryIcon.Daily -> Icons.Outlined.CleaningServices
                CategoryIcon.Snacks -> Icons.Outlined.Cookie
                CategoryIcon.Breakfast -> Icons.Outlined.Coffee
                CategoryIcon.Phone -> Icons.Outlined.PhoneAndroid
                CategoryIcon.Essentials -> Icons.Outlined.Kitchen
                CategoryIcon.Housing -> Icons.Outlined.Home
                CategoryIcon.Drinks -> Icons.Outlined.LocalCafe
                else -> Icons.Outlined.MoreHoriz
            },
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(16.dp)
        )
    }
}

/**
 * 带防重叠功能的标签绘制系统
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawLabelsWithAntiOverlap(
    list: List<CategoryEntry>,
    normalized: List<Float>,
    angles: List<Pair<Float, Float>>,
    centerX: Float,
    centerY: Float,
    radius: Float
) {
    val threshold = LabelLayoutConfig.MERGE_THRESHOLD
    val minSpacing = LabelLayoutConfig.MIN_LABEL_SPACING

    // 阶段1：收集所有标签信息
    val leftLabels = mutableListOf<LabelInfo>()
    val rightLabels = mutableListOf<LabelInfo>()

    list.forEachIndexed { index, entry ->
        if (normalized[index] >= threshold) {
            val (startAngle, sweepAngle) = angles[index]
            val midAngle = startAngle + sweepAngle / 2f
            val angleRad = Math.toRadians(midAngle.toDouble())

            // 计算锚点（圆环上的点）
            val anchorX = centerX + radius * cos(angleRad).toFloat()
            val anchorY = centerY + radius * sin(angleRad).toFloat()

            // 计算径向延伸点
            val radialEndX = centerX + (radius + LabelLayoutConfig.RADIAL_EXTENSION) * cos(angleRad).toFloat()
            val radialEndY = centerY + (radius + LabelLayoutConfig.RADIAL_EXTENSION) * sin(angleRad).toFloat()

            // 智能计算水平延伸长度
            val verticalDistance = kotlin.math.abs(kotlin.math.sin(angleRad)).toFloat()
            val horizontalLength = LabelLayoutConfig.MIN_HORIZONTAL_EXT +
                (LabelLayoutConfig.MAX_HORIZONTAL_EXT - LabelLayoutConfig.MIN_HORIZONTAL_EXT) * (1f - verticalDistance)

            // 判断左右侧（归一化角度到0-360范围，防止旋转多圈后判断失效）
            val normalizedMidAngle = ((midAngle % 360f) + 360f) % 360f
            val isLeftSide = normalizedMidAngle > 90f && normalizedMidAngle < 270f
            val horizontalEndX = if (isLeftSide) {
                radialEndX - horizontalLength
            } else {
                radialEndX + horizontalLength
            }

            // 创建标签信息
            val labelInfo = LabelInfo(
                entry = entry,
                angle = midAngle,
                anchorX = anchorX,
                anchorY = anchorY,
                originalY = radialEndY,
                adjustedY = radialEndY,
                horizontalEndX = horizontalEndX,
                isLeftSide = isLeftSide,
                text = "${entry.name} ${(entry.percent * 100).toInt()}%"
            )

            // 按左右分组
            if (isLeftSide) {
                leftLabels.add(labelInfo)
            } else {
                rightLabels.add(labelInfo)
            }
        }
    }

    // 阶段2：调整标签位置避免重叠
    adjustLabelPositions(leftLabels, minSpacing)
    adjustLabelPositions(rightLabels, minSpacing)

    // 阶段3：绘制调整后的标签和引导线
    (leftLabels + rightLabels).forEach { label ->
        drawAdjustedLabel(label, centerX, centerY, radius)
    }
}

/**
 * 调整标签位置以避免重叠
 */
private fun adjustLabelPositions(
    labels: MutableList<LabelInfo>,
    minSpacing: Float
) {
    if (labels.size <= 1) return

    // 按Y坐标排序（从上到下）
    labels.sortBy { it.originalY }

    // 从上到下检查重叠，必要时向下移动
    for (i in 1 until labels.size) {
        val prevLabel = labels[i - 1]
        val currLabel = labels[i]
        val requiredY = prevLabel.adjustedY + minSpacing

        if (currLabel.adjustedY < requiredY) {
            // 发生重叠，向下移动当前标签
            currLabel.adjustedY = requiredY
        }
    }

    // 计算整体偏移并回调
    val totalOffset = labels.sumOf { (it.adjustedY - it.originalY).toDouble() }.toFloat()
    val averageOffset = totalOffset / labels.size

    // 如果整体偏移过大，进行部分回调
    if (kotlin.math.abs(averageOffset) > 20f) {
        labels.forEach { label ->
            label.adjustedY -= averageOffset / 2f
        }
    }
}

/**
 * 绘制单个调整后的标签
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawAdjustedLabel(
    label: LabelInfo,
    centerX: Float,
    centerY: Float,
    radius: Float
) {
    val angleRad = Math.toRadians(label.angle.toDouble())

    // 计算径向延伸点
    val radialEndX = centerX + (radius + LabelLayoutConfig.RADIAL_EXTENSION) * cos(angleRad).toFloat()
    val radialEndY = centerY + (radius + LabelLayoutConfig.RADIAL_EXTENSION) * sin(angleRad).toFloat()

    // 判断是否需要垂直调整
    val needsVerticalAdjustment = kotlin.math.abs(label.adjustedY - radialEndY) > 2f

    if (!needsVerticalAdjustment) {
        // 标准L形引导线
        drawLine(
            color = Color(LabelLayoutConfig.LINE_COLOR),
            start = Offset(label.anchorX, label.anchorY),
            end = Offset(radialEndX, radialEndY),
            strokeWidth = 1f
        )
        drawLine(
            color = Color(LabelLayoutConfig.LINE_COLOR),
            start = Offset(radialEndX, radialEndY),
            end = Offset(label.horizontalEndX, label.adjustedY),
            strokeWidth = 1f
        )
    } else {
        // 三段式引导线（有垂直调整）
        val verticalX = radialEndX + if (label.isLeftSide) -10f else 10f

        // 径向段
        drawLine(
            color = Color(LabelLayoutConfig.LINE_COLOR),
            start = Offset(label.anchorX, label.anchorY),
            end = Offset(radialEndX, radialEndY),
            strokeWidth = 1f
        )

        // 垂直调整段
        drawLine(
            color = Color(LabelLayoutConfig.LINE_COLOR),
            start = Offset(radialEndX, radialEndY),
            end = Offset(verticalX, label.adjustedY),
            strokeWidth = 1f
        )

        // 水平段
        drawLine(
            color = Color(LabelLayoutConfig.LINE_COLOR),
            start = Offset(verticalX, label.adjustedY),
            end = Offset(label.horizontalEndX, label.adjustedY),
            strokeWidth = 1f
        )
    }

    // 绘制圆点
    drawCircle(
        color = label.entry.color,
        radius = LabelLayoutConfig.DOT_RADIUS,
        center = Offset(label.anchorX, label.anchorY)
    )

    // 绘制文字
    drawIntoCanvas { canvas ->
        val paint = Paint().apply {
            color = label.entry.color.toArgb()  // 使用分类颜色
            textSize = LabelLayoutConfig.LABEL_TEXT_SIZE.sp.toPx()
            isAntiAlias = true
            textAlign = if (label.isLeftSide) Paint.Align.RIGHT else Paint.Align.LEFT
        }

        val textX = if (label.isLeftSide) {
            label.horizontalEndX - LabelLayoutConfig.TEXT_PADDING
        } else {
            label.horizontalEndX + LabelLayoutConfig.TEXT_PADDING
        }

        canvas.nativeCanvas.drawText(
            label.text,
            textX,
            label.adjustedY + paint.textSize / 3f,
            paint
        )
    }
}

