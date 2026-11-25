@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.example.cardpocket.ui.edit

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cardpocket.data.AppData
import com.example.cardpocket.data.KEY_RELATIONSHIP
import com.example.cardpocket.data.model.CardField
import com.example.cardpocket.data.model.FieldType
import com.example.cardpocket.ui.components.TemplatePickerDialog
import com.example.cardpocket.ui.components.AppDatePickerDialog
import com.example.cardpocket.ui.components.DropdownSelect
import com.example.cardpocket.ui.theme.Bg
import com.example.cardpocket.ui.theme.ChipBorder
import com.example.cardpocket.ui.theme.DividerColor
import com.example.cardpocket.ui.theme.SurfaceWhite
import com.example.cardpocket.ui.theme.Terracotta
import com.example.cardpocket.ui.theme.TextMuted
import com.example.cardpocket.ui.theme.TextPrimary
import com.example.cardpocket.util.formatDate
import kotlinx.coroutines.launch

private val presetEmojis = listOf(
    "😀", "😎", "🤓", "😇", "🥰", "😺", "🐱", "🦊",
    "🐶", "🐰", "🦁", "🐼", "🐯", "🐨", "🐸", "🌟"
)

@Composable
fun CardEditScreen(
    cardId: Long?,
    initialTemplateId: Long?,
    onDone: () -> Unit,
    onBack: () -> Unit
) {
    var templateId by remember { mutableStateOf(0L) }
    var templateName by remember { mutableStateOf("") }
    var avatarEmoji by remember { mutableStateOf("") }
    var fields by remember { mutableStateOf<List<CardField>>(emptyList()) }
    var values by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var showPicker by remember { mutableStateOf(false) }
    var pickerVisible by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var dateFieldKey by remember { mutableStateOf<String?>(null) }

    val relationshipTags by AppData.relationshipTags.collectAsState()
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    LaunchedEffect(cardId, initialTemplateId) {
        AppData.refresh()
        if (cardId != null) {
            val card = AppData.getCard(cardId)
            templateId = card?.templateId ?: 0L
            templateName = AppData.getTemplate(templateId)?.name ?: ""
            avatarEmoji = card?.avatarEmoji ?: ""
            fields = AppData.getCardFields(cardId)
            values = AppData.getCardValues(cardId)
        } else if (initialTemplateId != null) {
            val tFields = AppData.getTemplateFields(initialTemplateId)
            fields = tFields.mapIndexed { i, f ->
                CardField(0L, f.key, f.name, f.type, f.options, f.required, i)
            }
            values = fields.associate { it.key to "" }
            templateId = initialTemplateId
            templateName = AppData.getTemplate(initialTemplateId)?.name ?: ""
        } else {
            showPicker = true
        }
    }

    fun loadTemplate(id: Long) {
        scope.launch {
            val oldNameToValue = fields.associate { it.name to (values[it.key] ?: "") }
            val tFields = AppData.getTemplateFields(id)
            val newFields = tFields.mapIndexed { i, f ->
                CardField(
                    cardId = cardId ?: 0L,
                    key = f.key,
                    name = f.name,
                    type = f.type,
                    options = f.options,
                    required = f.required,
                    sortOrder = i
                )
            }
            fields = newFields
            values = newFields.associate { f -> f.key to (oldNameToValue[f.name] ?: "") }
            templateId = id
            templateName = AppData.getTemplate(id)?.name ?: ""
        }
    }

    fun save() {
        val missing = fields.firstOrNull { it.required && values[it.key].orEmpty().isBlank() }
        if (missing != null) {
            error = "请填写${missing.name}"
            return
        }
        scope.launch {
            AppData.saveCard(cardId ?: 0L, templateId, avatarEmoji, fields, values)
            onDone()
        }
    }

    BackHandler { onBack() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
            .statusBarsPadding()
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
            TextButton(onClick = { save() }, enabled = fields.isNotEmpty()) {
                Text(
                    "保存",
                    color = if (fields.isNotEmpty()) TextPrimary else TextMuted,
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
            Spacer(Modifier.height(20.dp))
            AvatarPicker(
                emoji = avatarEmoji,
                pickerVisible = pickerVisible,
                onToggle = { pickerVisible = !pickerVisible },
                onChange = { avatarEmoji = it }
            )

            if (cardId == null) {
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceWhite)
                        .clickable { showPicker = true }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("模板", fontSize = 13.sp, color = TextMuted)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = templateName.ifBlank { "选择模板" },
                        fontSize = 15.sp,
                        color = TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    Text("更换", fontSize = 13.sp, color = Terracotta)
                }
            }

            Spacer(Modifier.height(20.dp))
            if (fields.isNotEmpty()) {
                FormCard {
                    fields.forEachIndexed { index, field ->
                        FieldInput(
                            field = field,
                            value = values[field.key].orEmpty(),
                            onChange = { values = values + (field.key to it) },
                            onPickDate = { dateFieldKey = field.key },
                            relationshipOptions = relationshipTags.map { it.name }
                        )
                        if (index < fields.lastIndex) FormDivider()
                    }
                }
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

            Spacer(Modifier.height(24.dp))
            Text(
                text = "联系人信息仅保存在你的名片夹中",
                fontSize = 12.sp,
                color = TextMuted,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(32.dp))
        }
    }

    if (showPicker) {
        TemplatePickerDialog(
            onDismiss = { if (fields.isNotEmpty()) showPicker = false },
            onPick = { template ->
                showPicker = false
                loadTemplate(template.id)
            }
        )
    }

    val dk = dateFieldKey
    if (dk != null) {
        AppDatePickerDialog(
            initialMillis = values[dk]?.toLongOrNull(),
            onDismiss = { dateFieldKey = null },
            onConfirm = { millis ->
                values = if (millis == null) values - dk else values + (dk to millis.toString())
                dateFieldKey = null
            }
        )
    }
}

