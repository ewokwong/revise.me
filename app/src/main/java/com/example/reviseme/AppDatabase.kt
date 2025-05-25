// Database for revise.me

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.reviseme.data.Section
import com.example.reviseme.data.SectionDao

@Database(entities = [Topic::class, Section::class], version = 3)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun topicDao(): TopicDao
    abstract fun sectionDao(): SectionDao
}