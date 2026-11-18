package com.greenctl.mydeepseek.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.greenctl.mydeepseek.data.DsModel
import com.greenctl.mydeepseek.ui.ChatController
import com.greenctl.mydeepseek.ui.components.ChatInputBar
import com.greenctl.mydeepseek.ui.components.DsIcon
import com.greenctl.mydeepseek.ui.components.DsIconButton
import com.greenctl.mydeepseek.ui.components.DsIconType
import com.greenctl.mydeepseek.ui.components.MessageItem
import com.greenctl.mydeepseek.ui.components.SessionDrawer
import com.greenctl.mydeepseek.ui.components.clickableNoRipple
import com.greenctl.mydeepseek.ui.components.toast
import com.greenctl.mydeepseek.ui.theme.DsTheme
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext

private val SUGGESTIONS = listOf(
    "帮我写一份周报",
    "解释一下什么是量子纠缠",
    "总结这篇文章的要点",
    "帮我制定一个学习计划",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    controller: ChatController,
    onOpenSettings: () -> Unit,
    onReadAloud: (String) -> Unit,
    onAttachClick: () -> Unit,
) {
    val colors = DsTheme.colors
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val listState = rememberLazyListState()
    var showModelSheet by remember { mutableStateOf(false) }

    val messages = controller.messages

    LaunchedEffect(Unit) {
        controller.events.collect { context.toast(it) }
    }

    suspend fun jumpToLatest() {
        if (messages.isEmpty()) return
        runCatching {
            listState.scrollToItem(messages.lastIndex)
            listState.scrollBy(200_000f)
        }
    }

    LaunchedEffect(controller.currentSessionId, messages.size) {
        jumpToLatest()
    }

    LaunchedEffect(
        messages.lastOrNull()?.content?.length,
        messages.lastOrNull()?.reasoning?.length,
    ) {
        val info = listState.layoutInfo
        val lastVisible = info.visibleItemsInfo.lastOrNull()
        val distanceFromEnd = if (lastVisible == null) {
            Int.MAX_VALUE
        } else {
            lastVisible.offset + lastVisible.size - info.viewportEndOffset + info.afterContentPadding
        }
        if (distanceFromEnd <= 240) jumpToLatest()
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = colors.background,
                drawerContentColor = colors.textPrimary,
            ) {
                SessionDrawer(
                    sessions = controller.sessions,
                    currentSessionId = controller.currentSessionId,
                    onNewChat = {
                        controller.newChat()
                        scope.launch { drawerState.close() }
                    },
                    onOpenSession = { id ->
                        controller.openSession(id)
                        scope.launch { drawerState.close() }
                    },
                    onDeleteSession = { controller.deleteSession(it.id) },
                    onRenameSession = { target, title -> controller.renameSession(target.id, title) },
                    onTogglePin = { controller.togglePin(it.id) },
                    onOpenSettings = {
                        scope.launch { drawerState.close() }
                        onOpenSettings()
                    },
                )
            }
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background)
                .imePadding(),
        ) {
            ChatTopBar(
                title = if (controller.deepThink) "深度思考" else controller.model.label,
                onMenu = { scope.launch { drawerState.open() } },
                onTitleClick = { showModelSheet = true },
                onNewChat = { controller.newChat() },
            )

            Box(modifier = Modifier.weight(1f)) {
                if (messages.isEmpty()) {
                    EmptyState(
                        modelLabel = if (controller.deepThink) "深度思考" else controller.model.label,
                        onSuggestion = { controller.updateDraft(it) },
                    )
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 8.dp,
                            bottom = 16.dp,
                        ),
                    ) {
                        items(messages, key = { it.id }) { message ->
                            MessageItem(
                                message = message,
                                textScale = controller.settings.textScale,
                                thinkingAutoFold = controller.settings.thinkingAutoFold,
                                onRegenerate = { controller.regenerate(message.id) },
                                onFeedback = { controller.setFeedback(message.id, it) },
                                onReadAloud = { onReadAloud(message.content) },
                            )
                        }
                    }
                }
            }

            if (!controller.hasApiKey) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .background(colors.accentSoft, RoundedCornerShape(10.dp))
                        .clickableNoRipple(onClick = onOpenSettings)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "尚未配置 DeepSeek API Key，点击前往设置",
                        fontSize = 12.sp,
                        color = colors.accent,
                    )
                }
            }

            ChatInputBar(
                draft = controller.draft,
                onDraftChange = controller::updateDraft,
                deepThink = controller.deepThink,
                webSearch = controller.webSearch,
                isGenerating = controller.isGenerating,
                attachments = controller.attachments,
                textScale = controller.settings.textScale,
                onToggleDeepThink = controller::toggleDeepThink,
                onToggleWebSearch = controller::toggleWebSearch,
                onAttach = onAttachClick,
                onRemoveAttachment = controller::removeAttachment,
                onSend = controller::send,
                onStop = { controller.stopGeneration() },
            )
        }
    }

    if (showModelSheet) {
        ModalBottomSheet(
            onDismissRequest = { showModelSheet = false },
            sheetState = rememberModalBottomSheetState(),
            containerColor = colors.surface,
        ) {
            Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 28.dp)) {
                Text(
                    text = "选择模式",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary,
                    modifier = Modifier.padding(bottom = 12.dp),
                )
                DsModel.entries.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickableNoRipple {
                                controller.setModel(option)
                                showModelSheet = false
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = option.label,
                                fontSize = 15.sp,
                                color = colors.textPrimary,
                                fontWeight = FontWeight.Medium,
                            )
                            Text(
                                text = option.description,
                                fontSize = 12.sp,
                                color = colors.textSecondary,
                                modifier = Modifier.padding(top = 2.dp),
                            )
                        }
                        if (controller.model == option) {
                            DsIcon(DsIconType.Check, colors.accent, size = 18.dp, strokeWidth = 2.dp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatTopBar(
    title: String,
    onMenu: () -> Unit,
    onTitleClick: () -> Unit,
    onNewChat: () -> Unit,
) {
    val colors = DsTheme.colors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .background(colors.background),
    ) {
        Box(modifier = Modifier.align(Alignment.CenterStart).padding(start = 4.dp)) {
            DsIconButton(DsIconType.Menu, onClick = onMenu, tint = colors.textPrimary)
        }
        Row(
            modifier = Modifier
                .align(Alignment.Center)
                .clickableNoRipple(onClick = onTitleClick)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = colors.textPrimary,
            )
            Spacer(Modifier.width(2.dp))
            DsIcon(DsIconType.ChevronDown, colors.textSecondary, size = 15.dp, strokeWidth = 1.8.dp)
        }
        Box(modifier = Modifier.align(Alignment.CenterEnd).padding(end = 4.dp)) {
            DsIconButton(DsIconType.Plus, onClick = onNewChat, tint = colors.textPrimary)
        }
    }
}

@Composable
private fun EmptyState(
    modelLabel: String,
    onSuggestion: (String) -> Unit,
) {
    val colors = DsTheme.colors
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.weight(1f))
        Text(
            text = "今天有什么可以帮到你？",
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.textPrimary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "使用${modelLabel}开始对话",
            fontSize = 13.sp,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(28.dp))
        SUGGESTIONS.chunked(2).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                row.forEach { suggestion ->
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .background(colors.elevated, RoundedCornerShape(12.dp))
                            .clickableNoRipple { onSuggestion(suggestion) }
                            .padding(horizontal = 12.dp, vertical = 12.dp),
                    ) {
                        Text(
                            text = suggestion,
                            fontSize = 13.sp,
                            color = colors.textPrimary,
                            maxLines = 2,
                        )
                    }
                }
                if (row.size == 1) {
                    Box(modifier = Modifier.weight(1f))
                }
            }
            Spacer(Modifier.height(8.dp))
        }
        Spacer(Modifier.weight(1.3f))
    }
}
