package com.ccxiaoji.feature.habit.presentation.screen.demo

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.padding

/**
 * 占位的习惯重设计 Demo 组件
 * 仅用于保证编译通过与基本展示，不包含真实交互与布局。
 */
@Composable
fun HabitRedesignDemo(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Card(modifier = Modifier.padding(16.dp)) {
            Text(text = "Habit Redesign Demo (stub)", modifier = Modifier.padding(16.dp))
        }
    }
}

