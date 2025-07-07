package com.ccxiaoji.feature.schedule.presentation.settings.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ccxiaoji.feature.schedule.R
import com.ccxiaoji.ui.theme.DesignTokens

/**
 * 开发者信息部分 - 扁平化设计
 */
@Composable
fun DeveloperSection(
    emailSubject: String,
    shareText: String,
    shareTitle: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    AboutSectionCard(
        title = stringResource(R.string.schedule_about_developer),
        modifier = modifier
    ) {
        Text(
            stringResource(R.string.schedule_about_developer_message),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.3
        )
        
        Spacer(modifier = Modifier.height(DesignTokens.Spacing.medium))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.small)
        ) {
            // 发送反馈按钮
            OutlinedButton(
                onClick = {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:support@ccxiaoji.com")
                        putExtra(Intent.EXTRA_SUBJECT, emailSubject)
                    }
                    if (intent.resolveActivity(context.packageManager) != null) {
                        context.startActivity(intent)
                    }
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    width = 1.dp,
                    brush = androidx.compose.ui.graphics.SolidColor(
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                )
            ) {
                Icon(
                    Icons.Default.Email, 
                    contentDescription = null, 
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(DesignTokens.Spacing.xs))
                Text(stringResource(R.string.schedule_about_feedback))
            }
            
            // 分享应用按钮
            OutlinedButton(
                onClick = {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, shareText)
                    }
                    context.startActivity(Intent.createChooser(intent, shareTitle))
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    width = 1.dp,
                    brush = androidx.compose.ui.graphics.SolidColor(
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                )
            ) {
                Icon(
                    Icons.Default.Share, 
                    contentDescription = null, 
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(DesignTokens.Spacing.xs))
                Text(stringResource(R.string.schedule_about_share))
            }
        }
    }
}