package glaadiss.exploreyourself

import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object Logger {
    private fun file() =
        File(ContextProvider.context.filesDir, "logs.txt")

    fun write(log: String) {
        val sdf = SimpleDateFormat("dd/M/yyyy hh:mm:ss")
        val currentDate = sdf.format(Date())
        FileOutputStream(file(), true).bufferedWriter().use {
            it.append("FILE_LOGS $currentDate - $log \n")
        }
    }

    fun read() =
        file().readText()

}