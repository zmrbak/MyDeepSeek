package com.greenctl.mydeepseek.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.greenctl.mydeepseek.data.ChatSession
import com.greenctl.mydeepseek.data.SessionGrouping
import com.greenctl.mydeepseek.ui.theme.DsTheme

@Composable
fun SessionDrawer(
    sessions: List<ChatSession>,
    currentSessionId: String?,
    onNewChat: () -> Unit,
    onOpenSession: (String) -> Unit,
    onDeleteSession: (ChatSession) -> Unit,
    onRenameSession: (ChatSession, String) -> Unit,
    onTogglePin: (ChatSession) -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = DsTheme.colors
    var query by remember { mutableStateOf("") }
    var menuSessionId by remember { mutableStateOf<String?>(null) }
    var renameTarget by remember { mutableStateOf<ChatSession?>(null) }
    var deleteTarget by remember { mutableStateOf<ChatSession?>(null) }

    val filtered = remember(sessions, query) {
        if (query.isBlank()) {
            sessions
        } else {
            sessions.filter {
                it.title.contains(query, ignoreCase = true) ||
                    it.preview.contains(query, ignoreCase = true)
            }
        }
    }
    val groups = remember(filtered) { SessionGrouping.group(filtered) }

    Column(modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Spacer(Modifier.height(20.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "DeepSeek",
                fontSize = 21.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                color = colors.textPrimary,
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = "免费 · 官方",
                fontSize = 11.sp,
                color = colors.textTertiary,
            )
        }
        Spacer(Modifier.height(16.dp))

        Surface(
            color = Color.Transparent,
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, colors.divider),
            modifier = Modifier
                .fillMaxWidth()
                .clickableNoRipple(onClick = onNewChat),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 11.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                DsIcon(DsIconType.Plus, colors.accent, size = 17.dp, strokeWidth = 1.9.dp)
                Spacer(Modifier.width(8.dp))
                Text(text = "开启新对话", fontSize = 15.sp, color = colors.textPrimary)
            }
        }
        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(colors.elevated)
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DsIcon(DsIconType.Search, colors.textTertiary, size = 15.dp, strokeWidth = 1.6.dp)
            Spacer(Modifier.width(6.dp))
            Box(modifier = Modifier.weight(1f)) {
                if (query.isEmpty()) {
                    Text(text = "搜索对话内容...", fontSize = 13.sp, color = colors.textTertiary)
                }
                BasicTextField(
                    value = query,
                    onValueChange = { query = it },
                    singleLine = true,
                    textStyle = TextStyle(fontSize = 13.sp, color = colors.textPrimary),
                    cursorBrush = SolidColor(colors.accent),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
        Spacer(Modifier.height(12.dp))

        if (groups.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "暂无对话", fontSize = 13.sp, color = colors.textTertiary)
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                groups.forEach { group ->
                    item(key = "header-${group.label}") {
                        Text(
                            text = group.label,
                            fontSize = 12.sp,
                            color = colors.textTertiary,
                            modifier = Modifier.padding(start = 4.dp, top = 10.dp, bottom = 4.dp),
                        )
                    }
                    items(group.sessions, key = { it.id }) { session ->
                        SessionRow(
                            session = session,
                            selected = session.id == currentSessionId,
                            menuExpanded = menuSessionId == session.id,
                            onOpen = { onOpenSession(session.id) },
                            onMenuClick = {
                                menuSessionId = if (menuSessionId == session.id) null else session.id
                            },
                            onDismissMenu = { menuSessionId = null },
                            onPin = {
                                menuSessionId = null
                                onTogglePin(session)
                            },
                            onRename = {
                                menuSessionId = null
                                renameTarget = session
                            },
                            onDelete = {
                                menuSessionId = null
                                deleteTarget = session
                            },
                        )
                    }
                }
            }
        }

        Divider(color = colors.divider)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickableNoRipple(onClick = onOpenSettings)
                .padding(vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DsIcon(DsIconType.Gear, colors.textSecondary, size = 18.dp, strokeWidth = 1.6.dp)
            Spacer(Modifier.width(8.dp))
            Text(text = "设置", fontSize = 14.sp, color = colors.textPrimary)
            Spacer(Modifier.weight(1f))
            Text(text = "v1.0.0", fontSize = 12.sp, color = colors.textTertiary)
        }
    }

    renameTarget?.let { target ->
        var text by remember(target.id) { mutableStateOf(target.title) }
        AlertDialog(
            onDismissRequest = { renameTarget = null },
            title = { Text("重命名对话") },
            text = {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onRenameSession(target, text)
                    renameTarget = null
                }) { Text("确认") }
            },
            dismissButton = {
                TextButton(onClick = { renameTarget = null }) { Text("取消") }
            },
        )
    }

    deleteTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("删除该对话") },
            text = { Text("删除后，该对话将不可恢复") },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteSession(target)
                    deleteTarget = null
                }) { Text("删除", color = colors.danger) }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) { Text("取消") }
            },
        )
    }
}

@Composable
private fun SessionRow(
    session: ChatSession,
    selected: Boolean,
    menuExpanded: Boolean,
    onOpen: () -> Unit,
    onMenuClick: () -> Unit,
    onDismissMenu: () -> Unit,
    onPin: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
) {
    val colors = DsTheme.colors
    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(if (selected) colors.elevated else Color.Transparent)
                .clickableNoRipple(onClick = onOpen)
                .padding(start = 10.dp, end = 2.dp, top = 9.dp, bottom = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
        ) {
            Text(
                text = session.title,
                fontSize = 14.sp,
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clickableNoRipple(onClick = onMenuClick),
                contentAlignment = Alignment.Center,
            ) {
                DsIcon(DsIconType.More, colors.textTertiary, size = 14.dp, strokeWidth = 1.6.dp)
            }
        }
        DropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = onDismissMenu,
        ) {
            DropdownMenuItem(
                text = { Text(if (session.pinned) "取消置顶" else "置顶") },
                leadingIcon = { DsIcon(DsIconType.Pin, colors.textSecondary, size = 16.dp, strokeWidth = 1.6.dp) },
                onClick = onPin,
            )
            DropdownMenuItem(
                text = { Text("重命名") },
                leadingIcon = { DsIcon(DsIconType.Edit, colors.textSecondary, size = 16.dp, strokeWidth = 1.6.dp) },
                onClick = onRename,
            )
            DropdownMenuItem(
                text = { Text("删除", color = colors.danger) },
                leadingIcon = { DsIcon(DsIconType.Trash, colors.danger, size = 16.dp, strokeWidth = 1.6.dp) },
                onClick = onDelete,
            )
        }
    }
}
