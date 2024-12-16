package com.appdev.medicare.model

data class MedicineRecord(
    var medicineRecordID: Int,
    var name: String,
    var patientName: String,
    var recordTime: String
)