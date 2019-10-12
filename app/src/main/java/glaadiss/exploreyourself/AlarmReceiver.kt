package glaadiss.exploreyourself

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Logger.write("BroadcastReceiver triggered with action: $intent.action ")

        if (intent.action == "android.intent.action.BOOT_COMPLETED" || intent.action == "android.intent.action.ACTION_BOOT_COMPLETED") {
            Alarm.setRateDayAlarm()
            Alarm.setSendStatsAlarm()
        }

        if (intent.action == "rate_day") {
            val i = Intent(context, AlertActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(i)

        }

        if (intent.action == "send_stats") {
            Logger.write("SENDING STATS...")
            API.sendStats(
                UsageStatsUtil.prepareStatsData(
                    UsageStatsUtil.fetchStatsData(
                        UsageStatsUtil.createStatsManager()
                    )
                )
            )
        }
    }


}