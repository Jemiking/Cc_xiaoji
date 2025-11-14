package com.ccxiaoji.feature.ledger.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.LocalActivity
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocalMall
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Train
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ccxiaoji.feature.ledger.R
import com.ccxiaoji.ui.theme.DesignTokens

/**
 * 图标选择对话框
 *
 * 提供图标网格选择界面，支持分类筛选和搜索功能。
 * 适用于分类、账户等需要图标标识的场景。
 *
 * 特性：
 * - Material Icons图标库
 * - 分类筛选（财务、生活、娱乐等）
 * - 关键词搜索
 * - 选中状态可视化
 *
 * @param onIconSelected 选择图标后的回调，返回图标名称
 * @param onDismissRequest 关闭对话框的回调
 * @param modifier 外部修饰符
 * @param initialSelectedIconName 初始选中的图标名称
 * @param title 对话框标题
 */
@Composable
fun IconPickerDialog(
    onIconSelected: (String) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    initialSelectedIconName: String? = null,
    title: String = stringResource(R.string.select_icon)
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedCategory by rememberSaveable { mutableStateOf(IconCategory.ALL) }
    var currentSelection by rememberSaveable { mutableStateOf(initialSelectedIconName) }

    val allIcons = remember { IconPickerDefaults.allIcons() }
    val filteredIcons = remember(searchQuery, selectedCategory) {
        filterIcons(
            icons = allIcons,
            query = searchQuery,
            category = selectedCategory
        )
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
                    .fillMaxHeight(0.9f)
            ) {
                // 分类选择器
                IconCategoryTabs(
                    selectedCategory = selectedCategory,
                    onCategorySelected = { selectedCategory = it }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 搜索框
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(R.string.search_icon)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null
                        )
                    },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 图标网格
                IconGrid(
                    icons = filteredIcons,
                    selectedIconName = currentSelection,
                    onIconClick = { iconOption ->
                        currentSelection = iconOption.name
                    }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    currentSelection?.let { name ->
                        onIconSelected(name)
                    }
                },
                enabled = currentSelection != null
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
 * 图标分类
 */
enum class IconCategory(val displayName: String) {
    ALL("全部"),
    FINANCE("财务"),
    LIFE("生活"),
    ENTERTAINMENT("娱乐"),
    WORK("工作"),
    OTHER("其他")
}

/**
 * 图标数据模型
 */
data class IconOption(
    val name: String,
    val icon: ImageVector,
    val category: IconCategory,
    val keywords: List<String> = emptyList()
)

/**
 * 图标分类标签页
 */
@Composable
private fun IconCategoryTabs(
    selectedCategory: IconCategory,
    onCategorySelected: (IconCategory) -> Unit
) {
    ScrollableTabRow(
        selectedTabIndex = IconCategory.values().indexOf(selectedCategory),
        edgePadding = 8.dp
    ) {
        IconCategory.values().forEach { category ->
            Tab(
                selected = category == selectedCategory,
                onClick = { onCategorySelected(category) },
                text = {
                    Text(
                        text = category.displayName,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            )
        }
    }
}

/**
 * 图标网格展示
 */
@Composable
private fun IconGrid(
    icons: List<IconOption>,
    selectedIconName: String?,
    onIconClick: (IconOption) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(5),
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = icons,
            key = { it.name }
        ) { iconOption ->
            IconItem(
                iconOption = iconOption,
                isSelected = iconOption.name == selectedIconName,
                onClick = { onIconClick(iconOption) }
            )
        }
    }
}

/**
 * 单个图标项
 */
@Composable
private fun IconItem(
    iconOption: IconOption,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) {
                    DesignTokens.BrandColors.Ledger.copy(alpha = 0.1f)
                } else {
                    Color.Transparent
                }
            )
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) DesignTokens.BrandColors.Ledger else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = iconOption.icon,
            contentDescription = iconOption.name,
            modifier = Modifier.size(24.dp),
            tint = if (isSelected) {
                DesignTokens.BrandColors.Ledger
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        )
    }
}

/**
 * 过滤图标列表
 */
private fun filterIcons(
    icons: List<IconOption>,
    query: String,
    category: IconCategory
): List<IconOption> {
    val normalizedQuery = query.trim().lowercase()

    return icons.filter { icon ->
        val categoryMatch = category == IconCategory.ALL || icon.category == category
        val queryMatch = if (normalizedQuery.isEmpty()) {
            true
        } else {
            icon.name.lowercase().contains(normalizedQuery) ||
            icon.keywords.any { it.lowercase().contains(normalizedQuery) }
        }
        categoryMatch && queryMatch
    }
}

