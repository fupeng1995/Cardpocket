@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.example.cardpocket.ui.detail

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import com.example.cardpocket.data.AppData
import com.example.cardpocket.data.model.CardField
import com.example.cardpocket.data.model.ContactRecord
import com.example.cardpocket.data.model.FieldType
import com.example.cardpocket.data.model.TemplateField
import com.example.cardpocket.ui.components.Avatar
import com.example.cardpocket.ui.components.Badge
import com.example.cardpocket.ui.components.CircularIconButton
import com.example.cardpocket.ui.components.AppDatePickerDialog
import com.example.cardpocket.ui.components.FieldFormDialog
import com.example.cardpocket.ui.theme.Bg
import com.example.cardpocket.ui.theme.ChipBorder
import com.example.cardpocket.ui.theme.DividerColor
import com.example.cardpocket.ui.theme.GreenDeep
import com.example.cardpocket.ui.theme.IconChipGreen
import com.example.cardpocket.ui.theme.SurfaceWhite
import com.example.cardpocket.ui.theme.Terracotta
import com.example.cardpocket.ui.theme.TextMuted
import com.example.cardpocket.ui.theme.TextPrimary
import com.example.cardpocket.util.formatDate
import com.example.cardpocket.util.formatFieldValue
import com.example.cardpocket.util.formatMonth
import com.example.cardpocket.util.formatTime
import java.util.Calendar
import kotlinx.coroutines.launch

private enum class ViewMode { CALENDAR, TIMELINE }

