package glaadiss.exploreyourself

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import android.widget.RatingBar
import android.widget.Toast


class AlertActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val rootView = layoutInflater.inflate(R.layout.rate_layout, null, false)
        val ratingBar = rootView.findViewById<RatingBar>(R.id.ratingBar)
        this.window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
            (WindowManager.LayoutParams.FLAG_FULLSCREEN or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
        )

        AlertDialog.Builder(this@AlertActivity)
            .setTitle("Rate the day!")
            .setMessage("Tell us how you feel today using all of those apps!")
            .setView(rootView)
            .setCancelable(false)
            .setPositiveButton("Send Rate") { _, _ ->
                val rating = "Rating is :" + ratingBar.rating
                Toast.makeText(applicationContext, rating, Toast.LENGTH_LONG).show()
                API.rate(ratingBar.rating)
                Handler().postDelayed({ finish() }, 1000)
            }
            .show()
    }

    override fun onBackPressed() {
        // block back button action
    }
}