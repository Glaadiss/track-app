package glaadiss.exploreyourself

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.*

object Alarm {

    private val rateDateConfig = Pair("rate_day", 101)
    private val sendStatsConfig = Pair("send_stats", 102)

    private fun getCalendar(time: Int = Calendar.getInstance().get(Calendar.HOUR_OF_DAY), minute: Int = 0) =
        Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, time)
            set(Calendar.MINUTE, minute)
        }


    private fun getAlarmManager() =
        ContextProvider.context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun getAlarmIntent(intentInfo: Pair<String, Int>, flag: Int = PendingIntent.FLAG_UPDATE_CURRENT) =
        Intent(ContextProvider.context, AlarmReceiver::class.java).let { intent ->
            intent.action = intentInfo.first
//            Logger.write("Intent created for $intentInfo.first")
            PendingIntent.getBroadcast(
                ContextProvider.context,
                intentInfo.second,
                intent,
                flag
            )
        }

    private fun setRepeating(
        config: Pair<String, Int>,
        alarmManager: AlarmManager,
        calendar: Calendar,
        intervalTime: Long = AlarmManager.INTERVAL_DAY
    ) {
//        if (getAlarmIntent(config, PendingIntent.FLAG_NO_CREATE) != null)
//            return

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            if (System.currentTimeMillis() > calendar.timeInMillis)
                calendar.timeInMillis + intervalTime
            else calendar.timeInMillis,
            intervalTime,
            getAlarmIntent(config)
        )
    }


    fun setRateDayAlarm() =
        setRepeating(
            rateDateConfig,
            getAlarmManager(),
            getCalendar(21),
            AlarmManager.INTERVAL_DAY
        )

    fun setSendStatsAlarm() =
        setRepeating(
            sendStatsConfig,
            getAlarmManager(),
            getCalendar(),
            AlarmManager.INTERVAL_HOUR

        )

}