package glaadiss.exploreyourself

import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.GridView
import android.widget.Spinner
import android.widget.TextView
import java.util.*


/**
 * Activity to display package usage statistics.
 */
class MainActivity : Activity(), OnItemSelectedListener {
    override fun onNothingSelected(parent: AdapterView<*>?) {
        // Not implemented
    }

    private var mAdapter: UsageStatsAdapter? = null

    private fun requestPermissions(statsManager: UsageStatsManager) {
        val stats = statsManager
            .queryUsageStats(UsageStatsManager.INTERVAL_DAILY, 0, System.currentTimeMillis())
        val isEmpty = stats.isEmpty()
        if (isEmpty) {
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        }
    }

//    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
//        super.onActivityResult(requestCode, resultCode, data)
//        API.handleGoogleAuthResponse(requestCode, data)
//
//    }

    private var alarmMgr: AlarmManager? = null
    private lateinit var alarmIntent: PendingIntent
    private fun askForScore() {

        val calendar: Calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, 21)
        }


        alarmMgr = applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        alarmIntent = Intent(applicationContext, AlarmReceiver::class.java).let { intent ->
            intent.action = "rate_day"
            PendingIntent.getBroadcast(applicationContext, 101, intent, PendingIntent.FLAG_CANCEL_CURRENT)
        }



        alarmMgr?.setRepeating(
            AlarmManager.RTC_WAKEUP,
            if (System.currentTimeMillis() > calendar.timeInMillis)
                calendar.timeInMillis + AlarmManager.INTERVAL_DAY
            else calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            alarmIntent
        )
    }

    /**
     * Called when the activity is first created.
     */
    override fun onCreate(icicle: Bundle?) {
//        CustomManager.getInstance()

        val mInflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val mPm = packageManager
        super.onCreate(icicle)
        setContentView(R.layout.usage_stats)
        API.authenticate(this)
        val statsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        requestPermissions(statsManager)
        val typeSpinner = findViewById<Spinner>(R.id.typeSpinner)
        typeSpinner.onItemSelectedListener = this
        val listView = findViewById<GridView>(R.id.pkg_list)
        mAdapter = UsageStatsAdapter()
            .setPackageManager(mPm)
            .setLayoutInflater(mInflater)
            .setUsageStatsManager(statsManager)
            .init()
        listView.adapter = mAdapter
        API.rate(4)
        askForScore()
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        mAdapter!!.sortList(position)
    }

    // View Holder used when displaying views
    internal class AppViewHolder {
        var pkgName: TextView? = null
        var lastTimeUsed: TextView? = null
        var usageTime: TextView? = null
    }


}