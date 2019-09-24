package glaadiss.exploreyourself

import android.app.Application
import android.content.Context

class ContextProvider: Application() {
    companion object {
        lateinit var context: Context
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
    }
}