package glaadiss.exploreyourself

import java.util.*

internal class UsageStatsComparator {
    class AppNameComparator internal constructor(private val mAppLabelList: Map<String, String>) :
        Comparator<AppUsageInfo> {

        override fun compare(a: AppUsageInfo, b: AppUsageInfo): Int {
            return mAppLabelList.getValue(a.packageName).compareTo(mAppLabelList[b.packageName]!!)
        }
    }

    class LastTimeUsedComparator : Comparator<AppUsageInfo> {
        override fun compare(a: AppUsageInfo, b: AppUsageInfo): Int {
                return (b.previousEventTimestamp- a.previousEventTimestamp).toInt()
        }
    }

    class UsageTimeComparator : Comparator<AppUsageInfo> {
        override fun compare(a: AppUsageInfo, b: AppUsageInfo): Int {
            return (b.timeInForeground - a.timeInForeground).toInt()
        }
    }
}
