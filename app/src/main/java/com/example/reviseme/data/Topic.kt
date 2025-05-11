// Topic Data Class

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "topics")
data class Topic(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String,
    val interval: Float = 0.0f,
    val studiedOn: List<Date> = emptyList(),
    val nextStudyDay: Date? = null
)