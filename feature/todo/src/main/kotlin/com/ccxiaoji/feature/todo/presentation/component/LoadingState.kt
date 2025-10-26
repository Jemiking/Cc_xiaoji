package com.ccxiaoji.feature.todo.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.ccxiaoji.ui.theme.DesignTokens
import com.ccxiaoji.feature.todo.presentation.theme.TodoGrid

@Composable
fun LoadingState(
    message: String? = null,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            CircularProgressIndicator(color = DesignTokens.BrandColors.Todo)
            if (message != null) {
                Spacer(modifier = Modifier.height(TodoGrid.x1))
                Text(text = message, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

