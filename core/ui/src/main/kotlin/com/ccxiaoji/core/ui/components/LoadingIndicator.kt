package com.ccxiaoji.core.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ccxiaoji.core.ui.theme.CcXiaoJiTheme

/**
 * 加载指示器组件
 * @param modifier 修饰符
 * @param fillMaxSize 是否填充最大尺寸，默认为true
 */
@Composable
fun LoadingIndicator(
    modifier: Modifier = Modifier,
    fillMaxSize: Boolean = true
) {
    Box(
        modifier = modifier.then(
            if (fillMaxSize) Modifier.fillMaxSize() else Modifier
        ),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
            color = MaterialTheme.colorScheme.primary
        )
    }
}

/**
 * 小型加载指示器
 * @param modifier 修饰符
 */
@Composable
fun SmallLoadingIndicator(
    modifier: Modifier = Modifier
) {
    CircularProgressIndicator(
        modifier = modifier.size(24.dp),
        strokeWidth = 2.dp,
        color = MaterialTheme.colorScheme.primary
    )
}

@Preview(showBackground = true)
@Composable
private fun LoadingIndicatorPreview() {
    CcXiaoJiTheme {
        LoadingIndicator()
    }
}

@Preview(showBackground = true)
@Composable
private fun SmallLoadingIndicatorPreview() {
    CcXiaoJiTheme {
        SmallLoadingIndicator()
    }
}