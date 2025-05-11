import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Date

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromDateList(value: List<Date>?): String? {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toDateList(value: String?): List<Date>? {
        val type = object : TypeToken<List<Date>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun fromDate(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun toDate(timestamp: Long?): Date? {
        return timestamp?.let { Date(it) }
    }
}