package com.appdev.calendarpage.model

import java.util.*

// 日期下的数据
data class DateItem(
    val dateIdentifier: Int,            // YYYYMMDD 格式的唯一标识符
    val date: Date,                     // 当前日期
    var medicationData: MutableList<MedicationData>? = null
)