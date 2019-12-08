package glaadiss.exploreyourself

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import com.google.gson.Gson
import java.util.*
import kotlin.collections.ArrayList

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

    private fun getLastTimeRange(): Pair<Long, Long> {
        val millis = System.currentTimeMillis()
        val endTimeCalendar = Calendar.getInstance()
        endTimeCalendar.timeInMillis = millis
        endTimeCalendar.set(Calendar.HOUR, 0)
        endTimeCalendar.set(Calendar.MINUTE, 0)
        endTimeCalendar.set(Calendar.SECOND, 0)
        val endTime = endTimeCalendar.timeInMillis

        val beginTimeCalendar = Calendar.getInstance()
        beginTimeCalendar.timeInMillis = endTime
        beginTimeCalendar.add(Calendar.DATE, -1)
        val beginTime = beginTimeCalendar.timeInMillis

        return Pair(beginTime, endTime)
    }

    private fun getCurrentTimeRange(): Pair<Long, Long> {
        val endTime = System.currentTimeMillis()

        val beginTimeCalendar = Calendar.getInstance()
        beginTimeCalendar.timeInMillis = endTime
        beginTimeCalendar.add(Calendar.DATE, -1)
        val beginTime = beginTimeCalendar.timeInMillis

        return Pair(beginTime, endTime)
    }

    fun fetchStatsDataForExternal(mUsageStatsManager: UsageStatsManager): List<Stat> {
        val (beginTime, endTime) = getLastTimeRange()
        val events = mUsageStatsManager.queryEvents(beginTime, endTime)
        val filteredEvents = ArrayList<UsageEvents.Event>()
        val result = ArrayList<Stat>()

        while (events.hasNextEvent()) {
            val currentEvent = UsageEvents.Event()
            events.getNextEvent(currentEvent)
            if (currentEvent.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND || currentEvent.eventType == UsageEvents.Event.MOVE_TO_BACKGROUND) {
                filteredEvents.add(currentEvent)
            }
        }

        var fromBuffor = beginTime
        filteredEvents.forEach {
            if (it.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                fromBuffor = it.timeStamp
            } else {
                val edge = 3000
                val stat = Stat(
                    it.packageName,
                    from = fromBuffor,
                    to = it.timeStamp
                )
                if (it.timeStamp - fromBuffor > edge && !it.packageName.contains("com.android.launch"))
                    result.add(stat)
            }
        }

        return result

    }

    fun fetchStatsData(mUsageStatsManager: UsageStatsManager, now: Boolean = false): HashMap<String, AppUsageInfo> {
        val (beginTime, endTime) = if (now) getCurrentTimeRange() else getLastTimeRange()
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

    fun prepareStatsData(stats: List<Stat>): String {
        return Gson().toJson(stats)
    }


}