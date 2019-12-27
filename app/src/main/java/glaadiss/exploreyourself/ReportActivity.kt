package glaadiss.exploreyourself

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.result.Result
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson

data class Report (val bestApps: List<String>,
                   val worstApps: List<String>,
                   val bestType: String,
                   val worstType: String,
                   val bestDayPart: String,
                   val worstDayPart: String
)


fun getDayPartText(key: String)=
        when(key) {
            "day_part_early_morning" -> "Early Morning"
            "day_part_morning" -> "Morning"
            "day_part_afternoon" -> "Afternoon"
            "day_part_evening" -> "Evening"
            "day_part_night" -> "Night"
            "day_part_noon" -> "Noon"
            else -> ""
        }


class ReportActivity: Activity() {

    private val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when(item.itemId){
            R.id.navigation_home -> {
                println("home")
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                return@OnNavigationItemSelectedListener true
            }
        }
        return@OnNavigationItemSelectedListener false
    }

    fun getLabel(packageName: String): String {
        val appInfo = packageManager.getApplicationInfo(packageName.replaceFirst("app_name_", ""), 0)
        return appInfo.loadLabel(packageManager).toString()
    }

    fun getReportCallback(result: Result<String, FuelError>) =
            when (result) {
                is Result.Failure -> {
                    val ex = result.getException()
                    Log.i("Request-failure", ex.toString())
                    Logger.write("Request-failure $ex")
                }
                is Result.Success -> {
                    val data = result.get()
                    this.runOnUiThread {
                        val report = Gson().fromJson(data, Report::class.java)
                        findViewById<TextView>(R.id.bestApp1).text = getLabel(report.bestApps[0])
                        findViewById<TextView>(R.id.bestApp2).text = getLabel(report.bestApps[1])
                        findViewById<TextView>(R.id.bestApp3).text = getLabel(report.bestApps[2])
                        findViewById<TextView>(R.id.worstApp1).text = getLabel(report.worstApps[0])
                        findViewById<TextView>(R.id.worstApp2).text = getLabel(report.worstApps[1])
                        findViewById<TextView>(R.id.worstApp3).text = getLabel(report.worstApps[2])
                        findViewById<TextView>(R.id.bestDayPart).text = getDayPartText(report.bestDayPart)
                        findViewById<TextView>(R.id.worstDayPart).text = getDayPartText(report.worstDayPart)
                    }
                }
                else -> Logger.write("Request-failure unexpected failure")
            }


    override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
        API.authenticate(this)
        setContentView(R.layout.report)
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigation.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)
        API.getReport(this::getReportCallback)
    }


}