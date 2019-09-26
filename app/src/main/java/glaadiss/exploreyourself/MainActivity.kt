package glaadiss.exploreyourself

import android.app.Activity
import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.GridView
import android.widget.Spinner
import android.widget.TextView


/**
 * Activity to display package usage statistics.
 */
class MainActivity : Activity(), OnItemSelectedListener {
    override fun onNothingSelected(parent: AdapterView<*>?) {
        // Not implemented
    }

    private var mAdapter: UsageStatsAdapter? = null


//    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
//        super.onActivityResult(requestCode, resultCode, data)
//        API.handleGoogleAuthResponse(requestCode, data)
//
//    }

//    private var alarmMgr: AlarmManager? = null
//    private lateinit var alarmIntent: PendingIntent
//    private fun askForScore() {
//
//        val calendar: Calendar = Calendar.getInstance().apply {
//            timeInMillis = System.currentTimeMillis()
//            set(Calendar.HOUR_OF_DAY, 21)
//        }
//
//
//        alarmMgr = applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
//
//        alarmIntent = Intent(applicationContext, AlarmReceiver::class.java).let { intent ->
//            intent.action = "rate_day"
//            PendingIntent.getBroadcast(applicationContext, 101, intent, PendingIntent.FLAG_CANCEL_CURRENT)
//        }
//
//
//
//        alarmMgr?.setRepeating(
//            AlarmManager.RTC_WAKEUP,
//            if (System.currentTimeMillis() > calendar.timeInMillis)
//                calendar.timeInMillis + AlarmManager.INTERVAL_DAY
//            else calendar.timeInMillis,
//            AlarmManager.INTERVAL_DAY,
//            alarmIntent
//        )
//    }

    /**
     * Called when the activity is first created.
     */
    override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
        setContentView(R.layout.usage_stats)
        API.authenticate(this)
        val statsManager = UsageStatsUtil.createStatsManager()
        prepareStatsList(statsManager)
//        API.rate(4)
        Alarm.setRateDayAlarm()
        Alarm.setSendStatsAlarm()
    }

    private fun prepareStatsList(statsManager: UsageStatsManager){
        val mInflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val typeSpinner = findViewById<Spinner>(R.id.typeSpinner)
        typeSpinner.onItemSelectedListener = this
        val listView = findViewById<GridView>(R.id.pkg_list)
        mAdapter = UsageStatsAdapter()
            .setPackageManager(packageManager)
            .setLayoutInflater(mInflater)
            .setUsageStatsManager(statsManager)
            .init()
        listView.adapter = mAdapter
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