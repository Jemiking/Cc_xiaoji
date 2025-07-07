package com.ccxiaoji.feature.schedule.presentation.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ccxiaoji.feature.schedule.R
import com.ccxiaoji.feature.schedule.presentation.settings.components.*
import com.ccxiaoji.ui.theme.DesignTokens

/**
 * 关于页面 - 扁平化设计
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onBack: () -> Unit
) {
    val emailSubject = stringResource(R.string.schedule_about_email_subject)
    val shareText = stringResource(R.string.schedule_about_share_text)
    val shareTitle = stringResource(R.string.schedule_about_share)
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.schedule_about_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = stringResource(R.string.schedule_back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = DesignTokens.Spacing.medium),
            verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.medium),
            contentPadding = PaddingValues(vertical = DesignTokens.Spacing.medium)
        ) {
            // 应用图标和名称
            item {
                AppInfoSection(version = "1.0.0")
            }
            
            // 应用描述
            item {
                AboutSectionCard(
                    title = stringResource(R.string.schedule_about_section_about)
                ) {
                    Text(
                        stringResource(R.string.schedule_about_description),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.3
                    )
                }
            }
            
            // 功能特色
            item {
                AboutSectionCard(
                    title = stringResource(R.string.schedule_about_section_features)
                ) {
                    val features = listOf(
                        stringResource(R.string.schedule_about_feature_calendar) to stringResource(R.string.schedule_about_feature_calendar_desc),
                        stringResource(R.string.schedule_about_feature_quick) to stringResource(R.string.schedule_about_feature_quick_desc),
                        stringResource(R.string.schedule_about_feature_patterns) to stringResource(R.string.schedule_about_feature_patterns_desc),
                        stringResource(R.string.schedule_about_feature_statistics) to stringResource(R.string.schedule_about_feature_statistics_desc),
                        stringResource(R.string.schedule_about_feature_backup) to stringResource(R.string.schedule_about_feature_backup_desc),
                        stringResource(R.string.schedule_about_feature_export) to stringResource(R.string.schedule_about_feature_export_desc),
                        stringResource(R.string.schedule_about_feature_dark) to stringResource(R.string.schedule_about_feature_dark_desc),
                        stringResource(R.string.schedule_about_feature_reminder) to stringResource(R.string.schedule_about_feature_reminder_desc)
                    )
                    
                    features.forEachIndexed { index, (title, description) ->
                        FeatureItem(title, description)
                        if (index < features.size - 1) {
                            Spacer(modifier = Modifier.height(DesignTokens.Spacing.small))
                        }
                    }
                }
            }
            
            // 技术信息
            item {
                AboutSectionCard(
                    title = stringResource(R.string.schedule_about_section_tech)
                ) {
                    InfoItem(stringResource(R.string.schedule_about_tech_language), stringResource(R.string.schedule_about_tech_language_value))
                    InfoItem(stringResource(R.string.schedule_about_tech_ui), stringResource(R.string.schedule_about_tech_ui_value))
                    InfoItem(stringResource(R.string.schedule_about_tech_arch), stringResource(R.string.schedule_about_tech_arch_value))
                    InfoItem(stringResource(R.string.schedule_about_tech_storage), stringResource(R.string.schedule_about_tech_storage_value))
                    InfoItem(stringResource(R.string.schedule_about_tech_di), stringResource(R.string.schedule_about_tech_di_value))
                    InfoItem(stringResource(R.string.schedule_about_tech_min_version), stringResource(R.string.schedule_about_tech_min_version_value))
                }
            }
            
            // 开发者信息
            item {
                DeveloperSection(
                    emailSubject = emailSubject,
                    shareText = shareText,
                    shareTitle = shareTitle
                )
            }
            
            // 版权信息
            item {
                Text(
                    stringResource(R.string.schedule_about_copyright),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                )
            }
        }
    }
}