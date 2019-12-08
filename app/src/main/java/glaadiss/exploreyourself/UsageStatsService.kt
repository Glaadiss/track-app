package glaadiss.exploreyourself

import android.app.IntentService
import android.content.Intent

class UsageStatsService : IntentService("send_stats") {

    override fun onHandleIntent(intent: Intent?) {
        Logger.write("SENDING STATS...")
//        API.sendStats(
//            UsageStatsUtil.prepareStatsData(
//                UsageStatsUtil.fetchStatsData(
//                    UsageStatsUtil.createStatsManager()
//                )
//            )
//        )
    }
}