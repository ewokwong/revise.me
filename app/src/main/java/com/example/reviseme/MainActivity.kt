package com.example.reviseme

import AppDatabase
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
import androidx.compose.runtime.collectAsState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items


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
            ).build()
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
            Text("+ Topic")
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

    if (topics.isEmpty()) {
        Text(
            text = "No Topics Yet. Add a topic to get started.",
            modifier = modifier
                .fillMaxSize()
                .padding(top = 32.dp, start = 16.dp, end = 16.dp)
        )
    } else {
        LazyColumn(modifier = modifier.padding(16.dp)) {
            items(topics) { topic ->
                Text(text = "${topic.name}: ${topic.description}")
            }
        }
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