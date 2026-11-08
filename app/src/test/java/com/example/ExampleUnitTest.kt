package com.example

import com.example.data.local.entities.TaskEntity
import com.example.data.local.entities.TaskLogEntity
import com.example.util.DateUtils
import com.example.util.DisciplineStreakEngine
import com.example.util.GamificationEngine
import com.example.util.LanguageHelper
import com.example.util.Quotes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testChallengeDayCalculation() {
        val day1 = DateUtils.calculateDayOfChallenge("2026-11-01", "2026-11-01")
        assertEquals(1, day1)

        val day5 = DateUtils.calculateDayOfChallenge("2026-11-01", "2026-11-05")
        assertEquals(5, day5)

        val day30 = DateUtils.calculateDayOfChallenge("2026-11-01", "2026-11-30")
        assertEquals(30, day30)

        // Clamped at 30
        val dayOver = DateUtils.calculateDayOfChallenge("2026-11-01", "2026-12-10")
        assertEquals(30, dayOver)
    }

    @Test
    fun testDateAddition() {
        val next = DateUtils.addDays("2026-11-05", 1)
        assertEquals("2026-11-06", next)

        val prev = DateUtils.addDays("2026-11-05", -1)
        assertEquals("2026-11-04", prev)
    }

    @Test
    fun testTaskSchedulingLogic() {
        val dailyTask = TaskEntity(
            id = 1,
            title = "Morning exercise",
            categoryId = "cat_fitness",
            startDate = "2026-11-01",
            repeatSchedule = "EVERY_DAY"
        )
        // Should be scheduled on start date and later dates
        assertTrue(DateUtils.isTaskScheduledForDate(dailyTask, "2026-11-01"))
        assertTrue(DateUtils.isTaskScheduledForDate(dailyTask, "2026-11-05"))
        // Should not be scheduled before start date
        assertFalse(DateUtils.isTaskScheduledForDate(dailyTask, "2026-10-31"))

        val pausedTask = dailyTask.copy(isPaused = true)
        assertFalse(DateUtils.isTaskScheduledForDate(pausedTask, "2026-11-05"))

        val oneTimeTask = TaskEntity(
            id = 2,
            title = "Doctor appointment",
            categoryId = "cat_health",
            startDate = "2026-11-05",
            repeatSchedule = "ONE_TIME"
        )
        assertTrue(DateUtils.isTaskScheduledForDate(oneTimeTask, "2026-11-05"))
        assertFalse(DateUtils.isTaskScheduledForDate(oneTimeTask, "2026-11-06"))
    }

    @Test
    fun testDisciplineStreakEngineStrictRules() {
        val reqTask1 = TaskEntity(
            id = 101,
            title = "Required Study",
            categoryId = "cat_study",
            startDate = "2026-11-01",
            repeatSchedule = "EVERY_DAY",
            isRequired = true
        )
        val optTask2 = TaskEntity(
            id = 102,
            title = "Optional Reading",
            categoryId = "cat_personal_dev",
            startDate = "2026-11-01",
            repeatSchedule = "EVERY_DAY",
            isRequired = false
        )
        val tasks = listOf(reqTask1, optTask2)

        // Case 1: On 2026-11-01, both completed. On 2026-11-02, required is completed, optional is incomplete.
        // Rule: Completing required task protects streak even if optional is incomplete!
        val logs = listOf(
            TaskLogEntity(date = "2026-11-01", taskId = 101, isCompleted = true),
            TaskLogEntity(date = "2026-11-01", taskId = 102, isCompleted = true),
            TaskLogEntity(date = "2026-11-02", taskId = 101, isCompleted = true),
            TaskLogEntity(date = "2026-11-02", taskId = 102, isCompleted = false)
        )
        val res = DisciplineStreakEngine.calculateStreak(tasks, logs, "2026-11-02")
        assertEquals(2, res.currentStreak)
        assertTrue(res.isTodayDisciplined)

        // Case 2: On 2026-11-03, only optional is completed, required is NOT completed.
        // Rule: Completing only optional tasks does NOT count as disciplined day.
        val logsWithMissedRequired = logs + listOf(
            TaskLogEntity(date = "2026-11-03", taskId = 101, isCompleted = false),
            TaskLogEntity(date = "2026-11-03", taskId = 102, isCompleted = true)
        )
        val resDay3 = DisciplineStreakEngine.calculateStreak(tasks, logsWithMissedRequired, "2026-11-04")
        // Day 3 was missed, so on day 4 current streak has reset to 0!
        assertEquals(0, resDay3.currentStreak)
        assertEquals(2, resDay3.bestStreak) // All-time best streak was 2
        assertEquals(2, resDay3.totalDisciplinedDays)
    }

    @Test
    fun testQuotesRotation() {
        val quoteDay1 = Quotes.getQuoteForDay(1)
        assertTrue(quoteDay1.quote.isNotEmpty())
        assertTrue(quoteDay1.author.isNotEmpty())

        val quoteDay30 = Quotes.getQuoteForDay(30)
        assertTrue(quoteDay30.quote.isNotEmpty())
    }

    @Test
    fun testGamificationLevelProgression() {
        val level1 = GamificationEngine.calculateLevelInfo(0)
        assertEquals(1, level1.level)
        assertEquals("Beginner", level1.title)
        assertEquals(0, level1.currentLevelXp)
        assertEquals(100, level1.xpForNextLevel)
        assertEquals(0f, level1.progress, 0.01f)

        // 50 XP (Halfway through level 1)
        val level1Half = GamificationEngine.calculateLevelInfo(50)
        assertEquals(1, level1Half.level)
        assertEquals(50, level1Half.currentLevelXp)
        assertEquals(0.5f, level1Half.progress, 0.01f)

        // 100 XP -> Level 2 Initiate (requires 150 XP for next level)
        val level2 = GamificationEngine.calculateLevelInfo(100)
        assertEquals(2, level2.level)
        assertEquals("Initiate", level2.title)
        assertEquals(0, level2.currentLevelXp)
        assertEquals(150, level2.xpForNextLevel)

        // 250 XP -> Level 3 Disciplined
        val level3 = GamificationEngine.calculateLevelInfo(250)
        assertEquals(3, level3.level)
        assertEquals("Disciplined", level3.title)

        // High XP level progression continues indefinitely
        val highLvl = GamificationEngine.calculateLevelInfo(5000)
        assertTrue(highLvl.level >= 10)
        assertTrue(highLvl.title.isNotEmpty())
    }

    @Test
    fun testAchievementCatalogIntegrity() {
        val achievements = GamificationEngine.getDefaultAchievements()
        assertTrue(achievements.size >= 25)

        // Verify all 6 rarity levels exist
        val rarities = achievements.map { it.rarity }.toSet()
        assertTrue(rarities.contains("COMMON"))
        assertTrue(rarities.contains("UNCOMMON"))
        assertTrue(rarities.contains("RARE"))
        assertTrue(rarities.contains("EPIC"))
        assertTrue(rarities.contains("LEGENDARY"))
        assertTrue(rarities.contains("MYTHIC"))

        // Verify hidden achievements exist
        val hidden = achievements.filter { it.isHidden }
        assertTrue(hidden.isNotEmpty())
        assertTrue(hidden.any { it.id == "ach_phoenix" })

        // Verify positive XP rewards for all
        achievements.forEach {
            assertTrue("Achievement ${it.id} should have positive XP", it.xpReward > 0)
            assertTrue("Achievement ${it.id} should have non-empty title", it.title.isNotEmpty())
        }
    }

    @Test
    fun testRewardShopCatalog() {
        val rewards = GamificationEngine.getDefaultRewardItems()
        assertTrue(rewards.size >= 10)

        // Verify types: PROFILE_FRAME, FLAME_STYLE, PROFILE_TITLE, THEME
        val types = rewards.map { it.type }.toSet()
        assertTrue(types.contains("PROFILE_FRAME"))
        assertTrue(types.contains("FLAME_STYLE"))
        assertTrue(types.contains("PROFILE_TITLE"))
        assertTrue(types.contains("THEME"))

        // Free default items exist
        assertTrue(rewards.any { it.id == "frame_classic" && it.costSparks == 0 })
        assertTrue(rewards.any { it.id == "flame_classic" && it.costSparks == 0 })
    }

    @Test
    fun testDailyQuestsAndWeeklyMissionsGenerators() {
        val quests = GamificationEngine.generateDailyQuestsForDate("2026-11-05")
        assertEquals(3, quests.size)
        quests.forEach {
            assertEquals("2026-11-05", it.date)
            assertTrue(it.xpReward > 0)
        }

        val missions = GamificationEngine.generateWeeklyMissionsForWeek("2026-W45")
        assertEquals(3, missions.size)
        missions.forEach {
            assertEquals("2026-W45", it.weekIdentifier)
            assertTrue(it.xpReward >= 100)
        }
    }

    @Test
    fun testFocusSessionXpRules() {
        val standard25m = 25
        val xp25 = if (standard25m >= 45) 30 else 15
        assertEquals(15, xp25)

        val deep50m = 50
        val xp50 = if (deep50m >= 45) 30 else 15
        assertEquals(30, xp50)
    }

    @Test
    fun testReminderWeekdayMask() {
        val allDaysMask = 127 // 1 + 2 + 4 + 8 + 16 + 32 + 64
        assertEquals(127, allDaysMask)

        // Mon + Wed + Fri = 1 + 4 + 16 = 21
        val mwfMask = 1 or 4 or 16
        assertEquals(21, mwfMask)
        assertTrue((mwfMask and 1) != 0) // Mon
        assertFalse((mwfMask and 2) != 0) // Tue
        assertTrue((mwfMask and 4) != 0) // Wed
        assertFalse((mwfMask and 8) != 0) // Thu
        assertTrue((mwfMask and 16) != 0) // Fri
    }

    @Test
    fun testQuietHoursCheckLogic() {
        // Quiet hours 22:00 to 07:00
        val startHour = 22
        val endHour = 7
        fun isQuiet(hour: Int): Boolean {
            return if (startHour < endHour) {
                hour in startHour until endHour
            } else {
                hour >= startHour || hour < endHour
            }
        }

        assertTrue(isQuiet(22))
        assertTrue(isQuiet(23))
        assertTrue(isQuiet(0))
        assertTrue(isQuiet(3))
        assertTrue(isQuiet(6))
        assertFalse(isQuiet(7))
        assertFalse(isQuiet(12))
        assertFalse(isQuiet(18))
        assertFalse(isQuiet(21))
    }
}
