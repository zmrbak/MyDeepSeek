package com.greenctl.mydeepseek.ui

import android.content.Context
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.greenctl.mydeepseek.data.ApiTurn
import com.greenctl.mydeepseek.data.AppSettings
import com.greenctl.mydeepseek.data.Attachment
import com.greenctl.mydeepseek.data.ChatMessage
import com.greenctl.mydeepseek.data.ChatRepository
import com.greenctl.mydeepseek.data.ChatSession
import com.greenctl.mydeepseek.data.DeepSeekApi
import com.greenctl.mydeepseek.data.DsModel
import com.greenctl.mydeepseek.data.MessageState
import com.greenctl.mydeepseek.data.Role
import com.greenctl.mydeepseek.data.SearchState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * 会话状态与流式生成的持有者。所有改动都会写入本地存储。
 */
class ChatController(context: Context) {

    private val repository = ChatRepository(context)
    private val api = DeepSeekApi()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    @Immutable
    data class UiEvent(val id: Long = System.nanoTime(), val text: String)

    var settings by mutableStateOf(repository.loadSettings())
        private set
    var sessions by mutableStateOf(repository.loadSessions())
        private set
    var currentSessionId by mutableStateOf<String?>(null)
        private set
    var isGenerating by mutableStateOf(false)
        private set
    var draft by mutableStateOf("")
        private set
    var attachments by mutableStateOf<List<Attachment>>(emptyList())
        private set

    val events = MutableSharedFlow<String>(extraBufferCapacity = 16)

    private var streamJob: Job? = null

    val model: DsModel get() = DsModel.of(settings.modelName)
    val deepThink: Boolean get() = settings.deepThink
    val webSearch: Boolean get() = settings.webSearch
    val currentSession: ChatSession? get() = sessions.firstOrNull { it.id == currentSessionId }
    val messages: List<ChatMessage> get() = currentSession?.messages.orEmpty()
    val hasApiKey: Boolean get() = settings.apiKey.isNotBlank()

    init {
        val saved = settings.lastSessionId?.takeIf { id -> sessions.any { it.id == id } }
        currentSessionId = saved ?: sessions.maxByOrNull { it.updatedAt }?.id
    }

    // ---------- 会话操作 ----------

    fun newChat() {
        stopGeneration(silent = true)
        draft = ""
        attachments = emptyList()
        currentSessionId = null
        updateSettings(settings.copy(lastSessionId = null))
    }

    fun openSession(id: String) {
        if (id == currentSessionId) return
        stopGeneration(silent = true)
        draft = ""
        attachments = emptyList()
        currentSessionId = id
        updateSettings(settings.copy(lastSessionId = id))
    }

    fun deleteSession(id: String) {
        sessions = sessions.filterNot { it.id == id }
        if (currentSessionId == id) currentSessionId = null
        persistSessions()
    }

    fun renameSession(id: String, title: String) {
        val trimmed = title.trim()
        if (trimmed.isEmpty()) {
            emit("文件名不能为空")
            return
        }
        sessions = sessions.map { if (it.id == id) it.copy(title = trimmed) else it }
        persistSessions()
    }

    fun togglePin(id: String) {
        sessions = sessions.map { if (it.id == id) it.copy(pinned = !it.pinned) else it }
        persistSessions()
    }

    fun deleteAllSessions() {
        stopGeneration(silent = true)
        sessions = emptyList()
        currentSessionId = null
        repository.clearSessions()
        updateSettings(settings.copy(lastSessionId = null))
    }

    // ---------- 输入区 ----------

    fun updateDraft(text: String) {
        draft = text
    }

    fun addAttachment(attachment: Attachment) {
        attachments = attachments + attachment
        if (settings.webSearch) {
            emit("请先删除文件再开启联网搜索")
            setWebSearch(false)
        }
    }

    fun removeAttachment(index: Int) {
        attachments = attachments.filterIndexed { i, _ -> i != index }
    }

    fun toggleDeepThink() {
        val next = !settings.deepThink
        updateSettings(settings.copy(deepThink = next))
        emit(if (next) "已开启深度思考" else "已关闭深度思考")
    }

    fun setWebSearch(enabled: Boolean) {
        updateSettings(settings.copy(webSearch = enabled))
    }

