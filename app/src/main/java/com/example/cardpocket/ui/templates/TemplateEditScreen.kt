@file:OptIn(ExperimentalLayoutApi::class)

package com.example.cardpocket.ui.templates

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
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
import com.example.cardpocket.data.model.Template
import com.example.cardpocket.ui.theme.BadgeBg
import com.example.cardpocket.ui.theme.Bg
import com.example.cardpocket.ui.theme.ChipBorder
import com.example.cardpocket.ui.theme.DividerColor
import com.example.cardpocket.ui.theme.SurfaceWhite
import com.example.cardpocket.ui.theme.Terracotta
import com.example.cardpocket.ui.theme.TextMuted
import com.example.cardpocket.ui.theme.TextPrimary
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

private val DEFAULT_FIELDS = listOf("姓名", "职位", "公司", "城市", "关系标签", "关系备注")
private val QUICK_FIELDS = listOf("手机号码", "微信", "生日")

@Composable
fun TemplateEditScreen(
    templateId: Long?,
    onDone: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var fields by remember { mutableStateOf(DEFAULT_FIELDS) }
    var customField by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    LaunchedEffect(templateId) {
        if (templateId != null) {
            AppData.ensureDefaultTemplates()
            val t = AppData.getTemplate(templateId)
            if (t != null) {
                name = t.name
                fields = t.fields
            }
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
        scope.launch {
            AppData.saveTemplate(
                Template(
                    id = templateId ?: 0L,
                    name = n,
                    fields = fields
                )
            )
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
                }
            }

            Spacer(Modifier.height(18.dp))
            Text("字段与顺序  /  ${fields.size} 项", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(Modifier.height(6.dp))
            Text(
                text = "长按并拖动可调整填写顺序。姓名是识别联系人的必填项，不可删除。",
                fontSize = 12.sp,
                color = TextMuted
            )
            Spacer(Modifier.height(12.dp))

            DraggableFieldList(
                fields = fields,
                onMove = { from, to -> moveField(from, to) },
                onDelete = { index ->
                    fields = fields.filterIndexed { i, _ -> i != index }
                }
            )

            Spacer(Modifier.height(14.dp))
            Text("添加字段", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(Modifier.height(10.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QUICK_FIELDS.filter { it !in fields }.forEach { f ->
                    AddChip("+ $f") { fields = fields + f }
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceWhite)
                        .border(1.dp, ChipBorder, RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    BasicTextField(
                        value = customField,
                        onValueChange = { customField = it },
                        singleLine = true,
                        textStyle = TextStyle(fontSize = 15.sp, color = TextPrimary),
                        cursorBrush = SolidColor(Terracotta),
                        modifier = Modifier.fillMaxWidth(),
                        decorationBox = { inner ->
                            Box {
                                if (customField.isEmpty()) {
                                    Text("自定义字段名", fontSize = 15.sp, color = TextMuted)
                                }
                                inner()
                            }
                        }
                    )
                }
                Spacer(Modifier.width(8.dp))
                val canAdd = customField.isNotBlank() && customField.trim() !in fields
                TextButton(
                    enabled = canAdd,
                    onClick = {
                        fields = fields + customField.trim()
                        customField = ""
                    }
                ) { Text("添加", color = if (canAdd) Terracotta else TextMuted) }
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
}

/**
 * 长按拖动排序的字段列表：
 * 被拖动行半透明地跟随手指；其余行在拖动过程中实时让位到「松手后」的位置，
 * 并在插入点显示一条横线；松手时才真正移动该字段。
 */
@Composable
private fun DraggableFieldList(
    fields: List<String>,
    onMove: (from: Int, to: Int) -> Unit,
    onDelete: (index: Int) -> Unit
) {
    var draggingIndex by remember { mutableStateOf<Int?>(null) }
    var dragDelta by remember { mutableStateOf(0f) }
    val density = LocalDensity.current
    val pitchPx = with(density) { ROW_PITCH.toPx() }
    val currentFields by rememberUpdatedState(fields)
    val currentOnMove by rememberUpdatedState(onMove)

    val dragged = draggingIndex
    val target = dragged?.let { from ->
        (from + (dragDelta / pitchPx).roundToInt()).coerceIn(0, fields.lastIndex)
    }

    // 拖动时，非拖动行要移动到松手后所在的槽位
    fun visualSlot(index: Int): Int {
        val d = dragged ?: return index
        val t = target ?: return index
        if (index == d) return index
        return when {
            t > d && index > d && index <= t -> index - 1
            t < d && index >= t && index < d -> index + 1
            else -> index
        }
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
                                        val maxDelta = (currentFields.lastIndex - index) * pitchPx
                                        dragDelta = (dragDelta + amount.y).coerceIn(minDelta, maxDelta)
                                    }
                                },
                                onDragEnd = {
                                    val from = draggingIndex
                                    if (from != null) {
                                        val to = (from + (dragDelta / pitchPx).roundToInt())
                                            .coerceIn(0, currentFields.lastIndex)
                                        if (to != from) currentOnMove(from, to)
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
                        isRequired = field == "姓名",
                        onDelete = { onDelete(index) },
                        modifier = Modifier.fillMaxWidth().height(ROW_HEIGHT)
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

private val ROW_HEIGHT = 52.dp
private val ROW_PITCH = 58.dp

@Composable
private fun FieldRow(
    index: Int,
    field: String,
    isRequired: Boolean,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(color = SurfaceWhite, shape = RoundedCornerShape(14.dp), modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 14.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${(index + 1).toString().padStart(2, '0')}",
                fontSize = 13.sp,
                color = TextMuted,
                modifier = Modifier.width(28.dp)
            )
            Text(field, fontSize = 15.sp, color = TextPrimary)
            if (isRequired) {
                Spacer(Modifier.width(6.dp))
                Text("必填", fontSize = 11.sp, color = TextMuted)
            }
            Spacer(Modifier.weight(1f))
            TextButton(onClick = onDelete, enabled = !isRequired) {
                Text(
                    text = "删除",
                    fontSize = 13.sp,
                    color = if (isRequired) ChipBorder else MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun AddChip(text: String, onClick: () -> Unit) {
    Surface(
        color = BadgeBg,
        shape = RoundedCornerShape(50),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            fontSize = 13.sp,
            color = TextPrimary
        )
    }
}
