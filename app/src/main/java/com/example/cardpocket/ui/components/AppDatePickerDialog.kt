package com.example.cardpocket.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.cardpocket.ui.theme.Bg
import com.example.cardpocket.ui.theme.GreenDeep
import com.example.cardpocket.ui.theme.SurfaceWhite
import com.example.cardpocket.ui.theme.Terracotta
import com.example.cardpocket.ui.theme.TextMuted
import com.example.cardpocket.ui.theme.TextPrimary
import java.util.Calendar

/** 与整体风格一致的日期选择弹窗（米色底、圆角、深绿选中、陶土色「今」）。 */
@Composable
fun AppDatePickerDialog(
    initialMillis: Long?,
    onDismiss: () -> Unit,
    onConfirm: (Long?) -> Unit
) {
    val init = remember(initialMillis) {
        Calendar.getInstance().apply { if (initialMillis != null) timeInMillis = initialMillis }
    }
    var year by remember { mutableStateOf(init.get(Calendar.YEAR)) }
    var month by remember { mutableStateOf(init.get(Calendar.MONTH)) }
    var selectedDay by remember {
        mutableStateOf(if (initialMillis != null) init.get(Calendar.DAY_OF_MONTH) else -1)
    }

    val today = remember { Calendar.getInstance() }
    val todayYear = today.get(Calendar.YEAR)
    val todayMonth = today.get(Calendar.MONTH)
    val todayDay = today.get(Calendar.DAY_OF_MONTH)

    val firstCal = remember(year, month) {
        Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, 1)
        }
    }
    val offset = (firstCal.get(Calendar.DAY_OF_WEEK) + 5) % 7
    val daysInMonth = firstCal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val cells = buildList {
        repeat(offset) { add(null) }
        for (d in 1..daysInMonth) add(d)
        while (size % 7 != 0) add(null)
    }

    fun millisOf(day: Int): Long = Calendar.getInstance().apply {
        set(Calendar.YEAR, year)
        set(Calendar.MONTH, month)
        set(Calendar.DAY_OF_MONTH, day)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    Dialog(onDismissRequest = onDismiss) {
        Surface(color = Bg, shape = RoundedCornerShape(28.dp)) {
            Column(Modifier.padding(22.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "选择日期",
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
                Spacer(Modifier.height(14.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    MonthArrow(forward = false) {
                        if (month == 0) {
                            month = 11
                            year -= 1
                        } else {
                            month -= 1
                        }
                    }
                    Text(
                        text = "${year}年 ${month + 1}月",
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    MonthArrow(forward = true) {
                        if (month == 11) {
                            month = 0
                            year += 1
                        } else {
                            month += 1
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))

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
                                modifier = Modifier.weight(1f).height(40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (day != null) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clickable { selectedDay = day },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(30.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (selectedDay == day) GreenDeep
                                                    else Color.Transparent
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "$day",
                                                fontSize = 13.sp,
                                                color = if (selectedDay == day) Color.White else TextPrimary
                                            )
                                        }
                                        if (year == todayYear && month == todayMonth && day == todayDay) {
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
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = {
                        year = todayYear
                        month = todayMonth
                        selectedDay = todayDay
                    }) { Text("今天", color = Terracotta) }
                    TextButton(onClick = { selectedDay = -1 }) {
                        Text("清除", color = TextMuted)
                    }
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = onDismiss) { Text("取消", color = TextMuted) }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (selectedDay > 0) GreenDeep else Color(0xFFDAD6CB))
                            .clickable(enabled = selectedDay > 0) {
                                onConfirm(millisOf(selectedDay))
                            }
                            .padding(horizontal = 18.dp, vertical = 9.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "确定",
                            fontSize = 14.sp,
                            color = if (selectedDay > 0) Color.White else TextMuted
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthArrow(forward: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(SurfaceWhite)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (forward) {
                Icons.AutoMirrored.Filled.KeyboardArrowRight
            } else {
                Icons.AutoMirrored.Filled.KeyboardArrowLeft
            },
            contentDescription = null,
            tint = TextPrimary,
            modifier = Modifier.size(18.dp)
        )
    }
}
