package com.ccxiaoji.app.presentation.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.ccxiaoji.app.presentation.screen.import.components.*
import com.ccxiaoji.app.presentation.viewmodel.DataImportViewModel
import com.ccxiaoji.app.presentation.viewmodel.ImportStep
import com.ccxiaoji.ui.theme.DesignTokens

/**
 * 数据导入界面
 */
/**
 * 数据导入界面 - 扁平化设计
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataImportScreen(
    navController: NavController,
    viewModel: DataImportViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // 文件选择器
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.selectAndValidateFile(it) }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("数据导入") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = "返回"
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AnimatedContent(
                targetState = uiState.importStep,
                transitionSpec = {
                    fadeIn() + slideInHorizontally() togetherWith fadeOut() + slideOutHorizontally()
                },
                label = "import_step_animation"
            ) { step ->
                when (step) {
                    ImportStep.SELECT_FILE -> {
                        SelectFileStep(
                            onSelectFile = { filePickerLauncher.launch("application/json") }
                        )
                    }
                    ImportStep.PREVIEW -> {
                        PreviewStep(
                            uiState = uiState,
                            onToggleModule = viewModel::toggleModuleSelection,
                            onToggleSelectAll = viewModel::toggleSelectAll,
                            onUpdateConfig = viewModel::updateImportConfig,
                            onStartImport = viewModel::startImport,
                            onCancel = viewModel::goBack
                        )
                    }
                    ImportStep.IMPORTING -> {
                        ImportingStep()
                    }
                    ImportStep.RESULT -> {
                        ResultStep(
                            result = uiState.importResult,
                            onFinish = {
                                viewModel.reset()
                                navController.navigateUp()
                            },
                            onRetry = viewModel::reset
                        )
                    }
                }
            }
            
            // 错误提示
            uiState.error?.let { error ->
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(DesignTokens.Spacing.medium),
                    action = {
                        TextButton(
                            onClick = { viewModel.reset() },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.inversePrimary
                            )
                        ) {
                            Text("重试")
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.inverseSurface,
                    contentColor = MaterialTheme.colorScheme.inverseOnSurface
                ) {
                    Text(error)
                }
            }
        }
    }
}

