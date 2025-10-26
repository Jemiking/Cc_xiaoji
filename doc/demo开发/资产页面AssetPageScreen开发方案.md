# 资产页面 AssetPageScreen 1:1 复刻开发方案

## 📋 概述

本文档详细记录了资产页面（AssetPageScreen）的像素级复刻开发方案，基于提供的设计图进行1:1还原。

**创建日期**: 2025-10-05
**目标页面**: AssetPageScreen.kt
**设计参考**: 理想效果图（包含净资产、信用卡、资金等模块）

---

## 🎯 核心问题分析

### 主要差异点

1. **架构问题**
   - 缺少滚动功能（当`enableInterop=false`时）
   - 顶部间距计算错误（spacedBy影响了三点图标）

2. **组件缺失**
   - "今日免息"缺少红色圆形图标
   - 信用卡第一项应为红色三角形警告图标
   - 所有账户图标缺少颜色区分

3. **文字内容错误**
   - "弹花呗" → "骅-花呗"
   - "榕花呗" → "榕-花呗"
   - "骋" → "骅"（4处）
   - 榕系账户缺少连字符"-"（4处）

---

## 🛠️ 完整开发实现方案

### 0. 整体架构修复

```kotlin
@Composable
fun AssetPageScreen(navController: NavController) {
    val t = Tokens
    val ctx = LocalContext.current
    val enableInterop = remember(ctx) { DeviceUtils.isLongShotInteropRecommended(ctx) }

    Surface(color = t.Bg) {
        if (!enableInterop) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()) // ← 添加滚动
                    .statusBarsPadding()
                    .padding(horizontal = 30.dp)
            ) {
                // 顶部三点图标（单独处理，不受spacedBy影响）
                TopBarSection(t)

                // 主内容区（有间距）
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    OverviewCard(t)
                    StatsCard(t)
                    CreditSection(t)
                    AssetSection(t)
                    Spacer(Modifier.height(24.dp))
                }
            }
        } else {
            // 保持原有的AndroidView实现
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    val host = androidx.core.widget.NestedScrollView(context).apply {
                        isFillViewport = true
                        overScrollMode = android.view.View.OVER_SCROLL_ALWAYS
                        isVerticalScrollBarEnabled = true
                    }
                    val compose = androidx.compose.ui.platform.ComposeView(context)
                    host.addView(
                        compose,
                        ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                    )
                    host.tag = compose
                    host
                },
                update = { host ->
                    val compose = host.tag as androidx.compose.ui.platform.ComposeView
                    compose.setContent {
                        MaterialTheme {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .statusBarsPadding()
                                    .padding(horizontal = 30.dp)
                            ) {
                                TopBarSection(t)
                                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                    OverviewCard(t)
                                    StatsCard(t)
                                    CreditSection(t)
                                    AssetSection(t)
                                    Spacer(Modifier.height(24.dp))
                                }
                            }
                        }
                    }
                }
            )
        }
    }
}
```

### 1. 顶部三点图标组件

```kotlin
@Composable
private fun TopBarSection(t: Tokens) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp), // 固定高度
        contentAlignment = Alignment.CenterEnd
    ) {
        IconButton(
            onClick = { /* TODO: 添加菜单功能 */ },
            modifier = Modifier.size(44.dp)
        ) {
            Icon(
                Icons.Filled.MoreVert,
                contentDescription = "更多",
                tint = t.Text.copy(alpha = 0.72f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
```

### 2. 净资产卡片（保持原样）

```kotlin
@Composable
private fun OverviewCard(t: Tokens) {
    Surface(
        color = t.Card,
        shape = RoundedCornerShape(Tokens.RadiusCard),
        shadowElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 30.dp, vertical = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("净资产", color = t.Muted, fontSize = 22.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(8.dp))
            Text(
                text = "-¥2320.49",
                style = TextStyle(
                    color = t.Text,
                    fontSize = 46.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFeatureSettings = "tnum"
                )
            )
            Spacer(Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("总资产", color = t.Muted, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    Text("-¥2140.72", color = t.Text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("总负债", color = t.Muted, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    Text("-¥179.77", color = t.Text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
```

