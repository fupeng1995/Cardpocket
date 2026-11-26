package com.example.cardpocket.ui.list

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cardpocket.data.AppData
import com.example.cardpocket.data.model.Card
import com.example.cardpocket.ui.components.Avatar
import com.example.cardpocket.ui.components.Badge
import com.example.cardpocket.ui.components.CircularIconButton
import com.example.cardpocket.ui.theme.Bg
import com.example.cardpocket.ui.theme.ChipBorder
import com.example.cardpocket.ui.theme.DividerColor
import com.example.cardpocket.ui.theme.GreenDeep
import com.example.cardpocket.ui.theme.SurfaceWhite
import com.example.cardpocket.ui.theme.TextMuted
import com.example.cardpocket.ui.theme.TextPrimary
import com.example.cardpocket.util.isSameMonth
import com.example.cardpocket.util.relativeTime
import com.example.cardpocket.util.todayLabel

enum class SortMode(val label: String, val short: String) {
    NAME("姓名", "姓名"),
    MEETING("最近联系", "最近"),
    CREATED("创建时间", "创建"),
    UPDATED("最近更新", "最近")
}

private fun filterAndSort(cards: List<Card>, query: String, sort: SortMode): List<Card> {
    val q = query.trim()
    val filtered = if (q.isEmpty()) cards else cards.filter {
        it.name.contains(q, ignoreCase = true) ||
            it.subtitle.contains(q, ignoreCase = true) ||
            it.relationship.contains(q, ignoreCase = true)
    }
    return filtered.sortedWith(
        when (sort) {
            SortMode.NAME -> compareBy(String.CASE_INSENSITIVE_ORDER) { it.name }
            SortMode.MEETING -> compareByDescending<Card> { it.lastContact ?: it.meetingDate ?: Long.MIN_VALUE }
                .thenBy(String.CASE_INSENSITIVE_ORDER) { it.name }
            SortMode.CREATED -> compareByDescending<Card> { it.createdAt }
            SortMode.UPDATED -> compareByDescending<Card> { it.updatedAt }
        }
    )
}

