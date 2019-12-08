package glaadiss.exploreyourself

import android.app.Activity
import android.app.usage.UsageStatsManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.material.bottomnavigation.BottomNavigationView

/**
 * Activity to display package usage statistics.
 */
class MainActivity : Activity(), OnItemSelectedListener {
    override fun onNothingSelected(parent: AdapterView<*>?) {
        // Not implemented
    }

    private val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when(item.itemId){
            R.id.navigation_home -> {
                println("home")
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_report -> {
                println("report")
                val intent = Intent(this, ReportActivity::class.java)
                startActivity(intent)
                return@OnNavigationItemSelectedListener true
            }
        }
        return@OnNavigationItemSelectedListener false
    }

    private var mAdapter: UsageStatsAdapter? = null

    /**
     * Called when the activity is first created.
     */
    override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
        setContentView(R.layout.usage_stats)
        API.authenticate(this)
        enableAlarms()
        val statsManager = UsageStatsUtil.createStatsManager()
        prepareStatsList(statsManager)
        Alarm.setRateDayAlarm()
        Alarm.setSendStatsAlarm()
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigation.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)



// FOR TESTING PURPOSE
//        Alarm.getAlarmIntent(Pair("send_stats", 102)).send()
    }

    private fun enableAlarms() {
        val receiver = ComponentName(applicationContext, AlarmReceiver::class.java)
        applicationContext.packageManager.setComponentEnabledSetting(
            receiver,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == API.RC_SIGN_IN) {
            Toast.makeText(applicationContext, "resultCode: $resultCode", Toast.LENGTH_LONG).show()
            GoogleSignIn.getSignedInAccountFromIntent(data)
                .addOnSuccessListener {
                    Toast.makeText(applicationContext, "Welcome ${it.email}!", Toast.LENGTH_LONG)
                        .show()
                }
                .addOnFailureListener {
                    Toast.makeText(applicationContext, "Can't authorize:  $it", Toast.LENGTH_LONG)
                        .show()
                }
        }
    }

    private fun prepareStatsList(statsManager: UsageStatsManager) {
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