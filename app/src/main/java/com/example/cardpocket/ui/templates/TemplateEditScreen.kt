@file:OptIn(ExperimentalLayoutApi::class)

package com.example.cardpocket.ui.templates

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.cardpocket.data.AppData
import com.example.cardpocket.data.KEY_NAME
import com.example.cardpocket.data.model.FieldType
import com.example.cardpocket.data.model.TemplateField
import com.example.cardpocket.ui.components.CircularIconButton
import com.example.cardpocket.ui.components.FieldFormDialog
import com.example.cardpocket.ui.theme.Bg
import com.example.cardpocket.ui.theme.ChipBorder
import com.example.cardpocket.ui.theme.DividerColor
import com.example.cardpocket.ui.theme.SurfaceWhite
import com.example.cardpocket.ui.theme.Terracotta
import com.example.cardpocket.ui.theme.TextMuted
import com.example.cardpocket.ui.theme.TextPrimary
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

private val TYPE_OPTIONS = listOf(
    FieldType.TEXT to "文本",
    FieldType.MULTILINE_TEXT to "多行",
    FieldType.DATE to "日期",
    FieldType.SINGLE_SELECT to "单选",
    FieldType.MULTI_SELECT to "多选",
    FieldType.SWITCH to "开关"
)

private fun typeLabel(type: FieldType): String =
    TYPE_OPTIONS.firstOrNull { it.first == type }?.second ?: "文本"

