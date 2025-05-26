package com.example.reviseme.viewmodels

import AppDatabase
import Topic
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TopicViewModel(private val database: AppDatabase) : ViewModel() {
    private val _topics = MutableStateFlow<List<Topic>>(emptyList())
    val topics: StateFlow<List<Topic>> = _topics.asStateFlow()

    init {
        fetchTopics()
    }

    // Function to add topic
    fun addTopic(name: String, description: String, sectionId: String? = null) {
        viewModelScope.launch {
            val sectionIdInt = sectionId?.toIntOrNull() // Convert String? to Int?
            database.topicDao().insertTopic(Topic(name = name, description = description, sectionId = sectionIdInt))
            fetchTopics()
        }
    }

    // Function to delete topic
    fun deleteTopic(topic: Topic) {
        viewModelScope.launch {
            database.topicDao().deleteTopic(topic.id)
            fetchTopics()
        }
    }

    // Function to update topic
    fun updateTopic(topic: Topic) {
        viewModelScope.launch {
            database.topicDao().updateTopic(
                topic.id,
                topic.name,
                topic.description,
                topic.sectionId
            )
            fetchTopics()
        }
    }

    // Function to iterate topic after studied
    fun iterateTopic(topic: Topic) {
        viewModelScope.launch {
            database.topicDao().iterateTopic(
                topic.id,
                topic.name,
                topic.description,
                topic.studiedOn,
                topic.interval,
                topic.nextStudyDay
            )
            fetchTopics()
        }
    }

    private fun fetchTopics() {
        viewModelScope.launch {
            _topics.value = database.topicDao().getAllTopics()
        }
    }

    // Function to get unassigned topics
    fun getUnassignedTopics(): List<Topic> {
        return _topics.value.filter { it.sectionId == null }
    }
}