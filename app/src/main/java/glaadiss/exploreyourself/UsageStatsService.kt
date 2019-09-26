package glaadiss.exploreyourself

import android.app.IntentService
import android.content.Intent
import android.util.Log

class UsageStatsService : IntentService("send_stats") {
    override fun onHandleIntent(intent: Intent?) {
        Log.d("SEND_STATS", "SENDING STATS...")
        API.sendStats(
            UsageStatsUtil.prepareStatsData(
                UsageStatsUtil.fetchStatsData(
                    UsageStatsUtil.createStatsManager()
                )
            )
        )
    }
}