package glaadiss.exploreyourself

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.*

object Alarm {

    private val rateDateConfig = Pair("rate_day", 101)
    private val sendStatsConfig = Pair("send_stats", 102)

    private fun getCalendar(time: Int = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) =
        Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, time)
        }


    private fun getAlarmManager() =
        ContextProvider.context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    private fun getAlarmIntent(intentInfo: Pair<String, Int>) =
        Intent(ContextProvider.context, AlarmReceiver::class.java).let { intent ->
            intent.action = intentInfo.first
            PendingIntent.getBroadcast(
                ContextProvider.context,
                intentInfo.second,
                intent,
                PendingIntent.FLAG_CANCEL_CURRENT
            )
        }

    private fun setRepeating(alarmManager: AlarmManager, calendar: Calendar, alarmIntent: PendingIntent, intervalTime: Long = AlarmManager.INTERVAL_DAY) =
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            if (System.currentTimeMillis() > calendar.timeInMillis)
                calendar.timeInMillis + intervalTime
            else calendar.timeInMillis,
            intervalTime,
            alarmIntent
        )


    fun setRateDayAlarm() =
        setRepeating(
            getAlarmManager(),
            getCalendar(21),
            getAlarmIntent(rateDateConfig),
            AlarmManager.INTERVAL_DAY

        )

    fun setSendStatsAlarm() =
        setRepeating(
            getAlarmManager(),
            getCalendar(),
            getAlarmIntent(sendStatsConfig),
            AlarmManager.INTERVAL_HOUR
        )

}