package com.example.notice

import android.media.Image
import android.text.format.Time
import java.util.Date

data class NoticeData(
    val title : String="",
    val image :String="",
    val date : String="",
    val time : String="",
    val key : String?=""
    )
