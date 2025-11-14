package com.ccxiaoji.feature.ledger.presentation.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ccxiaoji.feature.ledger.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * EntityEditorScaffold 使用示例
 *
 * 展示如何在实际页面中使用EntityEditorScaffold组件
 */
@Composable
fun EntityEditorScaffoldExample(
    categoryId: String? = null,  // null表示新增模式
    onNavigateBack: () -> Unit
) {
    // 状态管理
    var categoryName by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // 创建编辑器状态
    val editorState = rememberEntityEditorState(
        isEditMode = categoryId != null,
        isLoading = isLoading,
        isSaving = isSaving,
        saveEnabled = categoryName.isNotBlank(),  // 名称不为空时才能保存
        hasUnsavedChanges = categoryName.isNotBlank()  // 有内容就算有变更
    )

    // 使用EntityEditorScaffold
    EntityEditorScaffold(
        title = if (categoryId != null) "编辑分类" else "新增分类",
        state = editorState,
        onBack = onNavigateBack,
        onSave = {
            scope.launch {
                isSaving = true
                try {
                    // 模拟保存操作
                    delay(2000)
                    editorState.showSuccess("保存成功")
                    delay(500)
                    onNavigateBack()
                } catch (e: Exception) {
                    editorState.showError("保存失败：${e.message}")
                } finally {
                    isSaving = false
                }
            }
        }
    ) {
        // 页面内容
        OutlinedTextField(
            value = categoryName,
            onValueChange = { categoryName = it },
            label = { Text("分类名称") },
            placeholder = { Text("请输入分类名称") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            singleLine = true
        )

        // 可以添加更多表单字段...
    }
}

/**
 * 简化版使用示例
 *
 * 使用EntityEditorScaffold的简化重载版本
 */
@Composable
fun SimpleEntityEditorExample(
    onNavigateBack: () -> Unit
) {
    var content by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    var isSaving by remember { mutableStateOf(false) }

    EntityEditorScaffold(
        title = "创建笔记",
        isEditMode = false,
        onBack = onNavigateBack,
        onSave = {
            scope.launch {
                isSaving = true
                // 执行保存逻辑
                delay(1500)
                onNavigateBack()
            }
        },
        isSaving = isSaving
    ) {
        OutlinedTextField(
            value = content,
            onValueChange = { content = it },
            label = { Text("笔记内容") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            minLines = 5
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EntityEditorScaffoldPreview() {
    EntityEditorScaffoldExample(
        categoryId = null,
        onNavigateBack = {}
    )
}

@Preview(showBackground = true)
@Composable
private fun EntityEditorScaffoldEditModePreview() {
    EntityEditorScaffoldExample(
        categoryId = "123",  // 编辑模式
        onNavigateBack = {}
    )
}