/**
 * 默认图标集合
 */
object IconPickerDefaults {
    fun allIcons(): List<IconOption> = listOf(
        // 财务类
        IconOption(
            name = "AttachMoney",
            icon = Icons.Default.AttachMoney,
            category = IconCategory.FINANCE,
            keywords = listOf("钱", "金钱", "现金", "收入", "支出")
        ),
        IconOption(
            name = "AccountBalance",
            icon = Icons.Default.AccountBalance,
            category = IconCategory.FINANCE,
            keywords = listOf("银行", "账户", "余额")
        ),
        IconOption(
            name = "AccountBalanceWallet",
            icon = Icons.Default.AccountBalanceWallet,
            category = IconCategory.FINANCE,
            keywords = listOf("钱包", "现金")
        ),
        IconOption(
            name = "CreditCard",
            icon = Icons.Default.CreditCard,
            category = IconCategory.FINANCE,
            keywords = listOf("信用卡", "银行卡", "支付")
        ),
        IconOption(
            name = "Savings",
            icon = Icons.Default.Savings,
            category = IconCategory.FINANCE,
            keywords = listOf("存钱", "储蓄", "存款")
        ),

        // 生活类
        IconOption(
            name = "Home",
            icon = Icons.Default.Home,
            category = IconCategory.LIFE,
            keywords = listOf("家", "住宅", "房租", "房贷")
        ),
        IconOption(
            name = "Fastfood",
            icon = Icons.Default.Fastfood,
            category = IconCategory.LIFE,
            keywords = listOf("快餐", "汉堡", "外卖")
        ),
        IconOption(
            name = "Restaurant",
            icon = Icons.Default.Restaurant,
            category = IconCategory.LIFE,
            keywords = listOf("餐厅", "吃饭", "餐饮", "聚餐")
        ),
        IconOption(
            name = "DirectionsCar",
            icon = Icons.Default.DirectionsCar,
            category = IconCategory.LIFE,
            keywords = listOf("汽车", "开车", "交通", "油费")
        ),
        IconOption(
            name = "Train",
            icon = Icons.Default.Train,
            category = IconCategory.LIFE,
            keywords = listOf("火车", "地铁", "公交", "交通")
        ),
        IconOption(
            name = "Flight",
            icon = Icons.Default.Flight,
            category = IconCategory.LIFE,
            keywords = listOf("飞机", "旅行", "出差")
        ),
        IconOption(
            name = "Hotel",
            icon = Icons.Default.Hotel,
            category = IconCategory.LIFE,
            keywords = listOf("酒店", "住宿", "旅游")
        ),
        IconOption(
            name = "LocalMall",
            icon = Icons.Default.LocalMall,
            category = IconCategory.LIFE,
            keywords = listOf("购物", "商场", "买东西")
        ),
        IconOption(
            name = "ShoppingCart",
            icon = Icons.Default.ShoppingCart,
            category = IconCategory.LIFE,
            keywords = listOf("购物车", "超市", "买菜")
        ),
        IconOption(
            name = "LocalHospital",
            icon = Icons.Default.LocalHospital,
            category = IconCategory.LIFE,
            keywords = listOf("医院", "医疗", "看病", "健康")
        ),
        IconOption(
            name = "Phone",
            icon = Icons.Default.Phone,
            category = IconCategory.LIFE,
            keywords = listOf("电话", "话费", "通讯")
        ),

        // 娱乐类
        IconOption(
            name = "Movie",
            icon = Icons.Default.Movie,
            category = IconCategory.ENTERTAINMENT,
            keywords = listOf("电影", "影院", "看电影")
        ),
        IconOption(
            name = "SportsEsports",
            icon = Icons.Default.SportsEsports,
            category = IconCategory.ENTERTAINMENT,
            keywords = listOf("游戏", "电竞", "娱乐")
        ),
        IconOption(
            name = "LocalActivity",
            icon = Icons.Default.LocalActivity,
            category = IconCategory.ENTERTAINMENT,
            keywords = listOf("活动", "门票", "演出")
        ),
        IconOption(
            name = "FitnessCenter",
            icon = Icons.Default.FitnessCenter,
            category = IconCategory.ENTERTAINMENT,
            keywords = listOf("健身", "运动", "健身房")
        ),

        // 工作类
        IconOption(
            name = "Work",
            icon = Icons.Default.Work,
            category = IconCategory.WORK,
            keywords = listOf("工作", "办公", "公司")
        ),
        IconOption(
            name = "School",
            icon = Icons.Default.School,
            category = IconCategory.WORK,
            keywords = listOf("学校", "教育", "学习", "培训")
        )
    )
}