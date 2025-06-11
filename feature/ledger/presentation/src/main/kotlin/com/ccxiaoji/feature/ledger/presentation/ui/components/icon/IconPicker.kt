package com.ccxiaoji.feature.ledger.presentation.ui.components.icon

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IconPicker(
    selectedIcon: String,
    onIconSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val icons = listOf(
        "savings" to Icons.Default.Savings,
        "house" to Icons.Default.Home,
        "car" to Icons.Default.DirectionsCar,
        "vacation" to Icons.Default.BeachAccess,
        "education" to Icons.Default.School,
        "emergency" to Icons.Default.LocalHospital,
        "shopping" to Icons.Default.ShoppingCart,
        "gift" to Icons.Default.CardGiftcard,
        "phone" to Icons.Default.PhoneAndroid,
        "computer" to Icons.Default.Computer,
        "camera" to Icons.Default.CameraAlt,
        "fitness" to Icons.Default.FitnessCenter,
        "travel" to Icons.Default.Flight,
        "dining" to Icons.Default.Restaurant,
        "entertainment" to Icons.Default.TheaterComedy,
        "investment" to Icons.Default.TrendingUp
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择图标") },
        text = {
            Column {
                icons.chunked(4).forEach { rowIcons ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        rowIcons.forEach { (name, icon) ->
                            IconItem(
                                icon = icon,
                                isSelected = name == selectedIcon,
                                onClick = { onIconSelected(name) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("确定")
            }
        }
    )
}

@Composable
private fun IconItem(
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(
                if (isSelected) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = if (isSelected) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}