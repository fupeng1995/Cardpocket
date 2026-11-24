package com.example.cardpocket.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cardpocket.data.AppData
import com.example.cardpocket.data.model.Card
import com.example.cardpocket.ui.components.Avatar
import com.example.cardpocket.ui.components.CircularIconButton
import com.example.cardpocket.ui.theme.DividerColor
import com.example.cardpocket.ui.theme.GreenDeep
import com.example.cardpocket.ui.theme.IconChipBeige
import com.example.cardpocket.ui.theme.IconChipBlue
import com.example.cardpocket.ui.theme.IconChipGreen
import com.example.cardpocket.ui.theme.IconChipPink
import com.example.cardpocket.ui.theme.TextMuted
import com.example.cardpocket.ui.theme.TextPrimary
import com.example.cardpocket.util.isSameMonth

@Composable
fun ProfileScreen(onOpenTemplates: () -> Unit, onOpenFeedback: () -> Unit) {
    val cards by AppData.cards.collectAsState()
    val templateList by AppData.templates.collectAsState()
    var showAbout by remember { mutableStateOf(false) }
    var showComingSoon by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        AppData.refresh()
    }

    val interactions = remember(cards) {
        cards.count { isSameMonth(it.meetingDate ?: it.updatedAt) }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item { ProfileHeader(onSettings = { showAbout = true }) }
        item { ProfileCard() }
        item { ProfileStats(friends = cards.size, templates = templateList.size, interactions = interactions) }
        item { MenuList(onOpenTemplates = onOpenTemplates, onOpenFeedback = onOpenFeedback, onComingSoon = { showComingSoon = it }) }
    }
}

@Composable
private fun ProfileHeader(onSettings: () -> Unit) {
    Column(Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
        Text("个人空间", fontSize = 13.sp, color = TextMuted)
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "我的",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.weight(1f)
            )
            CircularIconButton(onClick = onSettings) {
                Icon(
                    Icons.Filled.Settings,
                    contentDescription = "设置",
                    tint = TextPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun ProfileCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(GreenDeep)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Avatar(name = "名片夹用户", emoji = "", size = 52.dp)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text("名片夹用户", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(3.dp))
                Text(
                    "纯本地存储 · 数据不上传",
                    color = Color.White.copy(alpha = 0.70f),
                    fontSize = 13.sp
                )
            }
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.70f)
            )
        }
    }
}

@Composable
private fun ProfileStats(friends: Int, templates: Int, interactions: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        StatColumn("$friends", "人脉", Modifier.weight(1f))
        StatDivider()
        StatColumn("$templates", "模板", Modifier.weight(1f))
        StatDivider()
        StatColumn("$interactions", "本月互动", Modifier.weight(1f))
    }
}

@Composable
private fun StatColumn(value: String, label: String, modifier: Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Spacer(Modifier.height(4.dp))
        Text(label, fontSize = 12.sp, color = TextMuted)
    }
}

@Composable
private fun StatDivider() {
    Box(Modifier.width(1.dp).height(32.dp).background(DividerColor))
}

@Composable
private fun MenuList(
    onOpenTemplates: () -> Unit,
    onOpenFeedback: () -> Unit,
    onComingSoon: (String) -> Unit
) {
    Column(Modifier.padding(horizontal = 20.dp)) {
        MenuRow("我的名片模板", IconChipBeige, onClick = onOpenTemplates) {
            Icon(Icons.Filled.AccountBox, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(20.dp))
        }
        MenuDivider()
        MenuRow("数据备份", IconChipBlue, onClick = { onComingSoon("数据备份") }) {
            Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(20.dp))
        }
        MenuDivider()
        MenuRow("隐私与安全", IconChipPink, onClick = { onComingSoon("隐私与安全") }) {
            Icon(Icons.Filled.Lock, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(20.dp))
        }
        MenuDivider()
        MenuRow("反馈与建议", IconChipGreen, onClick = onOpenFeedback) {
            Icon(Icons.Filled.Email, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun MenuDivider() {
    HorizontalDivider(color = DividerColor, modifier = Modifier.padding(start = 56.dp))
}

@Composable
private fun MenuRow(
    title: String,
    chipColor: Color,
    onClick: () -> Unit,
    icon: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(chipColor),
            contentAlignment = Alignment.Center
        ) {
            icon()
        }
        Spacer(Modifier.width(14.dp))
        Text(title, fontSize = 15.sp, color = TextPrimary, modifier = Modifier.weight(1f))
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = TextMuted,
            modifier = Modifier.size(18.dp)
        )
    }
}