### 3. 总借入/总借出卡片（保持原样）

```kotlin
@Composable
private fun StatsCard(t: Tokens) {
    Surface(
        color = t.Card,
        shape = RoundedCornerShape(Tokens.RadiusCard),
        shadowElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatCell(
                title = "总借入",
                value = "¥0.00",
                leading = {
                    Icon(
                        Icons.Filled.CloudDownload,
                        contentDescription = null,
                        tint = t.Muted,
                        modifier = Modifier.size(20.dp)
                    )
                },
                modifier = Modifier.weight(1f)
            )
            StatCell(
                title = "总借出",
                value = "¥0.00",
                leading = {
                    Icon(
                        Icons.Filled.CloudUpload,
                        contentDescription = null,
                        tint = t.Muted,
                        modifier = Modifier.size(20.dp)
                    )
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StatCell(
    title: String,
    value: String,
    leading: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Tokens.Card,
        shape = RoundedCornerShape(22.dp),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            leading?.invoke()
            Column {
                Text(title, color = Tokens.Muted, fontSize = 13.sp)
                Text(value, color = Tokens.Text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
```

### 4. 信用卡区域（重大修改）

```kotlin
@Composable
private fun CreditSection(t: Tokens) {
    Surface(
        color = t.Card,
        shape = RoundedCornerShape(Tokens.RadiusCard),
        shadowElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp)) {
            // 标题行
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("信用卡", color = t.Text, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("-¥179.77", color = t.Text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        Icons.Filled.ChevronRight,
                        contentDescription = null,
                        tint = Color(0xFFC5C8CE),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // "今日免息"行 - 添加红色图标
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 红色圆形图标容器
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEF4444)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.Notifications,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text("今日免息", color = t.Text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
                Text("骅-花呗 37天", color = t.Muted, fontSize = 14.sp)
            }

            Spacer(Modifier.height(12.dp))

            // 信用卡列表
            val items = listOf(
                DebtVM("信用卡", "-¥12386.52",
                    listOf("30天内还款", "不计入", "可用：¥35613.48"),
                    0.25785f,
                    iconType = DebtIconType.WARNING),
                DebtVM("骅-花呗", "-¥132.86",
                    listOf("5天内还款", "可用：¥1367.14"),
                    0.08857f,
                    iconType = DebtIconType.HUABEI),
                DebtVM("榕-花呗", "-¥46.91",
                    listOf("5天内还款", "可用：¥1453.09"),
                    0.03127f,
                    iconType = DebtIconType.HUABEI),
                DebtVM("京东白条", "-¥1155.34",
                    listOf("12天内还款", "不计入", "可用：¥0.00"),
                    1.0f,
                    iconType = DebtIconType.JD)
            )

            items.forEachIndexed { i, vm ->
                DebtItemV2(vm, t)
                if (i != items.lastIndex) Spacer(Modifier.height(12.dp))
            }
        }
    }
}

// 定义图标类型枚举
enum class DebtIconType {
    WARNING,  // 红色三角形警告
    HUABEI,   // 蓝色圆形花呗
    JD        // 红色圆形京东
}

// 更新DebtVM数据类
private data class DebtVM(
    val title: String,
    val amount: String,
    val pills: List<String>,
    val percent: Float,
    val iconType: DebtIconType
)

// 新的DebtItem实现
@Composable
private fun DebtItemV2(vm: DebtVM, t: Tokens) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // 彩色图标
                DebtIcon(vm.iconType)

                Spacer(Modifier.width(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(vm.title, color = t.Text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        vm.pills.take(3).forEach { Pill(it, t) }
                    }
                }
            }
            Box(modifier = Modifier.width(160.dp), contentAlignment = Alignment.TopEnd) {
                Text(vm.amount, color = t.Text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }
        Spacer(Modifier.height(10.dp))

        // 进度条
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(Tokens.RadiusBar))
                .background(t.BarTrack)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(vm.percent.coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(Tokens.RadiusBar))
                    .background(t.BarFill)
            )
        }
    }
}

// 信用卡图标组件
@Composable
private fun DebtIcon(type: DebtIconType) {
    when (type) {
        DebtIconType.WARNING -> {
            // 红色三角形警告
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFDC2626)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Warning,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        DebtIconType.HUABEI -> {
            // 蓝色圆形花呗
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1677FF)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "花",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        DebtIconType.JD -> {
            // 红色圆形京东
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE1251B)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "JD",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// Pill组件
@Composable
private fun Pill(text: String, t: Tokens) {
    Surface(
        color = t.PillBg,
        shape = RoundedCornerShape(Tokens.RadiusPill),
        tonalElevation = 0.dp
    ) {
        Text(
            text = text,
            color = t.Text,
            fontSize = 14.sp,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
        )
    }
}
```

