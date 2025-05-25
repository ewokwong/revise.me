// Controller to interact with the database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import java.util.Date

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

    // Query to iterate topic after studied
    @Query("""
        UPDATE topics 
        SET name = :name, 
            description = :description, 
            studiedOn = :studiedOn, 
            interval = :interval, 
            nextStudyDay = :nextStudyDay 
        WHERE id = :id
    """)
    suspend fun iterateTopic(
        id: Int,
        name: String,
        description: String,
        studiedOn: List<Date>,
        interval: Float,
        nextStudyDay: Date?
    )

    // Query to get topics that are not assigned to any section
    @Query("SELECT * FROM topics WHERE sectionId IS NULL")
    fun getUnassignedTopics(): List<Topic>
}