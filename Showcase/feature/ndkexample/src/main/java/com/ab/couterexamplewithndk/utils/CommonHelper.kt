package com.example.counterapp.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CommonHelper {
    fun formatTimestamp(timestamp: Long): String {
        val sdf = SimpleDateFormat("hh:mm:ss", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

}