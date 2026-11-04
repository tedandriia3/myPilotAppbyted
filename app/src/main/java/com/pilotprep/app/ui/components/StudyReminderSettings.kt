package com.pilotprep.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pilotprep.app.util.StudyReminderManager

/**
 * Composant pour configurer les rappels d'étude
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyReminderSettings() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val reminderManager = remember { StudyReminderManager(context) }
    
    var morningEnabled by remember { 
        mutableStateOf(reminderManager.isReminderEnabled(StudyReminderManager.REMINDER_MORNING))
    }
    var morningHour by remember { 
        mutableStateOf(reminderManager.getReminderTime(StudyReminderManager.REMINDER_MORNING)?.first ?: 8)
    }
    var morningMinute by remember { 
        mutableStateOf(reminderManager.getReminderTime(StudyReminderManager.REMINDER_MORNING)?.second ?: 0)
    }
    
    var afternoonEnabled by remember { 
        mutableStateOf(reminderManager.isReminderEnabled(StudyReminderManager.REMINDER_AFTERNOON))
    }
    var afternoonHour by remember { 
        mutableStateOf(reminderManager.getReminderTime(StudyReminderManager.REMINDER_AFTERNOON)?.first ?: 13)
    }
    var afternoonMinute by remember { 
        mutableStateOf(reminderManager.getReminderTime(StudyReminderManager.REMINDER_AFTERNOON)?.second ?: 0)
    }
    
    var eveningEnabled by remember { 
        mutableStateOf(reminderManager.isReminderEnabled(StudyReminderManager.REMINDER_EVENING))
    }
    var eveningHour by remember { 
        mutableStateOf(reminderManager.getReminderTime(StudyReminderManager.REMINDER_EVENING)?.first ?: 20)
    }
    var eveningMinute by remember { 
        mutableStateOf(reminderManager.getReminderTime(StudyReminderManager.REMINDER_EVENING)?.second ?: 0)
    }
    
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Rappels d'étude",
                style = MaterialTheme.typography.titleLarge
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Recevez des notifications pour vous rappeler de réviser",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Rappel du matin
            ReminderSection(
                title = "Matin",
                enabled = morningEnabled,
                hour = morningHour,
                minute = morningMinute,
                onEnabledChange = { 
                    morningEnabled = it
                    reminderManager.scheduleDailyReminder(
                        StudyReminderManager.REMINDER_MORNING,
                        morningHour,
                        morningMinute,
                        it
                    )
                },
                onHourChange = { 
                    morningHour = it
                    if (morningEnabled) {
                        reminderManager.scheduleDailyReminder(
                            StudyReminderManager.REMINDER_MORNING,
                            morningHour,
                            morningMinute,
                            true
                        )
                    }
                },
                onMinuteChange = { 
                    morningMinute = it
                    if (morningEnabled) {
                        reminderManager.scheduleDailyReminder(
                            StudyReminderManager.REMINDER_MORNING,
                            morningHour,
                            morningMinute,
                            true
                        )
                    }
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Rappel de l'après-midi
            ReminderSection(
                title = "Après-midi",
                enabled = afternoonEnabled,
                hour = afternoonHour,
                minute = afternoonMinute,
                onEnabledChange = { 
                    afternoonEnabled = it
                    reminderManager.scheduleDailyReminder(
                        StudyReminderManager.REMINDER_AFTERNOON,
                        afternoonHour,
                        afternoonMinute,
                        it
                    )
                },
                onHourChange = { 
                    afternoonHour = it
                    if (afternoonEnabled) {
                        reminderManager.scheduleDailyReminder(
                            StudyReminderManager.REMINDER_AFTERNOON,
                            afternoonHour,
                            afternoonMinute,
                            true
                        )
                    }
                },
                onMinuteChange = { 
                    afternoonMinute = it
                    if (afternoonEnabled) {
                        reminderManager.scheduleDailyReminder(
                            StudyReminderManager.REMINDER_AFTERNOON,
                            afternoonHour,
                            afternoonMinute,
                            true
                        )
                    }
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Rappel du soir
            ReminderSection(
                title = "Soir",
                enabled = eveningEnabled,
                hour = eveningHour,
                minute = eveningMinute,
                onEnabledChange = { 
                    eveningEnabled = it
                    reminderManager.scheduleDailyReminder(
                        StudyReminderManager.REMINDER_EVENING,
                        eveningHour,
                        eveningMinute,
                        it
                    )
                },
                onHourChange = { 
                    eveningHour = it
                    if (eveningEnabled) {
                        reminderManager.scheduleDailyReminder(
                            StudyReminderManager.REMINDER_EVENING,
                            eveningHour,
                            eveningMinute,
                            true
                        )
                    }
                },
                onMinuteChange = { 
                    eveningMinute = it
                    if (eveningEnabled) {
                        reminderManager.scheduleDailyReminder(
                            StudyReminderManager.REMINDER_EVENING,
                            eveningHour,
                            eveningMinute,
                            true
                        )
                    }
                }
            )
        }
    }
}

@Composable
fun ReminderSection(
    title: String,
    enabled: Boolean,
    hour: Int,
    minute: Int,
    onEnabledChange: (Boolean) -> Unit,
    onHourChange: (Int) -> Unit,
    onMinuteChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = enabled,
            onCheckedChange = onEnabledChange
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
            
            if (enabled) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TimePicker(
                        hour = hour,
                        minute = minute,
                        onHourChange = onHourChange,
                        onMinuteChange = onMinuteChange
                    )
                }
            }
        }
    }
}

@Composable
fun TimePicker(
    hour: Int,
    minute: Int,
    onHourChange: (Int) -> Unit,
    onMinuteChange: (Int) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}")
    }
}




