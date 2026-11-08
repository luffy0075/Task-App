package com.example.ui.screens.achievements

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.data.local.entities.AchievementEntity
import com.example.ui.components.AnimatedFlame
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.FlameOrange
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.RoseAccent
import com.example.ui.theme.VioletPrimary
import com.example.ui.viewmodel.RebootViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AchievementsScreen(
    viewModel: RebootViewModel,
    modifier: Modifier = Modifier
) {
    val streakResult by viewModel.streakResult.collectAsStateWithLifecycle()
    val reducedMotion by viewModel.reducedMotion.collectAsStateWithLifecycle()
    val allAchievements by viewModel.allAchievements.collectAsStateWithLifecycle()
    val userLevelInfo by viewModel.userLevelInfo.collectAsStateWithLifecycle()
    val totalXp by viewModel.totalXp.collectAsStateWithLifecycle()
    val activeFlameStyle by viewModel.activeFlameStyle.collectAsStateWithLifecycle()

    var selectedFilter by remember { mutableStateOf("ALL") } // "ALL", "UNLOCKED", "LOCKED", "DAILY", "STREAK", "TASKS", "CONSISTENCY", "GROWTH", "HIDDEN"

    val unlockedCount = remember(allAchievements) { allAchievements.count { it.isUnlocked } }
    val lockedCount = remember(allAchievements) { allAchievements.count { !it.isUnlocked } }

    val filterOptions = listOf(
        "ALL" to "All (${allAchievements.size})",
        "UNLOCKED" to "Unlocked ($unlockedCount)",
        "LOCKED" to "Locked ($lockedCount)",
        "DAILY" to "Daily",
        "STREAK" to "Streak",
        "TASKS" to "Tasks",
        "CONSISTENCY" to "Consistency",
        "GROWTH" to "Growth",
        "HIDDEN" to "Secret"
    )

    val displayedAchievements = remember(selectedFilter, allAchievements) {
        when (selectedFilter) {
            "ALL" -> allAchievements
            "UNLOCKED" -> allAchievements.filter { it.isUnlocked }
            "LOCKED" -> allAchievements.filter { !it.isUnlocked }
            "DAILY" -> allAchievements.filter { it.category == "DAILY" }
            "STREAK" -> allAchievements.filter { it.category == "STREAK" }
            "TASKS" -> allAchievements.filter { it.category == "TASKS" }
            "CONSISTENCY" -> allAchievements.filter { it.category == "CONSISTENCY" }
            "GROWTH" -> allAchievements.filter { it.category == "GROWTH" }
            "HIDDEN" -> allAchievements.filter { it.category == "HIDDEN" }
            else -> allAchievements
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("achievements_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Achievements & Ranks",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Real milestones forged through unbroken action",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = { viewModel.showRewardShopDialog.value = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                    modifier = Modifier.testTag("btn_achievements_reward_shop")
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Shop ⚡", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Hero Level & Rank Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Box(
                    modifier = Modifier
                        .border(
                            1.dp,
                            Brush.horizontalGradient(listOf(FlameOrange, VioletPrimary)),
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
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(FlameOrange.copy(alpha = 0.15f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "DISCIPLINE LEVEL ${userLevelInfo.level}",
                                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                                        color = FlameOrange,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = "${userLevelInfo.iconEmoji} ${userLevelInfo.title}",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "Current Streak: ${streakResult.currentStreak} Days • Best: ${streakResult.bestStreak} Days",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "Total XP: $totalXp • Unlocked: $unlockedCount / ${allAchievements.size}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = EmeraldAccent,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            AnimatedFlame(
                                size = 72.dp,
                                isActive = streakResult.currentStreak > 0,
                                reducedMotion = reducedMotion,
                                flameStyle = activeFlameStyle
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // XP to next level bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Next Level: ${userLevelInfo.currentLevelXp} / ${userLevelInfo.xpForNextLevel} XP",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${(userLevelInfo.progress * 100).toInt()}%",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = VioletPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

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

        // Filter chips bar
        item {
            ScrollableTabRow(
                selectedTabIndex = filterOptions.indexOfFirst { it.first == selectedFilter }.coerceAtLeast(0),
                edgePadding = 0.dp,
                containerColor = Color.Transparent,
                divider = {}
            ) {
                filterOptions.forEach { (key, label) ->
                    val isSelected = selectedFilter == key
                    Tab(
                        selected = isSelected,
                        onClick = { selectedFilter = key },
                        text = {
                            Text(
                                text = label,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) VioletPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                }
            }
        }

        // Achievements List
        items(displayedAchievements, key = { it.id }) { ach ->
            val rarityColor = when (ach.rarity) {
                "UNCOMMON" -> EmeraldAccent
                "RARE" -> CyanAccent
                "EPIC" -> VioletPrimary
                "LEGENDARY" -> GoldAccent
                "MYTHIC" -> RoseAccent
                else -> Color(0xFF94A3B8)
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ach_card_${ach.id}"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (ach.isUnlocked) {
                        MaterialTheme.colorScheme.surface
                    } else {
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.45f)
                    }
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = if (ach.isUnlocked) 2.dp else 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .border(
                            1.dp,
                            if (ach.isUnlocked) rarityColor.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                            RoundedCornerShape(18.dp)
                        )
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Badge Icon
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(
                                    if (ach.isUnlocked) rarityColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .border(
                                    1.dp,
                                    if (ach.isUnlocked) rarityColor else Color.Transparent,
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (ach.isHidden && !ach.isUnlocked) "❓" else ach.iconEmoji,
                                fontSize = 26.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (ach.isHidden && !ach.isUnlocked) "Secret Achievement" else ach.title,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (ach.isUnlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                // Rarity Badge
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(rarityColor.copy(alpha = 0.15f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = ach.rarity,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                        color = rarityColor,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = if (ach.isHidden && !ach.isUnlocked) {
                                    "Perform special discipline acts to discover this secret milestone."
                                } else {
                                    ach.description
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (ach.isUnlocked) 0.9f else 0.6f)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Column(horizontalAlignment = Alignment.End) {
                            Icon(
                                imageVector = if (ach.isUnlocked) Icons.Default.Check else Icons.Default.Lock,
                                contentDescription = if (ach.isUnlocked) "Unlocked" else "Locked",
                                tint = if (ach.isUnlocked) rarityColor else MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "+${ach.xpReward} XP",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = GoldAccent
                            )
                        }
                    }

                    // Progress indicator if locked and has target > 1
                    if (!ach.isUnlocked && ach.targetValue > 1 && !ach.isHidden) {
                        Spacer(modifier = Modifier.height(10.dp))
                        val progressRatio = (ach.currentProgress.toFloat() / ach.targetValue.toFloat()).coerceIn(0f, 1f)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Progress",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${ach.currentProgress} / ${ach.targetValue}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = rarityColor
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { progressRatio },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = rarityColor,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }

                    // Unlock Timestamp
                    if (ach.isUnlocked && ach.unlockedAt != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        val dateStr = SimpleDateFormat("MMM dd, yyyy", Locale.US).format(Date(ach.unlockedAt))
                        Text(
                            text = "Unlocked on $dateStr",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = EmeraldAccent
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
