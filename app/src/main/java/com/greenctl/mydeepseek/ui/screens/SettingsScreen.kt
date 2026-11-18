package com.greenctl.mydeepseek.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.greenctl.mydeepseek.data.DsModel
import com.greenctl.mydeepseek.ui.ChatController
import com.greenctl.mydeepseek.ui.components.DsIcon
import com.greenctl.mydeepseek.ui.components.DsIconButton
import com.greenctl.mydeepseek.ui.components.DsIconType
import com.greenctl.mydeepseek.ui.components.clickableNoRipple
import com.greenctl.mydeepseek.ui.theme.DsTheme

@Composable
fun SettingsScreen(
    controller: ChatController,
    onBack: () -> Unit,
) {
    val colors = DsTheme.colors
    val settings = controller.settings
    var showDeleteDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
        ) {
            Box(modifier = Modifier.align(Alignment.CenterStart).padding(start = 4.dp)) {
                DsIconButton(DsIconType.Back, onClick = onBack, tint = colors.textPrimary)
            }
            Text(
                text = "设置",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = colors.textPrimary,
                modifier = Modifier.align(Alignment.Center),
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 4.dp,
                bottom = 32.dp,
            ),
        ) {
            item { SectionTitle("应用") }
            item {
                SettingCard {
                    SettingRow(title = "外观", subtitle = "跟随系统或手动指定深浅色") {
                        SegmentedChoice(
                            options = listOf("系统", "浅色", "深色"),
                            selected = settings.appearance,
                            onSelect = controller::setAppearance,
                        )
                    }
                    Divider(color = colors.divider)
                    SettingRow(title = "字体大小", subtitle = "影响消息正文的显示大小") {
                        SegmentedChoice(
                            options = listOf("小", "标准", "大"),
                            selected = settings.fontScale,
                            onSelect = controller::setFontScale,
                        )
                    }
                    Divider(color = colors.divider)
                    SettingRow(title = "自动朗读", subtitle = "回答完成后自动播放语音") {
                        Switch(
                            checked = settings.autoReadAloud,
                            onCheckedChange = controller::setAutoReadAloud,
                        )
                    }
                    Divider(color = colors.divider)
                    SettingRow(title = "思考内容自动折叠", subtitle = "思考结束后自动收起思考过程") {
                        Switch(
                            checked = settings.thinkingAutoFold,
                            onCheckedChange = controller::setThinkingAutoFold,
                        )
                    }
                }
            }

            item { SectionTitle("回答设置") }
            item {
                SettingCard {
                    DsModel.entries.forEachIndexed { index, option ->
                        if (index > 0) Divider(color = colors.divider)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickableNoRipple { controller.setModel(option) }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = option.label,
                                    fontSize = 14.sp,
                                    color = colors.textPrimary,
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

            item { SectionTitle("API 配置") }
            item {
                SettingCard {
                    Column(modifier = Modifier.padding(vertical = 12.dp)) {
                        Text(text = "API Key", fontSize = 14.sp, color = colors.textPrimary)
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = settings.apiKey,
                            onValueChange = controller::setApiKey,
                            singleLine = true,
                            placeholder = { Text("sk-...", fontSize = 13.sp) },
                            visualTransformation = PasswordVisualTransformation(),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Spacer(Modifier.height(14.dp))
                        Text(text = "接口地址", fontSize = 14.sp, color = colors.textPrimary)
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = settings.baseUrl,
                            onValueChange = controller::setBaseUrl,
                            singleLine = true,
                            placeholder = { Text("https://api.deepseek.com", fontSize = 13.sp) },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Text(
                            text = "在 platform.deepseek.com 创建 API Key。快速模式调用 deepseek-chat，" +
                                "专家模式与深度思考调用 deepseek-reasoner。",
                            fontSize = 12.sp,
                            color = colors.textSecondary,
                            modifier = Modifier.padding(top = 10.dp),
                        )
                    }
                }
            }

            item { SectionTitle("数据管理") }
            item {
                SettingCard {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickableNoRipple { showDeleteDialog = true }
                            .padding(vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        DsIcon(DsIconType.Trash, colors.danger, size = 17.dp, strokeWidth = 1.6.dp)
                        Spacer(Modifier.width(8.dp))
                        Text(text = "删除所有对话", fontSize = 14.sp, color = colors.danger)
                    }
                }
            }

            item { SectionTitle("关于") }
            item {
                SettingCard {
                    Column(modifier = Modifier.padding(vertical = 12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "DeepSeek 客户端复刻版",
                                fontSize = 14.sp,
                                color = colors.textPrimary,
                                modifier = Modifier.weight(1f),
                            )
                            Text(text = "v1.0.0", fontSize = 12.sp, color = colors.textSecondary)
                        }
                        Text(
                            text = "界面与交互参考 DeepSeek 官方 Android 客户端，模型能力由 DeepSeek 开放平台提供。" +
                                "输出内容由 AI 生成，医疗、法律、金融等专业领域的内容不构成任何建议，请注意甄别。",
                            fontSize = 12.sp,
                            color = colors.textSecondary,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("删除所有对话") },
            text = { Text("删除后，所有对话将都被清空且无法找回") },
            confirmButton = {
                TextButton(onClick = {
                    controller.deleteAllSessions()
                    showDeleteDialog = false
                }) { Text("删除", color = colors.danger) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("取消") }
            },
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    val colors = DsTheme.colors
    Text(
        text = text,
        fontSize = 12.sp,
        color = colors.textTertiary,
        modifier = Modifier.padding(start = 4.dp, top = 18.dp, bottom = 8.dp),
    )
}

@Composable
private fun SettingCard(content: @Composable () -> Unit) {
    val colors = DsTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(colors.elevated)
            .padding(horizontal = 14.dp),
        content = { content() },
    )
}

@Composable
private fun SettingRow(
    title: String,
    subtitle: String? = null,
    trailing: @Composable () -> Unit,
) {
    val colors = DsTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 14.sp, color = colors.textPrimary)
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = colors.textSecondary,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
        Spacer(Modifier.width(8.dp))
        trailing()
    }
}

@Composable
private fun SegmentedChoice(
    options: List<String>,
    selected: Int,
    onSelect: (Int) -> Unit,
) {
    val colors = DsTheme.colors
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(9.dp))
            .background(colors.surface)
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        options.forEachIndexed { index, label ->
            val active = index == selected
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(7.dp))
                    .background(if (active) colors.accentSoft else Color.Transparent)
                    .clickableNoRipple { onSelect(index) }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
            ) {
                Text(
                    text = label,
                    fontSize = 12.sp,
                    color = if (active) colors.accent else colors.textSecondary,
                )
            }
        }
    }
}
