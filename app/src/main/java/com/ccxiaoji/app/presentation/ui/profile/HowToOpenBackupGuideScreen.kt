package com.ccxiaoji.app.presentation.ui.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * 如何打开备份文件的详细指南页面
 * 提供各平台操作步骤的图文并茂说明
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HowToOpenBackupGuideScreen(
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("如何打开备份文件") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 介绍卡片
            item {
                IntroductionCard()
            }
            
            // 平台指南列表
            items(platformGuides) { guide ->
                PlatformGuideCard(guide = guide)
            }
            
            // 常见问题
            item {
                FAQCard()
            }
            
            // 技术支持
            item {
                SupportCard()
            }
        }
    }
}

/**
 * 介绍卡片
 */
@Composable
private fun IntroductionCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "关于 .zip 备份文件",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = ".zip 文件是 CC小记 的备份文件格式，包含 CSV 数据的标准压缩包。您可以：",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Column {
                BulletPoint("直接在 CC小记 中导入，恢复完整数据")
                BulletPoint("解压后用 Excel、Numbers、WPS 等软件打开 CSV 文件")
                BulletPoint("通过邮件、云盘等方式在设备间传输")
            }
        }
    }
}

/**
 * 平台指南卡片
 */
@Composable
private fun PlatformGuideCard(
    guide: PlatformGuide
) {
    var isExpanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 可展开的标题行
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        guide.icon,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = guide.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = guide.subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                IconButton(
                    onClick = { isExpanded = !isExpanded }
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "收起" else "展开"
                    )
                }
            }
            
            // 可展开的操作步骤
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Divider()
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    guide.steps.forEachIndexed { index, step ->
                        GuideStepItem(
                            stepNumber = index + 1,
                            step = step
                        )
                        if (index < guide.steps.size - 1) {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                    
                    if (guide.tips.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        TipsSection(tips = guide.tips)
                    }
                }
            }
        }
    }
}

/**
 * 指南步骤项
 */
@Composable
private fun GuideStepItem(
    stepNumber: Int,
    step: GuideStep
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        // 步骤编号
        Box(
            modifier = Modifier.size(28.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = stepNumber.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(6.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // 步骤内容
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = step.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = step.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            if (step.note != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            Icons.Default.Lightbulb,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = step.note,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/**
 * 小贴士部分
 */
@Composable
private fun TipsSection(tips: List<String>) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Lightbulb,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "小贴士",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            tips.forEach { tip ->
                BulletPoint(tip)
            }
        }
    }
}

/**
 * 常见问题卡片
 */
@Composable
private fun FAQCard() {
    var isExpanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Quiz,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "常见问题",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                IconButton(
                    onClick = { isExpanded = !isExpanded }
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "收起" else "展开"
                    )
                }
            }
            
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Divider()
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    faqItems.forEach { faq ->
                        FAQItem(faq = faq)
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

/**
 * FAQ项目
 */
@Composable
private fun FAQItem(faq: FAQ) {
    Column {
        Text(
            text = "Q: ${faq.question}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "A: ${faq.answer}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 技术支持卡片
 */
@Composable
private fun SupportCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Support,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "需要帮助？",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "如果您在使用备份文件时遇到问题，可以通过以下方式获得帮助：",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { /* TODO: 打开邮件客户端 */ },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Email, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("邮件反馈")
                }
                
                OutlinedButton(
                    onClick = { /* TODO: 打开反馈页面 */ },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Feedback, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("意见反馈")
                }
            }
        }
    }
}

/**
 * 项目符号点
 */