@Composable
fun CardDetailScreen(
    cardId: Long,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDeleted: () -> Unit
) {
    val cards by AppData.cards.collectAsState()
    var fields by remember { mutableStateOf<List<CardField>>(emptyList()) }
    var values by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var records by remember { mutableStateOf<List<ContactRecord>>(emptyList()) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showAddRecord by remember { mutableStateOf(false) }
    var showAddField by remember { mutableStateOf(false) }
    var addRecordDate by remember { mutableStateOf(System.currentTimeMillis()) }
    val scope = rememberCoroutineScope()
    val keyboard = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) { keyboard?.hide() }

    LaunchedEffect(cardId) {
        fields = AppData.getCardFields(cardId)
        values = AppData.getCardValues(cardId)
        records = AppData.getRecords(cardId)
    }

    val card = cards.find { it.id == cardId }
    BackHandler { onBack() }

    Box(Modifier.fillMaxSize().background(Bg)) {
        if (card == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("加载中…", color = TextMuted)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().height(56.dp),
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
                    Spacer(Modifier.weight(1f))
                    var showMore by remember { mutableStateOf(false) }
                    Box {
                        CircularIconButton(onClick = { showMore = true }) {
                            Icon(
                                Icons.Filled.MoreVert,
                                contentDescription = "更多",
                                tint = TextPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        DropdownMenu(expanded = showMore, onDismissRequest = { showMore = false }) {
                            DropdownMenuItem(
                                text = { Text("修改信息") },
                                onClick = { showMore = false; onEdit() }
                            )
                            DropdownMenuItem(
                                text = { Text("删除名片", color = MaterialTheme.colorScheme.error) },
                                onClick = { showMore = false; showDeleteDialog = true }
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Avatar(name = card.name, emoji = card.avatarEmoji, size = 88.dp)
                    Spacer(Modifier.height(12.dp))
                    Text(card.name, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    if (card.subtitle.isNotBlank()) {
                        Spacer(Modifier.height(4.dp))
                        Text(card.subtitle, fontSize = 14.sp, color = TextMuted, textAlign = TextAlign.Center)
                    }
                    if (card.relationship.isNotBlank()) {
                        Spacer(Modifier.height(8.dp))
                        Badge(card.relationship)
                    }
                }

                Spacer(Modifier.height(20.dp))

                Surface(
                    color = SurfaceWhite,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(horizontal = 16.dp)) {
                        fields.forEachIndexed { index, field ->
                            DetailRow(field.name, formatFieldValue(field.type, values[field.key]))
                            if (index < fields.lastIndex) DetailDivider()
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onEdit,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, ChipBorder)
                    ) {
                        Icon(Icons.Filled.Edit, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("修改信息", fontSize = 14.sp, color = TextPrimary)
                    }
                    Button(
                        onClick = { showAddField = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFF3DDD2),
                            contentColor = Terracotta
                        )
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("增加字段", fontSize = 14.sp)
                    }
                }

                Spacer(Modifier.height(14.dp))

                TimelineCard(
                    records = records,
                    onAddRecord = { millis ->
                        addRecordDate = millis
                        showAddRecord = true
                    }
                )

                Spacer(Modifier.height(24.dp))
            }
        }

        if (showAddRecord) {
            AddRecordOverlay(
                cardId = cardId,
                initialDateMillis = addRecordDate,
                onDismiss = { showAddRecord = false },
                onSave = { rec ->
                    scope.launch {
                        AppData.saveRecord(rec)
                        records = AppData.getRecords(cardId)
                        showAddRecord = false
                    }
                }
            )
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("删除名片") },
            text = { Text("确定要删除「${card?.name}」吗？此操作无法撤销。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        scope.launch {
                            AppData.deleteCard(cardId)
                            onDeleted()
                        }
                    }
                ) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("取消") }
            }
        )
    }

    if (showAddField) {
        FieldFormDialog(
            title = "添加字段",
            confirmText = "确认添加",
            existingNames = fields.map { it.name },
            onDismiss = { showAddField = false },
            onConfirm = { fieldName, fieldType, options, required ->
                scope.launch {
                    AppData.addCardField(
                        cardId,
                        CardField(
                            key = "c_" + java.util.UUID.randomUUID().toString().replace("-", "").take(10),
                            name = fieldName,
                            type = fieldType,
                            options = options,
                            required = required
                        )
                    )
                    fields = AppData.getCardFields(cardId)
                    values = AppData.getCardValues(cardId)
                    showAddField = false
                }
            }
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(label, fontSize = 14.sp, color = TextMuted, modifier = Modifier.width(88.dp))
        Text(value, fontSize = 14.sp, color = TextPrimary, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun DetailDivider() {
    HorizontalDivider(color = DividerColor.copy(alpha = 0.75f))
}

@Composable
private fun TimelineCard(
    records: List<ContactRecord>,
    onAddRecord: (Long) -> Unit
) {
    var mode by remember { mutableStateOf(ViewMode.CALENDAR) }

    val initialMillis = remember(records) {
        records.maxOfOrNull { it.dateMillis } ?: System.currentTimeMillis()
    }
    val initCal = remember(initialMillis) {
        Calendar.getInstance().apply { timeInMillis = initialMillis }
    }
    var displayYear by remember(initialMillis) { mutableStateOf(initCal.get(Calendar.YEAR)) }
    var displayMonth by remember(initialMillis) { mutableStateOf(initCal.get(Calendar.MONTH)) }
    var selectedDay by remember(initialMillis) { mutableStateOf(initCal.get(Calendar.DAY_OF_MONTH)) }

    val todayStart = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
    val selectedDayMillis = remember(displayYear, displayMonth, selectedDay) {
        Calendar.getInstance().apply {
            set(Calendar.YEAR, displayYear)
            set(Calendar.MONTH, displayMonth)
            set(Calendar.DAY_OF_MONTH, selectedDay)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    Surface(
        color = SurfaceWhite,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (mode == ViewMode.TIMELINE) "联系时间线" else "联系日历",
                    fontSize = 12.sp,
                    color = TextMuted
                )
                Spacer(Modifier.weight(1f))
                ViewToggle(mode) { mode = it }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = "${displayYear}年 ${displayMonth + 1}月",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(Modifier.height(12.dp))

            if (mode == ViewMode.TIMELINE) {
                if (records.isEmpty()) {
                    Text(
                        text = "还没有联系记录",
                        fontSize = 13.sp,
                        color = TextMuted,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    records.forEach { rec ->
                        TimelineEntry(rec, showAddTime = false)
                    }
                }
                Spacer(Modifier.height(10.dp))
                DashedAddButton("添加新的联系记录") { onAddRecord(todayStart) }
            } else {
                MonthCalendar(
                    year = displayYear,
                    month = displayMonth,
                    records = records,
                    selectedDay = selectedDay,
                    onSelectDay = { selectedDay = it }
                )

                val dayRecords = records.filter {
                    val c = Calendar.getInstance().apply { timeInMillis = it.dateMillis }
                    c.get(Calendar.YEAR) == displayYear &&
                        c.get(Calendar.MONTH) == displayMonth &&
                        c.get(Calendar.DAY_OF_MONTH) == selectedDay
                }
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${displayMonth + 1}月${selectedDay}日",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(Modifier.weight(1f))
                    SmallAddButton("记录联系") { onAddRecord(selectedDayMillis) }
                }
                Spacer(Modifier.height(2.dp))
                Text("${dayRecords.size} 条记录", fontSize = 12.sp, color = TextMuted)
                Spacer(Modifier.height(8.dp))
                if (dayRecords.isEmpty()) {
                    Text("这一天还没有记录", fontSize = 13.sp, color = TextMuted)
                } else {
                    dayRecords.forEach { rec -> TimelineEntry(rec, showAddTime = true) }
                }
            }
        }
    }
}

@Composable
private fun ViewToggle(mode: ViewMode, onChange: (ViewMode) -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(DividerColor)
            .padding(2.dp)
    ) {
        ToggleSegment("月历", mode == ViewMode.CALENDAR) { onChange(ViewMode.CALENDAR) }
        ToggleSegment("时间线", mode == ViewMode.TIMELINE) { onChange(ViewMode.TIMELINE) }
    }
}

@Composable
private fun ToggleSegment(text: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (selected) SurfaceWhite else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 5.dp)
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            color = if (selected) TextPrimary else TextMuted,
            fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal
        )
    }
}

