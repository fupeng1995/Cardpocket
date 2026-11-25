package com.example.cardpocket.ui.relationships

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import com.example.cardpocket.data.AppData
import com.example.cardpocket.data.model.RelationshipTag
import com.example.cardpocket.ui.components.CircularIconButton
import com.example.cardpocket.ui.theme.Bg
import com.example.cardpocket.ui.theme.ChipBorder
import com.example.cardpocket.ui.theme.DividerColor
import com.example.cardpocket.ui.theme.GreenDeep
import com.example.cardpocket.ui.theme.IconChipGreen
import com.example.cardpocket.ui.theme.SurfaceWhite
import com.example.cardpocket.ui.theme.Terracotta
import com.example.cardpocket.ui.theme.TextMuted
import com.example.cardpocket.ui.theme.TextPrimary
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

@Composable
fun RelationshipTagsScreen(onBack: () -> Unit) {
    val tags by AppData.relationshipTags.collectAsState()
    val cards by AppData.cards.collectAsState()
    val scope = rememberCoroutineScope()
    var showAdd by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<RelationshipTag?>(null) }
    var blocked by remember { mutableStateOf<Pair<String, Int>?>(null) }

    LaunchedEffect(Unit) { AppData.refresh() }
    BackHandler { onBack() }

    val usage = remember(cards) {
        cards.filter { it.relationship.isNotBlank() }
            .groupingBy { it.relationship }
            .eachCount()
    }

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
            CircularIconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "返回",
                    tint = TextPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Text(
                text = "关系标签管理",
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            CircularIconButton(onClick = { showAdd = true }) {
                Icon(Icons.Filled.Add, contentDescription = "新增标签", tint = TextPrimary, modifier = Modifier.size(22.dp))
            }
        }
        HorizontalDivider(color = DividerColor)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(14.dp))
            Text(
                text = "标签会同步到所有模板的「关系标签」；被名片使用的标签不可删除。",
                fontSize = 12.sp,
                color = TextMuted
            )
            Spacer(Modifier.height(14.dp))

            DraggableTagList(
                tags = tags,
                usage = usage,
                onMove = { from, to -> scope.launch { AppData.reorderRelationshipTags(from, to) } },
                onEdit = { editing = it },
                onDelete = { tag ->
                    scope.launch {
                        val ok = AppData.deleteRelationshipTag(tag.id)
                        if (!ok) blocked = tag.name to (usage[tag.name] ?: 0)
                    }
                }
            )

            Spacer(Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(SurfaceWhite)
                    .clickable { showAdd = true }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("＋ 新增标签", fontSize = 14.sp, color = Terracotta)
            }
            Spacer(Modifier.height(28.dp))
        }
    }

    if (showAdd) {
        TagNameDialog(
            title = "新增标签",
            initial = "",
            onDismiss = { showAdd = false },
            onConfirm = { name ->
                scope.launch {
                    if (AppData.addRelationshipTag(name)) showAdd = false
                }
            }
        )
    }

    editing?.let { tag ->
        TagNameDialog(
            title = "重命名标签",
            initial = tag.name,
            onDismiss = { editing = null },
            onConfirm = { name ->
                scope.launch {
                    if (AppData.renameRelationshipTag(tag.id, name)) editing = null
                }
            }
        )
    }

    blocked?.let { (name, count) ->
        AlertDialog(
            onDismissRequest = { blocked = null },
            title = { Text("无法删除") },
            text = { Text("「$name」正被 $count 张名片使用，请先把这些名片改到其他标签后再删除。") },
            confirmButton = {
                TextButton(onClick = { blocked = null }) { Text("知道了") }
            }
        )
    }
}

@Composable
private fun DraggableTagList(
    tags: List<RelationshipTag>,
    usage: Map<String, Int>,
    onMove: (from: Int, to: Int) -> Unit,
    onEdit: (RelationshipTag) -> Unit,
    onDelete: (RelationshipTag) -> Unit
) {
    var draggingIndex by remember { mutableStateOf<Int?>(null) }
    var dragDelta by remember { mutableStateOf(0f) }
    val density = LocalDensity.current
    val pitchPx = with(density) { ROW_PITCH.toPx() }

    val dragged = draggingIndex
    val target = dragged?.let { from ->
        (from + (dragDelta / pitchPx).roundToInt()).coerceIn(0, tags.lastIndex)
    }

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

    Box(Modifier.fillMaxWidth()) {
        Column {
            tags.forEachIndexed { index, tag ->
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
                                        val maxDelta = (tags.lastIndex - index) * pitchPx
                                        dragDelta = (dragDelta + amount.y).coerceIn(minDelta, maxDelta)
                                    }
                                },
                                onDragEnd = {
                                    val from = draggingIndex
                                    if (from != null) {
                                        val to = (from + (dragDelta / pitchPx).roundToInt())
                                            .coerceIn(0, tags.lastIndex)
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
                    TagRow(
                        index = visualSlot(index),
                        tag = tag,
                        usage = usage[tag.name] ?: 0,
                        onEdit = { onEdit(tag) },
                        onDelete = { onDelete(tag) }
                    )
                }
            }
        }
    }
}

private val ROW_HEIGHT = 56.dp
private val ROW_PITCH = 62.dp

@Composable
private fun TagRow(
    index: Int,
    tag: RelationshipTag,
    usage: Int,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
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
                Text(tag.name, fontSize = 15.sp, color = TextPrimary)
                Text("$usage 张名片", fontSize = 11.sp, color = TextMuted)
            }
            IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Filled.Edit, contentDescription = "重命名", tint = TextMuted, modifier = Modifier.size(18.dp))
            }
            TextButton(onClick = onDelete) {
                Text("删除", fontSize = 13.sp, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun TagNameDialog(
    title: String,
    initial: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember { mutableStateOf(initial) }
    val canConfirm = name.isNotBlank()

    Dialog(onDismissRequest = onDismiss) {
        Surface(color = Bg, shape = RoundedCornerShape(28.dp)) {
            Column(Modifier.padding(22.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary, modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(ChipBorder.copy(alpha = 0.35f))
                            .clickable(onClick = onDismiss),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("×", fontSize = 18.sp, color = TextPrimary)
                    }
                }
                Spacer(Modifier.height(18.dp))
                Text("标签名称", fontSize = 13.sp, color = TextMuted)
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceWhite)
                        .padding(horizontal = 14.dp, vertical = 14.dp)
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
                                    Text("例如：客户、合作伙伴", fontSize = 15.sp, color = TextMuted)
                                }
                                inner()
                            }
                        }
                    )
                }
                Spacer(Modifier.height(20.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = onDismiss) { Text("取消", color = TextMuted) }
                    Spacer(Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (canConfirm) IconChipGreen else ChipBorder.copy(alpha = 0.35f))
                            .clickable(enabled = canConfirm) { onConfirm(name.trim()) }
                            .padding(horizontal = 22.dp, vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("确定", fontSize = 15.sp, color = if (canConfirm) GreenDeep else TextMuted)
                    }
                }
            }
        }
    }
}