### 5. 资金区域（添加彩色图标）

```kotlin
@Composable
private fun AssetSection(t: Tokens) {
    Surface(
        color = t.Card,
        shape = RoundedCornerShape(Tokens.RadiusCard),
        shadowElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp)) {
            // 标题行
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("资金", color = t.Text, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("-¥2140.72", color = t.Text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        Icons.Filled.ChevronRight,
                        contentDescription = null,
                        tint = Color(0xFFC5C8CE),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // 修正账户名称和图标颜色
            val rows = listOf(
                AccountInfo("骅-建行卡", "¥65.10", AccountType.BANK),
                AccountInfo("榕-建行", "-¥2426.92", AccountType.BANK),
                AccountInfo("榕-工资卡", "¥5.55", AccountType.SALARY),
                AccountInfo("骅-工资卡", "¥58.00", AccountType.SALARY),
                AccountInfo("骅-支付宝", "¥34.79", AccountType.ALIPAY),
                AccountInfo("榕-支付宝", "¥0.00", AccountType.ALIPAY),
                AccountInfo("骅-微信零钱", "¥217.59", AccountType.WECHAT),
                AccountInfo("榕-微信零钱", "-¥94.83", AccountType.WECHAT)
            )

            rows.forEachIndexed { i, info ->
                AccountRowV2(info, t)
                if (i != rows.lastIndex) Spacer(Modifier.height(10.dp))
            }
        }
    }
}

// 账户类型枚举
enum class AccountType {
    BANK,    // 银行卡 - 蓝色
    SALARY,  // 工资卡 - 紫色
    ALIPAY,  // 支付宝 - 蓝色
    WECHAT   // 微信 - 绿色
}

// 账户信息数据类
data class AccountInfo(
    val name: String,
    val amount: String,
    val type: AccountType
)

// 获取账户颜色
private fun getAccountColor(type: AccountType): Color {
    return when (type) {
        AccountType.BANK -> Color(0xFF3B82F6)    // 蓝色
        AccountType.SALARY -> Color(0xFF8B5CF6)  // 紫色
        AccountType.ALIPAY -> Color(0xFF1677FF)  // 支付宝蓝
        AccountType.WECHAT -> Color(0xFF07C160)  // 微信绿
    }
}

// 新的账户行组件
@Composable
private fun AccountRowV2(info: AccountInfo, t: Tokens) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // 彩色账户图标
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(getAccountColor(info.type)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    info.name.firstOrNull()?.toString() ?: "·",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(Modifier.width(12.dp))
            Text(
                info.name,
                color = t.Text,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        Text(
            info.amount,
            color = t.Text,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
```

### 6. 颜色Token定义

