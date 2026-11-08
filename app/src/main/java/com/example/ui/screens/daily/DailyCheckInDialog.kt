package com.example.ui.screens.daily

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.entities.DailyRecordEntity
import com.example.data.model.MoodType
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.VioletPrimary
import com.example.util.DateUtils

@Composable
fun DailyCheckInDialog(
    date: String,
    existingRecord: DailyRecordEntity?,
    onDismiss: () -> Unit,
    onSave: (
        screenTimeMinutes: Int,
        studyMinutes: Int,
        exerciseMinutes: Int,
        sleepMinutes: Int,
        mood: String,
        productivityRating: Int,
        journalNotes: String
    ) -> Unit
) {
    var screenHours by remember { mutableStateOf((existingRecord?.screenTimeMinutes?.div(60))?.toString() ?: "2") }
    var screenMins by remember { mutableStateOf((existingRecord?.screenTimeMinutes?.rem(60))?.toString() ?: "0") }

    var studyHours by remember { mutableStateOf((existingRecord?.studyMinutes?.div(60))?.toString() ?: "2") }
    var studyMins by remember { mutableStateOf((existingRecord?.studyMinutes?.rem(60))?.toString() ?: "0") }

    var exerciseMins by remember { mutableStateOf((existingRecord?.exerciseMinutes)?.toString() ?: "30") }
    var sleepHours by remember { mutableStateOf((existingRecord?.sleepMinutes?.div(60))?.toString() ?: "8") }

    var mood by remember { mutableStateOf(existingRecord?.mood ?: "Good") }
    var rating by remember { mutableIntStateOf(existingRecord?.productivityRating ?: 4) }
    var notes by remember { mutableStateOf(existingRecord?.journalNotes ?: "") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(24.dp))
                .testTag("daily_checkin_dialog"),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Daily Progress Check-in",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = DateUtils.formatDisplayDate(date),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Mood Selection
                Text(
                    text = "How are you feeling today?",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MoodType.entries.forEach { m ->
                        val isSelected = mood.equals(m.label, ignoreCase = true)
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) VioletPrimary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                )
                                .border(
                                    width = if (isSelected) 1.5.dp else 0.dp,
                                    color = if (isSelected) VioletPrimary else Color.Transparent,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { mood = m.label }
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Text(text = m.emoji, fontSize = 22.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = m.label,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = if (isSelected) VioletPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Productivity Rating (1..5 stars)
                Text(
                    text = "Daily Productivity Rating",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    (1..5).forEach { star ->
                        val isFilled = star <= rating
                        IconButton(onClick = { rating = star }) {
                            Icon(
                                imageVector = if (isFilled) Icons.Filled.Star else Icons.Outlined.Star,
                                contentDescription = "$star stars",
                                tint = if (isFilled) AmberAccent else MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Screen Time & Study Duration inputs
                Text(
                    text = "Metrics Logging",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = screenHours,
                        onValueChange = { screenHours = it },
                        label = { Text("Screen Time (h)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = studyHours,
                        onValueChange = { studyHours = it },
                        label = { Text("Study Time (h)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = exerciseMins,
                        onValueChange = { exerciseMins = it },
                        label = { Text("Exercise (min)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = sleepHours,
                        onValueChange = { sleepHours = it },
                        label = { Text("Sleep (hours)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Reflection Notes
                Text(
                    text = "Personal Notes & Dopamine Triggers",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    placeholder = { Text("What tempted you today? How did your discipline hold up?") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("daily_notes_input"),
                    minLines = 3,
                    maxLines = 5,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = {
                            val sHours = screenHours.toIntOrNull() ?: 0
                            val sMins = screenMins.toIntOrNull() ?: 0
                            val totalScreen = (sHours * 60) + sMins

                            val stHours = studyHours.toIntOrNull() ?: 0
                            val stMins = studyMins.toIntOrNull() ?: 0
                            val totalStudy = (stHours * 60) + stMins

                            val totalExercise = exerciseMins.toIntOrNull() ?: 0
                            val totalSleep = (sleepHours.toIntOrNull() ?: 0) * 60

                            onSave(totalScreen, totalStudy, totalExercise, totalSleep, mood, rating, notes)
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("save_checkin_button")
                    ) {
                        Text("Save Record")
                    }
                }
            }
        }
    }
}
