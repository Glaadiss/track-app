package glaadiss.exploreyourself

import android.app.AlarmManager
import android.app.usage.UsageStatsManager
import android.content.pm.PackageManager
import android.text.format.DateUtils
import android.util.ArrayMap
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import java.text.DateFormat
import java.util.*


class AppUsageInfo internal constructor(internal var packageName: String) {
    var isInBackground = true
    internal var timeInForeground: Long = 0
    internal var previousEventTimestamp: Long = 0
}


internal class UsageStatsAdapter : BaseAdapter() {
    private var mInflater: LayoutInflater? = null
    private var mPm: PackageManager? = null
    private var mUsageStatsManager: UsageStatsManager? = null
    private val mAppLabelMap = ArrayMap<String, String>()
    private val mPackageStats = ArrayList<AppUsageInfo>()
    private var mDisplayOrder = DISPLAY_ORDER_USAGE_TIME
    private val mLastTimeUsedComparator = UsageStatsComparator.LastTimeUsedComparator()
    private val mUsageTimeComparator = UsageStatsComparator.UsageTimeComparator()
    private var mAppLabelComparator: UsageStatsComparator.AppNameComparator? = null

    fun init(): UsageStatsAdapter? {
        val eventsMap = UsageStatsUtil.fetchStatsData(mUsageStatsManager!!, timeAgo = AlarmManager.INTERVAL_DAY)
        val stats = eventsMap.toList()
        for (stat in stats) {
            val pkgStats = stat.second
            try {
                val appInfo = mPm!!.getApplicationInfo(pkgStats.packageName, 0)
                val label = appInfo.loadLabel(mPm!!).toString()
                mAppLabelMap[pkgStats.packageName] = label

            } catch (e: PackageManager.NameNotFoundException) {
                Log.i(TAG, e.message)
            }
        }

        mPackageStats.addAll(eventsMap.values)
        mAppLabelComparator = UsageStatsComparator.AppNameComparator(mAppLabelMap)
        sortList()
        return this
    }


    override fun getCount(): Int {
        return mPackageStats.size
    }

    override fun getItem(position: Int): Any {
        return mPackageStats[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, cView: View?, parent: ViewGroup): View {
        var convertView = cView
        val holder: MainActivity.AppViewHolder
        if (convertView == null) {
            convertView = mInflater!!.inflate(R.layout.usage_stats_item, null)
            holder = MainActivity.AppViewHolder()
            holder.pkgName = convertView!!.findViewById(R.id.package_name)
            holder.lastTimeUsed = convertView.findViewById(R.id.last_time_used)
            holder.usageTime = convertView.findViewById(R.id.usage_time)
            convertView.tag = holder
        } else {
            holder = convertView.tag as MainActivity.AppViewHolder
        }
        val pkgStats = mPackageStats[position]
        val label = mAppLabelMap[pkgStats.packageName]
        holder.pkgName?.text = label
        holder.lastTimeUsed?.text = DateUtils.formatSameDayTime(
            pkgStats.previousEventTimestamp,
            System.currentTimeMillis(), DateFormat.MEDIUM, DateFormat.MEDIUM
        )
        holder.usageTime?.text = DateUtils.formatElapsedTime(pkgStats.timeInForeground / 1000)
        return convertView
    }

    fun sortList(sortOrder: Int) {
        if (mDisplayOrder == sortOrder) {
            return
        }
        mDisplayOrder = sortOrder
        sortList()
    }

    private fun sortList() {
        when (mDisplayOrder) {
            DISPLAY_ORDER_USAGE_TIME -> Collections.sort(mPackageStats, mUsageTimeComparator)
            DISPLAY_ORDER_LAST_TIME_USED -> Collections.sort(mPackageStats, mLastTimeUsedComparator)
            DISPLAY_ORDER_APP_NAME -> Collections.sort(mPackageStats, mAppLabelComparator)
        }
        notifyDataSetChanged()
    }

    fun setPackageManager(mPm: PackageManager): UsageStatsAdapter {
        this.mPm = mPm
        return this
    }

    fun setLayoutInflater(mInflater: LayoutInflater): UsageStatsAdapter {
        this.mInflater = mInflater
        return this
    }

    fun setUsageStatsManager(mUsageStatsManager: UsageStatsManager): UsageStatsAdapter {
        this.mUsageStatsManager = mUsageStatsManager
        return this
    }

    companion object {
        private const val TAG = "UsageStatsActivity"
        private const val DISPLAY_ORDER_USAGE_TIME = 0
        private const val DISPLAY_ORDER_LAST_TIME_USED = 1
        private const val DISPLAY_ORDER_APP_NAME = 2
    }
}