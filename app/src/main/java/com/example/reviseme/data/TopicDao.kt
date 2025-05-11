// Controller to interact with the database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface TopicDao {
    @Insert
    suspend fun insertTopic(topic: Topic)

    @Query("SELECT * FROM topics")
    suspend fun getAllTopics(): List<Topic>
}