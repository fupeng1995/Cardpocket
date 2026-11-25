package com.example.cardpocket.ui.templates

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cardpocket.data.AppData
import com.example.cardpocket.data.model.Template
import com.example.cardpocket.data.model.TemplateField
import com.example.cardpocket.ui.components.CircularIconButton
import com.example.cardpocket.ui.theme.Bg
import com.example.cardpocket.ui.theme.ChipBorder
import com.example.cardpocket.ui.theme.SurfaceWhite
import com.example.cardpocket.ui.theme.Terracotta
import com.example.cardpocket.ui.theme.TextMuted
import com.example.cardpocket.ui.theme.TextPrimary
import kotlinx.coroutines.launch

@Composable
fun TemplateListScreen(
    onBack: () -> Unit,
    onEditTemplate: (Long?) -> Unit
) {
    val templates by AppData.templates.collectAsState()
    val scope = rememberCoroutineScope()
    var fieldsByTemplate by remember { mutableStateOf<Map<Long, List<TemplateField>>>(emptyMap()) }

    LaunchedEffect(templates) {
        fieldsByTemplate = AppData.getAllTemplateFields().groupBy { it.templateId }
    }
    BackHandler { onBack() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
            .statusBarsPadding()
    ) {
        Column(Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
            Text("为不同关系，留下合适的信息", fontSize = 13.sp, color = TextMuted)
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularIconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "返回",
                        tint = TextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "名片模板",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f)
                )
                CircularIconButton(onClick = { onEditTemplate(null) }) {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = "新建模板",
                        tint = TextPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { TemplateBanner() }
            item { TemplateSectionHeader(templates.size) }
            itemsIndexed(templates, key = { _, t -> t.id }) { index, template ->
                TemplateCard(
                    index = index,
                    template = template,
                    fields = fieldsByTemplate[template.id].orEmpty(),
                    onClick = { onEditTemplate(template.id) },
                    onSetDefault = {
                        scope.launch { AppData.setDefaultTemplate(template.id) }
                    }
                )
            }
            item { CreateTemplateButton { onEditTemplate(null) } }
            item {
                Text(
                    text = "模板保存在本机；新建联系人时可选择模板。",
                    fontSize = 12.sp,
                    color = TextMuted,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun TemplateBanner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Terracotta)
    ) {
        Column(Modifier.padding(20.dp)) {
            Text("联系人字段模板", color = Color.White.copy(alpha = 0.78f), fontSize = 12.sp)
            Spacer(Modifier.height(10.dp))
            Text("让每段关系各有侧重", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text("自定义字段 · 自由排序 · 新建时选用", color = Color.White.copy(alpha = 0.82f), fontSize = 12.sp)
        }

    }
}

@Composable
private fun TemplateSectionHeader(count: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("我的模板", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
    }
}

@Composable
private fun TemplateCard(
    index: Int,
    template: Template,
    fields: List<TemplateField>,
    onClick: () -> Unit,
    onSetDefault: () -> Unit
) {
    Surface(
        color = SurfaceWhite,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(template.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                if (template.isDefault) {
                    Spacer(Modifier.width(8.dp))
                    Surface(color = Terracotta.copy(alpha = 0.14f), shape = RoundedCornerShape(50)) {
                        Text(
                            "默认",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            fontSize = 11.sp,
                            color = Terracotta
                        )
                    }
                }
                Spacer(Modifier.weight(1f))
                Text(
                    text = "${(index + 1).toString().padStart(2, '0')} / ${fields.size} 个字段",
                    fontSize = 12.sp,
                    color = TextMuted
                )
                Spacer(Modifier.width(4.dp))
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(16.dp)
                )
            }
            if (!template.isDefault) {
                Spacer(Modifier.height(4.dp))
                TextButton(onClick = onSetDefault) {
                    Text("设为默认", color = Terracotta, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun CreateTemplateButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                drawRoundRect(
                    color = ChipBorder,
                    cornerRadius = CornerRadius(14.dp.toPx()),
                    style = Stroke(
                        width = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8.dp.toPx(), 6.dp.toPx()))
                    )
                )
            }
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Add, contentDescription = null, tint = Terracotta, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text("创建新的字段模板", fontSize = 14.sp, color = Terracotta)
        }
    }
}
