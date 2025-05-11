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
    fun addTopic(name: String, description: String) {
        viewModelScope.launch {
            database.topicDao().insertTopic(Topic(name = name, description = description))
            fetchTopics() // Refresh the list after adding a topic
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
            database.topicDao().updateTopic(topic.id, topic.name, topic.description)
            fetchTopics()
        }
    }

    private fun fetchTopics() {
        viewModelScope.launch {
            _topics.value = database.topicDao().getAllTopics()
        }
    }
}