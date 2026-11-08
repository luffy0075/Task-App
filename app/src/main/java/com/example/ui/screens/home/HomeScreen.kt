package com.example.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.AnimatedFlame
import com.example.ui.components.DailyChartPoint
import com.example.ui.components.DateSelectorBar
import com.example.ui.components.MetricStatCard
import com.example.ui.components.ProgressRing
import com.example.ui.components.StreakProtectionBanner
import com.example.ui.components.TaskItemCard
import com.example.ui.components.WeeklyStatisticsChart
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.FlameOrange
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.VioletPrimary
import com.example.ui.viewmodel.AppNavTab
import com.example.ui.viewmodel.RebootViewModel
import com.example.util.DateUtils
import com.example.util.LanguageHelper
import com.example.util.Quotes
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: RebootViewModel,
    modifier: Modifier = Modifier
) {
    val streakResult by viewModel.streakResult.collectAsStateWithLifecycle()
    val tasksToday by viewModel.tasksForSelectedDate.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val lang by viewModel.language.collectAsStateWithLifecycle()
    val allLogs by viewModel.allLogs.collectAsStateWithLifecycle()
    val allTasks by viewModel.allActiveTasks.collectAsStateWithLifecycle()
    val userDisplayName by viewModel.userDisplayName.collectAsStateWithLifecycle()
    val reducedMotion by viewModel.reducedMotion.collectAsStateWithLifecycle()
    val userLevelInfo by viewModel.userLevelInfo.collectAsStateWithLifecycle()
    val dailyQuests by viewModel.dailyQuests.collectAsStateWithLifecycle()
    val activeFlameStyle by viewModel.activeFlameStyle.collectAsStateWithLifecycle()
    val totalXp by viewModel.totalXp.collectAsStateWithLifecycle()

    val quote = remember(streakResult.currentStreak) {
        val dayIndex = (streakResult.currentStreak + 1).coerceIn(1, 30)
        Quotes.getQuoteForDay(dayIndex)
    }

    // Weekly points for the last 7 days ending with selected date
    val weeklyPoints = remember(allLogs, allTasks, selectedDate) {
        val points = mutableListOf<DailyChartPoint>()
        val isoFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val dayLabelFormat = SimpleDateFormat("EEE", Locale.US)

        for (i in -6..0) {
            val d = DateUtils.addDays(selectedDate, i)
            val dayDate = try { isoFormat.parse(d) } catch (_: Exception) { null }
            val dayLabel = if (dayDate != null) dayLabelFormat.format(dayDate) else "Day"

            val completedForDay = allLogs.count { it.date == d && it.isCompleted }
            val scheduledForDay = allTasks.count { DateUtils.isTaskScheduledForDate(it, d) }
            val ratio = if (scheduledForDay > 0) completedForDay.toFloat() / scheduledForDay.toFloat() else 0f

            points.add(
                DailyChartPoint(
                    date = d,
                    dayLabel = dayLabel,
                    completedCount = completedForDay,
                    totalCount = scheduledForDay,
                    completionRatio = ratio
                )
            )
        }
        points
    }

    val totalScheduledToday = tasksToday.size
    val totalCompletedToday = tasksToday.count { it.isCompleted }
    val todayCompletionPct = if (totalScheduledToday > 0) totalCompletedToday.toFloat() / totalScheduledToday.toFloat() else 0f

    val isCurrentDateToday = selectedDate == DateUtils.getTodayDate()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header & Greeting
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${DateUtils.getGreeting()}, $userDisplayName",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = DateUtils.formatDisplayDate(selectedDate),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Shop Button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(GoldAccent.copy(alpha = 0.15f))
                                .border(1.dp, GoldAccent.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                .clickable { viewModel.showRewardShopDialog.value = true }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .testTag("btn_home_reward_shop")
                        ) {
                            Text(
                                text = "Shop ⚡",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = GoldAccent
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Level Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(VioletPrimary.copy(alpha = 0.15f))
                                .border(1.dp, VioletPrimary.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                                .clickable { viewModel.setTab(AppNavTab.ACHIEVEMENTS) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Lv.${userLevelInfo.level} ${userLevelInfo.iconEmoji}",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = VioletPrimary
                            )
                        }
                    }
                }

                // Date Navigation Bar
                DateSelectorBar(
                    selectedDate = selectedDate,
                    onPrevDay = { viewModel.shiftSelectedDate(-1) },
                    onNextDay = { viewModel.shiftSelectedDate(1) },
                    onToday = { viewModel.resetToToday() },
                    onOpenDatePicker = { viewModel.resetToToday() }
                )
            }
        }

        // Level & XP Progress Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .border(
                            1.dp,
                            Brush.horizontalGradient(listOf(VioletPrimary.copy(alpha = 0.5f), CyanAccent.copy(alpha = 0.5f))),
                            RoundedCornerShape(20.dp)
                        )
                        .padding(16.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = userLevelInfo.iconEmoji,
                                    fontSize = 20.sp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Level ${userLevelInfo.level} • ${userLevelInfo.title}",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${userLevelInfo.currentLevelXp} / ${userLevelInfo.xpForNextLevel} XP to Level ${userLevelInfo.level + 1}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(VioletPrimary.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "$totalXp XP",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = VioletPrimary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Progress Bar
                        LinearProgressIndicator(
                            progress = { userLevelInfo.progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = VioletPrimary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }
            }
        }

        // Comeback Encouragement Card (If streak is 0 but user has previous best >= 2)
        if (streakResult.currentStreak == 0 && streakResult.bestStreak >= 2) {
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    com.example.ui.components.ComebackCard(
                        bestStreak = streakResult.bestStreak,
                        lifetimeTasks = streakResult.totalCompletedTasks
                    )
                }
            }
        }

        // 2. Streak Protection Warning (Only displayed if streak is at risk on today's view)
        if (isCurrentDateToday && streakResult.isStreakAtRisk) {
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    StreakProtectionBanner(
                        requiredRemaining = streakResult.requiredTasksToday - streakResult.completedRequiredToday,
                        hoursLeft = streakResult.hoursRemainingToday,
                        minutesLeft = streakResult.minutesRemainingToday,
                        onViewRequiredTasks = {
                            viewModel.taskRequiredFilter.value = "REQUIRED"
                            viewModel.setTab(AppNavTab.TASKS)
                        }
                    )
                }
            }
        }

        // 3. Infinite Discipline Streak Hero Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Box(
                    modifier = Modifier
                        .border(
                            1.dp,
                            if (streakResult.isTodayDisciplined) {
                                EmeraldAccent.copy(alpha = 0.5f)
                            } else {
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                            },
                            RoundedCornerShape(24.dp)
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "DISCIPLINE STREAK",
                                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                                        color = FlameOrange,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    if (streakResult.isTodayDisciplined) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(EmeraldAccent.copy(alpha = 0.2f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "COMPLETED",
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                                color = EmeraldAccent,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text(
                                        text = "${streakResult.currentStreak}",
                                        style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Black),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "DAYS",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(bottom = 6.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(2.dp))

                                Text(
                                    text = "Personal Best: ${streakResult.bestStreak} Days • ${streakResult.totalDisciplinedDays} Total Disciplined",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Animated pulsing flame
                            AnimatedFlame(
                                size = 76.dp,
                                isActive = streakResult.currentStreak > 0,
                                reducedMotion = reducedMotion,
                                flameStyle = activeFlameStyle
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Dual Progress Breakdown: Required vs Optional
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Required Tasks Counter
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(FlameOrange.copy(alpha = 0.12f))
                                    .border(1.dp, FlameOrange.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                    .padding(12.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "REQUIRED",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = FlameOrange,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${streakResult.completedRequiredToday}/${streakResult.requiredTasksToday} Done",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            // Optional Tasks Counter
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(CyanAccent.copy(alpha = 0.12f))
                                    .border(1.dp, CyanAccent.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                    .padding(12.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "OPTIONAL",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = CyanAccent,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${streakResult.completedOptionalToday}/${streakResult.optionalTasksToday} Done",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Upcoming Milestone teaser
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .clickable { viewModel.setTab(AppNavTab.ACHIEVEMENTS) }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "🎯 Next: ${streakResult.nextMilestone.badge} ${streakResult.nextMilestone.title}",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${streakResult.daysToNextMilestone}d away",
                                style = MaterialTheme.typography.labelSmall,
                                color = VioletPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // 3.5 Daily Quests Section
        if (dailyQuests.isNotEmpty()) {
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Daily Quests",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Earn bonus XP",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    dailyQuests.forEach { quest ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (quest.isClaimed) {
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                } else {
                                    MaterialTheme.colorScheme.surface
                                }
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .border(
                                        1.dp,
                                        if (quest.isCompleted && !quest.isClaimed) GoldAccent.copy(alpha = 0.6f) else Color.Transparent,
                                        RoundedCornerShape(16.dp)
                                    )
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(quest.iconEmoji, fontSize = 20.sp)
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = quest.title,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = quest.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Progress: ${quest.currentProgress.coerceAtMost(quest.targetCount)}/${quest.targetCount}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = VioletPrimary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                if (quest.isClaimed) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(EmeraldAccent.copy(alpha = 0.15f))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "Claimed ✓",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = EmeraldAccent
                                        )
                                    }
                                } else if (quest.isCompleted) {
                                    Button(
                                        onClick = { viewModel.claimDailyQuest(quest) },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                                        modifier = Modifier.testTag("btn_claim_quest_${quest.id}")
                                    ) {
                                        Text(
                                            text = "Claim +${quest.xpReward} XP",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = Color.Black
                                        )
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant)
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "+${quest.xpReward} XP",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = GoldAccent
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. Motivational Focus Quote Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "DAILY FOCUS MANTRA",
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "\"${quote.quote}\"",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "— ${quote.author}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // 5. Quick Actions Row
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = "Quick Tools & Actions",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(10.dp))
                // Row 1: Focus Timer & Journal
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { viewModel.showFocusTimerDialog.value = true },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("quick_focus_timer_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Focus Timer")
                    }

                    OutlinedButton(
                        onClick = { viewModel.showJournalDialog.value = true },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("quick_journal_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.Create, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Journal")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Row 2: Digital Detox & Add Task
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.showDigitalDetoxDialog.value = true },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("quick_detox_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.PhoneAndroid, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Digital Detox")
                    }

                    OutlinedButton(
                        onClick = {
                            viewModel.editingTask.value = null
                            viewModel.showAddTaskDialog.value = true
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("quick_add_task_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Add Task")
                    }
                }
            }
        }

        // 6. Today's Tasks Section with Required Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Today's Tasks (${tasksToday.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )

                Text(
                    text = "See all",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { viewModel.setTab(AppNavTab.TASKS) }
                )
            }
        }

        if (tasksToday.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No tasks scheduled for this day.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                viewModel.editingTask.value = null
                                viewModel.showAddTaskDialog.value = true
                            }
                        ) {
                            Text("Create Task")
                        }
                    }
                }
            }
        } else {
            items(tasksToday, key = { it.task.id }) { taskWithStatus ->
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    TaskItemCard(
                        taskWithStatus = taskWithStatus,
                        onToggleCompletion = { viewModel.toggleTaskCompletion(taskWithStatus) },
                        onEdit = {
                            viewModel.editingTask.value = taskWithStatus.task
                            viewModel.showAddTaskDialog.value = true
                        },
                        onDelete = { viewModel.confirmDeleteTask.value = taskWithStatus.task },
                        onTogglePause = { viewModel.toggleTaskPaused(taskWithStatus.task) },
                        onSetReminder = {
                            viewModel.reminderSelectedTaskId.value = taskWithStatus.task.id
                            viewModel.showTaskReminderDialog.value = true
                        }
                    )
                }
            }
        }

        // 7. Weekly Activity Chart
        item {
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                WeeklyStatisticsChart(
                    points = weeklyPoints,
                    selectedDate = selectedDate,
                    onDateSelected = { date -> viewModel.setSelectedDate(date) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
