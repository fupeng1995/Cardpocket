@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.example.cardpocket.ui.edit

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cardpocket.data.AppData
import com.example.cardpocket.data.model.Card
import com.example.cardpocket.data.model.FieldDef
import com.example.cardpocket.data.model.FieldType
import com.example.cardpocket.data.model.OPTION_SEPARATOR
import com.example.cardpocket.ui.theme.Bg
import com.example.cardpocket.ui.theme.DividerColor
import com.example.cardpocket.ui.theme.IconChipGreen
import com.example.cardpocket.ui.theme.SurfaceWhite
import com.example.cardpocket.ui.theme.Terracotta
import com.example.cardpocket.ui.theme.TextMuted
import com.example.cardpocket.ui.theme.TextPrimary
import com.example.cardpocket.util.formatDate
import kotlinx.coroutines.launch

private val relationshipPresets = listOf(
    "家人", "朋友", "同事", "同学", "老师", "客户", "邻居", "网友", "一面之缘"
)

private val presetEmojis = listOf(
    "😀", "😎", "🤓", "😇", "🥰", "😺", "🐱", "🦊",
    "🐶", "🐰", "🦁", "🐼", "🐯", "🐨", "🐸", "🌟"
)

@Composable
fun CardEditScreen(
    cardId: Long?,
    onDone: () -> Unit,
    onBack: () -> Unit
) {
    val fieldDefs by AppData.fieldDefs.collectAsState()

    var name by remember { mutableStateOf("") }
    var jobTitle by remember { mutableStateOf("") }
    var company by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var relationship by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var avatarEmoji by remember { mutableStateOf("") }
    var fieldValues by remember { mutableStateOf<Map<Long, String>>(emptyMap()) }
    var error by remember { mutableStateOf<String?>(null) }
    var pickerVisible by remember { mutableStateOf(false) }

    // 编辑时保留表单里不展示的原有字段，避免数据丢失
    var originalCard by remember { mutableStateOf<Card?>(null) }

    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    LaunchedEffect(cardId) {
        AppData.refresh()
        val defs = AppData.fieldDefs.value
        val card = cardId?.let { AppData.getCard(it) }
        val vals = cardId?.let { AppData.getFieldValues(it) } ?: emptyList()
        originalCard = card
        name = card?.name ?: ""
        jobTitle = card?.jobTitle ?: ""
        company = card?.company ?: ""
        city = card?.city ?: ""
        relationship = card?.relationship ?: ""
        note = card?.note ?: ""
        avatarEmoji = card?.avatarEmoji ?: ""
        fieldValues = defs.associate { d ->
            d.id to (vals.firstOrNull { it.fieldId == d.id }?.value ?: "")
        }
    }

    fun save() {
        val n = name.trim()
        if (n.isEmpty()) {
            error = "请输入姓名"
            return
        }
        val original = originalCard
        val card = Card(
            id = cardId ?: 0L,
            name = n,
            nickname = original?.nickname ?: "",
            avatarEmoji = avatarEmoji.trim(),
            jobTitle = jobTitle.trim(),
            company = company.trim(),
            city = city.trim(),
            relationship = relationship.trim(),
            meetingDate = original?.meetingDate,
            note = note
        )
        scope.launch {
            AppData.saveCard(card, fieldValues)
            onDone()
        }
    }

    BackHandler { onBack() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
            .statusBarsPadding()
            // 点击空白处收起输入法（输入框/按钮会自行消费点击，不受影响）
            .pointerInput(Unit) {
                detectTapGestures { focusManager.clearFocus() }
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBack) {
                Text("取消", color = TextMuted, fontSize = 15.sp)
            }
            Text(
                text = if (cardId == null) "添加联系人" else "编辑联系人",
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            TextButton(onClick = { save() }) {
                Text("保存", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            }
        }
        HorizontalDivider(color = DividerColor)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(20.dp))
            AvatarPicker(
                emoji = avatarEmoji,
                pickerVisible = pickerVisible,
                onToggle = { pickerVisible = !pickerVisible },
                onChange = { avatarEmoji = it }
            )

            Spacer(Modifier.height(20.dp))
            FormCard {
                FormRow(
                    label = "姓名",
                    value = name,
                    placeholder = "请输入姓名",
                    onValueChange = {
                        name = it
                        if (error != null) error = null
                    }
                )
                FormDivider()
                FormRow("职位", jobTitle, "例如：产品经理", { jobTitle = it })
                FormDivider()
                FormRow("公司", company, "公司或组织", { company = it })
                FormDivider()
                FormRow("城市", city, "所在城市", { city = it })
            }

            val err = error
            if (err != null) {
                Text(
                    text = err,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 4.dp, top = 6.dp)
                )
            }

            Spacer(Modifier.height(16.dp))
            FormCard {
                RelationshipRow(relationship = relationship, onChange = { relationship = it })
                FormDivider()
                NoteRow(
                    label = "关系备注",
                    value = note,
                    placeholder = "记录你们如何认识、对方的关注方向等",
                    onValueChange = { note = it }
                )
            }

            if (fieldDefs.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                FormCard {
                    fieldDefs.forEachIndexed { index, def ->
                        key(def.id) {
                            CustomFieldRow(
                                def = def,
                                value = fieldValues[def.id] ?: "",
                                onChange = { fieldValues = fieldValues + (def.id to it) }
                            )
                        }
                        if (index < fieldDefs.lastIndex) FormDivider()
                    }
                }
            }

            Spacer(Modifier.height(18.dp))
            Text(
                text = "联系人信息仅保存在你的名片夹中",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontSize = 12.sp,
                color = TextMuted
            )
            Spacer(Modifier.height(28.dp))
        }
    }
}

