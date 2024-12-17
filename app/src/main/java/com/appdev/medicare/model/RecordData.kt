package com.appdev.medicare.model

data class RecordData(
    var medicineRecordID: Int,
    var name: String,
    var patientName: String,
    var recordTime: String,
    var advice: String?,
    var picture: String?,
)