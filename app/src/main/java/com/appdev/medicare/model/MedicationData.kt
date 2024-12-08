package com.appdev.medicare.model

import android.os.Parcel
import android.os.Parcelable
import java.util.*

data class MedicationData(
    var medicationName: String,
    var patientName: String,
    var dosage: String,
    var remainingAmount: Int,
    var dailyIntakeFrequency: Int,
    var dailyIntakeTimes: MutableList<String>,
    var intakeIntervalDays: Int,
    var weekMode: String,
    var reminderMode: String,
    var expiryDate: Date
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readInt(),
        parcel.readInt(),
        mutableListOf<String>().apply {
            parcel.readStringList(this)
        },
        parcel.readInt(),
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readSerializable() as Date
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(medicationName)
        parcel.writeString(patientName)
        parcel.writeString(dosage)
        parcel.writeInt(remainingAmount)
        parcel.writeInt(dailyIntakeFrequency)
        parcel.writeStringList(dailyIntakeTimes)
        parcel.writeInt(intakeIntervalDays)
        parcel.writeString(weekMode)
        parcel.writeString(reminderMode)
        parcel.writeSerializable(expiryDate)
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
            "Medication: %s, Patient: %s, Dosage: %s, Remaining: %d, Frequency: %d, Times: %s, Interval: %d, Week Mode: %s, Reminder Mode: %s, Expiry: %s",
            medicationName,
            patientName,
            dosage,
            remainingAmount,
            dailyIntakeFrequency,
            dailyIntakeTimes.joinToString(", "),
            intakeIntervalDays,
            weekMode,
            reminderMode,
            expiryDate.toString()
        )
    }
}