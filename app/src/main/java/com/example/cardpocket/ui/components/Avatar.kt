package com.example.cardpocket.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cardpocket.ui.theme.AvatarPastel1
import com.example.cardpocket.ui.theme.AvatarPastel2
import com.example.cardpocket.ui.theme.AvatarPastel3
import com.example.cardpocket.ui.theme.AvatarPastel4
import com.example.cardpocket.ui.theme.AvatarPastel5
import com.example.cardpocket.ui.theme.AvatarPastel6
import com.example.cardpocket.ui.theme.TextPrimary

private val pastels = listOf(
    AvatarPastel1, AvatarPastel2, AvatarPastel3,
    AvatarPastel4, AvatarPastel5, AvatarPastel6
)

/** 根据姓名稳定地取一个柔和底色。 */
fun avatarPastel(seed: String): Color {
    val count = pastels.size
    val idx = ((seed.hashCode() % count) + count) % count
    return pastels[idx]
}

/** 头像文字：英文取前两位字母，中文取首字。 */
fun initialsOf(name: String): String {
    val trimmed = name.trim()
    if (trimmed.isEmpty()) return "?"
    val letters = trimmed.filter { it.isLetterOrDigit() }
    return if (letters.isNotEmpty() && letters.all { it.code < 128 }) {
        letters.take(2).uppercase()
    } else {
        trimmed.first().toString()
    }
}

/**
 * 方形圆角头像：有表情时展示表情，否则展示姓名首字母。
 */
@Composable
fun Avatar(
    name: String,
    emoji: String,
    size: Dp = 48.dp,
    modifier: Modifier = Modifier
) {
    val trimmedEmoji = emoji.trim()
    val isEmoji = trimmedEmoji.isNotEmpty()
    val content = if (isEmoji) trimmedEmoji else initialsOf(name)
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.30f))
            .background(avatarPastel(name)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = content,
            color = TextPrimary,
            fontSize = (size.value * if (isEmoji) 0.44f else 0.30f).sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
        )
    }
}