    fun toggleWebSearch() {
        if (!settings.webSearch && attachments.isNotEmpty()) {
            emit("请先删除文件再开启联网搜索")
            return
        }
        val next = !settings.webSearch
        setWebSearch(next)
        if (next) emit("联网搜索暂不可用，将在回答中提示")
    }

    fun setModel(target: DsModel) {
        if (target == model) return
        updateSettings(settings.copy(modelName = target.name))
        if (messages.isNotEmpty()) {
            newChat()
        }
        emit("使用${target.label}开始对话")
    }

    // ---------- 生成 ----------

    fun send() {
        if (isGenerating) return
        val text = draft.trim()
        if (text.isEmpty()) {
            emit("不能发送空白消息")
            return
        }
        if (!hasApiKey) {
            emit(DeepSeekApi.ERR_NO_KEY)
            return
        }

        val session = ensureSession(text)
        val extraContext = attachments.joinToString("\n\n") { "【${it.name}】\n${it.text}" }
        val userMessage = ChatMessage(role = Role.USER, content = text)
        val assistantMessage = ChatMessage(
            role = Role.ASSISTANT,
            state = MessageState.STREAMING,
            search = if (settings.webSearch) SearchState.SEARCHING else SearchState.NONE,
        )
        appendMessages(session.id, listOf(userMessage, assistantMessage))
        draft = ""
        attachments = emptyList()
        startGeneration(session.id, assistantMessage.id, extraContext)
    }

    fun stopGeneration(silent: Boolean = false) {
        val job = streamJob
        streamJob = null
        api.cancel()
        job?.cancel()
        isGenerating = false
        val session = currentSession ?: return
        val streaming = session.messages.lastOrNull()?.takeIf { it.state == MessageState.STREAMING }
        if (streaming != null) {
            updateMessage(session.id, streaming.id) { it.copy(state = MessageState.STOPPED) }
            if (!silent) emit("已停止生成")
        }
    }

    fun regenerate(messageId: String) {
        if (isGenerating) return
        val session = currentSession ?: return
        val index = session.messages.indexOfFirst { it.id == messageId }
        if (index < 0) return
        val kept = session.messages.take(index)
        val placeholder = ChatMessage(role = Role.ASSISTANT, state = MessageState.STREAMING)
        replaceMessages(session.id, kept + placeholder)
        startGeneration(session.id, placeholder.id, "")
    }

    fun retry(messageId: String) = regenerate(messageId)

    fun setFeedback(messageId: String, value: Int) {
        val session = currentSession ?: return
        updateMessage(session.id, messageId) { it.copy(feedback = if (it.feedback == value) 0 else value) }
    }

    // ---------- 设置 ----------

    fun updateSettings(next: AppSettings) {
        settings = next
        repository.saveSettings(next)
    }

    fun setApiKey(key: String) = updateSettings(settings.copy(apiKey = key.trim()))
    fun setBaseUrl(url: String) = updateSettings(settings.copy(baseUrl = url.trim()))
    fun setAppearance(mode: Int) = updateSettings(settings.copy(appearance = mode))
    fun setFontScale(scale: Int) = updateSettings(settings.copy(fontScale = scale))
    fun setAutoReadAloud(enabled: Boolean) = updateSettings(settings.copy(autoReadAloud = enabled))
    fun setThinkingAutoFold(enabled: Boolean) = updateSettings(settings.copy(thinkingAutoFold = enabled))

    // ---------- 内部实现 ----------

    private fun ensureSession(firstText: String): ChatSession {
        currentSessionId?.let { id -> sessions.firstOrNull { it.id == id }?.let { return it } }
        val session = ChatSession(
            id = UUID.randomUUID().toString(),
            title = titleFor(firstText),
            messages = emptyList(),
        )
        sessions = listOf(session) + sessions
        currentSessionId = session.id
        updateSettings(settings.copy(lastSessionId = session.id))
        persistSessions()
        return session
    }

    private fun titleFor(text: String): String {
        val singleLine = text.lineSequence().firstOrNull()?.trim().orEmpty().ifBlank { "新对话" }
        return if (singleLine.length > 24) singleLine.take(24) + "…" else singleLine
    }

