import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class TopicViewModelFactory(private val database: AppDatabase) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TopicViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TopicViewModel(database) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}