@Composable
private fun FieldInput(
    field: CardField,
    value: String,
    onChange: (String) -> Unit,
    onPickDate: () -> Unit,
    relationshipOptions: List<String>
) {
    when (field.type) {
        FieldType.TEXT -> InputRow(field.name, value, onChange, required = field.required)
        FieldType.MULTILINE_TEXT -> MultilineRow(field.name, value, onChange, required = field.required)
        FieldType.DATE -> DateRow(field.name, value, onPickDate)
        FieldType.SINGLE_SELECT -> SelectRow(
            field.name,
            value,
            if (field.key == KEY_RELATIONSHIP) relationshipOptions else field.options,
            onChange
        )
        FieldType.MULTI_SELECT -> MultiSelectRow(field.name, value, field.options, onChange)
        FieldType.SWITCH -> SwitchRow(field.name, value, onChange)
        else -> InputRow(field.name, value, onChange, required = field.required)
    }
}

@Composable
private fun InputRow(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    required: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(56.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label + if (required) " *" else "",
            fontSize = 14.sp,
            color = TextPrimary,
            modifier = Modifier.width(92.dp)
        )
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = TextStyle(fontSize = 15.sp, color = TextPrimary),
            cursorBrush = SolidColor(Terracotta),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            modifier = Modifier.weight(1f),
            decorationBox = { inner ->
                Box {
                    if (value.isEmpty()) {
                        Text("请输入$label", fontSize = 15.sp, color = TextMuted)
                    }
                    inner()
                }
            }
        )
    }
}

@Composable
private fun MultilineRow(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    required: Boolean = false
) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
        Text(
            text = label + if (required) " *" else "",
            fontSize = 14.sp,
            color = TextPrimary
        )
        Spacer(Modifier.height(6.dp))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = TextStyle(fontSize = 15.sp, color = TextPrimary),
            cursorBrush = SolidColor(Terracotta),
            modifier = Modifier.fillMaxWidth().height(72.dp),
            decorationBox = { inner ->
                Box {
                    if (value.isEmpty()) {
                        Text("请输入$label", fontSize = 15.sp, color = TextMuted)
                    }
                    inner()
                }
            }
        )
    }
}

@Composable
private fun DateRow(label: String, value: String, onPick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onPick).padding(horizontal = 16.dp).height(56.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 14.sp, color = TextPrimary, modifier = Modifier.width(92.dp))
        Text(
            text = value.toLongOrNull()?.let { formatDate(it) } ?: "选择日期",
            fontSize = 15.sp,
            color = if (value.isBlank()) TextMuted else TextPrimary,
            modifier = Modifier.weight(1f)
        )
        Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null, tint = TextMuted, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun SelectRow(label: String, value: String, options: List<String>, onChange: (String) -> Unit) {
    DropdownSelect(
        selected = value,
        options = options,
        label = { it },
        onSelect = onChange,
        modifier = Modifier.fillMaxWidth(),
        header = "选择$label",
        clearLabel = "清空",
        onClear = { onChange("") }
    ) { expanded ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(56.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, fontSize = 14.sp, color = TextPrimary, modifier = Modifier.width(92.dp))
            Text(
                text = value.ifBlank { "选择$label" },
                fontSize = 15.sp,
                color = if (value.isBlank()) TextMuted else TextPrimary,
                modifier = Modifier.weight(1f)
            )
            Icon(
                Icons.Filled.KeyboardArrowDown,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(18.dp).rotate(if (expanded) 180f else 0f)
            )
        }
    }
}

@Composable
private fun MultiSelectRow(label: String, value: String, options: List<String>, onChange: (String) -> Unit) {
    val selected = if (value.isBlank()) emptySet() else value.split("\u001F").toSet()
    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
        Text(label, fontSize = 14.sp, color = TextPrimary)
        Spacer(Modifier.height(8.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { option ->
                FilterChip(
                    selected = option in selected,
                    onClick = {
                        val next = if (option in selected) selected - option else selected + option
                        onChange(next.joinToString("\u001F"))
                    },
                    label = { Text(option, fontSize = 12.sp) }
                )
            }
        }
    }
}

@Composable
private fun SwitchRow(label: String, value: String, onChange: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(56.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 14.sp, color = TextPrimary, modifier = Modifier.weight(1f))
        Switch(
            checked = value == "true",
            onCheckedChange = { onChange(it.toString()) }
        )
    }
}

@Composable
private fun AvatarPicker(
    emoji: String,
    pickerVisible: Boolean,
    onToggle: () -> Unit,
    onChange: (String) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(com.example.cardpocket.ui.theme.IconChipGreen)
                .clickable(onClick = onToggle),
            contentAlignment = Alignment.Center
        ) {
            if (emoji.isBlank()) {
                Text("＋", fontSize = 34.sp, color = TextMuted)
            } else {
                Text(emoji, fontSize = 40.sp)
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = if (emoji.isBlank()) "添加头像" else "更换头像",
            fontSize = 13.sp,
            color = Terracotta,
            modifier = Modifier.clickable(onClick = onToggle)
        )
        if (pickerVisible) {
            Spacer(Modifier.height(10.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                presetEmojis.forEach { e ->
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceWhite)
                            .clickable {
                                onChange(e)
                                onToggle()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(e, fontSize = 20.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun FormCard(content: @Composable () -> Unit) {
    Surface(
        color = SurfaceWhite,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(content = { content() })
    }
}

@Composable
private fun FormDivider() {
    HorizontalDivider(color = DividerColor, modifier = Modifier.padding(horizontal = 16.dp))
}
