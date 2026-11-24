package com.example.cardpocket.ui.fields

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cardpocket.data.AppData
import com.example.cardpocket.data.model.FieldDef
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FieldsScreen(
    onBack: () -> Unit,
    onAddField: () -> Unit,
    onEditField: (Long) -> Unit
) {
    val fields by AppData.fieldDefs.collectAsState()
    var pendingDelete by remember { mutableStateOf<FieldDef?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) { AppData.refresh() }
    BackHandler { onBack() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("自定义字段") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("返回") }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddField) {
                Text("＋", fontSize = 22.sp)
            }
        }
    ) { padding ->
        if (fields.isEmpty()) {
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("还没有自定义字段", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "点击右下角 + 创建字段，例如：生日、喜好、LinkedIn…",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                itemsIndexed(fields, key = { _, f -> f.id }) { index, field ->
                    FieldRow(
                        field = field,
                        canUp = index > 0,
                        canDown = index < fields.lastIndex,
                        onUp = { scope.launch { AppData.moveFieldDef(field.id, up = true) } },
                        onDown = { scope.launch { AppData.moveFieldDef(field.id, up = false) } },
                        onEdit = { onEditField(field.id) },
                        onDelete = { pendingDelete = field }
                    )
                }
            }
        }
    }

    pendingDelete?.let { field ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("删除字段") },
            text = { Text("确定要删除字段「${field.name}」吗？所有名片中该字段的取值也会一并删除。") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        AppData.deleteFieldDef(field.id)
                        pendingDelete = null
                    }
                }) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("取消") }
            }
        )
    }
}

@Composable
private fun FieldRow(
    field: FieldDef,
    canUp: Boolean,
    canDown: Boolean,
    onUp: () -> Unit,
    onDown: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val summary = buildString {
        append(field.type.label)
        if (field.type.needsOptions && field.options.isNotEmpty()) {
            append(" · ")
            append(field.options.joinToString("/"))
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = field.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = summary,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                TextButton(onClick = onEdit) { Text("编辑") }
                TextButton(onClick = onDelete) { Text("删除") }
            }
            Row {
                TextButton(onClick = onUp, enabled = canUp) { Text("↑ 上移") }
                TextButton(onClick = onDown, enabled = canDown) { Text("↓ 下移") }
            }
        }
    }
}
