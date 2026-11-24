package com.example.cardpocket.ui.fields

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.cardpocket.data.AppData
import com.example.cardpocket.data.model.FieldDef
import com.example.cardpocket.data.model.FieldType
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FieldEditScreen(
    fieldId: Long?,
    onDone: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(FieldType.TEXT) }
    var options by remember { mutableStateOf<List<String>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }
    var typeExpanded by remember { mutableStateOf(false) }
    var newOption by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()

    LaunchedEffect(fieldId) {
        val existing = fieldId?.let { AppData.getFieldDef(it) }
        name = existing?.name ?: ""
        type = existing?.type ?: FieldType.TEXT
        options = existing?.options ?: emptyList()
    }

    fun save() {
        val n = name.trim()
        if (n.isEmpty()) {
            error = "字段名称不能为空"
            return
        }
        scope.launch {
            AppData.saveFieldDef(
                FieldDef(
                    id = fieldId ?: 0L,
                    name = n,
                    type = type,
                    options = options.filter { it.isNotBlank() }
                )
            )
            onDone()
        }
    }

    BackHandler { onDone() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (fieldId == null) "新增字段" else "编辑字段") },
                navigationIcon = {
                    TextButton(onClick = onDone) { Text("返回") }
                },
                actions = {
                    TextButton(onClick = { save() }) { Text("保存") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    if (error != null) error = null
                },
                label = { Text("字段名称 *") },
                singleLine = true,
                isError = error != null,
                supportingText = error?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))
            Text("类型", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(8.dp))
            Box {
                OutlinedButton(
                    onClick = { typeExpanded = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("${type.label} ▾", modifier = Modifier.fillMaxWidth())
                }
                DropdownMenu(expanded = typeExpanded, onDismissRequest = { typeExpanded = false }) {
                    FieldType.entries.forEach { t ->
                        DropdownMenuItem(
                            text = { Text(t.label) },
                            onClick = {
                                type = t
                                if (!t.needsOptions) options = emptyList()
                                typeExpanded = false
                            }
                        )
                    }
                }
            }

            if (type.needsOptions) {
                Spacer(Modifier.height(20.dp))
                Text("选项", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(8.dp))
                options.forEachIndexed { index, opt ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 4.dp)
                    ) {
                        OutlinedTextField(
                            value = opt,
                            onValueChange = { newValue ->
                                options = options.mapIndexed { i, v -> if (i == index) newValue else v }
                            },
                            placeholder = { Text("选项 ${index + 1}") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = {
                            options = options.filterIndexed { i, _ -> i != index }
                        }) { Text("删除") }
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = newOption,
                        onValueChange = { newOption = it },
                        placeholder = { Text("新增选项") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(
                        enabled = newOption.isNotBlank(),
                        onClick = {
                            val v = newOption.trim()
                            if (v.isNotEmpty()) options = options + v
                            newOption = ""
                        }
                    ) { Text("添加") }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}
