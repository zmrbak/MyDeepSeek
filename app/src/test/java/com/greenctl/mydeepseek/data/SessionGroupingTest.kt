package com.greenctl.mydeepseek.data

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

class SessionGroupingTest {

    private val zone: ZoneId = ZoneOffset.UTC
    private val today: LocalDate = LocalDate.of(2026, 3, 20)
    private val now: Long = today.atStartOfDay(zone).toInstant().toEpochMilli() + 12 * 3600_000

    private fun session(id: String, daysAgo: Long, pinned: Boolean = false): ChatSession {
        val time = today.minusDays(daysAgo).atStartOfDay(zone).toInstant().toEpochMilli() + 3600_000
        return ChatSession(
            id = id,
            title = id,
            createdAt = time,
            updatedAt = time,
            pinned = pinned,
        )
    }

    @Test
    fun groupsSessionsIntoFriendlyBuckets() {
        val sessions = listOf(
            session("today", 0),
            session("yesterday", 1),
            session("three", 3),
            session("ten", 10),
            session("old", 60),
        )
        val groups = SessionGrouping.group(sessions, now, zone)
        assertEquals(
            listOf(
                SessionGrouping.LABEL_TODAY,
                SessionGrouping.LABEL_YESTERDAY,
                SessionGrouping.LABEL_LAST_7_DAYS,
                SessionGrouping.LABEL_LAST_30_DAYS,
                SessionGrouping.LABEL_EARLIER,
            ),
            groups.map { it.label },
        )
        assertEquals(listOf("today"), groups[0].sessions.map { it.id })
        assertEquals(listOf("three"), groups[2].sessions.map { it.id })
        assertEquals(listOf("old"), groups[4].sessions.map { it.id })
    }

    @Test
    fun pinnedGroupComesFirstAndOnlyContainsPinned() {
        val sessions = listOf(
            session("today", 0),
            session("pinnedOld", 40, pinned = true),
        )
        val groups = SessionGrouping.group(sessions, now, zone)
        assertEquals(SessionGrouping.LABEL_PINNED, groups.first().label)
        assertEquals(listOf("pinnedOld"), groups.first().sessions.map { it.id })
        assertEquals(SessionGrouping.LABEL_TODAY, groups[1].label)
    }

    @Test
    fun sortsWithinGroupByUpdatedAtDescending() {
        val sessions = listOf(session("a", 0), session("b", 0), session("c", 0))
        val groups = SessionGrouping.group(sessions, now, zone)
        assertEquals(listOf("a", "b", "c"), groups.first().sessions.map { it.id })
    }

    @Test
    fun emptyInputProducesNoGroups() {
        assertEquals(emptyList<SessionGroup>(), SessionGrouping.group(emptyList(), now, zone))
    }
}
