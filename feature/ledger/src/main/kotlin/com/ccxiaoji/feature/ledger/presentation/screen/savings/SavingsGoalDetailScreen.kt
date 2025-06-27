package com.ccxiaoji.feature.ledger.presentation.screen.savings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ccxiaoji.feature.ledger.domain.model.SavingsContribution
import com.ccxiaoji.feature.ledger.domain.model.SavingsGoal
import com.ccxiaoji.feature.ledger.presentation.component.*
import com.ccxiaoji.feature.ledger.presentation.viewmodel.SavingsGoalViewModel
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavingsGoalDetailScreen(
    goalId: Long,
    onNavigateBack: () -> Unit,
    viewModel: SavingsGoalViewModel = hiltViewModel()
) {
    val goal by viewModel.selectedGoal.collectAsStateWithLifecycle()
    val contributions by viewModel.contributions.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    var showContributionDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var contributionToDelete by remember { mutableStateOf<SavingsContribution?>(null) }
    
    LaunchedEffect(goalId) {
        viewModel.selectGoal(goalId)
    }
    
    LaunchedEffect(uiState.message, uiState.error) {
        if (uiState.message != null || uiState.error != null) {
            kotlinx.coroutines.delay(3000)
            viewModel.clearMessage()
        }
    }
    
    goal?.let { currentGoal ->
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(currentGoal.name) },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                        }
                    },
                    actions = {
                        IconButton(onClick = { showEditDialog = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "编辑")
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "删除")
                        }
                    }
                )
            },
            floatingActionButton = {
                if (!currentGoal.isCompleted) {
                    ExtendedFloatingActionButton(
                        onClick = { showContributionDialog = true },
                        icon = { Icon(Icons.Default.Add, contentDescription = null) },
                        text = { Text("记录存款") }
                    )
                }
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Goal overview card
                item {
                    GoalOverviewCard(goal = currentGoal)
                }
                
                // Progress details
                item {
                    ProgressDetailsCard(goal = currentGoal)
                }
                
                // Quick actions
                if (!currentGoal.isCompleted) {
                    item {
                        QuickActionsCard(
                            onDeposit = { showContributionDialog = true },
                            onWithdraw = { showContributionDialog = true }
                        )
                    }
                }
                
                // Contributions history
                item {
                    Text(
                        text = "存款记录",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                
                if (contributions.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Text(
                                text = "暂无存款记录",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp)
                            )
                        }
                    }
                } else {
                    items(contributions) { contribution ->
                        ContributionItem(
                            contribution = contribution,
                            onDelete = {
                                contributionToDelete = contribution
                            }
                        )
                    }
                }
                
                // Add spacing for FAB
                item {
                    Spacer(modifier = Modifier.height(72.dp))
                }
            }
            
            // Show loading or message
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            // Messages are handled by LaunchedEffect above
        }
        
        // Dialogs
        if (showContributionDialog) {
            ContributionDialog(
                goalName = currentGoal.name,
                onDismiss = { showContributionDialog = false },
                onConfirm = { amount, note, _ ->
                    viewModel.addContribution(currentGoal.id, amount, note)
                    showContributionDialog = false
                }
            )
        }
        
        if (showEditDialog) {
            SavingsGoalDialog(
                goal = currentGoal,
                onDismiss = { showEditDialog = false },
                onConfirm = { name, targetAmount, targetDate, description, color, iconName ->
                    viewModel.updateSavingsGoal(
                        currentGoal.copy(
                            name = name,
                            targetAmount = targetAmount,
                            targetDate = targetDate,
                            description = description,
                            color = color,
                            iconName = iconName,
                            updatedAt = java.time.LocalDateTime.now()
                        )
                    )
                    showEditDialog = false
                }
            )
        }
        
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("删除储蓄目标") },
                text = { Text("确定要删除「${currentGoal.name}」吗？此操作不可恢复。") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteSavingsGoal(currentGoal)
                            showDeleteDialog = false
                            onNavigateBack()
                        }
                    ) {
                        Text("删除", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("取消")
                    }
                }
            )
        }
        
        if (contributionToDelete != null) {
            val contribution = contributionToDelete!!
            AlertDialog(
                onDismissRequest = { contributionToDelete = null },
                title = { Text("删除记录") },
                text = { 
                    Text("确定要删除这条${if (contribution.amount > 0) "存入" else "取出"}记录吗？") 
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteContribution(contribution)
                            contributionToDelete = null
                        }
                    ) {
                        Text("删除", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { contributionToDelete = null }) {
                        Text("取消")
                    }
                }
            )
        }
    } ?: run {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}

