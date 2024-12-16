package com.appdev.medicare.model

data class MedicineBox(
    var medicineBoxID: Int,
    var name: String,
    var type: String,
    var medicineSet: MutableList<MedicationData>?
)