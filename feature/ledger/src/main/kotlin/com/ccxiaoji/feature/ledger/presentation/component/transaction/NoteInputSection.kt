package com.ccxiaoji.feature.ledger.presentation.component.transaction

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

/**
 * 备注输入区域组件
 *
 * 提供多行文本输入功能，用于输入交易备注信息。
 *
 * @param note 当前备注内容
 * @param onNoteChanged 备注内容变化的回调函数
 * @param modifier 修饰符，用于自定义组件布局
 * @param label 输入框的标签文本
 * @param placeholder 输入框的占位提示文本
 * @param minLines 最小显示行数
 * @param maxLines 最大显示行数
 * @param maxLength 最大字符长度，null表示不限制
 *
 * @sample NoteInputSectionPreview
 */
@Composable
fun NoteInputSection(
    note: String,
    onNoteChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "备注",
    placeholder: String = "添加备注信息...",
    minLines: Int = 2,
    maxLines: Int = 4,
    maxLength: Int? = null
) {
    OutlinedTextField(
        value = note,
        onValueChange = { newValue ->
            // 如果设置了最大长度，限制输入
            if (maxLength == null || newValue.length <= maxLength) {
                onNoteChanged(newValue)
            }
        },
        label = { Text(label) },
        placeholder = {
            if (note.isEmpty()) {
                Text(placeholder)
            }
        },
        modifier = modifier.fillMaxWidth(),
        minLines = minLines,
        maxLines = maxLines,
        supportingText = if (maxLength != null) {
            {
                Text(
                    text = "${note.length} / $maxLength",
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall
                )
            }
        } else null
    )
}

@Preview(showBackground = true, name = "Empty Note")
@Composable
private fun NoteInputSectionEmptyPreview() {
    var note by remember { mutableStateOf("") }
    NoteInputSection(
        note = note,
        onNoteChanged = { note = it }
    )
}

@Preview(showBackground = true, name = "With Content")
@Composable
private fun NoteInputSectionWithContentPreview() {
    var note by remember {
        mutableStateOf("今天和同事一起吃午饭，AA制")
    }
    NoteInputSection(
        note = note,
        onNoteChanged = { note = it }
    )
}

@Preview(showBackground = true, name = "With Max Length")
@Composable
private fun NoteInputSectionWithMaxLengthPreview() {
    var note by remember {
        mutableStateOf("这是一个有长度限制的备注")
    }
    NoteInputSection(
        note = note,
        onNoteChanged = { note = it },
        maxLength = 100
    )
}

@Preview(showBackground = true, name = "Long Text")
@Composable
private fun NoteInputSectionLongTextPreview() {
    var note by remember {
        mutableStateOf(
            """这是一个比较长的备注内容。
            |可以输入多行文本。
            |用于记录更详细的交易信息。
            |比如参与人员、地点、目的等。""".trimMargin()
        )
    }
    NoteInputSection(
        note = note,
        onNoteChanged = { note = it }
    )
}

@Preview(showBackground = true, name = "Custom Label")
@Composable
private fun NoteInputSectionCustomLabelPreview() {
    var note by remember { mutableStateOf("") }
    NoteInputSection(
        note = note,
        onNoteChanged = { note = it },
        label = "交易说明",
        placeholder = "请输入本次交易的详细说明..."
    )
}