package com.example.cardpocket.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.cardpocket.ui.detail.CardDetailScreen
import com.example.cardpocket.ui.edit.CardEditScreen
import com.example.cardpocket.ui.feedback.FeedbackScreen
import com.example.cardpocket.ui.templates.TemplateEditScreen
import com.example.cardpocket.ui.templates.TemplateListScreen

sealed class Screen {
    data object Main : Screen()
    data class Detail(val cardId: Long) : Screen()
    data class Edit(val cardId: Long?, val templateId: Long? = null) : Screen()
    data object Templates : Screen()
    data class TemplateEdit(val templateId: Long?) : Screen()
    data object Feedback : Screen()
}

@Composable
fun CardPocketApp() {
    var backStack by remember { mutableStateOf(listOf<Screen>(Screen.Main)) }
    val current = backStack.last()
    val push: (Screen) -> Unit = { backStack = backStack + it }
    val pop: () -> Unit = { if (backStack.size > 1) backStack = backStack.dropLast(1) }

    when (val s = current) {
        Screen.Main -> MainScreen(
            onCardClick = { push(Screen.Detail(it)) },
            onAddCard = { templateId -> push(Screen.Edit(null, templateId)) },
            onOpenTemplates = { push(Screen.Templates) },
            onOpenFeedback = { push(Screen.Feedback) }
        )

        is Screen.Detail -> CardDetailScreen(
            cardId = s.cardId,
            onBack = pop,
            onEdit = { push(Screen.Edit(s.cardId)) },
            onDeleted = pop
        )

        is Screen.Edit -> CardEditScreen(
            cardId = s.cardId,
            initialTemplateId = s.templateId,
            onDone = pop,
            onBack = pop
        )

        Screen.Templates -> TemplateListScreen(
            onBack = pop,
            onEditTemplate = { push(Screen.TemplateEdit(it)) }
        )

        is Screen.TemplateEdit -> TemplateEditScreen(
            templateId = s.templateId,
            onDone = pop
        )

        Screen.Feedback -> FeedbackScreen(onBack = pop)
    }
}
