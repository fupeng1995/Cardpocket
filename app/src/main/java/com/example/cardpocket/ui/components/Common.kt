package com.example.cardpocket.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cardpocket.ui.theme.BadgeBg
import com.example.cardpocket.ui.theme.BadgeText
import com.example.cardpocket.ui.theme.GreenDeep
import com.example.cardpocket.ui.theme.SurfaceWhite
import com.example.cardpocket.ui.theme.Terracotta
import com.example.cardpocket.ui.theme.TextMuted

/** 圆形图标按钮（搜索 / 设置 / 关闭 等）。 */
@Composable
fun CircularIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    container: Color = SurfaceWhite,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(container)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

/** 关系标签小徽章。 */
@Composable
fun Badge(text: String) {
    Surface(color = BadgeBg, shape = RoundedCornerShape(6.dp)) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            fontSize = 11.sp,
            color = BadgeText,
            fontWeight = FontWeight.Medium
        )
    }
}

enum class MainTab(val label: String) {
    Connections("人脉"),
    Profile("我的")
}

/** 底部导航：人脉 / 中间加号 / 我的。 */
@Composable
fun BottomNavBar(
    selected: MainTab,
    onSelect: (MainTab) -> Unit,
    onAdd: () -> Unit
) {
    Surface(color = SurfaceWhite) {
        Column(Modifier.navigationBarsPadding()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    NavItem(MainTab.Connections, Icons.AutoMirrored.Filled.List, selected, onSelect)
                }
                FloatingActionButton(
                    onClick = onAdd,
                    containerColor = Terracotta,
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = "新增名片",
                        modifier = Modifier.size(26.dp)
                    )
                }
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    NavItem(MainTab.Profile, Icons.Filled.Person, selected, onSelect)
                }
            }
        }
    }
}

@Composable
private fun NavItem(
    tab: MainTab,
    icon: ImageVector,
    selected: MainTab,
    onSelect: (MainTab) -> Unit
) {
    val active = tab == selected
    val tint = if (active) GreenDeep else TextMuted
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onSelect(tab) }
            .padding(horizontal = 14.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, contentDescription = tab.label, tint = tint, modifier = Modifier.size(22.dp))
        Spacer(Modifier.height(3.dp))
        Text(
            text = tab.label,
            fontSize = 11.sp,
            color = tint,
            fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}
