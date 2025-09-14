@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class
)

package com.ccxiaoji.feature.ledger.presentation.screen.card

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.activity.compose.BackHandler
import com.ccxiaoji.feature.ledger.domain.model.Card
import com.ccxiaoji.feature.ledger.domain.model.CardType
import com.ccxiaoji.feature.ledger.presentation.viewmodel.CardViewModel
import com.ccxiaoji.feature.ledger.domain.model.InstitutionType
import androidx.compose.ui.platform.LocalContext
import java.io.File
import java.io.FileOutputStream
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.compose.foundation.layout.FlowRow

@Composable
fun AddEditCardScreen(
    navController: NavHostController,
    cardId: String? = null,
    onNavigateBack: (() -> Unit)? = null
) {
    val vm: CardViewModel = hiltViewModel()
    val uiState by vm.uiState.collectAsState()
    val context = LocalContext.current

    // 初始数据（简化：从列表中查找，真实项目可用 UseCase 按ID加载）
    val initial: Card? = remember(uiState.cards, cardId) {
        uiState.cards.find { it.id == cardId }
    }

    // 表单状态
    var type by remember { mutableStateOf(initial?.cardType ?: CardType.OTHER) }
    var institutionName by remember { mutableStateOf("") } // 银行/券商选择结果（用于快速填充）
    var name by remember { mutableStateOf(initial?.name ?: "") } // 卡片名称（必填）
    var number by remember { mutableStateOf(initial?.maskedNumber ?: "") }
    var showPlain by remember { mutableStateOf(false) } // 卡号显示：掩码/明码
    var holderName by remember { mutableStateOf(initial?.holderName ?: "") }
    var note by remember { mutableStateOf(initial?.note ?: "") }
    var frontPath by remember { mutableStateOf(initial?.frontImagePath) }
    var backPath by remember { mutableStateOf(initial?.backImagePath) }

    // 简易拍照/相片
    var frontTemp: File? by remember { mutableStateOf(null) }
    val takeFront = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
        if (success && frontTemp != null) {
            frontPath = frontTemp!!.absolutePath
            frontTemp = null
        }
    }
    val pickFront = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            val dest = createNewImageFile(context)
            context.contentResolver.openInputStream(it)?.use { input ->
                FileOutputStream(dest).use { out -> input.copyTo(out) }
            }
            frontPath = dest.absolutePath
        }
    }
    var backTemp: File? by remember { mutableStateOf(null) }
    val takeBack = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
        if (success && backTemp != null) {
            backPath = backTemp!!.absolutePath
            backTemp = null
        }
    }
    val pickBack = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            val dest = createNewImageFile(context)
            context.contentResolver.openInputStream(it)?.use { input ->
                FileOutputStream(dest).use { out -> input.copyTo(out) }
            }
            backPath = dest.absolutePath
        }
    }

    // 系统返回
    BackHandler { onNavigateBack?.invoke() ?: navController.popBackStack() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (cardId == null) "添加卡片" else "编辑卡片", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = { onNavigateBack?.invoke() ?: navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        // 校验：名称必填
                        if (name.isBlank()) return@IconButton
                        vm.saveCard(
                            id = initial?.id,
                            name = name,
                            cardType = type,
                            maskedNumber = number,
                            holderName = holderName.ifBlank { null },
                            institutionName = institutionName.ifBlank { null },
                            institutionType = when (type) {
                                CardType.SECURITIES -> InstitutionType.BROKER
                                CardType.BANK_DEBIT, CardType.BANK_CREDIT, CardType.PASSBOOK -> InstitutionType.BANK
                                CardType.OTHER -> InstitutionType.NONE
                            },
                            frontImagePath = frontPath,
                            backImagePath = backPath,
                            expiryMonth = null,
                            expiryYear = null,
                            note = note.ifBlank { null }
                        )
                        onNavigateBack?.invoke() ?: navController.popBackStack()
                    }) {
                        Icon(Icons.Default.Save, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 类型选择
            TypeSelector(type = type, onChange = { type = it })

            // 机构 + 名称（同一行，机构用于快速填充名称；机构可空）
            if (type != CardType.OTHER) {
                InstitutionAndNameRow(
                    type = type,
                    institutionName = institutionName,
                    onInstitutionChange = { sel ->
                        institutionName = sel
                        // 若名称为空或用户未自定义，可智能填充
                        if (name.isBlank()) name = buildNameFromInstitution(type, sel)
                    },
                    name = name,
                    onNameChange = { name = it }
                )
            } else {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("卡片名称（必填）") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            // 卡号（右上角掩码/明码开关）
            CardNumberField(
                number = number,
                onNumberChange = { number = it },
                showPlain = showPlain,
                onToggle = { showPlain = it }
            )

            // 户名
            OutlinedTextField(
                value = holderName,
                onValueChange = { holderName = it },
                label = { Text("户名") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // 备注
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("备注") },
                modifier = Modifier.fillMaxWidth()
            )

            // 照片：仅保留“拍照/相片”
            PhotoRow(
                title = "正面",
                onTake = {
                    val (uri, file) = createTempCaptureUri(context)
                    frontTemp = file
                    takeFront.launch(uri)
                },
                onPick = { pickFront.launch("image/*") }
            )
            PhotoRow(
                title = "背面",
                onTake = {
                    val (uri, file) = createTempCaptureUri(context)
                    backTemp = file
                    takeBack.launch(uri)
                },
                onPick = { pickBack.launch("image/*") }
            )
        }
    }
}

