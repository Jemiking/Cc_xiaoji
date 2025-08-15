package com.ccxiaoji.feature.plan.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Observer
import com.ccxiaoji.feature.plan.presentation.viewmodel.TemplateDetailViewModel
import com.ccxiaoji.feature.plan.presentation.screen.templatedetail.components.TemplateDetailContent
import com.ccxiaoji.ui.components.FlatExtendedFAB
import com.ccxiaoji.ui.theme.DesignTokens

/**
 * 模板详情页面 - 扁平化设计
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplateDetailScreen(
    templateId: String,
    onBack: () -> Unit,
    onNavigateToPlanDetail: (String) -> Unit,
    onNavigateToApplyTemplate: () -> Unit = {},
    viewModel: TemplateDetailViewModel = hiltViewModel(),
    navController: androidx.navigation.NavController? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(templateId) {
        viewModel.loadTemplate(templateId)
    }
    
    // 处理应用模板成功后的导航
    LaunchedEffect(uiState.appliedPlanId) {
        uiState.appliedPlanId?.let { planId ->
            onNavigateToPlanDetail(planId)
            viewModel.resetAppliedPlanId()
        }
    }
    
    // 处理导航返回结果
    navController?.currentBackStackEntry?.savedStateHandle?.let { savedStateHandle ->
        val lifecycleOwner = LocalLifecycleOwner.current
        DisposableEffect(lifecycleOwner) {
            val observer = Observer<String> { planId ->
                if (planId != null) {
                    onNavigateToPlanDetail(planId)
                    savedStateHandle.remove<String>("applied_plan_id")
                }
            }
            savedStateHandle.getLiveData<String>("applied_plan_id").observe(lifecycleOwner, observer)
            
            onDispose {
                savedStateHandle.getLiveData<String>("applied_plan_id").removeObserver(observer)
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("模板详情") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
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
        },
        floatingActionButton = {
            if (uiState.template != null) {
                FlatExtendedFAB(
                    onClick = onNavigateToApplyTemplate,
                    text = { Text("应用模板") },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { paddingValues ->
        val currentState = uiState
        when {
            currentState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            currentState.error != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = currentState.error,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(DesignTokens.Spacing.medium))
                    Button(
                        onClick = { viewModel.loadTemplate(templateId) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text("重试")
                    }
                }
            }
            currentState.template != null -> {
                TemplateDetailContent(
                    template = currentState.template,
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}