@Composable
fun TemplateEditScreen(
    templateId: Long?,
    onDone: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var isDefault by remember { mutableStateOf(false) }
    var fields by remember { mutableStateOf<List<TemplateField>>(emptyList()) }
    var editingField by remember { mutableStateOf<TemplateField?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(templateId) {
        if (templateId != null) {
            val t = AppData.getTemplate(templateId)
            if (t != null) {
                name = t.name
                isDefault = t.isDefault
                fields = AppData.getTemplateFields(templateId)
            }
        } else {
            name = ""
            isDefault = false
            fields = listOf(
                TemplateField(
                    id = 0,
                    templateId = 0,
                    key = KEY_NAME,
                    name = "名字",
                    type = FieldType.TEXT,
                    required = true,
                    sortOrder = 0
                )
            )
        }
    }

    fun moveField(from: Int, to: Int) {
        if (from == to || from !in fields.indices || to !in fields.indices) return
        fields = fields.toMutableList().also { list ->
            list.add(to, list.removeAt(from))
        }
    }

    fun save() {
        val n = name.trim()
        if (n.isEmpty()) return
        if (fields.none { it.key == KEY_NAME }) {
            fields = listOf(
                TemplateField(0, 0, KEY_NAME, "名字", FieldType.TEXT, emptyList(), true, 0)
            ) + fields
        }
        scope.launch {
            AppData.saveTemplate(templateId ?: 0L, n, isDefault, fields)
            onDone()
        }
    }

    BackHandler { onDone() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onDone) {
                Text("取消", color = TextMuted, fontSize = 15.sp)
            }
            Text(
                text = if (templateId == null) "新增字段模板" else "编辑字段模板",
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            TextButton(onClick = { save() }, enabled = name.isNotBlank()) {
                Text(
                    text = "保存",
                    color = if (name.isNotBlank()) Terracotta else TextMuted,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        HorizontalDivider(color = DividerColor)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            Surface(color = SurfaceWhite, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("模板名称", fontSize = 13.sp, color = TextMuted)
                    Spacer(Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Bg)
                            .padding(horizontal = 14.dp, vertical = 12.dp)
                    ) {
                        BasicTextField(
                            value = name,
                            onValueChange = { name = it },
                            singleLine = true,
                            textStyle = TextStyle(fontSize = 15.sp, color = TextPrimary),
                            cursorBrush = SolidColor(Terracotta),
                            modifier = Modifier.fillMaxWidth(),
                            decorationBox = { inner ->
                                Box {
                                    if (name.isEmpty()) {
                                        Text("输入模板名称", fontSize = 15.sp, color = TextMuted)
                                    }
                                    inner()
                                }
                            }
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("设为默认模板", fontSize = 15.sp, color = TextPrimary)
                            Spacer(Modifier.height(2.dp))
                            Text("新建联系人时默认选中此模板", fontSize = 12.sp, color = TextMuted)
                        }
                        Checkbox(checked = isDefault, onCheckedChange = { isDefault = it })
                    }
                }
            }

            Spacer(Modifier.height(18.dp))
            Text("字段与顺序  /  ${fields.size} 项", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(Modifier.height(6.dp))
            Text(
                text = "长按并拖动可调整填写顺序。名字是识别联系人的必填项，不可删除。",
                fontSize = 12.sp,
                color = TextMuted
            )
            Spacer(Modifier.height(12.dp))

            DraggableFieldList(
                fields = fields,
                onMove = { from, to -> moveField(from, to) },
                onEdit = { editingField = it },
                onDelete = { index ->
                    if (fields.getOrNull(index)?.key != KEY_NAME) {
                        fields = fields.filterIndexed { i, _ -> i != index }
                    }
                }
            )

            Spacer(Modifier.height(14.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(SurfaceWhite)
                    .clickable { showAddDialog = true }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("＋ 新增字段", fontSize = 14.sp, color = Terracotta)
            }

            if (templateId != null) {
                Spacer(Modifier.height(18.dp))
                TextButton(
                    onClick = {
                        scope.launch {
                            AppData.deleteTemplate(templateId)
                            onDone()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("删除模板", color = MaterialTheme.colorScheme.error, fontSize = 14.sp)
                }
            }

            Spacer(Modifier.height(28.dp))
        }
    }

    if (showAddDialog) {
        FieldEditorDialog(
            initial = null,
            existingNames = fields.map { it.name },
            onDismiss = { showAddDialog = false },
            onSave = { fieldName, type, options, required ->
                showAddDialog = false
                fields = fields + TemplateField(
                    id = 0,
                    templateId = templateId ?: 0L,
                    key = "f_" + java.util.UUID.randomUUID().toString().replace("-", "").take(10),
                    name = fieldName,
                    type = type,
                    options = options,
                    required = required,
                    sortOrder = fields.size
                )
            }
        )
    }

    editingField?.let { field ->
        FieldEditorDialog(
            initial = field,
            existingNames = fields.filter { it.key != field.key }.map { it.name },
            onDismiss = { editingField = null },
            onSave = { fieldName, type, options, required ->
                editingField = null
                fields = fields.map {
                    if (it.key == field.key) {
                        it.copy(name = fieldName, type = type, options = options, required = required)
                    } else {
                        it
                    }
                }
            }
        )
    }
}

@Composable
private fun DraggableFieldList(
    fields: List<TemplateField>,
    onMove: (from: Int, to: Int) -> Unit,
    onEdit: (TemplateField) -> Unit,
    onDelete: (index: Int) -> Unit
) {
    var draggingIndex by remember { mutableStateOf<Int?>(null) }
    var dragDelta by remember { mutableStateOf(0f) }
    val density = LocalDensity.current
    val pitchPx = with(density) { ROW_PITCH.toPx() }

    fun visualSlot(index: Int): Int {
        val d = draggingIndex ?: return index
        val t = (d + (dragDelta / pitchPx).roundToInt()).coerceIn(0, fields.lastIndex)
        if (index == d) return index
        return when {
            t > d && index > d && index <= t -> index - 1
            t < d && index >= t && index < d -> index + 1
            else -> index
        }
    }

    val dragged = draggingIndex
    val target = dragged?.let { d ->
        (d + (dragDelta / pitchPx).roundToInt()).coerceIn(0, fields.lastIndex)
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        Column {
            fields.forEachIndexed { index, field ->
                val isDragging = dragged == index
                val offsetY = if (isDragging) dragDelta else (visualSlot(index) - index) * pitchPx
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(ROW_PITCH)
                        .zIndex(if (isDragging) 1f else 0f)
                        .offset { IntOffset(0, offsetY.roundToInt()) }
                        .graphicsLayer { alpha = if (isDragging) 0.5f else 1f }
                        .pointerInput(index) {
                            detectDragGesturesAfterLongPress(
                                onDragStart = {
                                    draggingIndex = index
                                    dragDelta = 0f
                                },
                                onDrag = { change, amount ->
                                    if (draggingIndex == index) {
                                        change.consume()
                                        val minDelta = -index * pitchPx
                                        val maxDelta = (fields.lastIndex - index) * pitchPx
                                        dragDelta = (dragDelta + amount.y).coerceIn(minDelta, maxDelta)
                                    }
                                },
                                onDragEnd = {
                                    val from = draggingIndex
                                    if (from != null) {
                                        val to = (from + (dragDelta / pitchPx).roundToInt())
                                            .coerceIn(0, fields.lastIndex)
                                        if (to != from) onMove(from, to)
                                    }
                                    draggingIndex = null
                                    dragDelta = 0f
                                },
                                onDragCancel = {
                                    draggingIndex = null
                                    dragDelta = 0f
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    FieldRow(
                        index = if (isDragging) (target ?: index) else visualSlot(index),
                        field = field,
                        onEdit = { onEdit(field) },
                        onDelete = { onDelete(index) }
                    )
                }
            }
        }

        if (dragged != null && target != null && target != dragged) {
            Box(
                modifier = Modifier
                    .offset(y = ROW_PITCH * target)
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp)
                    .height(2.dp)
                    .background(Terracotta)
                    .zIndex(2f)
            )
        }
    }
}

private val ROW_HEIGHT = 56.dp
private val ROW_PITCH = 62.dp

@Composable
private fun FieldRow(
    index: Int,
    field: TemplateField,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isName = field.key == KEY_NAME
    Surface(color = SurfaceWhite, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth().height(ROW_HEIGHT)) {
        Row(
            modifier = Modifier.fillMaxSize().padding(start = 14.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${(index + 1).toString().padStart(2, '0')}",
                fontSize = 13.sp,
                color = TextMuted,
                modifier = Modifier.width(28.dp)
            )
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(field.name, fontSize = 15.sp, color = TextPrimary)
                    if (isName) {
                        Spacer(Modifier.width(6.dp))
                        Text("必填", fontSize = 11.sp, color = TextMuted)
                    }
                }
                Text(typeLabel(field.type), fontSize = 11.sp, color = TextMuted)
            }
            IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                Icon(
                    Icons.Filled.Edit,
                    contentDescription = "编辑",
                    tint = TextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
            TextButton(onClick = onDelete, enabled = !isName) {
                Text(
                    text = "删除",
                    fontSize = 13.sp,
                    color = if (isName) ChipBorder else MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun FieldEditorDialog(
    initial: TemplateField?,
    existingNames: List<String>,
    onDismiss: () -> Unit,
    onSave: (name: String, type: FieldType, options: List<String>, required: Boolean) -> Unit
) {
    val isNameField = initial?.key == KEY_NAME
    FieldFormDialog(
        title = if (initial == null) "添加字段" else "编辑字段",
        confirmText = if (initial == null) "确认添加" else "保存",
        initialName = initial?.name ?: "",
        initialType = initial?.type ?: FieldType.TEXT,
        initialOptions = initial?.options ?: emptyList(),
        initialRequired = initial?.required ?: false,
        lockType = isNameField,
        lockRequired = isNameField,
        existingNames = existingNames,
        onDismiss = onDismiss,
        onConfirm = onSave
    )
}
