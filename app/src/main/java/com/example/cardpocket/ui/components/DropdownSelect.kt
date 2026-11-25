package com.example.cardpocket.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cardpocket.ui.theme.SurfaceWhite
import com.example.cardpocket.ui.theme.Terracotta
import com.example.cardpocket.ui.theme.TextMuted
import com.example.cardpocket.ui.theme.TextPrimary

/**
 * 通用下拉选择：触发器由 [trigger] 自定义，浮层为统一的圆角白底样式，
 * 选中项高亮 + 对勾，可选「清空」项。
 */
@Composable
fun <T> DropdownSelect(
    selected: T,
    options: List<T>,
    label: (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    header: String = "",
    clearLabel: String? = null,
    onClear: (() -> Unit)? = null,
    trigger: @Composable (expanded: Boolean) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var widthPx by remember { mutableStateOf(0) }

    Box(
        modifier = modifier.onGloballyPositioned { widthPx = it.size.width }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = enabled) { expanded = !expanded }
        ) {
            trigger(expanded)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .width(with(LocalDensity.current) { widthPx.toDp() })
                .heightIn(max = 320.dp),
            offset = DpOffset(0.dp, 6.dp),
            shape = RoundedCornerShape(16.dp),
            containerColor = SurfaceWhite,
            shadowElevation = 12.dp
        ) {
            Column(Modifier.padding(6.dp)) {
                if (header.isNotEmpty()) {
                    Text(
                        text = header,
                        fontSize = 11.sp,
                        color = TextMuted,
                        modifier = Modifier.padding(start = 10.dp, top = 4.dp, bottom = 4.dp)
                    )
                }
                options.forEach { option ->
                    val isSelected = option == selected
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isSelected) Terracotta.copy(alpha = 0.10f)
                                else Color.Transparent
                            )
                            .clickable {
                                onSelect(option)
                                expanded = false
                            }
                            .padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = label(option),
                            fontSize = 14.sp,
                            color = if (isSelected) Terracotta else TextPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        if (isSelected) {
                            Icon(
                                Icons.Filled.Check,
                                contentDescription = null,
                                tint = Terracotta,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
                if (clearLabel != null && onClear != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                onClear()
                                expanded = false
                            }
                            .padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(clearLabel, fontSize = 14.sp, color = TextMuted, modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}
