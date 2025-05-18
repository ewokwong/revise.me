package com.example.reviseme.services

import com.example.reviseme.utils.NotificationUtils
import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.reviseme.DatabaseProvider
import kotlinx.coroutines.runBlocking
import java.util.Calendar

class NotificationWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        val database = DatabaseProvider.getDatabase(applicationContext)
        val topics = runBlocking {
            database.topicDao().getAllTopics() // Call the suspend function inside runBlocking
        }

        val currentDate = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time

        topics.filter { topic ->
            topic.nextStudyDay?.let { it <= currentDate } == true // Check if the topic is due
        }.forEach { topic ->
            NotificationUtils.showNotification(
                context = applicationContext,
                title = "Study Reminder",
                message = "It's time to study: ${topic.name}"
            )
        }

        return Result.success()
    }
}