@Composable
private fun AvatarPicker(
    emoji: String,
    pickerVisible: Boolean,
    onToggle: () -> Unit,
    onChange: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(IconChipGreen)
                .clickable(onClick = onToggle),
            contentAlignment = Alignment.Center
        ) {
            val em = emoji.trim()
            if (em.isNotEmpty()) {
                Text(em, fontSize = 42.sp)
            } else {
                Text("+", fontSize = 34.sp, color = TextMuted)
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = if (emoji.isBlank()) "添加头像" else "更换头像",
            color = Terracotta,
            fontSize = 13.sp
        )
        if (pickerVisible) {
            Spacer(Modifier.height(12.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                presetEmojis.forEach { e ->
                    SelectableChip(
                        text = e,
                        selected = emoji == e,
                        onClick = {
                            onChange(if (emoji == e) "" else e)
                            onToggle()
                        },
                        fontSize = 22.sp
                    )
                }
                SelectableChip(
                    text = "首字母",
                    selected = emoji.isBlank(),
                    onClick = { onChange(""); onToggle() },
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun SelectableChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    fontSize: TextUnit = 20.sp
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (selected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            fontSize = fontSize,
            color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
            else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun FormCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        color = SurfaceWhite,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp), content = content)
    }
}

@Composable
private fun FormDivider() {
    HorizontalDivider(color = DividerColor.copy(alpha = 0.75f))
}

@Composable
private fun FormRow(
    label: String,
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = TextPrimary,
            modifier = Modifier.width(72.dp)
        )
        BasicInput(
            value = value,
            onValueChange = onValueChange,
            placeholder = placeholder,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun NoteRow(
    label: String,
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = TextPrimary,
            modifier = Modifier.width(72.dp).padding(top = 2.dp)
        )
        BasicInput(
            value = value,
            onValueChange = onValueChange,
            placeholder = placeholder,
            singleLine = false,
            maxLines = 3,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun BasicInput(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else 3,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = singleLine,
        maxLines = maxLines,
        textStyle = TextStyle(fontSize = 15.sp, color = TextPrimary),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        cursorBrush = SolidColor(Terracotta),
        modifier = modifier.fillMaxWidth(),
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

@Composable
private fun RelationshipRow(relationship: String, onChange: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    var showCustom by remember { mutableStateOf(false) }
    var customText by remember { mutableStateOf("") }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "关系标签",
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = TextPrimary,
            modifier = Modifier.width(72.dp)
        )
        Box(Modifier.weight(1f)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = true }
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (relationship.isBlank()) "选择关系" else relationship,
                    fontSize = 15.sp,
                    color = if (relationship.isBlank()) TextMuted else TextPrimary,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    Icons.Filled.KeyboardArrowDown,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                relationshipPresets.forEach { preset ->
                    DropdownMenuItem(
                        text = { Text(preset) },
                        onClick = {
                            onChange(preset)
                            expanded = false
                        }
                    )
                }
                DropdownMenuItem(
                    text = { Text("自定义…") },
                    onClick = {
                        customText = relationship
                        showCustom = true
                        expanded = false
                    }
                )
                if (relationship.isNotBlank()) {
                    DropdownMenuItem(
                        text = { Text("清空") },
                        onClick = {
                            onChange("")
                            expanded = false
                        }
                    )
                }
            }
        }
    }

    if (showCustom) {
        AlertDialog(
            onDismissRequest = { showCustom = false },
            title = { Text("自定义关系") },
            text = {
                BasicTextField(
                    value = customText,
                    onValueChange = { customText = it },
                    singleLine = true,
                    textStyle = TextStyle(fontSize = 15.sp, color = TextPrimary),
                    cursorBrush = SolidColor(Terracotta),
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { inner ->
                        Box {
                            if (customText.isEmpty()) {
                                Text("例如：球友", fontSize = 15.sp, color = TextMuted)
                            }
                            inner()
                        }
                    }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onChange(customText.trim())
                    showCustom = false
                }) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = { showCustom = false }) { Text("取消") }
            }
        )
    }
}

