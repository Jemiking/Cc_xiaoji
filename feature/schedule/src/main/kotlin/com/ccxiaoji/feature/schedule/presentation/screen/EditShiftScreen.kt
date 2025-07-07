package com.ccxiaoji.feature.schedule.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ccxiaoji.feature.schedule.R
import com.ccxiaoji.feature.schedule.domain.model.Shift
import com.ccxiaoji.feature.schedule.presentation.navigation.Screen
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.runtime.DisposableEffect
import com.ccxiaoji.feature.schedule.presentation.viewmodel.EditShiftViewModel
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditShiftScreen(
    shiftId: Long? = null,
    navController: NavController,
    viewModel: EditShiftViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    
    
    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            navController.popBackStack()
        }
    }
    
    // 处理自定义时间选择结果
    navController.currentBackStackEntry?.savedStateHandle?.let { savedStateHandle ->
        val lifecycleOwner = LocalLifecycleOwner.current
        DisposableEffect(lifecycleOwner) {
            val observer = androidx.lifecycle.Observer<java.time.LocalTime> { selectedTime ->
                selectedTime?.let { time ->
                    // 根据当前是开始时间还是结束时间来更新相应的时间
                    // 这里需要添加一个标识来区分是开始时间还是结束时间
                    val timeType = savedStateHandle.get<String>("time_type")
                    when (timeType) {
                        "start_time" -> {
                            viewModel.updateStartTime(time)
                            savedStateHandle.remove<String>("time_type")
                        }
                        "end_time" -> {
                            viewModel.updateEndTime(time)
                            savedStateHandle.remove<String>("time_type")
                        }
                    }
                    savedStateHandle.remove<java.time.LocalTime>("selected_custom_time")
                }
            }
            savedStateHandle.getLiveData<java.time.LocalTime>("selected_custom_time").observe(lifecycleOwner, observer)
            onDispose {
                savedStateHandle.getLiveData<java.time.LocalTime>("selected_custom_time").removeObserver(observer)
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = if (shiftId == null) {
                            stringResource(R.string.schedule_shift_new)
                        } else {
                            stringResource(R.string.schedule_shift_edit)
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.schedule_back)
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.saveShift() },
                        enabled = !uiState.isLoading && uiState.name.isNotBlank()
                    ) {
                        Text(stringResource(R.string.schedule_save))
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 班次名称
            OutlinedTextField(
                value = uiState.name,
                onValueChange = { viewModel.updateName(it) },
                label = { Text(stringResource(R.string.schedule_shift_name)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = uiState.nameError != null,
                supportingText = uiState.nameError?.let {
                    { Text(it) }
                }
            )
            
            // 时间选择
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 开始时间
                OutlinedTextField(
                    value = uiState.startTime.format(timeFormatter),
                    onValueChange = { },
                    label = { Text(stringResource(R.string.schedule_shift_start_time)) },
                    modifier = Modifier.weight(1f),
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { 
                            navController.currentBackStackEntry?.savedStateHandle?.set("time_type", "start_time")
                            navController.navigate(Screen.CustomTimePicker.createRoute(uiState.startTime))
                        }) {
                            Icon(
                                Icons.Default.Schedule,
                                contentDescription = stringResource(R.string.schedule_shift_select_start_time)
                            )
                        }
                    }
                )
                
                // 结束时间
                OutlinedTextField(
                    value = uiState.endTime.format(timeFormatter),
                    onValueChange = { },
                    label = { Text(stringResource(R.string.schedule_shift_end_time)) },
                    modifier = Modifier.weight(1f),
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { 
                            navController.currentBackStackEntry?.savedStateHandle?.set("time_type", "end_time")
                            navController.navigate(Screen.CustomTimePicker.createRoute(uiState.endTime))
                        }) {
                            Icon(
                                Icons.Default.Schedule,
                                contentDescription = stringResource(R.string.schedule_shift_select_end_time)
                            )
                        }
                    },
                    isError = uiState.timeError != null,
                    supportingText = uiState.timeError?.let {
                        { Text(it) }
                    }
                )
            }
            
            // 颜色选择
            Column {
                Text(
                    text = stringResource(R.string.schedule_shift_select_color),
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(Shift.PRESET_COLORS) { color ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    color = Color(color),
                                    shape = MaterialTheme.shapes.small
                                )
                                .clickable { viewModel.updateColor(color) }
                                .then(
                                    if (uiState.selectedColor == color) {
                                        Modifier.border(
                                            width = 2.dp,
                                            color = MaterialTheme.colorScheme.primary,
                                            shape = MaterialTheme.shapes.small
                                        )
                                    } else Modifier
                                )
                        )
                    }
                }
            }
            
            // 描述
            OutlinedTextField(
                value = uiState.description,
                onValueChange = { viewModel.updateDescription(it) },
                label = { Text(stringResource(R.string.schedule_shift_description_optional)) },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )
            
            // Loading indicator
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
    
}