@Composable
private fun TimelineEntry(record: ContactRecord, showAddTime: Boolean) {
    val cal = remember(record.dateMillis) {
        Calendar.getInstance().apply { timeInMillis = record.dateMillis }
    }
    val day = cal.get(Calendar.DAY_OF_MONTH)
    val month = cal.get(Calendar.MONTH) + 1

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                val x = 52.dp.toPx()
                drawLine(
                    color = DividerColor,
                    start = Offset(x, 0f),
                    end = Offset(x, size.height),
                    strokeWidth = 1.dp.toPx()
                )
                drawCircle(
                    color = GreenDeep,
                    radius = 5.dp.toPx(),
                    center = Offset(x, 22.dp.toPx())
                )
            }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
            verticalAlignment = Alignment.Top
        ) {
            if (showAddTime) {
                Box(
                    modifier = Modifier.width(44.dp).padding(top = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = record.timeText.ifBlank {
                            if (record.createdAt > 0L) formatTime(record.createdAt) else "—"
                        },
                        fontSize = 12.sp,
                        color = TextMuted,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Column(
                    modifier = Modifier.width(44.dp).padding(top = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("$day", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("${month}月", fontSize = 11.sp, color = TextMuted)
                }
            }
            Spacer(Modifier.width(16.dp))
            Surface(
                color = Bg,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f).padding(top = 2.dp)
            ) {
                Column(Modifier.padding(12.dp)) {
                    val head = if (showAddTime) {
                        record.type
                    } else {
                        listOfNotNull(
                            record.type.takeIf { it.isNotBlank() },
                            record.timeText.takeIf { it.isNotBlank() }
                        ).joinToString(" · ")
                    }
                    if (head.isNotBlank()) {
                        Text(head, fontSize = 11.sp, color = TextMuted)
                    }
                    if (record.content.isNotBlank()) {
                        if (head.isNotBlank()) Spacer(Modifier.height(4.dp))
                        Text(record.content, fontSize = 14.sp, color = TextPrimary)
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthCalendar(
    year: Int,
    month: Int,
    records: List<ContactRecord>,
    selectedDay: Int,
    onSelectDay: (Int) -> Unit
) {
    val cal = remember(year, month) {
        Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, 1)
        }
    }
    val todayCal = remember { Calendar.getInstance() }
    val todayYear = todayCal.get(Calendar.YEAR)
    val todayMonth = todayCal.get(Calendar.MONTH)
    val todayDay = todayCal.get(Calendar.DAY_OF_MONTH)
    val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
    val offset = (firstDayOfWeek + 5) % 7
    val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

    val cells = mutableListOf<Int?>()
    repeat(offset) { cells.add(null) }
    for (d in 1..daysInMonth) cells.add(d)
    while (cells.size % 7 != 0) cells.add(null)

    val daysWithRecords = remember(records, year, month) {
        val set = mutableSetOf<Int>()
        records.forEach { r ->
            val c = Calendar.getInstance().apply { timeInMillis = r.dateMillis }
            if (c.get(Calendar.YEAR) == year && c.get(Calendar.MONTH) == month) {
                set.add(c.get(Calendar.DAY_OF_MONTH))
            }
        }
        set
    }

    Column {
        Row(Modifier.fillMaxWidth()) {
            listOf("一", "二", "三", "四", "五", "六", "日").forEach { label ->
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(label, fontSize = 11.sp, color = TextMuted)
                }
            }
        }
        Spacer(Modifier.height(4.dp))
        cells.chunked(7).forEach { week ->
            Row(Modifier.fillMaxWidth()) {
                week.forEach { day ->
                    Box(
                        modifier = Modifier.weight(1f).height(46.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (day != null) {
                            DayCell(
                                day = day,
                                selected = day == selectedDay,
                                hasRecord = day in daysWithRecords,
                                isToday = year == todayYear && month == todayMonth && day == todayDay,
                                onClick = { onSelectDay(day) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    day: Int,
    selected: Boolean,
    hasRecord: Boolean,
    isToday: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(if (selected) GreenDeep else Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$day",
                fontSize = 13.sp,
                color = if (selected) Color.White else TextPrimary
            )
        }
        if (hasRecord) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(if (selected) Color.White else Terracotta)
            )
        }
        if (isToday) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-4).dp, y = 2.dp)
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(Terracotta),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "今",
                    fontSize = 8.sp,
                    lineHeight = 9.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun SmallAddButton(text: String, onClick: () -> Unit) {
    Surface(color = Color(0xFFF3DDD2), shape = RoundedCornerShape(50)) {
        Row(
            modifier = Modifier
                .clickable(onClick = onClick)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Add, contentDescription = null, tint = Terracotta, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(4.dp))
            Text(text, fontSize = 12.sp, color = Terracotta)
        }
    }
}

@Composable
private fun DashedAddButton(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                drawRoundRect(
                    color = ChipBorder,
                    cornerRadius = CornerRadius(12.dp.toPx()),
                    style = Stroke(
                        width = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8.dp.toPx(), 6.dp.toPx()))
                    )
                )
            }
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Add, contentDescription = null, tint = Terracotta, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text(text, fontSize = 13.sp, color = Terracotta)
        }
    }
}

@Composable
private fun AddRecordOverlay(
    cardId: Long,
    initialDateMillis: Long,
    onDismiss: () -> Unit,
    onSave: (ContactRecord) -> Unit
) {
    var dateMillis by remember(initialDateMillis) { mutableStateOf(initialDateMillis) }
    var type by remember { mutableStateOf("见面") }
    var timeText by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    val canSave = content.isNotBlank()
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.45f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            ),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            color = Bg,
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        focusManager.clearFocus(force = true)
                        keyboard?.hide()
                    }
                    .padding(22.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "添加联系记录",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEAE6DD))
                            .clickable(onClick = onDismiss),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("×", fontSize = 18.sp, color = TextPrimary)
                    }
                }
                Spacer(Modifier.height(16.dp))

                RecordLabel("日期")
                Spacer(Modifier.height(8.dp))
                Surface(
                    color = SurfaceWhite,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { showDatePicker = true }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formatDate(dateMillis),
                            fontSize = 15.sp,
                            color = TextPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            Icons.Filled.KeyboardArrowDown,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))
                RecordLabel("类型")
                Spacer(Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("提醒", "消息", "见面", "电话").forEach { t ->
                        RecordTypeChip(selected = type == t, label = t) { type = t }
                    }
                }

                Spacer(Modifier.height(16.dp))
                RecordLabel("时间")
                Spacer(Modifier.height(8.dp))
                RecordInputBox(
                    value = timeText,
                    onValueChange = { timeText = it },
                    placeholder = "例如：10:20"
                )

                Spacer(Modifier.height(16.dp))
                RecordLabel("记录内容")
                Spacer(Modifier.height(8.dp))
                RecordInputBox(
                    value = content,
                    onValueChange = { content = it },
                    placeholder = "记录这次联系的内容…",
                    multiline = true
                )

                Spacer(Modifier.height(20.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = onDismiss) {
                        Text("取消", color = TextMuted)
                    }
                    Spacer(Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (canSave) IconChipGreen else Color(0xFFEFEDE6))
                            .clickable(enabled = canSave) {
                                onSave(
                                    ContactRecord(
                                        id = 0L,
                                        cardId = cardId,
                                        dateMillis = dateMillis,
                                        type = type,
                                        timeText = timeText.trim(),
                                        content = content.trim()
                                    )
                                )
                            }
                            .padding(horizontal = 22.dp, vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "保存",
                            fontSize = 15.sp,
                            color = if (canSave) GreenDeep else TextMuted
                        )
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        AppDatePickerDialog(
            initialMillis = dateMillis,
            onDismiss = { showDatePicker = false },
            onConfirm = { millis ->
                if (millis != null) dateMillis = millis
                showDatePicker = false
            }
        )
    }
}

@Composable
private fun RecordLabel(text: String) {
    Text(text, fontSize = 13.sp, color = TextMuted)
}

@Composable
private fun RecordTypeChip(selected: Boolean, label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (selected) Terracotta.copy(alpha = 0.14f) else SurfaceWhite)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = if (selected) Terracotta else TextPrimary,
            fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal
        )
    }
}

@Composable
private fun RecordInputBox(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    multiline: Boolean = false
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceWhite)
            .padding(horizontal = 14.dp, vertical = 14.dp)
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = !multiline,
            textStyle = TextStyle(fontSize = 15.sp, color = TextPrimary),
            cursorBrush = SolidColor(Terracotta),
            modifier = Modifier
                .fillMaxWidth()
                .then(if (multiline) Modifier.height(72.dp) else Modifier),
            decorationBox = { inner ->
                Box {
                    if (value.isEmpty()) {
                        Text(placeholder, fontSize = 15.sp, color = TextMuted)
                    }
                    inner()
                }
            }
        )
    }
}
