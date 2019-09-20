package glaadiss.exploreyourself

import android.app.Activity
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.GridView
import android.widget.Spinner
import android.widget.TextView
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.Headers
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import android.app.AlarmManager
import android.app.PendingIntent
import android.os.SystemClock
import java.util.*


private const val RC_SIGN_IN = 9001


/**
 * Activity to display package usage statistics.
 */
class MainActivity : Activity(), OnItemSelectedListener {
    override fun onNothingSelected(parent: AdapterView<*>?) {
        // Not implemented
    }

    private var mUsageStatsManager: UsageStatsManager? = null
    private var mAdapter: UsageStatsAdapter? = null

    private fun requestPermissions() {
        val stats = mUsageStatsManager!!
            .queryUsageStats(UsageStatsManager.INTERVAL_DAILY, 0, System.currentTimeMillis())
        val isEmpty = stats.isEmpty()
        if (isEmpty) {
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        val account = completedTask.getResult(ApiException::class.java)
        Log.d("authh", account?.idToken)

//        Fuel.get("http://192.168.0.17:3000/users").header(Headers.AUTHORIZATION, account?.idToken!!).response { result ->
//            Log.d("authh", result.toString())
//        }
    }

    private var alarmMgr: AlarmManager? = null
    private lateinit var alarmIntent: PendingIntent
    private fun askForScore() {

        val calendar: Calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, 10)
        }

        alarmMgr = applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmIntent = Intent(applicationContext, AlarmReceiver::class.java).let { intent ->
            intent.action = "rate_day"
            PendingIntent.getBroadcast(applicationContext, 101, intent, PendingIntent.FLAG_CANCEL_CURRENT)
        }



        alarmMgr?.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_HOUR,
//            AlarmManager.INTERVAL_DAY,
            alarmIntent
        )
    }

    /**
     * Called when the activity is first created.
     */
    override fun onCreate(icicle: Bundle?) {

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.server_client_id))
            .requestEmail()
            .build()

        val signInIntent = GoogleSignIn.getClient(this, gso).signInIntent

        startActivityForResult(signInIntent, RC_SIGN_IN)


        val mInflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val mPm = packageManager
        super.onCreate(icicle)
        setContentView(R.layout.usage_stats)
        mUsageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        requestPermissions()
        val typeSpinner = findViewById<Spinner>(R.id.typeSpinner)
        typeSpinner.onItemSelectedListener = this
        val listView = findViewById<GridView>(R.id.pkg_list)
        mAdapter = UsageStatsAdapter()
            .setPackageManager(mPm)
            .setLayoutInflater(mInflater)
            .setUsageStatsManager(mUsageStatsManager!!)
            .init()
        listView.adapter = mAdapter
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