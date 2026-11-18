package com.greenctl.mydeepseek.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.greenctl.mydeepseek.R
import com.greenctl.mydeepseek.data.DsModel
import com.greenctl.mydeepseek.ui.ChatController
import com.greenctl.mydeepseek.ui.components.ChatInputBar
import com.greenctl.mydeepseek.ui.components.DsIcon
import com.greenctl.mydeepseek.ui.components.DsIconType
import com.greenctl.mydeepseek.ui.components.DsMenuIcon
import com.greenctl.mydeepseek.ui.components.MessageItem
import com.greenctl.mydeepseek.ui.components.SessionDrawer
import com.greenctl.mydeepseek.ui.components.clickableNoRipple
import com.greenctl.mydeepseek.ui.components.toast
import com.greenctl.mydeepseek.ui.theme.DsTheme
import kotlinx.coroutines.launch
import java.util.Calendar

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
    val sessionTitle = controller.sessions
        .firstOrNull { it.id == controller.currentSessionId }
        ?.title
        .orEmpty()

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
                    modelLabel = controller.model.label,
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
                    onOpenModelSheet = {
                        scope.launch { drawerState.close() }
                        showModelSheet = true
                    },
                    onOpenSettings = {
                        scope.launch { drawerState.close() }
                        onOpenSettings()
                    },
                )
            }
        },
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background)
                .imePadding(),
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                ChatTopBar(
                    title = sessionTitle,
                    onMenu = { scope.launch { drawerState.open() } },
                    onNewChat = { controller.newChat() },
                )

                if (messages.isEmpty()) {
                    EmptyState(modifier = Modifier.weight(1f))
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 4.dp,
                            bottom = 168.dp,
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

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp)
                    .padding(bottom = 8.dp),
            ) {
                if (!controller.hasApiKey) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp)
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
                            DsIcon(DsIconType.Check, colors.accent, size = 18.dp)
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
    onNewChat: () -> Unit,
) {
    val colors = DsTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .clickableNoRipple(onClick = onMenu),
            contentAlignment = Alignment.Center,
        ) {
            DsMenuIcon(tint = colors.textPrimary, size = 19.dp)
        }

        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.textPrimary,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 6.dp),
        )

        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .clickableNoRipple(onClick = onNewChat),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.AddCircleOutline,
                contentDescription = null,
                tint = colors.textPrimary,
                modifier = Modifier.size(23.dp),
            )
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    val colors = DsTheme.colors
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.weight(1f))
        Icon(
            painter = painterResource(R.drawable.ic_deepseek_logo),
            contentDescription = null,
            tint = colors.accent,
            modifier = Modifier.size(42.dp),
        )
        Spacer(Modifier.height(18.dp))
        Text(
            text = greetingText(),
            fontSize = 17.sp,
            fontWeight = FontWeight.Medium,
            color = colors.textPrimary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.weight(1.35f))
    }
}

private fun greetingText(hour: Int = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)): String {
    val prefix = when {
        hour < 9 -> "早上好"
        hour < 12 -> "上午好"
        hour < 14 -> "中午好"
        hour < 18 -> "下午好"
        else -> "晚上好"
    }
    return "$prefix，有什么可以帮到你？"
}
