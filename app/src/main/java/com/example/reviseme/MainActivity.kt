package com.example.reviseme

import AppDatabase
import com.example.reviseme.services.NotificationWorker
import Topic
import com.example.reviseme.viewmodels.TopicViewModel
import TopicViewModelFactory
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.reviseme.ui.theme.RevisemeTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.OutlinedTextField
import android.content.Context
import androidx.room.Room
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.runtime.collectAsState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.OutlinedButton
import androidx.compose.ui.graphics.Color
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit
import androidx.compose.foundation.BorderStroke
import kotlin.math.ceil
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.reviseme.services.NotificationService
import androidx.work.OneTimeWorkRequestBuilder
import com.example.reviseme.data.Section
import com.example.reviseme.viewmodels.SectionViewModel
import com.example.reviseme.viewmodels.SectionViewModelFactory
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.LaunchedEffect

class MainActivity : ComponentActivity() {
    // Initialise the database
    private val database by lazy { DatabaseProvider.getDatabase(this) }
    private val topicViewModel: TopicViewModel by viewModels {
        TopicViewModelFactory(database)
    }

    private val sectionViewModel: SectionViewModel by viewModels {
        SectionViewModelFactory(database)
    }

    private fun testNotification() {
        val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>().build()
        WorkManager.getInstance(this).enqueue(workRequest)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NotificationService.createNotificationChannel(this)
//        testNotification()
        scheduleDailyNotifications()
        enableEdgeToEdge()

        setContent {
            RevisemeTheme {
                HomePage(topicViewModel, sectionViewModel)
            }
        }
    }

    private fun scheduleDailyNotifications() {
        val currentTime = Calendar.getInstance()
        val targetTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 9)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (before(currentTime)) {
                add(Calendar.DAY_OF_YEAR, 1) // Schedule for the next day if 9 AM has passed
            }
        }

        val initialDelay = targetTime.timeInMillis - currentTime.timeInMillis

        val workRequest = PeriodicWorkRequestBuilder<NotificationWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "DailyStudyNotifications",
            androidx.work.ExistingPeriodicWorkPolicy.REPLACE,
            workRequest
        )
    }
}

// Initialise the database
object DatabaseProvider {
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "app_database"
            ).fallbackToDestructiveMigration()
                .build()
        }
        return INSTANCE!!
    }
}

// Home Page
@Composable
fun HomePage(topicViewModel: TopicViewModel, sectionViewModel: SectionViewModel) {
    Scaffold(
        topBar = { CustomTopBar(
            topicViewModel = topicViewModel,
            sectionViewModel = sectionViewModel
        ) },
        content = { innerPadding ->
            HomeContent(
                modifier = Modifier.padding(innerPadding),
                topicViewModel = topicViewModel,
                sectionViewModel = sectionViewModel
            )
        }
    )
}