@Composable
private fun GoalOverviewCard(goal: SavingsGoal) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(android.graphics.Color.parseColor(goal.color)).copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color(android.graphics.Color.parseColor(goal.color)).copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getIconForGoal(goal.iconName),
                    contentDescription = null,
                    tint = Color(android.graphics.Color.parseColor(goal.color)),
                    modifier = Modifier.size(48.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = goal.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            goal.description?.let { desc ->
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Progress
            Box(
                modifier = Modifier.size(160.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = { goal.progress },
                    modifier = Modifier.fillMaxSize(),
                    strokeWidth = 12.dp,
                    color = Color(android.graphics.Color.parseColor(goal.color)),
                    trackColor = Color(android.graphics.Color.parseColor(goal.color)).copy(alpha = 0.2f)
                )
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${goal.progressPercentage}%",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(android.graphics.Color.parseColor(goal.color))
                    )
                    
                    if (goal.isCompleted) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "已达成",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProgressDetailsCard(goal: SavingsGoal) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                DetailItem(
                    label = "已储蓄",
                    value = formatCurrency(goal.currentAmount),
                    color = MaterialTheme.colorScheme.primary
                )
                
                DetailItem(
                    label = "目标金额",
                    value = formatCurrency(goal.targetAmount),
                    color = MaterialTheme.colorScheme.secondary
                )
                
                DetailItem(
                    label = "还需储蓄",
                    value = formatCurrency(goal.remainingAmount),
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
            
            goal.targetDate?.let { date ->
                Spacer(modifier = Modifier.height(16.dp))
                Divider()
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "目标日期",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = date.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日")),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    goal.daysRemaining?.let { days ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = when {
                                days <= 7 -> MaterialTheme.colorScheme.errorContainer
                                days <= 30 -> MaterialTheme.colorScheme.tertiaryContainer
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        ) {
                            Text(
                                text = "剩余 $days 天",
                                style = MaterialTheme.typography.labelLarge,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                color = when {
                                    days <= 7 -> MaterialTheme.colorScheme.onErrorContainer
                                    days <= 30 -> MaterialTheme.colorScheme.onTertiaryContainer
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailItem(
    label: String,
    value: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun QuickActionsCard(
    onDeposit: () -> Unit,
    onWithdraw: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            OutlinedButton(
                onClick = onDeposit,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("存入")
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            OutlinedButton(
                onClick = onWithdraw,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Remove, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("取出")
            }
        }
    }
}

@Composable
private fun ContributionItem(
    contribution: SavingsContribution,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            if (contribution.isDeposit) 
                                MaterialTheme.colorScheme.primaryContainer
                            else 
                                MaterialTheme.colorScheme.errorContainer
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (contribution.isDeposit) Icons.Default.Add else Icons.Default.Remove,
                        contentDescription = null,
                        tint = if (contribution.isDeposit) 
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else 
                            MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (contribution.isDeposit) "存入" else "取出",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = contribution.createdAt.format(
                            DateTimeFormatter.ofPattern("MM月dd日 HH:mm")
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    contribution.note?.let { note ->
                        Text(
                            text = note,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatCurrency(kotlin.math.abs(contribution.amount)),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (contribution.isDeposit) 
                        MaterialTheme.colorScheme.primary
                    else 
                        MaterialTheme.colorScheme.error
                )
                
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "删除",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private fun getIconForGoal(iconName: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when (iconName) {
        "house" -> Icons.Default.Home
        "car" -> Icons.Default.DirectionsCar
        "vacation" -> Icons.Default.BeachAccess
        "education" -> Icons.Default.School
        "emergency" -> Icons.Default.LocalHospital
        "shopping" -> Icons.Default.ShoppingCart
        "gift" -> Icons.Default.CardGiftcard
        "phone" -> Icons.Default.PhoneAndroid
        "computer" -> Icons.Default.Computer
        "camera" -> Icons.Default.CameraAlt
        else -> Icons.Default.Savings
    }
}

private fun formatCurrency(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale.CHINA)
    return format.format(amount)
}