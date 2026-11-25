package com.example.cardpocket.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.example.cardpocket.data.model.FieldType
import com.example.cardpocket.ui.theme.Bg
import com.example.cardpocket.ui.theme.ChipBorder
import com.example.cardpocket.ui.theme.GreenDeep
import com.example.cardpocket.ui.theme.IconChipGreen
import com.example.cardpocket.ui.theme.SurfaceWhite
import com.example.cardpocket.ui.theme.Terracotta
import com.example.cardpocket.ui.theme.TextMuted
import com.example.cardpocket.ui.theme.TextPrimary

private val TYPE_OPTIONS = listOf(
    FieldType.TEXT to "单行文本",
    FieldType.MULTILINE_TEXT to "多行文本",
    FieldType.DATE to "日期",
    FieldType.SINGLE_SELECT to "单选",
    FieldType.MULTI_SELECT to "多选",
    FieldType.SWITCH to "开关"
)

private fun typeLabel(type: FieldType): String =
    TYPE_OPTIONS.firstOrNull { it.first == type }?.second ?: "单行文本"

/** 新增 / 编辑字段弹窗（名称 + 类型 + 选项 + 是否必填）。 */
@Composable
fun FieldFormDialog(
    title: String,
    confirmText: String,
    initialName: String = "",
    initialType: FieldType = FieldType.TEXT,
    initialOptions: List<String> = emptyList(),
    initialRequired: Boolean = false,
    lockType: Boolean = false,
    lockRequired: Boolean = false,
    existingNames: List<String> = emptyList(),
    onDismiss: () -> Unit,
    onConfirm: (name: String, type: FieldType, options: List<String>, required: Boolean) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var type by remember { mutableStateOf(initialType) }
    var optionsText by remember { mutableStateOf(initialOptions.joinToString("\n")) }
    var required by remember { mutableStateOf(initialRequired) }
    var typeMenu by remember { mutableStateOf(false) }
    var triggerWidthPx by remember { mutableStateOf(0) }
    var triggerHeightPx by remember { mutableStateOf(0) }
    val chevronRotation by animateFloatAsState(if (typeMenu) 180f else 0f, label = "chevron")

    val parsedOptions = optionsText.split("\n").map { it.trim() }.filter { it.isNotBlank() }.distinct()
    val isSelect = type == FieldType.SINGLE_SELECT || type == FieldType.MULTI_SELECT
    val canConfirm = name.isNotBlank() && name.trim() !in existingNames && (!isSelect || parsedOptions.isNotEmpty())

    Dialog(onDismissRequest = onDismiss) {
        Surface(color = Bg, shape = RoundedCornerShape(28.dp)) {
            Column(Modifier.padding(22.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
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
                Spacer(Modifier.height(18.dp))

                Text("字段名称", fontSize = 13.sp, color = TextMuted)
                Spacer(Modifier.height(8.dp))
                InputBox(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = "例如：生日、手机号码、兴趣爱好"
                )

                Spacer(Modifier.height(16.dp))
                Text("字段类型", fontSize = 13.sp, color = TextMuted)
                Spacer(Modifier.height(8.dp))
                Box {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .onGloballyPositioned {
                                triggerWidthPx = it.size.width
                                triggerHeightPx = it.size.height
                            }
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceWhite)
                            .border(1.dp, ChipBorder, RoundedCornerShape(12.dp))
                            .clickable(enabled = !lockType) { typeMenu = !typeMenu }
                            .padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(typeLabel(type), fontSize = 15.sp, color = TextPrimary, modifier = Modifier.weight(1f))
                        Icon(
                            Icons.Filled.KeyboardArrowDown,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(20.dp).rotate(chevronRotation)
                        )
                    }

                    if (typeMenu && triggerWidthPx > 0) {
                        Popup(
                            offset = IntOffset(0, triggerHeightPx + 6),
                            onDismissRequest = { typeMenu = false },
                            properties = PopupProperties(focusable = true)
                        ) {
                            Surface(
                                modifier = Modifier.width(with(LocalDensity.current) { triggerWidthPx.toDp() }),
                                shape = RoundedCornerShape(16.dp),
                                color = SurfaceWhite,
                                shadowElevation = 12.dp
                            ) {
                                Column(Modifier.padding(6.dp)) {
                                    Text(
                                        text = "选择字段类型",
                                        fontSize = 11.sp,
                                        color = TextMuted,
                                        modifier = Modifier.padding(start = 10.dp, top = 4.dp, bottom = 4.dp)
                                    )
                                    TYPE_OPTIONS.forEach { (t, label) ->
                                        val selected = type == t
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(44.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(if (selected) Terracotta.copy(alpha = 0.10f) else Color.Transparent)
                                                .clickable {
                                                    type = t
                                                    typeMenu = false
                                                }
                                                .padding(horizontal = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = label,
                                                fontSize = 14.sp,
                                                color = if (selected) Terracotta else TextPrimary,
                                                modifier = Modifier.weight(1f)
                                            )
                                            if (selected) {
                                                Icon(
                                                    Icons.Filled.Check,
                                                    contentDescription = null,
                                                    tint = Terracotta,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (isSelect) {
                    Spacer(Modifier.height(16.dp))
                    Text("选项（每行一个）", fontSize = 13.sp, color = TextMuted)
                    Spacer(Modifier.height(8.dp))
                    InputBox(
                        value = optionsText,
                        onValueChange = { optionsText = it },
                        placeholder = "例如：家人、朋友、同事",
                        multiline = true
                    )
                }

                Spacer(Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("设为必填", fontSize = 15.sp, color = TextPrimary)
                        Spacer(Modifier.height(2.dp))
                        Text("保存联系人前必须填写此项", fontSize = 12.sp, color = TextMuted)
                    }
                    Checkbox(
                        checked = if (lockRequired) true else required,
                        onCheckedChange = { if (!lockRequired) required = it },
                        enabled = !lockRequired
                    )
                }

                Spacer(Modifier.height(18.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (canConfirm) IconChipGreen else Color(0xFFEFEDE6))
                        .clickable(enabled = canConfirm) {
                            onConfirm(
                                name.trim(),
                                type,
                                if (isSelect) parsedOptions else emptyList(),
                                if (lockRequired) true else required
                            )
                        }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = confirmText,
                        fontSize = 15.sp,
                        color = if (canConfirm) GreenDeep else TextMuted
                    )
                }
            }
        }
    }
}

@Composable
private fun InputBox(
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
            textStyle = TextStyle(fontSize = 15.sp, color = TextPrimary),
            cursorBrush = SolidColor(Terracotta),
            modifier = Modifier
                .fillMaxWidth()
                .then(if (multiline) Modifier.height(90.dp) else Modifier),
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
