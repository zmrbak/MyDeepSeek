package com.greenctl.mydeepseek.data

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class SessionGroup(
    val label: String,
    val sessions: List<ChatSession>,
)

/**
 * 侧边栏会话分组：置顶 / 今天 / 昨天 / 7 天内 / 30 天内 / 更早。
 */
object SessionGrouping {

    const val LABEL_PINNED = "置顶"
    const val LABEL_TODAY = "今天"
    const val LABEL_YESTERDAY = "昨天"
    const val LABEL_LAST_7_DAYS = "7 天内"
    const val LABEL_LAST_30_DAYS = "30 天内"
    const val LABEL_EARLIER = "更早"

    fun group(
        sessions: List<ChatSession>,
        now: Long = System.currentTimeMillis(),
        zone: ZoneId = ZoneId.systemDefault(),
    ): List<SessionGroup> {
        val today = Instant.ofEpochMilli(now).atZone(zone).toLocalDate()
        val pinned = sessions.filter { it.pinned }.sortedByDescending { it.updatedAt }
        val buckets = LinkedHashMap<String, MutableList<ChatSession>>()

        sessions.filterNot { it.pinned }
            .sortedByDescending { it.updatedAt }
            .forEach { session ->
                val day = Instant.ofEpochMilli(session.updatedAt).atZone(zone).toLocalDate()
                val label = labelFor(today, day)
                buckets.getOrPut(label) { mutableListOf() }.add(session)
            }

        val result = mutableListOf<SessionGroup>()
        if (pinned.isNotEmpty()) result.add(SessionGroup(LABEL_PINNED, pinned))
        for (label in listOf(
            LABEL_TODAY,
            LABEL_YESTERDAY,
            LABEL_LAST_7_DAYS,
            LABEL_LAST_30_DAYS,
            LABEL_EARLIER,
        )) {
            buckets[label]?.let { result.add(SessionGroup(label, it)) }
        }
        return result
    }

    private fun labelFor(today: LocalDate, day: LocalDate): String {
        val diff = java.time.temporal.ChronoUnit.DAYS.between(day, today)
        return when {
            diff <= 0L -> LABEL_TODAY
            diff == 1L -> LABEL_YESTERDAY
            diff < 7L -> LABEL_LAST_7_DAYS
            diff < 30L -> LABEL_LAST_30_DAYS
            else -> LABEL_EARLIER
        }
    }
}
