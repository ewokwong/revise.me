package com.example.reviseme

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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RevisemeTheme {
                HomePage()
            }
        }
    }
}

// Home Page
@Composable
fun HomePage() {
    Scaffold(
        topBar = { CustomTopBar() },
        content = { innerPadding ->
            HomeContent(modifier = Modifier.padding(innerPadding))
        }
    )
}

// Top Bar for Home Page
@Composable
fun CustomTopBar() {
    var showDialog by remember { mutableStateOf(false) }
    var topicName by remember { mutableStateOf("") }
    var topicDescription by remember { mutableStateOf("") }

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
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
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
fun HomeContent(modifier: Modifier = Modifier) {
    Text(
        text = "Welcome to Revise.me! Add a topic to get started.",
        modifier = modifier
            .fillMaxSize()
            .padding(top = 32.dp, start = 16.dp, end = 16.dp)
    )
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