```kotlin
private object Tokens {
    val Bg = Color(0xFFF6F7F9)
    val Card = Color(0xFFFFFFFF)
    val Text = Color(0xFF1A1F24)
    val Muted = Color(0xFF6B7280)
    val PillBg = Color(0xFFF2F3F5)
    val BarTrack = Color(0xFFE8F0FF)
    val BarFill = Color(0xFF2F6FED)

    // 新增颜色定义
    val RedAlert = Color(0xFFEF4444)      // 红色警告/提醒
    val RedWarning = Color(0xFFDC2626)    // 红色警告图标背景
    val BlueAlipay = Color(0xFF1677FF)    // 支付宝蓝
    val RedJD = Color(0xFFE1251B)         // 京东红
    val GreenWechat = Color(0xFF07C160)   // 微信绿
    val PurpleSalary = Color(0xFF8B5CF6)  // 工资卡紫色
    val BlueBank = Color(0xFF3B82F6)      // 银行卡蓝色

    val RadiusCard = 30.dp
    val RadiusPill = 18.dp
    val RadiusIcon = 8.dp
    val RadiusBar = 5.dp
}
```

---

## 📊 设计规格对照表

### 颜色系统

| 用途 | 色值 | 说明 |
|-----|------|-----|
| 页面背景 | `#F6F7F9` | 浅灰色背景 |
| 卡片背景 | `#FFFFFF` | 纯白色 |
| 主文字 | `#1A1F24` | 深黑色 |
| 次要文字 | `#6B7280` | 中灰色 |
| Pills背景 | `#F2F3F5` | 浅灰色 |
| 进度条轨道 | `#E8F0FF` | 浅蓝色 |
| 进度条填充 | `#2F6FED` | 深蓝色 |
| 警告红色 | `#EF4444` | 提醒图标 |
| 支付宝蓝 | `#1677FF` | 品牌色 |
| 京东红 | `#E1251B` | 品牌色 |
| 微信绿 | `#07C160` | 品牌色 |

### 尺寸规格

| 组件 | 尺寸 | 说明 |
|-----|------|-----|
| 页面水平内边距 | 30dp | 两侧留白 |
| 卡片间距 | 16dp | 卡片之间的垂直间距 |
| 卡片圆角 | 30dp | 大卡片的圆角 |
| Pills圆角 | 18dp | 标签的圆角 |
| 图标圆角 | 8dp | 方形图标的圆角 |
| 进度条圆角 | 5dp | 进度条的圆角 |
| 信用卡图标 | 56dp × 56dp | 大图标 |
| 账户图标 | 28dp × 28dp | 小图标 |
| 今日免息图标 | 32dp × 32dp | 中等图标 |

### 字体规格

| 用途 | 字号 | 字重 |
|-----|------|-----|
| 净资产标题 | 22sp | Medium |
| 净资产金额 | 46sp | SemiBold |
| 卡片标题 | 18sp | SemiBold |
| 普通文字 | 16sp | Normal/SemiBold |
| 次要文字 | 14sp | Normal |
| 小标题 | 13sp | Normal |

---

## ✅ 实施步骤

1. **备份现有代码**
   ```bash
   git add .
   git commit -m "备份：AssetPageScreen原始实现"
   ```

2. **更新Tokens定义**
   - 添加新的颜色定义
   - 确保所有颜色值正确

3. **修改整体架构**
   - 添加滚动功能
   - 调整布局结构

4. **逐个更新组件**
   - TopBarSection（分离三点图标）
   - CreditSection（添加图标和修正文字）
   - AssetSection（添加彩色图标）

5. **测试验证**
   - 检查滚动是否正常
   - 验证所有颜色显示
   - 确认文字内容正确

---

## 📝 注意事项

1. **图标选择**
   - 今日免息图标可以使用 `Icons.Filled.Notifications` 或 `Icons.Filled.Schedule`
   - 警告图标使用 `Icons.Filled.Warning`
   - 如需更精确的图标，可以导入自定义SVG

2. **颜色精确性**
   - 品牌色（支付宝、京东、微信）建议使用官方色值
   - 可以根据实际效果微调

3. **性能优化**
   - 考虑使用 `remember` 缓存颜色计算
   - 图标组件可以提取为独立文件

4. **可维护性**
   - 使用枚举管理图标类型和账户类型
   - 保持组件的单一职责原则

---

## 🔄 更新记录

- **2025-10-05**: 初始版本，完成1:1复刻方案
- 待更新：添加点击交互、动画效果

---

## 📚 参考资料

- Material Design 3 指南
- Compose 官方文档
- 原设计图（已归档）