@Composable
private fun TypeSelector(type: CardType, onChange: (CardType) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("类型（必选）", style = MaterialTheme.typography.labelLarge)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(
                CardType.BANK_DEBIT to Icons.Filled.Wallet,
                CardType.BANK_CREDIT to Icons.Filled.CreditCard,
                CardType.PASSBOOK to Icons.Filled.Wallet,
                CardType.SECURITIES to Icons.Filled.Badge,
                CardType.OTHER to Icons.Filled.CreditCard
            ).forEach { (t, icon) ->
                FilterChip(
                    selected = type == t,
                    onClick = { onChange(t) },
                    label = { Text(labelOf(t)) },
                    leadingIcon = { Icon(icon, contentDescription = null) }
                )
            }
        }
    }
}

@Composable
private fun InstitutionAndNameRow(
    type: CardType,
    institutionName: String,
    onInstitutionChange: (String) -> Unit,
    name: String,
    onNameChange: (String) -> Unit
) {
    val label = if (type == CardType.SECURITIES) "券商" else "银行"
    val options = remember(type) {
        when (type) {
            CardType.SECURITIES -> defaultBrokers
            else -> defaultBanks
        }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        // 机构选择（可空）+ 自定义输入
        var expanded by remember { mutableStateOf(false) }
        Column(modifier = Modifier.weight(1f)) {
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                OutlinedTextField(
                    value = institutionName,
                    onValueChange = onInstitutionChange, // 允许自定义输入补充
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    label = { Text(label) },
                    singleLine = true
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    options.forEach { item ->
                        DropdownMenuItem(text = { Text(item) }, onClick = {
                            onInstitutionChange(item)
                            expanded = false
                        })
                    }
                }
            }
        }
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("卡片名称（必填）") },
            modifier = Modifier.weight(1f),
            singleLine = true
        )
    }
}

@Composable
private fun CardNumberField(
    number: String,
    onNumberChange: (String) -> Unit,
    showPlain: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("卡号", style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(if (showPlain) "明码" else "掩码", style = MaterialTheme.typography.labelSmall)
                Switch(checked = showPlain, onCheckedChange = onToggle)
            }
        }
        OutlinedTextField(
            value = if (showPlain) number else mask(number),
            onValueChange = onNumberChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("仅提示，不强制校验") }
        )
    }
}

@Composable
private fun PhotoRow(title: String, onTake: () -> Unit, onPick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(title, style = MaterialTheme.typography.labelLarge, modifier = Modifier.width(56.dp))
        OutlinedButton(onClick = onTake) { Text("拍照") }
        OutlinedButton(onClick = onPick) { Text("相片") }
    }
}

// 简化工具：默认银行/券商（允许自定义输入补充）
private val defaultBanks = listOf("招商银行", "建设银行", "工商银行", "农业银行", "交通银行", "中国银行")
private val defaultBrokers = listOf("中信证券", "国泰君安", "海通证券", "华泰证券")

private fun labelOf(t: CardType) = when (t) {
    CardType.BANK_DEBIT -> "储蓄卡"
    CardType.BANK_CREDIT -> "信用卡"
    CardType.PASSBOOK -> "存折"
    CardType.SECURITIES -> "证券账户"
    CardType.OTHER -> "其他"
}

private fun buildNameFromInstitution(type: CardType, institution: String): String {
    if (institution.isBlank()) return ""
    val suffix = when (type) {
        CardType.BANK_DEBIT -> "储蓄卡"
        CardType.BANK_CREDIT -> "信用卡"
        CardType.PASSBOOK -> "存折"
        CardType.SECURITIES -> "证券账户"
        CardType.OTHER -> ""
    }
    return if (suffix.isBlank()) institution else "$institution $suffix"
}

private fun mask(input: String): String {
    val digits = input.filter { it.isDigit() }
    return if (digits.length >= 4) "**** **** **** ${digits.takeLast(4)}" else input
}

private fun createTempCaptureUri(context: android.content.Context): Pair<Uri, File> {
    val cacheDir = File(context.cacheDir, "images").apply { if (!exists()) mkdirs() }
    val tempFile = File(cacheDir, "capture_${System.currentTimeMillis()}.jpg")
    val authority = context.packageName + ".fileprovider"
    val uri = FileProvider.getUriForFile(context, authority, tempFile)
    return uri to tempFile
}

private fun createNewImageFile(context: android.content.Context): File {
    val dir = File(context.filesDir, "cards").apply { if (!exists()) mkdirs() }
    return File(dir, "img_${System.currentTimeMillis()}.jpg")
}
