package com.ccxiaoji.feature.plan.presentation.screen.create.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.ccxiaoji.ui.components.FlatInputChip
import com.ccxiaoji.ui.components.FlatButton
import com.ccxiaoji.ui.theme.DesignTokens

/**
 * 扁平化标签输入组件
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagInput(
    tags: List<String>,
    onTagsChanged: (List<String>) -> Unit,
    modifier: Modifier = Modifier
) {
    var tagInput by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current
    
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = tagInput,
                onValueChange = { tagInput = it },
                label = { Text("添加标签") },
                placeholder = { Text("输入标签") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (tagInput.isNotBlank() && !tags.contains(tagInput.trim())) {
                            onTagsChanged(tags + tagInput.trim())
                            tagInput = ""
                            keyboardController?.hide()
                        }
                    }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    cursorColor = MaterialTheme.colorScheme.primary
                ),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(DesignTokens.BorderRadius.medium)
            )
            
            if (tagInput.isNotBlank()) {
                Spacer(modifier = Modifier.width(DesignTokens.Spacing.small))
                
                FlatButton(
                    onClick = {
                        if (tagInput.isNotBlank() && !tags.contains(tagInput.trim())) {
                            onTagsChanged(tags + tagInput.trim())
                            tagInput = ""
                            keyboardController?.hide()
                        }
                    },
                    backgroundColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.height(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "添加",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
        
        if (tags.isNotEmpty()) {
            Spacer(modifier = Modifier.height(DesignTokens.Spacing.medium))
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.small)
            ) {
                items(tags) { tag ->
                    FlatInputChip(
                        label = tag,
                        onDismiss = {
                            onTagsChanged(tags - tag)
                        },
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}