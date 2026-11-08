package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.AchievementUnlockDialog
import com.example.ui.components.CreateChallengeDialog
import com.example.ui.components.DigitalDetoxDialog
import com.example.ui.components.FocusTimerDialog
import com.example.ui.components.JournalEntryDialog
import com.example.ui.components.LevelUpDialog
import com.example.ui.components.RewardShopDialog
import com.example.ui.components.StreakCelebrationDialog
import com.example.ui.components.TaskReminderDialog
import com.example.ui.screens.achievements.AchievementsScreen
import com.example.ui.screens.analytics.AnalyticsScreen
import com.example.ui.screens.challenge.ChallengeScreen
import com.example.ui.screens.challenge.DayDetailDialog
import com.example.ui.screens.daily.DailyCheckInDialog
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.profile.ProfileScreen
import com.example.ui.screens.settings.BackupRestoreDialog
import com.example.ui.screens.settings.CategoryManagementDialog
import com.example.ui.screens.tasks.AddEditTaskDialog
import com.example.ui.screens.tasks.TasksScreen
import com.example.ui.theme.RebootTheme
import com.example.ui.viewmodel.AppNavTab
import com.example.ui.viewmodel.RebootViewModel
import com.example.util.LanguageHelper

data class NavItem(
    val tab: AppNavTab,
    val titleKey: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

@Composable
fun RebootApp(
    viewModel: RebootViewModel = viewModel()
) {
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val accentColor by viewModel.accentColor.collectAsStateWithLifecycle()
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val lang by viewModel.language.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val toastMessage by viewModel.userToastMessage.collectAsStateWithLifecycle()

    val showAddTaskDialog by viewModel.showAddTaskDialog.collectAsStateWithLifecycle()
    val editingTask by viewModel.editingTask.collectAsStateWithLifecycle()
    val showDailyCheckIn by viewModel.showDailyCheckInDialog.collectAsStateWithLifecycle()
    val selectedChallengeDay by viewModel.selectedChallengeDay.collectAsStateWithLifecycle()
    val showBackupRestore by viewModel.showBackupRestoreDialog.collectAsStateWithLifecycle()
    val showCategoryDialog by viewModel.showCategoryDialog.collectAsStateWithLifecycle()
    val confirmDeleteTask by viewModel.confirmDeleteTask.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val currentDailyRecord by viewModel.currentDailyRecord.collectAsStateWithLifecycle()

    // Streak celebration
    val showCelebration by viewModel.showCelebrationDialog.collectAsStateWithLifecycle()
    val celebratedStreak by viewModel.celebratedStreakCount.collectAsStateWithLifecycle()
    val streakResult by viewModel.streakResult.collectAsStateWithLifecycle()
    val reducedMotion by viewModel.reducedMotion.collectAsStateWithLifecycle()

    // Gamification dialogs
    val showAchievementUnlockDialog by viewModel.showAchievementUnlockDialog.collectAsStateWithLifecycle()
    val recentlyUnlockedAchievement by viewModel.recentlyUnlockedAchievement.collectAsStateWithLifecycle()
    val showLevelUpDialog by viewModel.showLevelUpDialog.collectAsStateWithLifecycle()
    val currentLevelUpInfo by viewModel.currentLevelUpInfo.collectAsStateWithLifecycle()
    val showRewardShopDialog by viewModel.showRewardShopDialog.collectAsStateWithLifecycle()
    val showCreateChallengeDialog by viewModel.showCreateChallengeDialog.collectAsStateWithLifecycle()
    val rewardItems by viewModel.rewardItems.collectAsStateWithLifecycle()
    val purchasedRewards by viewModel.purchasedRewards.collectAsStateWithLifecycle()
    val totalXp by viewModel.totalXp.collectAsStateWithLifecycle()
    val activeFrame by viewModel.activeFrame.collectAsStateWithLifecycle()
    val activeFlameStyle by viewModel.activeFlameStyle.collectAsStateWithLifecycle()
    val activeProfileTitle by viewModel.activeProfileTitle.collectAsStateWithLifecycle()
    val showFocusTimerDialog by viewModel.showFocusTimerDialog.collectAsStateWithLifecycle()
    val showDigitalDetoxDialog by viewModel.showDigitalDetoxDialog.collectAsStateWithLifecycle()
    val showJournalDialog by viewModel.showJournalDialog.collectAsStateWithLifecycle()
    val showTaskReminderDialog by viewModel.showTaskReminderDialog.collectAsStateWithLifecycle()
    val reminderSelectedTaskId by viewModel.reminderSelectedTaskId.collectAsStateWithLifecycle()
    val allActiveTasks by viewModel.allActiveTasks.collectAsStateWithLifecycle()
    val allTaskReminders by viewModel.allTaskReminders.collectAsStateWithLifecycle()
    val todayDailyRecord by viewModel.todayDailyRecord.collectAsStateWithLifecycle()
    val todayJournalEntry by viewModel.todayJournalEntry.collectAsStateWithLifecycle()
    val screenTimeGoalMinutes by viewModel.screenTimeGoalMinutes.collectAsStateWithLifecycle()

    // Handle toast messages
    LaunchedEffect(toastMessage) {
        toastMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.userToastMessage.value = null
        }
    }

    // BackHandler: Return to Home tab if on any sub-tab
    BackHandler(enabled = currentTab != AppNavTab.HOME) {
        viewModel.setTab(AppNavTab.HOME)
    }

    val navItems = listOf(
        NavItem(AppNavTab.HOME, "nav_home", Icons.Filled.Home, Icons.Outlined.Home, "nav_tab_home"),
        NavItem(AppNavTab.TASKS, "nav_tasks", Icons.Filled.Checklist, Icons.Outlined.Checklist, "nav_tab_tasks"),
        NavItem(AppNavTab.ACHIEVEMENTS, "nav_achievements", Icons.Filled.EmojiEvents, Icons.Outlined.EmojiEvents, "nav_tab_achievements"),
        NavItem(AppNavTab.CHALLENGE, "nav_challenge", Icons.Filled.Shield, Icons.Outlined.Shield, "nav_tab_challenge"),
        NavItem(AppNavTab.ANALYTICS, "nav_analytics", Icons.Filled.BarChart, Icons.Outlined.BarChart, "nav_tab_analytics"),
        NavItem(AppNavTab.PROFILE, "nav_profile", Icons.Filled.Person, Icons.Outlined.Person, "nav_tab_profile")
    )

    RebootTheme(themeMode = themeMode, accentColor = accentColor) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                NavigationBar(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .testTag("bottom_nav_bar"),
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp
                ) {
                    navItems.forEach { item ->
                        val isSelected = currentTab == item.tab
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { viewModel.setTab(item.tab) },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = LanguageHelper.getString(item.titleKey, lang)
                                )
                            },
                            label = {
                                Text(
                                    text = LanguageHelper.getString(item.titleKey, lang),
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    maxLines = 1
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.White,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.testTag(item.testTag)
                        )
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Crossfade(targetState = currentTab, label = "tab_transition") { tab ->
                    when (tab) {
                        AppNavTab.HOME -> HomeScreen(viewModel = viewModel)
                        AppNavTab.TASKS -> TasksScreen(viewModel = viewModel)
                        AppNavTab.ACHIEVEMENTS -> AchievementsScreen(viewModel = viewModel)
                        AppNavTab.CHALLENGE -> ChallengeScreen(viewModel = viewModel)
                        AppNavTab.ANALYTICS -> AnalyticsScreen(viewModel = viewModel)
                        AppNavTab.PROFILE -> ProfileScreen(viewModel = viewModel)
                    }
                }
            }
        }

        // Global Dialogs

        // Streak Celebration Dialog
        if (showCelebration) {
            StreakCelebrationDialog(
                streakCount = celebratedStreak,
                milestoneTitle = streakResult.nextMilestone.title,
                onDismiss = { viewModel.showCelebrationDialog.value = false },
                reducedMotion = reducedMotion
            )
        }

        // 1. Add / Edit Task Dialog
        if (showAddTaskDialog) {
            AddEditTaskDialog(
                taskToEdit = editingTask,
                categories = categories,
                onDismiss = {
                    viewModel.showAddTaskDialog.value = false
                    viewModel.editingTask.value = null
                },
                onSave = { id, title, desc, catId, icon, prio, dur, start, due, rep, mask, rem, count, req, color ->
                    viewModel.saveTask(id, title, desc, catId, icon, prio, dur, start, due, rep, mask, rem, count, req, color)
                }
            )
        }

        // 2. Daily Check-in Dialog
        if (showDailyCheckIn) {
            DailyCheckInDialog(
                date = selectedDate,
                existingRecord = currentDailyRecord,
                onDismiss = { viewModel.showDailyCheckInDialog.value = false },
                onSave = { screen, study, exercise, sleep, mood, rating, notes ->
                    viewModel.saveDailyProgress(screen, study, exercise, sleep, mood, rating, notes)
                }
            )
        }

        // 3. Challenge Day Detail Dialog
        selectedChallengeDay?.let { day ->
            DayDetailDialog(
                day = day,
                onDismiss = { viewModel.selectedChallengeDay.value = null },
                onToggleGoal = { goalIndex -> viewModel.toggleChallengeGoal(day, goalIndex) },
                onSaveReflection = { notes -> viewModel.saveChallengeReflection(day, notes) }
            )
        }

        // 4. Backup & Restore Dialog
        if (showBackupRestore) {
            BackupRestoreDialog(
                viewModel = viewModel,
                onDismiss = { viewModel.showBackupRestoreDialog.value = false }
            )
        }

        // 5. Category Management Dialog
        if (showCategoryDialog) {
            CategoryManagementDialog(
                categories = categories,
                onDismiss = { viewModel.showCategoryDialog.value = false },
                onCreateCategory = { name, icon, colorHex -> viewModel.createCategory(name, icon, colorHex) },
                onDeleteCategory = { id -> viewModel.deleteCategory(id) }
            )
        }

        // 6. Confirm Delete Task Dialog
        confirmDeleteTask?.let { task ->
            AlertDialog(
                onDismissRequest = { viewModel.confirmDeleteTask.value = null },
                title = { Text("Delete Task?") },
                text = { Text("Are you sure you want to delete \"${task.title}\"?") },
                confirmButton = {
                    Button(
                        onClick = { viewModel.deleteTask(task) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.confirmDeleteTask.value = null }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // 7. Achievement Unlock Dialog
        if (showAchievementUnlockDialog && recentlyUnlockedAchievement != null) {
            AchievementUnlockDialog(
                achievement = recentlyUnlockedAchievement!!,
                onDismiss = { viewModel.showAchievementUnlockDialog.value = false },
                onViewGallery = {
                    viewModel.showAchievementUnlockDialog.value = false
                    viewModel.setTab(AppNavTab.ACHIEVEMENTS)
                }
            )
        }

        // 8. Level Up Celebration Dialog
        if (showLevelUpDialog && currentLevelUpInfo != null) {
            LevelUpDialog(
                levelInfo = currentLevelUpInfo!!,
                onDismiss = { viewModel.showLevelUpDialog.value = false }
            )
        }

        // 9. Reward Shop Dialog
        if (showRewardShopDialog) {
            RewardShopDialog(
                rewardItems = rewardItems,
                purchasedRewards = purchasedRewards,
                availableSparks = totalXp,
                activeFrame = activeFrame,
                activeFlameStyle = activeFlameStyle,
                activeTitle = activeProfileTitle,
                onDismiss = { viewModel.showRewardShopDialog.value = false },
                onPurchaseItem = { viewModel.purchaseRewardItem(it) },
                onEquipItem = { viewModel.equipRewardItem(it) }
            )
        }

        // 10. Create Custom Challenge Dialog
        if (showCreateChallengeDialog) {
            CreateChallengeDialog(
                categories = categories,
                onDismiss = { viewModel.showCreateChallengeDialog.value = false },
                onCreate = { viewModel.createCustomChallenge(it) }
            )
        }

        // 11. Focus Timer Dialog (Part 3)
        if (showFocusTimerDialog) {
            FocusTimerDialog(
                tasks = allActiveTasks,
                onDismiss = { viewModel.showFocusTimerDialog.value = false },
                onSessionCompleted = { duration, type, taskId, taskTitle, notes ->
                    viewModel.saveFocusSession(duration, type, taskId, taskTitle, notes)
                }
            )
        }

        // 12. Digital Detox Dialog (Part 3)
        if (showDigitalDetoxDialog) {
            DigitalDetoxDialog(
                currentScreenTimeMinutes = todayDailyRecord?.screenTimeMinutes ?: 0,
                targetLimitMinutes = screenTimeGoalMinutes,
                onDismiss = { viewModel.showDigitalDetoxDialog.value = false },
                onSaveScreenTime = { logged, goal ->
                    viewModel.saveScreenTimeAndGoal(logged, goal)
                }
            )
        }

        // 13. Daily Journal Dialog (Part 3)
        if (showJournalDialog) {
            JournalEntryDialog(
                initialEntry = todayJournalEntry,
                date = selectedDate,
                onDismiss = { viewModel.showJournalDialog.value = false },
                onSaveJournal = { mood, energy, gratitude, reflection, highlights, challenges, tag ->
                    viewModel.saveJournalEntry(mood, energy, gratitude, reflection, highlights, challenges, tag)
                }
            )
        }

        // 14. Task Reminder Dialog (Part 3)
        if (showTaskReminderDialog && reminderSelectedTaskId != null) {
            val targetTask = allActiveTasks.firstOrNull { it.id == reminderSelectedTaskId }
            if (targetTask != null) {
                TaskReminderDialog(
                    task = targetTask,
                    existingReminders = allTaskReminders.filter { it.taskId == targetTask.id },
                    onDismiss = {
                        viewModel.showTaskReminderDialog.value = false
                        viewModel.reminderSelectedTaskId.value = null
                    },
                    onAddReminder = { time, mask, label ->
                        viewModel.saveTaskReminder(targetTask.id, time, mask, label)
                    },
                    onToggleReminder = { reminder, enabled ->
                        viewModel.toggleTaskReminder(reminder, enabled)
                    },
                    onDeleteReminder = { id ->
                        viewModel.deleteTaskReminder(id)
                    },
                    onSnoozeReminder = { id ->
                        viewModel.snoozeTaskReminder(id)
                    },
                    onSendTestNotification = {
                        viewModel.triggerTestReminder()
                    }
                )
            }
        }
    }
}
