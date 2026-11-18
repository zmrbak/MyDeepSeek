package com.greenctl.mydeepseek

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import com.greenctl.mydeepseek.data.Attachment
import com.greenctl.mydeepseek.data.MessageState
import com.greenctl.mydeepseek.data.Role
import com.greenctl.mydeepseek.ui.ChatController
import com.greenctl.mydeepseek.ui.components.toast
import com.greenctl.mydeepseek.ui.screens.ChatScreen
import com.greenctl.mydeepseek.ui.screens.SettingsScreen
import androidx.core.view.WindowCompat
import com.greenctl.mydeepseek.ui.theme.DsTheme
import com.greenctl.mydeepseek.ui.theme.MyDeepSeekTheme
import com.greenctl.mydeepseek.ui.theme.isDarkThemeEnabled
import java.util.Locale

class MainActivity : ComponentActivity() {

    private var tts: TextToSpeech? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.CHINA
            }
        }
        setContent {
            val controller = remember { ChatController(applicationContext) }
            var showSettings by remember { mutableStateOf(false) }
            val darkTheme = isDarkThemeEnabled(controller.settings.appearance)
            val view = LocalView.current

            SideEffect {
                val window = (view.context as? Activity)?.window ?: return@SideEffect
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = !darkTheme
                    isAppearanceLightNavigationBars = !darkTheme
                }
            }

            MyDeepSeekTheme(appearance = controller.settings.appearance) {
                Surface(color = DsTheme.colors.background) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(DsTheme.colors.background)
                            .statusBarsPadding()
                            .navigationBarsPadding()
                            .imePadding(),
                    ) {
                        AppContent(
                            controller = controller,
                            showSettings = showSettings,
                            onOpenSettings = { showSettings = true },
                            onCloseSettings = { showSettings = false },
                            onSpeak = ::speak,
                        )
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        super.onDestroy()
    }

    private fun speak(text: String) {
        val engine = tts ?: return
        if (text.isBlank()) return
        engine.stop()
        engine.speak(text.take(4000), TextToSpeech.QUEUE_FLUSH, null, "deepseek-reply")
    }
}

@Composable
private fun AppContent(
    controller: ChatController,
    showSettings: Boolean,
    onOpenSettings: () -> Unit,
    onCloseSettings: () -> Unit,
    onSpeak: (String) -> Unit,
) {
    val context = LocalContext.current
    var lastSpokenId by remember { mutableStateOf<String?>(null) }

    val pickFile = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        val result = runCatching {
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readNBytes(200_000) }
                ?: ByteArray(0)
            bytes.decodeToString()
        }
        result.onSuccess { text ->
            if (text.isBlank()) {
                context.toast("未提取到文字")
            } else {
                controller.addAttachment(Attachment(name = queryFileName(context, uri), text = text))
            }
        }.onFailure {
            context.toast("该格式暂不支持")
        }
    }

    val lastMessage = controller.messages.lastOrNull()
    LaunchedEffect(lastMessage?.id, lastMessage?.state) {
        if (
            lastMessage != null &&
            lastMessage.role == Role.ASSISTANT &&
            lastMessage.state == MessageState.DONE &&
            lastMessage.content.isNotBlank() &&
            controller.settings.autoReadAloud &&
            lastMessage.id != lastSpokenId
        ) {
            lastSpokenId = lastMessage.id
            onSpeak(lastMessage.content)
        }
    }

    BackHandler(enabled = showSettings, onBack = onCloseSettings)

    if (showSettings) {
        SettingsScreen(controller = controller, onBack = onCloseSettings)
    } else {
        ChatScreen(
            controller = controller,
            onOpenSettings = onOpenSettings,
            onReadAloud = onSpeak,
            onAttachClick = { pickFile.launch("text/*") },
        )
    }
}

private fun queryFileName(context: android.content.Context, uri: Uri): String {
    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (index >= 0 && cursor.moveToFirst()) {
            return cursor.getString(index) ?: "文件"
        }
    }
    return uri.lastPathSegment ?: "文件"
}
