package com.greenctl.mydeepseek.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/**
 * 基于 SharedPreferences 的本地存储：会话列表与应用设置。
 */
class ChatRepository(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun loadSettings(): AppSettings {
        val raw = prefs.getString(KEY_SETTINGS, null) ?: return AppSettings()
        return runCatching { settingsFromJson(JSONObject(raw)) }.getOrDefault(AppSettings())
    }

    fun saveSettings(settings: AppSettings) {
        prefs.edit().putString(KEY_SETTINGS, settings.toJson().toString()).apply()
    }

    fun loadSessions(): List<ChatSession> {
        val raw = prefs.getString(KEY_SESSIONS, null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (i in 0 until array.length()) {
                    array.optJSONObject(i)?.let { add(sessionFromJson(it)) }
                }
            }
        }.getOrDefault(emptyList())
    }

    fun saveSessions(sessions: List<ChatSession>) {
        val array = JSONArray()
        sessions.forEach { array.put(it.toJson()) }
        prefs.edit().putString(KEY_SESSIONS, array.toString()).apply()
    }

    fun clearSessions() {
        prefs.edit().remove(KEY_SESSIONS).apply()
    }

    private companion object {
        const val PREFS_NAME = "deepseek_chat_store"
        const val KEY_SESSIONS = "sessions"
        const val KEY_SETTINGS = "settings"
    }
}
