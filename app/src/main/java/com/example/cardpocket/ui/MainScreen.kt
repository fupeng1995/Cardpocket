package com.example.cardpocket.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.cardpocket.ui.components.BottomNavBar
import com.example.cardpocket.ui.components.MainTab
import com.example.cardpocket.ui.components.TemplatePickerDialog
import com.example.cardpocket.ui.list.CardListScreen
import com.example.cardpocket.ui.profile.ProfileScreen
import com.example.cardpocket.ui.theme.Bg

@Composable
fun MainScreen(
    onCardClick: (Long) -> Unit,
    onAddCard: (Long) -> Unit,
    onOpenTemplates: () -> Unit,
    onOpenFeedback: () -> Unit
) {
    var tab by remember { mutableStateOf(MainTab.Connections) }
    var showTemplatePicker by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Bg,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            BottomNavBar(
                selected = tab,
                onSelect = { tab = it },
                onAdd = { showTemplatePicker = true }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            when (tab) {
                MainTab.Connections -> CardListScreen(onCardClick = onCardClick)
                MainTab.Profile -> ProfileScreen(
                    onOpenTemplates = onOpenTemplates,
                    onOpenFeedback = onOpenFeedback
                )
            }
        }
    }

    if (showTemplatePicker) {
        TemplatePickerDialog(
            onDismiss = { showTemplatePicker = false },
            onPick = { template ->
                showTemplatePicker = false
                onAddCard(template.id)
            }
        )
    }
}
