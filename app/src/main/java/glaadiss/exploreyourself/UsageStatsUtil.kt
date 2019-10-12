package glaadiss.exploreyourself

import android.app.AlarmManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import com.google.gson.Gson
import java.util.*

data class Stat(
    val packageName: String,
    val from: Long,
    val to: Long
)

object UsageStatsUtil {
    fun createStatsManager(): UsageStatsManager {
        val statsManager = ContextProvider.context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val stats = statsManager
            .queryUsageStats(UsageStatsManager.INTERVAL_DAILY, 0, System.currentTimeMillis())
        val isEmpty = stats.isEmpty()
        if (isEmpty) {
            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            ContextProvider.context.startActivity(intent)
        }
        return statsManager
    }

    fun fetchStatsData(
        mUsageStatsManager: UsageStatsManager,
        timeAgo: Long = AlarmManager.INTERVAL_HOUR
    ): HashMap<String, AppUsageInfo> {
        val beginTime = System.currentTimeMillis() - timeAgo
        val endTime = System.currentTimeMillis()
        val eventsMap = HashMap<String, AppUsageInfo>()
        val allEvents = ArrayList<UsageEvents.Event>()
        val events = mUsageStatsManager.queryEvents(beginTime, endTime)
        while (events.hasNextEvent()) {
            val currentEvent = UsageEvents.Event()
            events.getNextEvent(currentEvent)
            if (currentEvent.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND || currentEvent.eventType == UsageEvents.Event.MOVE_TO_BACKGROUND) {
                allEvents.add(currentEvent)
                val key = currentEvent.packageName
                if (eventsMap[key] == null)
                    eventsMap[key] = AppUsageInfo(key)
            }
        }

        for (event in allEvents) {
            val usageInfo = eventsMap[event.packageName]!!
            if (event.eventType == UsageEvents.Event.MOVE_TO_BACKGROUND) {
                if (usageInfo.previousEventTimestamp.equals(0)) {
                    usageInfo.previousEventTimestamp = beginTime
                    usageInfo.isInBackground = !usageInfo.isInBackground

                }
                usageInfo.timeInForeground += event.timeStamp - usageInfo.previousEventTimestamp

            }
            usageInfo.previousEventTimestamp = event.timeStamp
            usageInfo.isInBackground = !usageInfo.isInBackground
        }
        return eventsMap
    }

    fun prepareStatsData(eventsMap: HashMap<String, AppUsageInfo>): String {
        val stats = eventsMap.toList()
        val currentTime = System.currentTimeMillis()
        return Gson().toJson(stats.map { (packageName, data) ->
            Stat(
                packageName,
                from = currentTime,
                to = data.timeInForeground + currentTime
            )
        })
    }


}