    private fun appendMessages(sessionId: String, newMessages: List<ChatMessage>) {
        sessions = sessions.map {
            if (it.id == sessionId) {
                it.copy(messages = it.messages + newMessages, updatedAt = System.currentTimeMillis())
            } else {
                it
            }
        }
        persistSessions()
    }

    private fun replaceMessages(sessionId: String, messages: List<ChatMessage>) {
        sessions = sessions.map {
            if (it.id == sessionId) it.copy(messages = messages, updatedAt = System.currentTimeMillis()) else it
        }
        persistSessions()
    }

    private fun updateMessage(sessionId: String, messageId: String, transform: (ChatMessage) -> ChatMessage) {
        sessions = sessions.map { session ->
            if (session.id != sessionId) {
                session
            } else {
                session.copy(
                    messages = session.messages.map { if (it.id == messageId) transform(it) else it },
                    updatedAt = System.currentTimeMillis(),
                )
            }
        }
    }

    private fun startGeneration(sessionId: String, assistantId: String, extraContext: String) {
        isGenerating = true
        val turns = buildTurns(sessionId, assistantId, extraContext)
        val apiModel = if (model == DsModel.EXPERT || settings.deepThink) {
            DsModel.EXPERT.apiModel
        } else {
            DsModel.FAST.apiModel
        }

        streamJob = scope.launch {
            val reasoning = StringBuilder()
            val content = StringBuilder()
            val startedAt = System.currentTimeMillis()
            var firstContentAt = 0L
            var lastPublished = 0L

            fun publish(state: MessageState = MessageState.STREAMING) {
                val think = if (firstContentAt > 0L) firstContentAt - startedAt else 0L
                updateMessage(sessionId, assistantId) {
                    it.copy(
                        content = content.toString(),
                        reasoning = reasoning.toString(),
                        thinkMillis = if (think > 0L) think else it.thinkMillis,
                        state = state,
                    )
                }
            }

            try {
                if (settings.webSearch) {
                    delay(700)
                    updateMessage(sessionId, assistantId) { it.copy(search = SearchState.UNAVAILABLE) }
                }
                api.streamChat(
                    baseUrl = settings.baseUrl,
                    apiKey = settings.apiKey,
                    model = apiModel,
                    turns = turns,
                    onDelta = { deltaContent, deltaReasoning ->
                        if (deltaReasoning.isNotEmpty()) reasoning.append(deltaReasoning)
                        if (deltaContent.isNotEmpty()) {
                            if (firstContentAt == 0L) firstContentAt = System.currentTimeMillis()
                            content.append(deltaContent)
                        }
                        val now = System.currentTimeMillis()
                        if (now - lastPublished >= 60L || content.isEmpty()) {
                            lastPublished = now
                            publish()
                        }
                    },
                )
                publish(MessageState.DONE)
            } catch (e: CancellationException) {
                publish(MessageState.STOPPED)
            } catch (e: Exception) {
                if (content.isEmpty() && reasoning.isEmpty()) {
                    updateMessage(sessionId, assistantId) {
                        it.copy(state = MessageState.ERROR, error = e.message ?: DeepSeekApi.ERR_SERVER)
                    }
                } else {
                    publish(MessageState.STOPPED)
                }
            } finally {
                isGenerating = false
                streamJob = null
                persistSessions()
            }
        }
    }

    private fun buildTurns(sessionId: String, assistantId: String, extraContext: String): List<ApiTurn> {
        val session = sessions.firstOrNull { it.id == sessionId } ?: return emptyList()
        val turns = session.messages
            .filter { it.id != assistantId }
            .filter { it.state != MessageState.ERROR && it.content.isNotBlank() }
            .map { ApiTurn(role = if (it.role == Role.USER) "user" else "assistant", content = it.content) }
            .toMutableList()
        if (extraContext.isNotBlank() && turns.isNotEmpty()) {
            val last = turns.removeAt(turns.lastIndex)
            turns.add(ApiTurn(last.role, last.content + "\n\n" + extraContext))
        }
        return turns
    }

    private fun persistSessions() {
        repository.saveSessions(sessions)
    }

    private fun emit(text: String) {
        events.tryEmit(text)
    }
}