@Composable
private fun CustomFieldRow(def: FieldDef, value: String, onChange: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = def.name,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = TextPrimary,
            modifier = Modifier.width(72.dp)
        )
        Box(Modifier.weight(1f)) {
            when (def.type) {
                FieldType.TEXT -> BasicInput(value, onChange, "请输入")
                FieldType.MULTILINE_TEXT -> BasicInput(value, onChange, "请输入", singleLine = false, maxLines = 3)
                FieldType.NUMBER -> BasicInput(value, onChange, "请输入数字", keyboardType = KeyboardType.Number)
                FieldType.LINK -> BasicInput(value, onChange, "请输入链接", keyboardType = KeyboardType.Uri)
                FieldType.DATE -> DateInput(value, onChange)
                FieldType.SWITCH -> Switch(
                    checked = value == "true",
                    onCheckedChange = { onChange(if (it) "true" else "false") }
                )
                FieldType.SINGLE_SELECT -> SelectInput(def, value, onChange, multi = false)
                FieldType.MULTI_SELECT -> SelectInput(def, value, onChange, multi = true)
            }
        }
    }
}

@Composable
private fun DateInput(value: String, onChange: (String) -> Unit) {
    var show by remember { mutableStateOf(false) }
    val millis = value.trim().toLongOrNull()

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .weight(1f)
                .clickable { show = true }
                .padding(vertical = 2.dp)
        ) {
            Text(
                text = millis?.let { formatDate(it) } ?: "选择日期",
                fontSize = 15.sp,
                color = if (millis == null) TextMuted else TextPrimary
            )
        }
        if (millis != null) {
            TextButton(onClick = { onChange("") }) { Text("清除", fontSize = 12.sp) }
        }
    }

    if (show) {
        val state = rememberDatePickerState(initialSelectedDateMillis = millis)
        DatePickerDialog(
            onDismissRequest = { show = false },
            confirmButton = {
                TextButton(onClick = {
                    onChange(state.selectedDateMillis?.toString() ?: "")
                    show = false
                }) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = { show = false }) { Text("取消") }
            }
        ) {
            DatePicker(state = state)
        }
    }
}

@Composable
private fun SelectInput(def: FieldDef, value: String, onChange: (String) -> Unit, multi: Boolean) {
    val selected: Set<String> = if (multi) {
        value.split(OPTION_SEPARATOR).filter { it.isNotBlank() }.toSet()
    } else {
        if (value.isNotBlank()) setOf(value) else emptySet()
    }

    if (def.options.isEmpty()) {
        Text("（暂无选项）", fontSize = 13.sp, color = TextMuted)
    } else {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            def.options.forEach { opt ->
                val isSel = opt in selected
                FilterChip(
                    selected = isSel,
                    onClick = {
                        val newSelected: Set<String> = if (multi) {
                            if (isSel) selected - opt else selected + opt
                        } else {
                            if (isSel) emptySet() else setOf(opt)
                        }
                        onChange(newSelected.joinToString(OPTION_SEPARATOR))
                    },
                    label = { Text(opt, fontSize = 13.sp) }
                )
            }
        }
    }
}
