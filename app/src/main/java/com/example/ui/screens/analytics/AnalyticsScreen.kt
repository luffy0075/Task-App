package com.example.ui.screens.analytics

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.CategoryProgressRow
import com.example.ui.components.DailyChartPoint
import com.example.ui.components.MetricStatCard
import com.example.ui.components.WeeklyStatisticsChart
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.FlameOrange
import com.example.ui.theme.RoseAccent
import com.example.ui.theme.VioletPrimary
import com.example.ui.viewmodel.RebootViewModel
import com.example.util.DateUtils
import java.util.Calendar
import java.util.Locale

enum class AnalyticsTab {
    DAILY,
    WEEKLY,
    MONTHLY,
    LIFETIME
}

@Composable
fun AnalyticsScreen(
    viewModel: RebootViewModel,
    modifier: Modifier = Modifier
) {
    var currentTab by remember { mutableStateOf(AnalyticsTab.DAILY) }

    val allLogs by viewModel.allLogs.collectAsStateWithLifecycle()
    val allDailyRecords by viewModel.allDailyRecords.collectAsStateWithLifecycle()
    val allTasks by viewModel.allActiveTasks.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val streakResult by viewModel.streakResult.collectAsStateWithLifecycle()
    val totalXp by viewModel.totalXp.collectAsStateWithLifecycle()
    val userLevelInfo by viewModel.userLevelInfo.collectAsStateWithLifecycle()
    val focusSessions by viewModel.focusSessions.collectAsStateWithLifecycle()
    val totalFocusMinutes by viewModel.totalFocusMinutes.collectAsStateWithLifecycle()
    val journalEntries by viewModel.journalEntries.collectAsStateWithLifecycle()
    val allAchievements by viewModel.allAchievements.collectAsStateWithLifecycle()
    val targetScreenTimeGoal by viewModel.screenTimeGoalMinutes.collectAsStateWithLifecycle()

    // Calendar state for Monthly Tab
    var calendarMonthOffset by remember { mutableIntStateOf(0) }
    val displayCalendar = remember(calendarMonthOffset) {
        val cal = Calendar.getInstance()
        cal.add(Calendar.MONTH, calendarMonthOffset)
        cal
    }
    val daysInMonth = remember(displayCalendar) {
        displayCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    }
    val firstDayOfWeek = remember(displayCalendar) {
        val cal = displayCalendar.clone() as Calendar
        cal.set(Calendar.DAY_OF_MONTH, 1)
        var day = cal.get(Calendar.DAY_OF_WEEK) - 2
        if (day < 0) day = 6
        day
    }

    // Category performance
    val categoryStats = remember(categories, allTasks, allLogs) {
        categories.map { cat ->
            val tasksInCat = allTasks.filter { it.categoryId == cat.id }
            val taskIds = tasksInCat.map { it.id }.toSet()
            val completedCount = allLogs.count { it.isCompleted && taskIds.contains(it.taskId) }
            val totalScheduledLogs = allLogs.count { taskIds.contains(it.taskId) }.coerceAtLeast(tasksInCat.size)
            Triple(cat, completedCount, totalScheduledLogs)
        }
    }

    // Today's details
    val currentDayRecord = remember(allDailyRecords, selectedDate) {
        allDailyRecords.firstOrNull { it.date == selectedDate }
    }
    val currentDayLogs = remember(allLogs, selectedDate) {
        allLogs.filter { it.date == selectedDate }
    }
    val currentDayFocusMinutes = remember(focusSessions, selectedDate) {
        focusSessions.filter { it.date == selectedDate && it.sessionType == "FOCUS" }.sumOf { it.durationMinutes }
    }
    val currentDayJournal = remember(journalEntries, selectedDate) {
        journalEntries.firstOrNull { it.date == selectedDate }
    }

    // Weekly calculation
    val weeklyPoints = remember(allDailyRecords, allLogs, allTasks) {
        val list = mutableListOf<DailyChartPoint>()
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -6)
        for (i in 0..6) {
            val dateStr = DateUtils.formatIso(cal)
            val dayName = DateUtils.formatDayAbbr(cal)
            val logs = allLogs.filter { it.date == dateStr }
            val completed = logs.count { it.isCompleted }
            val scheduled = allTasks.count { task ->
                val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
                val bit = when (dayOfWeek) {
                    Calendar.MONDAY -> 1
                    Calendar.TUESDAY -> 2
                    Calendar.WEDNESDAY -> 4
                    Calendar.THURSDAY -> 8
                    Calendar.FRIDAY -> 16
                    Calendar.SATURDAY -> 32
                    Calendar.SUNDAY -> 64
                    else -> 1
                }
                (task.repeatDaysMask and bit) != 0
            }.coerceAtLeast(1)
            val pct = (completed.toFloat() / scheduled.toFloat()).coerceIn(0f, 1f)
            list.add(
                DailyChartPoint(
                    date = dateStr,
                    dayLabel = dayName,
                    completedCount = completed,
                    totalCount = scheduled,
                    completionRatio = pct
                )
            )
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        list
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("analytics_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Screen Header
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Analytics & Insights",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Measure your willpower, habits and growth",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Streak flame tag
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = FlameOrange.copy(alpha = 0.15f),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            tint = FlameOrange,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${streakResult.currentStreak}d Streak",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = FlameOrange
                        )
                    }
                }
            }
        }

        // 4-Tab Navigation (Daily, Weekly, Monthly, Lifetime)
        item {
            TabRow(
                selectedTabIndex = currentTab.ordinal,
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                contentColor = MaterialTheme.colorScheme.primary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[currentTab.ordinal]),
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .testTag("analytics_tab_row")
            ) {
                listOf(
                    Pair(AnalyticsTab.DAILY, "Daily"),
                    Pair(AnalyticsTab.WEEKLY, "Weekly"),
                    Pair(AnalyticsTab.MONTHLY, "Monthly"),
                    Pair(AnalyticsTab.LIFETIME, "Lifetime")
                ).forEach { (tab, label) ->
                    Tab(
                        selected = currentTab == tab,
                        onClick = { currentTab = tab },
                        text = {
                            Text(
                                text = label,
                                fontWeight = if (currentTab == tab) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp
                            )
                        },
                        modifier = Modifier.testTag("analytics_tab_${label.lowercase()}")
                    )
                }
            }
        }

        // ==================== TAB 1: DAILY ====================
        if (currentTab == AnalyticsTab.DAILY) {
            // Selected Date Bar
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = DateUtils.formatDisplayDate(selectedDate),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedButton(
                                onClick = {
                                    val previous = DateUtils.addDays(selectedDate, -1)
                                    viewModel.setSelectedDate(previous)
                                },
                                shape = CircleShape,
                                modifier = Modifier.size(36.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Day", modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedButton(
                                onClick = { viewModel.setSelectedDate(DateUtils.getTodayDate()) },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Text("Today", fontSize = 11.sp)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedButton(
                                onClick = {
                                    val next = DateUtils.addDays(selectedDate, 1)
                                    viewModel.setSelectedDate(next)
                                },
                                shape = CircleShape,
                                modifier = Modifier.size(36.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Day", modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            // Daily Task Completion Card
            item {
                val completedCount = currentDayLogs.count { it.isCompleted }
                val totalScheduled = allTasks.count { DateUtils.isTaskScheduledForDate(it, selectedDate) }
                val pct = if (totalScheduled > 0) completedCount.toFloat() / totalScheduled.toFloat() else 0f

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Daily Task Completion",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$completedCount of $totalScheduled tasks finished",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (pct >= 1f) EmeraldAccent.copy(alpha = 0.2f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = if (pct >= 1f) "100% DISCIPLINED" else "${(pct * 100).toInt()}% COMPLETED",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (pct >= 1f) EmeraldAccent else MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(76.dp)) {
                            CircularProgressIndicator(
                                progress = { 1f },
                                modifier = Modifier.fillMaxSize(),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                strokeWidth = 8.dp
                            )
                            CircularProgressIndicator(
                                progress = { pct },
                                modifier = Modifier.fillMaxSize(),
                                color = if (pct >= 1f) EmeraldAccent else MaterialTheme.colorScheme.primary,
                                strokeWidth = 8.dp
                            )
                            Text(
                                text = "${(pct * 100).toInt()}%",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Daily Wellbeing & Productivity Row (Focus, Detox, Journal)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Focus Time Card
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { viewModel.showFocusTimerDialog.value = true },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Timer, contentDescription = null, tint = VioletPrimary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Focus", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "${currentDayFocusMinutes}m",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = VioletPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Deep work logged", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    // Screen Time / Detox Card
                    val screenTime = currentDayRecord?.screenTimeMinutes ?: 0
                    val isOver = screenTime > targetScreenTimeGoal
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { viewModel.showDigitalDetoxDialog.value = true },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = if (isOver) RoseAccent else EmeraldAccent, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Screen", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "${screenTime / 60}h ${screenTime % 60}m",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isOver) RoseAccent else EmeraldAccent
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Target: ${targetScreenTimeGoal / 60}h", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    // Journal Card
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { viewModel.showJournalDialog.value = true },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Create, contentDescription = null, tint = AmberAccent, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Journal", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = currentDayJournal?.mood ?: currentDayRecord?.mood ?: "Good",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = AmberAccent
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Drive: ${currentDayJournal?.energyRating ?: 4}/5", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // Journal reflection preview (if exists)
            if (!currentDayJournal?.reflectionNotes.isNullOrBlank() || !currentDayJournal?.gratitudeNotes.isNullOrBlank()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Daily Reflections",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                OutlinedButton(
                                    onClick = { viewModel.showJournalDialog.value = true },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Text("Edit", fontSize = 10.sp)
                                }
                            }
                            if (!currentDayJournal?.gratitudeNotes.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Gratitude: \"${currentDayJournal?.gratitudeNotes}\"",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (!currentDayJournal?.reflectionNotes.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Reflection: \"${currentDayJournal?.reflectionNotes}\"",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // ==================== TAB 2: WEEKLY ====================
        if (currentTab == AnalyticsTab.WEEKLY) {
            item {
                WeeklyStatisticsChart(
                    points = weeklyPoints,
                    selectedDate = selectedDate,
                    onDateSelected = { viewModel.setSelectedDate(it) }
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Category Performance Breakdown",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        categoryStats.forEach { (cat, completed, total) ->
                            val catColor = try {
                                Color(android.graphics.Color.parseColor(cat.colorHex))
                            } catch (_: Exception) {
                                VioletPrimary
                            }
                            CategoryProgressRow(
                                categoryName = cat.name,
                                categoryColor = catColor,
                                completedCount = completed,
                                totalCount = total
                            )
                        }
                    }
                }
            }

            // Summary Stats
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val totalStudyHours = allDailyRecords.sumOf { it.studyMinutes } / 60f
                    val totalExerciseHours = allDailyRecords.sumOf { it.exerciseMinutes } / 60f

                    MetricStatCard(
                        title = "Study & Focus",
                        value = String.format(Locale.US, "%.1fh", totalStudyHours),
                        subtitle = "Cumulative Deep Work",
                        accentColor = VioletPrimary,
                        modifier = Modifier.weight(1f),
                        icon = { Icon(Icons.Default.School, contentDescription = null, tint = VioletPrimary, modifier = Modifier.size(18.dp)) }
                    )

                    MetricStatCard(
                        title = "Exercise & Fitness",
                        value = String.format(Locale.US, "%.1fh", totalExerciseHours),
                        subtitle = "Active workout time",
                        accentColor = EmeraldAccent,
                        modifier = Modifier.weight(1f),
                        icon = { Icon(Icons.Default.Check, contentDescription = null, tint = EmeraldAccent, modifier = Modifier.size(18.dp)) }
                    )
                }
            }
        }

        // ==================== TAB 3: MONTHLY ====================
        if (currentTab == AnalyticsTab.MONTHLY) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Month Navigation Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = DateUtils.formatMonthYear(displayCalendar),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Row {
                                IconButton(onClick = { calendarMonthOffset-- }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Prev Month")
                                }
                                IconButton(onClick = { calendarMonthOffset++ }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Month")
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Weekday Labels
                        val weekDays = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            weekDays.forEach { dayName ->
                                Text(
                                    text = dayName,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Calendar Grid Days
                        val totalSlots = firstDayOfWeek + daysInMonth
                        val rows = (totalSlots + 6) / 7

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            for (r in 0 until rows) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    for (c in 0 until 7) {
                                        val index = r * 7 + c
                                        val dayNum = index - firstDayOfWeek + 1

                                        if (dayNum in 1..daysInMonth) {
                                            val calDay = displayCalendar.clone() as Calendar
                                            calDay.set(Calendar.DAY_OF_MONTH, dayNum)
                                            val dateIso = DateUtils.formatIso(calDay)
                                            val isSelected = dateIso == selectedDate
                                            val isToday = dateIso == DateUtils.getTodayDate()

                                            // Determine completion status
                                            val dayLogs = allLogs.filter { it.date == dateIso }
                                            val completedTasks = dayLogs.count { it.isCompleted }
                                            val scheduledRequired = allTasks.count { task ->
                                                task.isRequired && DateUtils.isTaskScheduledForDate(task, dateIso)
                                            }
                                            val isDisciplined = scheduledRequired > 0 && dayLogs.count { log ->
                                                log.isCompleted && allTasks.firstOrNull { it.id == log.taskId }?.isRequired == true
                                            } >= scheduledRequired

                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .aspectRatio(1f)
                                                    .padding(2.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        when {
                                                            isSelected -> MaterialTheme.colorScheme.primary
                                                            isDisciplined -> EmeraldAccent.copy(alpha = 0.25f)
                                                            completedTasks > 0 -> AmberAccent.copy(alpha = 0.2f)
                                                            else -> Color.Transparent
                                                        }
                                                    )
                                                    .border(
                                                        width = if (isToday) 1.5.dp else 0.dp,
                                                        color = if (isToday) MaterialTheme.colorScheme.primary else Color.Transparent,
                                                        shape = CircleShape
                                                    )
                                                    .clickable { viewModel.setSelectedDate(dateIso) },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = dayNum.toString(),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                                    color = when {
                                                        isSelected -> Color.White
                                                        isDisciplined -> EmeraldAccent
                                                        completedTasks > 0 -> AmberAccent
                                                        else -> MaterialTheme.colorScheme.onSurface
                                                    }
                                                )
                                            }
                                        } else {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Calendar Legend
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(EmeraldAccent))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("100% Disciplined", style = MaterialTheme.typography.labelSmall)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(AmberAccent))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Partial", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        }

        // ==================== TAB 4: LIFETIME ====================
        if (currentTab == AnalyticsTab.LIFETIME) {
            // Lifetime Hero Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Lifetime Discipline Record",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Level ${userLevelInfo.level} • ${userLevelInfo.title}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Level progress bar
                        LinearProgressIndicator(
                            progress = { userLevelInfo.progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )

                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "$totalXp Total XP Earned",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${(userLevelInfo.progress * 100).toInt()}% to Level ${userLevelInfo.level + 1}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // Lifetime 2x2 Grid of Stats
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricStatCard(
                            title = "Total Tasks Completed",
                            value = "${streakResult.totalCompletedTasks}",
                            subtitle = "Discipline Repetitions",
                            accentColor = VioletPrimary,
                            modifier = Modifier.weight(1f),
                            icon = { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = VioletPrimary) }
                        )
                        MetricStatCard(
                            title = "Disciplined Days",
                            value = "${streakResult.totalDisciplinedDays}",
                            subtitle = "100% Required Finished",
                            accentColor = EmeraldAccent,
                            modifier = Modifier.weight(1f),
                            icon = { Icon(Icons.Default.Star, contentDescription = null, tint = EmeraldAccent) }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricStatCard(
                            title = "Current Streak",
                            value = "${streakResult.currentStreak} Days",
                            subtitle = "Best: ${streakResult.bestStreak} Days",
                            accentColor = FlameOrange,
                            modifier = Modifier.weight(1f),
                            icon = { Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = FlameOrange) }
                        )
                        val unlockedAchievementsCount = allAchievements.count { it.isUnlocked }
                        MetricStatCard(
                            title = "Achievements Unlocked",
                            value = "$unlockedAchievementsCount / ${allAchievements.size}",
                            subtitle = "Gamification Badges",
                            accentColor = Color(0xFFF59E0B),
                            modifier = Modifier.weight(1f),
                            icon = { Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Color(0xFFF59E0B)) }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricStatCard(
                            title = "Focus Time Logged",
                            value = "${totalFocusMinutes / 60}h ${totalFocusMinutes % 60}m",
                            subtitle = "${focusSessions.size} Focus Sessions",
                            accentColor = VioletPrimary,
                            modifier = Modifier.weight(1f),
                            icon = { Icon(Icons.Default.Timer, contentDescription = null, tint = VioletPrimary) }
                        )
                        MetricStatCard(
                            title = "Journal Entries",
                            value = "${journalEntries.size}",
                            subtitle = "Reflections & Mindset",
                            accentColor = AmberAccent,
                            modifier = Modifier.weight(1f),
                            icon = { Icon(Icons.Default.Create, contentDescription = null, tint = AmberAccent) }
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
