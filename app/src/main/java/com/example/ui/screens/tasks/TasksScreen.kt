package com.example.ui.screens.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.DateSelectorBar
import com.example.ui.components.TaskItemCard
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.FlameOrange
import com.example.ui.theme.RoseAccent
import com.example.ui.theme.VioletPrimary
import com.example.ui.viewmodel.RebootViewModel

@Composable
fun TasksScreen(
    viewModel: RebootViewModel,
    modifier: Modifier = Modifier
) {
    val tasksToday by viewModel.tasksForSelectedDate.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val searchQuery by viewModel.taskSearchQuery.collectAsStateWithLifecycle()
    val selectedCategoryId by viewModel.taskSelectedCategoryId.collectAsStateWithLifecycle()
    val selectedPriority by viewModel.taskSelectedPriority.collectAsStateWithLifecycle()
    val statusFilter by viewModel.taskStatusFilter.collectAsStateWithLifecycle()
    val requiredFilter by viewModel.taskRequiredFilter.collectAsStateWithLifecycle()

    // Filter tasks according to query, classification, status, category, priority
    val filteredTasks = remember(tasksToday, searchQuery, selectedCategoryId, selectedPriority, statusFilter, requiredFilter) {
        tasksToday.filter { item ->
            val matchQuery = searchQuery.isBlank() ||
                    item.task.title.contains(searchQuery, ignoreCase = true) ||
                    item.task.description.contains(searchQuery, ignoreCase = true)

            val matchCategory = selectedCategoryId == null || item.task.categoryId == selectedCategoryId
            val matchPriority = selectedPriority == null || item.task.priority.equals(selectedPriority, ignoreCase = true)

            val matchClassification = when (requiredFilter) {
                "REQUIRED" -> item.task.isRequired
                "OPTIONAL" -> !item.task.isRequired
                else -> true
            }

            val matchStatus = when (statusFilter) {
                "PENDING" -> !item.isCompleted
                "COMPLETED" -> item.isCompleted
                "OVERDUE" -> item.isOverdue && !item.isCompleted
                else -> true
            }

            matchQuery && matchCategory && matchPriority && matchClassification && matchStatus
        }
    }

    val reqCount = remember(tasksToday) { tasksToday.count { it.task.isRequired } }
    val optCount = remember(tasksToday) { tasksToday.count { !it.task.isRequired } }
    val overdueCount = remember(tasksToday) { tasksToday.count { it.isOverdue && !it.isCompleted } }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.editingTask.value = null
                    viewModel.showAddTaskDialog.value = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .padding(bottom = 80.dp)
                    .testTag("add_task_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add New Task",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("tasks_screen"),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "My Tasks & Habits",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        Text(
                            text = "${filteredTasks.size} tasks",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Date selector bar
                    DateSelectorBar(
                        selectedDate = selectedDate,
                        onPrevDay = { viewModel.shiftSelectedDate(-1) },
                        onNextDay = { viewModel.shiftSelectedDate(1) },
                        onToday = { viewModel.resetToToday() },
                        onOpenDatePicker = { viewModel.resetToToday() }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Search input
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.taskSearchQuery.value = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_tasks_input"),
                        placeholder = { Text("Search your tasks & habits...") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.taskSearchQuery.value = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Required vs Optional Filter Tabs
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            "ALL" to "All (${tasksToday.size})",
                            "REQUIRED" to "🔥 Required ($reqCount)",
                            "OPTIONAL" to "Optional ($optCount)"
                        ).forEach { (key, label) ->
                            val isSelected = requiredFilter == key
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (isSelected) {
                                            if (key == "REQUIRED") FlameOrange.copy(alpha = 0.22f) else MaterialTheme.colorScheme.primaryContainer
                                        } else {
                                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                                        }
                                    )
                                    .border(
                                        width = if (isSelected) 1.dp else 0.dp,
                                        color = if (isSelected) (if (key == "REQUIRED") FlameOrange else MaterialTheme.colorScheme.primary) else Color.Transparent,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable { viewModel.taskRequiredFilter.value = key }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    color = if (isSelected) {
                                        if (key == "REQUIRED") FlameOrange else MaterialTheme.colorScheme.onPrimaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Status Tabs (All, Pending, Completed, Overdue)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "ALL" to "All",
                            "PENDING" to "Pending",
                            "COMPLETED" to "Completed",
                            "OVERDUE" to "Overdue"
                        ).forEach { (key, label) ->
                            val isSelected = statusFilter == key
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                    )
                                    .clickable { viewModel.taskStatusFilter.value = key }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    color = if (isSelected) {
                                        if (key == "OVERDUE") RoseAccent else MaterialTheme.colorScheme.onSurface
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Category Filter Scrollable Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val isAllCatSelected = selectedCategoryId == null
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isAllCatSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
                                .clickable { viewModel.taskSelectedCategoryId.value = null }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "All Categories",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isAllCatSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                fontWeight = if (isAllCatSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }

                        categories.forEach { cat ->
                            val isSelected = selectedCategoryId == cat.id
                            val catColor = try {
                                Color(android.graphics.Color.parseColor(cat.colorHex))
                            } catch (_: Exception) {
                                VioletPrimary
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) catColor else catColor.copy(alpha = 0.15f))
                                    .clickable {
                                        viewModel.taskSelectedCategoryId.value = if (isSelected) null else cat.id
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = cat.name,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSelected) Color.White else catColor,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }

            // Task Items
            if (filteredTasks.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (searchQuery.isNotEmpty()) "No tasks match \"$searchQuery\"" else "No tasks found for this filter.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = {
                                    viewModel.taskSearchQuery.value = ""
                                    viewModel.taskSelectedCategoryId.value = null
                                    viewModel.taskSelectedPriority.value = null
                                    viewModel.taskStatusFilter.value = "ALL"
                                    viewModel.taskRequiredFilter.value = "ALL"
                                }
                            ) {
                                Text("Clear Filters")
                            }
                        }
                    }
                }
            } else {
                items(filteredTasks, key = { it.task.id }) { taskWithStatus ->
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

            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}
