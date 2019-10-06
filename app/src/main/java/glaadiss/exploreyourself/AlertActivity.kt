package glaadiss.exploreyourself

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.WindowManager
import android.widget.RatingBar
import android.widget.Toast

fun AlertDialog.setSubmitEnabled(enabled: Boolean) {
    this.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = enabled
}


class AlertActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val rootView = layoutInflater.inflate(R.layout.rate_layout, null, false)
        val ratingBar = rootView.findViewById<RatingBar>(R.id.ratingBar)
        enableFullScreen()
        val dialog = createDialog(rootView, ratingBar)
        dialog.show()
        dialog.setSubmitEnabled(false)
        ratingBar.setOnRatingBarChangeListener { _, rating, _ ->
            dialog.setSubmitEnabled(rating > 0)
        }
    }

    override fun onBackPressed() {
        // block back button action
    }

    private fun createDialog(rootView: View, ratingBar: RatingBar): AlertDialog {
        return AlertDialog.Builder(this@AlertActivity)
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
            .create()
    }

    private fun enableFullScreen(){
        this.window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
            (WindowManager.LayoutParams.FLAG_FULLSCREEN or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
        )
    }
}