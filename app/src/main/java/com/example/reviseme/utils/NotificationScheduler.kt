import android.content.Context
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.reviseme.services.NotificationWorker
import java.util.concurrent.TimeUnit

object NotificationScheduler {
    fun scheduleNotification(context: Context, delayInMillis: Long) {
        val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInitialDelay(delayInMillis, TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(context).enqueue(workRequest)
    }
}