// Top Bar for Home Page
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun CustomTopBar(
    topicViewModel: TopicViewModel,
    sectionViewModel: SectionViewModel
) {
    var showDialog by remember { mutableStateOf(false) }
    var topicName by remember { mutableStateOf("") }
    var topicDescription by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var selectedSection by remember { mutableStateOf<Section?>(null) }
    var showSectionDialog by remember { mutableStateOf(false) }
    var sectionName by remember { mutableStateOf("") }
    var showSectionError by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 32.dp, start = 16.dp, end = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Revise.me",
            modifier = Modifier.weight(1f),
            style = androidx.compose.material3.MaterialTheme.typography.titleLarge
        )
        Button(
            onClick = { showSectionDialog = true },
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                containerColor = Color(0xFF1976D2)
            ),
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 4.dp)
        ) {
            Text("+ Add New Section")
        }
        Button(
            onClick = { showDialog = true },
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                containerColor = Color(0xFF388E3C)
            ),
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 4.dp)
        ) {
            Text("+ Add New Topic")
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Add a New Topic") },
            text = {
                Column {
                    OutlinedTextField(
                        value = topicName,
                        onValueChange = { topicName = it },
                        label = { Text("Topic Name") }
                    )
                    OutlinedTextField(
                        value = topicDescription,
                        onValueChange = { topicDescription = it },
                        label = { Text("Topic Description") }
                    )
                    var expanded by remember { mutableStateOf(false) }
                    val sections by sectionViewModel.sections.collectAsState(initial = emptyList())

                    // Dropdown for selecting a section
                    OutlinedTextField(
                        value = selectedSection?.name ?: "Unassigned",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Section") },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                modifier = Modifier.clickable { expanded = !expanded }
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        // Add "Unassigned" as the first option
                        DropdownMenuItem(
                            text = { Text("Unassigned") },
                            onClick = {
                                selectedSection = null
                                expanded = false
                            }
                        )
                        sections.forEach { section ->
                            DropdownMenuItem(
                                text = { Text(section.name) },
                                onClick = {
                                    selectedSection = section
                                    expanded = false
                                }
                            )
                        }
                    }


                    if (showError) {
                        Text(
                            text = "Both fields are required.",
                            color = Color.Red,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (topicName.isBlank() || topicDescription.isBlank()) {
                        showError = true
                    } else {
                        topicViewModel.addTopic(
                            name = topicName,
                            description = topicDescription,
                            sectionId = selectedSection?.id?.toString()
                        )
                        topicName = ""
                        topicDescription = ""
                        selectedSection = null
                        showError = false
                        showDialog = false
                    }
                }) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDialog = false
                    topicName = ""
                    topicDescription = ""
                    selectedSection = null
                    showError = false
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showSectionDialog) {
        AlertDialog(
            onDismissRequest = { showSectionDialog = false },
            title = { Text("Add a New Section") },
            text = {
                Column {
                    OutlinedTextField(
                        value = sectionName,
                        onValueChange = { sectionName = it },
                        label = { Text("Section Name") }
                    )
                    if (showSectionError) {
                        Text(
                            text = "A Section Name is required.",
                            color = Color.Red,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (sectionName.isBlank()) {
                        showSectionError = true
                    } else {
                        sectionViewModel.addSection(sectionName)
                        sectionName = ""
                        showSectionError = false
                        showSectionDialog = false
                    }
                }) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showSectionDialog = false
                    sectionName = ""
                    showSectionError = false
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// Main content for Home Page
@Composable
fun HomeContent(
    modifier: Modifier = Modifier,
    topicViewModel: TopicViewModel,
    sectionViewModel: SectionViewModel
) {
    val topics by topicViewModel.topics.collectAsState(initial = emptyList())
    val sections by sectionViewModel.sections.collectAsState(initial = emptyList())

    LazyColumn(modifier = modifier.padding(16.dp)) {
        // Sort sections alphabetically by name
        sections.sortedBy { it.name }.forEach { section ->
            item {
                SectionContent(
                    section = section,
                    topics = topics.filter { it.sectionId == section.id },
                    topicViewModel = topicViewModel,
                    sectionViewModel = sectionViewModel
                )
            }
        }

        // Handle unassigned topics
        item {
            UnassignedTopics(
                topics = topics.filter { it.sectionId == null },
                topicViewModel = topicViewModel,
                sectionViewModel = sectionViewModel
            )
        }
    }
}

@Composable
fun SectionContent(
    section: Section,
    topics: List<Topic>,
    topicViewModel: TopicViewModel,
    sectionViewModel: SectionViewModel
) {
    var showDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var editedSectionName by remember { mutableStateOf(section.name) }

    androidx.compose.material3.Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { showDialog = true },
        elevation = androidx.compose.material3.CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = section.name,
                style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            if (topics.isEmpty()) {
                Text(
                    text = "No topics",
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            } else {
                val sortedTopics = topics.sortedWith(
                    compareBy<Topic> { topic ->
                        topic.nextStudyDay?.let { nextStudyDay ->
                            val currentDate = Calendar.getInstance().apply {
                                set(Calendar.HOUR_OF_DAY, 0)
                                set(Calendar.MINUTE, 0)
                                set(Calendar.SECOND, 0)
                                set(Calendar.MILLISECOND, 0)
                            }.time

                            val normalizedNextStudyDay = Calendar.getInstance().apply {
                                time = nextStudyDay
                                set(Calendar.HOUR_OF_DAY, 0)
                                set(Calendar.MINUTE, 0)
                                set(Calendar.SECOND, 0)
                                set(Calendar.MILLISECOND, 0)
                            }.time

                            val diffInMillis = normalizedNextStudyDay.time - currentDate.time
                            maxOf(TimeUnit.MILLISECONDS.toDays(diffInMillis).toInt(), 0)
                        } ?: -1 // Place topics with no `nextStudyDay` at the top
                    }.thenBy { it.name } // Then sort alphabetically by name
                )

                if (sortedTopics.isEmpty()) {
                    Text(
                        text = "No topics",
                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                } else {
                    sortedTopics.forEach { topic ->
                        TopicCard(topic = topic, sectionName = section.name, topicViewModel = topicViewModel, sectionViewModel = sectionViewModel)
                    }
                }
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(section.name) },
            text = { Text("What would you like to do with this section?") },
            confirmButton = {
                Row {
                    TextButton(onClick = { showEditDialog = true; showDialog = false }) {
                        Text("Edit")
                    }
                    TextButton(onClick = { showDeleteConfirmation = true; showDialog = false }) {
                        Text("Delete")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Section") },
            text = {
                Column {
                    OutlinedTextField(
                        value = editedSectionName,
                        onValueChange = { editedSectionName = it },
                        label = { Text("Section Name") }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    sectionViewModel.addSection(editedSectionName) // Update section name
                    showEditDialog = false
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Confirm Deletion") },
            text = { Text("Are you sure you want to delete this section?") },
            confirmButton = {
                TextButton(onClick = {
                    sectionViewModel.deleteSection(section)
                    showDeleteConfirmation = false
                }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun UnassignedTopics(
    topics: List<Topic>,
    topicViewModel: TopicViewModel,
    sectionViewModel: SectionViewModel
) {
    Text(
        text = "Unassigned",
        style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(vertical = 8.dp)
    )

    val sortedTopics = topics.sortedWith(
        compareBy<Topic> { topic ->
            topic.nextStudyDay?.let { nextStudyDay ->
                val currentDate = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.time

                val normalizedNextStudyDay = Calendar.getInstance().apply {
                    time = nextStudyDay
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.time

                val diffInMillis = normalizedNextStudyDay.time - currentDate.time
                maxOf(TimeUnit.MILLISECONDS.toDays(diffInMillis).toInt(), 0)
            } ?: -1 // Place topics with no `nextStudyDay` at the top
        }.thenBy { it.name } // Then sort alphabetically by name
    )

    if (sortedTopics.isEmpty()) {
        Text(
            text = "No topics",
            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(vertical = 4.dp)
        )
    } else {
        sortedTopics.forEach { topic ->
            TopicCard(topic = topic, sectionName = "Unassigned", topicViewModel = topicViewModel, sectionViewModel = sectionViewModel)
        }
    }
}

@Composable
fun TopicCard(
    topic: Topic,
    sectionName: String,
    topicViewModel: TopicViewModel,
    sectionViewModel: SectionViewModel
) {
    var showDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var showStudyDialog by remember { mutableStateOf(false) }
    var editedName by remember { mutableStateOf(topic.name) }
    var editedDescription by remember { mutableStateOf(topic.description) }
    var selectedSection by remember { mutableStateOf<Section?>(null) }

    // Calculate days until the next study day
    val daysUntil = topic.nextStudyDay?.let { nextStudyDay ->
        val currentDate = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time

        val normalizedNextStudyDay = Calendar.getInstance().apply {
            time = nextStudyDay
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time

        val diffInMillis = normalizedNextStudyDay.time - currentDate.time
        maxOf(TimeUnit.MILLISECONDS.toDays(diffInMillis).toInt(), 0)
    } ?: 0

    androidx.compose.material3.Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { showDialog = true },
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = Color(0xFFE3F2FD) // Light blue background
        ),
        elevation = androidx.compose.material3.CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = topic.name,
                    style = androidx.compose.material3.MaterialTheme.typography.titleMedium
                )
                Text(
                    text = topic.description,
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (topic.nextStudyDay == null || daysUntil == 0) {
                Button(
                    onClick = {
                        val currentDate = Date()
                        val updatedStudiedOn = topic.studiedOn.toMutableList().apply { add(currentDate) }

                        val updatedInterval = if (topic.interval == 0.0f) 1.0f else topic.interval * 1.5f
                        val roundedInterval = ceil(updatedInterval.toDouble()).toInt()

                        val updatedNextStudyDay = Calendar.getInstance().apply {
                            time = currentDate
                            add(Calendar.DAY_OF_YEAR, roundedInterval)
                        }.time

                        val updatedTopic = topic.copy(
                            studiedOn = updatedStudiedOn,
                            interval = updatedInterval,
                            nextStudyDay = updatedNextStudyDay
                        )
                        topicViewModel.iterateTopic(updatedTopic)
                        showStudyDialog = true
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF228B22)
                    )
                ) {
                    Text("Study Now")
                }
            } else {
                OutlinedButton(
                    onClick = { /* No action needed */ },
                    enabled = false,
                    colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                        containerColor = Color(0xFFE0E0E0),
                        disabledContainerColor = Color(0xFFF5F5F5)
                    ),
                    border = BorderStroke(1.dp, Color.Gray),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    val dayText = if (daysUntil == 1) "1 day" else "$daysUntil days"
                    Text("To be studied in $dayText")
                }
            }
        }
    }

    if (showStudyDialog) {
        val dateFormat = SimpleDateFormat("EEE, dd MMM, yyyy", Locale.getDefault())
        AlertDialog(
            onDismissRequest = { showStudyDialog = false },
            title = { Text("Congratulations!") },
            text = { Text("Your next study day is on: ${topic.nextStudyDay?.let { dateFormat.format(it) } ?: "TBD"}") },
            confirmButton = {
                TextButton(onClick = { showStudyDialog = false }) {
                    Text("OK")
                }
            }
        )
    }

    if (showDialog) {
        val dateFormat = SimpleDateFormat("EEE, dd MMM, yyyy", Locale.getDefault())
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(topic.name) },
            text = {
                Column {
                    Text("Description", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    Text(topic.description)

                    Text("Next Study Date", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
                    Text(topic.nextStudyDay?.let { dateFormat.format(it) } ?: "Not been set")

                    Text("Study History", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
                    if (topic.studiedOn.isEmpty()) {
                        Text("Not been set")
                    } else {
                        topic.studiedOn.forEach { date ->
                            Text("• ${dateFormat.format(date)}")
                        }
                    }
                    Text("Section", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
                    Text(sectionName)
                }
            },
            confirmButton = {
                Row {
                    TextButton(onClick = { showEditDialog = true; showDialog = false }) {
                        Text("Edit")
                    }
                    TextButton(onClick = { showDeleteConfirmation = true; showDialog = false }) {
                        Text("Delete")
                    }
                }
            }
        )
    }

    if (showEditDialog) {
        // Initialize editedName and editedDescription when the dialog is shown
        LaunchedEffect(topic) {
            editedName = topic.name
            editedDescription = topic.description
            selectedSection = sectionViewModel.sections.value.find { it.id == topic.sectionId }
        }

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Topic") },
            text = {
                Column {
                    OutlinedTextField(
                        value = editedName,
                        onValueChange = { editedName = it },
                        label = { Text("Topic Name") }
                    )
                    OutlinedTextField(
                        value = editedDescription,
                        onValueChange = { editedDescription = it },
                        label = { Text("Topic Description") }
                    )

                    // Dropdown for selecting a section
                    var expanded by remember { mutableStateOf(false) }
                    val sections by sectionViewModel.sections.collectAsState(initial = emptyList())

                    OutlinedTextField(
                        value = selectedSection?.name ?: "Unassigned",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Section") },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                modifier = Modifier.clickable { expanded = !expanded }
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        sections.forEach { section ->
                            DropdownMenuItem(
                                text = { Text(section.name) },
                                onClick = {
                                    selectedSection = section
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    topicViewModel.updateTopic(
                        topic.copy(
                            name = editedName,
                            description = editedDescription,
                            sectionId = selectedSection?.id
                        )
                    )
                    showEditDialog = false
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Confirm Deletion") },
            text = { Text("Are you sure you want to delete this topic?") },
            confirmButton = {
                TextButton(onClick = {
                    topicViewModel.deleteTopic(topic)
                    showDeleteConfirmation = false
                }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    RevisemeTheme {
        Greeting("Android")
    }
}