@Composable
fun CardListScreen(onCardClick: (Long) -> Unit) {
    val cards by AppData.cards.collectAsState()
    var searchVisible by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var selectedRel by remember { mutableStateOf("全部") }
    var sortMode by remember { mutableStateOf(SortMode.UPDATED) }
    var sortMenuExpanded by remember { mutableStateOf(false) }
    val keyboard = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) { AppData.refresh() }

    val tagLibrary by AppData.relationshipTags.collectAsState()
    val relationships = remember(cards, tagLibrary) {
        val used = cards.map { it.relationship }.filter { it.isNotBlank() }
        val names = tagLibrary.map { it.name } + used
        listOf("全部") + names.distinct()
    }
    LaunchedEffect(relationships, selectedRel) {
        if (selectedRel !in relationships) selectedRel = "全部"
    }

    val total = cards.size
    val newThisMonth = remember(cards) { cards.count { isSameMonth(it.createdAt) } }
    val visible = remember(cards, query, selectedRel, sortMode) {
        val base = cards.filter { selectedRel == "全部" || it.relationship == selectedRel }
        filterAndSort(base, query, sortMode)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            HomeHeader(
                searchVisible = searchVisible,
                query = query,
                onQueryChange = { query = it },
                onToggleSearch = {
                    searchVisible = !searchVisible
                    if (!searchVisible) query = ""
                }
            )
        }
        item {
            StatsCard(
                total = total,
                newThisMonth = newThisMonth,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )
        }
        item {
            RelationshipChips(
                relationships = relationships,
                selected = selectedRel,
                onSelect = { selectedRel = it }
            )
        }
        item {
            SectionHeader(
                count = visible.size,
                sortMode = sortMode,
                expanded = sortMenuExpanded,
                onToggleSort = { sortMenuExpanded = it },
                onSortSelect = {
                    sortMode = it
                    sortMenuExpanded = false
                }
            )
        }
        if (visible.isEmpty()) {
            item { EmptyState(query = query, selectedRel = selectedRel) }
        } else {
            items(visible, key = { it.id }) { card ->
                Column {
                    Box(Modifier.padding(horizontal = 20.dp)) {
                        ContactRow(
                            card = card,
                            onClick = {
                                keyboard?.hide()
                                focusManager.clearFocus()
                                onCardClick(card.id)
                            }
                        )
                    }
                    HorizontalDivider(
                        color = DividerColor,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeHeader(
    searchVisible: Boolean,
    query: String,
    onQueryChange: (String) -> Unit,
    onToggleSearch: () -> Unit
) {
    Column(Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
        if (searchVisible) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    placeholder = { Text("搜索姓名、昵称、关系、备注…") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = onToggleSearch) { Text("取消") }
            }
        } else {
            Text(todayLabel(), fontSize = 13.sp, color = TextMuted)
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "我的人脉",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f)
                )
                CircularIconButton(onClick = onToggleSearch) {
                    Icon(
                        Icons.Filled.Search,
                        contentDescription = "搜索",
                        tint = TextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun StatsCard(total: Int, newThisMonth: Int, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(GreenDeep)
    ) {
        Canvas(Modifier.matchParentSize()) {
            val stroke = Color.White.copy(alpha = 0.06f)
            drawCircle(
                color = stroke,
                radius = size.height * 0.95f,
                center = Offset(size.width, 0f),
                style = Stroke(width = 1.5f)
            )
            drawCircle(
                color = stroke,
                radius = size.height * 0.68f,
                center = Offset(size.width, 0f),
                style = Stroke(width = 1.5f)
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 22.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text("人脉总数", color = Color.White.copy(alpha = 0.70f), fontSize = 13.sp)
                Spacer(Modifier.height(6.dp))
                Text("$total", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
            }
            Box(
                Modifier
                    .width(1.dp)
                    .height(46.dp)
                    .background(Color.White.copy(alpha = 0.18f))
            )
            Column(Modifier.weight(1f).padding(start = 24.dp)) {
                Text("本月新增", color = Color.White.copy(alpha = 0.70f), fontSize = 13.sp)
                Spacer(Modifier.height(6.dp))
                Text("+$newThisMonth", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun RelationshipChips(
    relationships: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
    ) {
        Text("关系标签", fontSize = 12.sp, color = TextMuted)
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            relationships.forEach { rel ->
                val active = rel == selected
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(if (active) GreenDeep else SurfaceWhite)
                        .border(1.dp, if (active) GreenDeep else ChipBorder, RoundedCornerShape(50))
                        .clickable { onSelect(rel) }
                        .padding(horizontal = 18.dp, vertical = 9.dp)
                ) {
                    Text(
                        text = rel,
                        color = if (active) Color.White else TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = if (active) FontWeight.Medium else FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(
    count: Int,
    sortMode: SortMode,
    expanded: Boolean,
    onToggleSort: (Boolean) -> Unit,
    onSortSelect: (SortMode) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("最近联系", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Spacer(Modifier.width(8.dp))
        Text("$count 位联系人", fontSize = 13.sp, color = TextMuted)
        Spacer(Modifier.weight(1f))
        Box {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onToggleSort(true) }
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(sortMode.short, fontSize = 13.sp, color = TextMuted)
                Icon(
                    Icons.Filled.KeyboardArrowDown,
                    contentDescription = "排序",
                    tint = TextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { onToggleSort(false) }) {
                SortMode.entries.forEach { mode ->
                    DropdownMenuItem(
                        text = { Text(mode.label) },
                        onClick = { onSortSelect(mode) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ContactRow(card: Card, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Avatar(name = card.name, emoji = card.avatarEmoji, size = 52.dp)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = card.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                if (card.relationship.isNotBlank()) {
                    Spacer(Modifier.width(8.dp))
                    Badge(card.relationship)
                }
            }
            val subtitle = card.subtitle
            if (subtitle.isNotBlank()) {
                Spacer(Modifier.height(3.dp))
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = TextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Spacer(Modifier.width(8.dp))
        Text(
            text = relativeTime(card.lastContact ?: card.meetingDate ?: card.updatedAt),
            fontSize = 12.sp,
            color = TextMuted
        )
        Spacer(Modifier.width(4.dp))
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = TextMuted,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun EmptyState(query: String, selectedRel: String) {
    val searching = query.isNotBlank()
    val filtered = selectedRel != "全部"
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(40.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = when {
                    searching -> "没有找到匹配的名片"
                    filtered -> "「$selectedRel」标签下还没有名片"
                    else -> "还没有名片"
                },
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = when {
                    searching -> "换个关键词或筛选试试"
                    filtered -> "给名片设置该标签后，就会出现在这里"
                    else -> "点击下方 + 开始收录你认识的人"
                },
                fontSize = 13.sp,
                color = TextMuted
            )
        }
    }
}
