package glaadiss.exploreyourself

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "rate_day") {
            val i = Intent(context, AlertActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(i)
        }

        if (intent.action == "send_stats") {
            val i = Intent(context, UsageStatsService::class.java)
            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startService(i)
        }

    }
}