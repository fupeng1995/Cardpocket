package com.example.cardpocket.util

import com.example.cardpocket.data.model.FieldType
import com.example.cardpocket.data.model.OPTION_SEPARATOR
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
private val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

fun formatDate(millis: Long?): String =
    if (millis == null) "" else dateFormat.format(Date(millis))

fun formatDateTime(millis: Long?): String =
    if (millis == null) "" else dateTimeFormat.format(Date(millis))

/** 例如：2024年10月 */
fun formatMonth(millis: Long?): String {
    if (millis == null || millis <= 0L) return "未填写"
    val cal = Calendar.getInstance().apply { timeInMillis = millis }
    return "${cal.get(Calendar.YEAR)}年${cal.get(Calendar.MONTH) + 1}月"
}

/** 例如：10:20 */
fun formatTime(millis: Long?): String {
    if (millis == null || millis <= 0L) return ""
    val cal = Calendar.getInstance().apply { timeInMillis = millis }
    val h = cal.get(Calendar.HOUR_OF_DAY)
    val m = cal.get(Calendar.MINUTE)
    return String.format(Locale.getDefault(), "%02d:%02d", h, m)
}

/** 例如：星期四 · 11月21日 */
fun todayLabel(): String {
    val cal = Calendar.getInstance()
    val week = when (cal.get(Calendar.DAY_OF_WEEK)) {
        Calendar.MONDAY -> "星期一"
        Calendar.TUESDAY -> "星期二"
        Calendar.WEDNESDAY -> "星期三"
        Calendar.THURSDAY -> "星期四"
        Calendar.FRIDAY -> "星期五"
        Calendar.SATURDAY -> "星期六"
        else -> "星期日"
    }
    return "$week · ${cal.get(Calendar.MONTH) + 1}月${cal.get(Calendar.DAY_OF_MONTH)}日"
}

/** 相对时间：今天 / 昨天 / N 天前 / M 月 d 日。 */
fun relativeTime(millis: Long?): String {
    if (millis == null || millis <= 0L) return ""
    val now = System.currentTimeMillis()
    val diff = now - millis
    if (diff < 0) return formatDate(millis)
    val days = diff / (24L * 60 * 60 * 1000)
    return when {
        days < 1 -> "今天"
        days < 2 -> "昨天"
        days < 7 -> "${days} 天前"
        else -> {
            val cal = Calendar.getInstance().apply { timeInMillis = millis }
            "${cal.get(Calendar.MONTH) + 1} 月 ${cal.get(Calendar.DAY_OF_MONTH)} 日"
        }
    }
}

/** 是否与当前时间为同一个月。 */
fun isSameMonth(millis: Long): Boolean {
    val now = Calendar.getInstance()
    val then = Calendar.getInstance().apply { timeInMillis = millis }
    return now.get(Calendar.YEAR) == then.get(Calendar.YEAR) &&
        now.get(Calendar.MONTH) == then.get(Calendar.MONTH)
}

/** 将字段存储值格式化为展示文案。 */
fun formatFieldValue(type: FieldType, value: String?): String {
    if (value.isNullOrBlank()) return "未填写"
    return when (type) {
        FieldType.SWITCH -> if (value.trim() == "true") "开" else "关"
        FieldType.DATE -> formatDate(value.trim().toLongOrNull())
        FieldType.MULTI_SELECT ->
            value.split(OPTION_SEPARATOR).filter { it.isNotBlank() }.joinToString("、")
        else -> value
    }
}
