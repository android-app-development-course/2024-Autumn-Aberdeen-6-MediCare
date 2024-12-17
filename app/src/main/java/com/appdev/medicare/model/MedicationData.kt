package com.appdev.medicare.model

import android.os.Parcel
import android.os.Parcelable
import java.util.*

data class MedicationData(
    var medicationID: Int,
    var medicationName: String,
    var patientName: String,
    var dosage: String,
    var remainingAmount: String,
    var dailyIntakeFrequency: Int,
    var dailyIntakeTimes: MutableList<String>,
    var weekMode: String,
    var reminderMode: String,
    var expiryDate: String
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readInt(),
        mutableListOf<String>().apply {
            parcel.readStringList(this)
        },
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(medicationID)
        parcel.writeString(medicationName)
        parcel.writeString(patientName)
        parcel.writeString(dosage)
        parcel.writeString(remainingAmount)
        parcel.writeInt(dailyIntakeFrequency)
        parcel.writeStringList(dailyIntakeTimes)
        parcel.writeString(weekMode)
        parcel.writeString(reminderMode)
        parcel.writeString(expiryDate)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<MedicationData> {
        override fun createFromParcel(parcel: Parcel): MedicationData {
            return MedicationData(parcel)
        }

        override fun newArray(size: Int): Array<MedicationData?> {
            return arrayOfNulls(size)
        }
    }

    override fun toString(): String {
        return String.format(
            Locale.getDefault(),
            "Id: %d, Medication: %s, Patient: %s, Dosage: %s, Remaining: %s, Frequency: %d, Times: %s, Week Mode: %s, Reminder Mode: %s, Expiry: %s",
            medicationID,
            medicationName,
            patientName,
            dosage,
            remainingAmount,
            dailyIntakeFrequency,
            dailyIntakeTimes.joinToString(", "),
            weekMode,
            reminderMode,
            expiryDate
        )
    }
}