package com.example.reviseme

import AppDatabase
import Topic
import TopicViewModel
import TopicViewModelFactory
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.OutlinedButton
import androidx.compose.ui.graphics.Color
import java.util.Date
import kotlin.math.floor
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit
import androidx.compose.foundation.BorderStroke


class MainActivity : ComponentActivity() {
    // Initialise the database
    private val database by lazy { DatabaseProvider.getDatabase(this) }
    private val topicViewModel: TopicViewModel by viewModels {
        TopicViewModelFactory(database)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RevisemeTheme {
                HomePage(topicViewModel)
            }
        }
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
fun HomePage(topicViewModel: TopicViewModel) {
    Scaffold(
        topBar = { CustomTopBar(topicViewModel = topicViewModel) },
        content = { innerPadding ->
            HomeContent(modifier = Modifier.padding(innerPadding), topicViewModel = topicViewModel)
        }
    )
}

// Top Bar for Home Page
@Composable
fun CustomTopBar(topicViewModel: TopicViewModel) {
    var showDialog by remember { mutableStateOf(false) }
    var topicName by remember { mutableStateOf("") }
    var topicDescription by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }


    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 32.dp, start = 16.dp, end = 16.dp), // Add top margin
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Revise.me",
            modifier = Modifier.weight(1f),
            style = androidx.compose.material3.MaterialTheme.typography.titleLarge
        )
        Button(onClick = { showDialog = true }) {
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
                    if (showError) {
                        Text(
                            text = "Both fields are required.",
                            color = androidx.compose.ui.graphics.Color.Red,
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
                        topicViewModel.addTopic(topicName, topicDescription)
                        topicName = "" // Clear the topic name field
                        topicDescription = "" // Clear the topic description field
                        showError = false // Reset error state
                        showDialog = false
                    }
                }) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// Main content for Home Page
@Composable
fun HomeContent(modifier: Modifier = Modifier, topicViewModel: TopicViewModel) {
    val topics by topicViewModel.topics.collectAsState(initial = emptyList())

    // Sort topics: null `nextStudyDay` first, then by ascending `nextStudyDay`
    val sortedTopics = topics.sortedWith(
        compareBy<Topic> { it.nextStudyDay != null } // Null values first
            .thenBy { it.nextStudyDay } // Then sort by ascending `nextStudyDay`
    )

    if (sortedTopics.isEmpty()) {
        Text(
            text = "No Topics Yet. Add a topic to get started.",
            modifier = modifier
                .fillMaxSize()
                .padding(top = 32.dp, start = 16.dp, end = 16.dp)
        )
    } else {
        LazyColumn(modifier = modifier.padding(16.dp)) {
            items(sortedTopics) { topic ->
                TopicCard(topic = topic, topicViewModel = topicViewModel)
            }
        }
    }
}

@Composable
fun TopicCard(topic: Topic, topicViewModel: TopicViewModel) {
    var showDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var showStudyDialog by remember { mutableStateOf(false) }
    var editedName by remember { mutableStateOf(topic.name) }
    var editedDescription by remember { mutableStateOf(topic.description) }

    // Calculate daysUntil at the start of the function
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
        maxOf(TimeUnit.MILLISECONDS.toDays(diffInMillis).toInt(), 0) // Clamp to 0 if in the past
    } ?: 0

    androidx.compose.material3.Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { showDialog = true },
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

            // Use daysUntil here
            if (topic.nextStudyDay == null || daysUntil == 0) {
                Button(
                    onClick = {
                        val currentDate = Date()
                        val updatedStudiedOn =
                            topic.studiedOn.toMutableList().apply { add(currentDate) }
                        val updatedInterval =
                            if (topic.interval == 0.0f) 1.0f else topic.interval * 1.5f
                        val roundedInterval = floor(updatedInterval.toDouble()).toInt()
                        val updatedNextStudyDay =
                            Date(currentDate.time + (roundedInterval * 24 * 60 * 60 * 1000).toLong())

                        val updatedTopic = topic.copy(
                            id = topic.id,
                            name = topic.name,
                            description = topic.description,
                            studiedOn = updatedStudiedOn,
                            interval = updatedInterval,
                            nextStudyDay = updatedNextStudyDay
                        )
                        topicViewModel.iterateTopic(updatedTopic)
                        showStudyDialog = true
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF228B22) // Forest Green
                    )
                ) {
                    Text("Study Now")
                }
            } else {
                OutlinedButton(
                    onClick = { /* No action needed */ },
                    enabled = false,
                    colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                        containerColor = Color(0xFFE0E0E0), // Light Gray
                        disabledContainerColor = Color(0xFFF5F5F5) // Slightly lighter gray
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

    // Existing AlertDialog logic for edit and delete actions
    if (showDialog) {
        val dateFormat = SimpleDateFormat("EEE, dd MMM, yyyy", Locale.getDefault())

        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(topic.name) },
            text = {
                Column {
                    Text(
                        text = "Description",
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                    Text(topic.description)

                    Text(
                        text = "Next Study Date",
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Text(topic.nextStudyDay?.let { dateFormat.format(it) } ?: "Not been set")

                    Text(
                        text = "Study History",
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    if (topic.studiedOn.isEmpty()) {
                        Text("Not been set")
                    } else {
                        topic.studiedOn.forEach { date ->
                            Text("• ${dateFormat.format(date)}")
                        }
                    }
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
            },
        )
    }

    if (showEditDialog) {
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
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    topicViewModel.updateTopic(topic.copy(name = editedName, description = editedDescription))
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

