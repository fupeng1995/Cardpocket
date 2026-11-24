package com.example.cardpocket.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cardpocket.data.AppData
import com.example.cardpocket.data.model.Template
import com.example.cardpocket.data.model.TemplateField
import com.example.cardpocket.ui.theme.Terracotta
import com.example.cardpocket.ui.theme.TextMuted
import com.example.cardpocket.ui.theme.TextPrimary

/** 新建名片前的模板选择弹窗。 */
@Composable
fun TemplatePickerDialog(
    onDismiss: () -> Unit,
    onPick: (Template) -> Unit
) {
    val templates by AppData.templates.collectAsState()
    var fieldsByTemplate by remember { mutableStateOf<Map<Long, List<TemplateField>>>(emptyMap()) }

    LaunchedEffect(Unit) {
        AppData.refresh()
        fieldsByTemplate = AppData.getAllTemplateFields().groupBy { it.templateId }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择模板") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text("选择模板后进入名片填写页", fontSize = 12.sp, color = TextMuted)
                Spacer(Modifier.height(8.dp))
                templates.forEach { t ->
                    val count = fieldsByTemplate[t.id]?.size ?: 0
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onPick(t) }
                            .padding(horizontal = 8.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = t.name,
                                fontSize = 15.sp,
                                color = TextPrimary,
                                fontWeight = FontWeight.Medium
                            )
                            Text("$count 个字段", fontSize = 11.sp, color = TextMuted)
                        }
                        if (t.isDefault) {
                            Text("默认", fontSize = 11.sp, color = Terracotta)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}
