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

    @Query("DELETE FROM topics WHERE id = :id")
    suspend fun deleteTopic(id: Int)

    @Query("UPDATE topics SET name = :name, description = :description WHERE id = :id")
    suspend fun updateTopic(id: Int, name: String, description: String)
}