@Composable
private fun BulletPoint(text: String) {
    Row(
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Text(
            text = "• ",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

// 数据模型
data class PlatformGuide(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val steps: List<GuideStep>,
    val tips: List<String> = emptyList()
)

data class GuideStep(
    val title: String,
    val description: String,
    val note: String? = null
)

data class FAQ(
    val question: String,
    val answer: String
)

// 静态数据
private val platformGuides = listOf(
    PlatformGuide(
        title = "CC小记应用",
        subtitle = "推荐方式 - 完整数据恢复",
        icon = Icons.Default.Smartphone,
        steps = listOf(
            GuideStep(
                title = "打开导入功能",
                description = "在 CC小记 中点击「数据导出」页面的「导入数据」按钮"
            ),
            GuideStep(
                title = "选择备份文件",
                description = "从文件管理器或云盘中选择 .zip 文件",
                note = "支持从邮件附件、云盘等各种来源选择文件"
            ),
            GuideStep(
                title = "确认导入",
                description = "检查文件信息，确认导入设置，开始导入过程",
                note = "导入过程会自动处理数据冲突和重复项"
            ),
            GuideStep(
                title = "完成恢复",
                description = "等待导入完成，系统会自动恢复所有数据和设置"
            )
        ),
        tips = listOf(
            "这种方式可以完整恢复所有功能和数据",
            "支持增量导入，不会覆盖现有数据",
            "导入速度快，大文件也能快速处理"
        )
    ),
    
    PlatformGuide(
        title = "Microsoft Excel",
        subtitle = "Windows / Mac - 查看和编辑数据",
        icon = Icons.Default.TableChart,
        steps = listOf(
            GuideStep(
                title = "解压备份文件",
                description = "直接使用解压软件打开 .zip 文件",
                note = "也可以使用 WinRAR、7-Zip 等压缩软件直接打开"
            ),
            GuideStep(
                title = "打开 CSV 文件",
                description = "用 Excel 打开解压后的 transactions.csv 等文件"
            ),
            GuideStep(
                title = "设置编码格式",
                description = "如果出现乱码，请选择 UTF-8 编码格式重新打开",
                note = "Excel 2016 及以上版本通常会自动识别 UTF-8 编码"
            ),
            GuideStep(
                title = "查看数据",
                description = "现在可以查看、编辑、分析您的记账数据了"
            )
        ),
        tips = listOf(
            "建议使用 Excel 2016 或更新版本",
            "可以使用 Excel 的透视表功能进行数据分析",
            "编辑后可以另存为新的 CSV 文件"
        )
    ),
    
    PlatformGuide(
        title = "Apple Numbers",
        subtitle = "Mac / iPad - 原生支持",
        icon = Icons.Default.TableView,
        steps = listOf(
            GuideStep(
                title = "解压备份文件",
                description = "双击 .zip 文件，macOS 会自动解压"
            ),
            GuideStep(
                title = "导入到 Numbers",
                description = "打开 Numbers，选择「导入」，选择 CSV 文件"
            ),
            GuideStep(
                title = "格式化数据",
                description = "Numbers 会自动检测数据格式，确认导入设置"
            ),
            GuideStep(
                title = "开始分析",
                description = "使用 Numbers 的图表和分析功能处理您的数据"
            )
        ),
        tips = listOf(
            "Numbers 对中文支持很好，通常不会有乱码问题",
            "可以创建漂亮的图表和报告",
            "支持导出为多种格式"
        )
    ),
    
    PlatformGuide(
        title = "WPS Office",
        subtitle = "全平台 - 国产软件",
        icon = Icons.Default.Description,
        steps = listOf(
            GuideStep(
                title = "解压文件",
                description = "右键点击 .zip 文件，选择「解压到当前文件夹」"
            ),
            GuideStep(
                title = "用 WPS 表格打开",
                description = "右键点击 CSV 文件，选择「用 WPS 表格打开」"
            ),
            GuideStep(
                title = "确认导入设置",
                description = "WPS 会显示导入预览，确认分隔符和编码设置"
            ),
            GuideStep(
                title = "查看和编辑",
                description = "现在可以在 WPS 中查看和编辑您的数据"
            )
        ),
        tips = listOf(
            "WPS 对中文和特殊字符支持很好",
            "免费版本就能满足基本需求",
            "可以直接在手机上使用 WPS 查看"
        )
    ),
    
    PlatformGuide(
        title = "Google Sheets",
        subtitle = "在线表格 - 协同编辑",
        icon = Icons.Default.CloudDone,
        steps = listOf(
            GuideStep(
                title = "解压并上传",
                description = "解压 .zip 文件，将 CSV 文件上传到 Google Drive"
            ),
            GuideStep(
                title = "用 Google Sheets 打开",
                description = "右键点击 CSV 文件，选择「用 Google Sheets 打开」"
            ),
            GuideStep(
                title = "检查格式",
                description = "确认数据格式正确，如有需要可调整列格式"
            ),
            GuideStep(
                title = "在线协作",
                description = "可以分享给他人一起查看和编辑数据"
            )
        ),
        tips = listOf(
            "需要稳定的网络连接",
            "支持实时协作和评论功能",
            "可以使用 Google Apps Script 进行高级处理"
        )
    )
)

private val faqItems = listOf(
    FAQ(
        question = "为什么我的备份文件打不开？",
        answer = "请确认文件扩展名是 .zip，如果还是无法打开，可以尝试使用其他解压软件。"
    ),
    FAQ(
        question = "CSV 文件中的中文显示乱码怎么办？",
        answer = "这通常是编码问题。请在打开 CSV 文件时选择 UTF-8 编码格式，或者使用支持 UTF-8 的软件打开。"
    ),
    FAQ(
        question = "可以编辑 CSV 文件后重新导入吗？",
        answer = "不建议直接编辑 CSV 文件后导入，因为可能破坏数据结构。如需修改数据，建议在 CC小记 中直接编辑。"
    ),
    FAQ(
        question = "备份文件可以在不同设备间传输吗？",
        answer = "可以的！.zip 文件是标准的压缩文件，可以通过邮件、云盘、蓝牙等方式在任意设备间传输。"
    ),
    FAQ(
        question = "多个备份文件可以合并吗？",
        answer = "目前不支持自动合并。如需合并多个备份，建议在同一设备上分别导入，系统会自动处理重复数据。"
    )
)