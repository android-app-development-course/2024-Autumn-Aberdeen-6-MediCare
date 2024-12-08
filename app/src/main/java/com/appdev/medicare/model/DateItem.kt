package com.appdev.medicare.model

import java.util.*
import android.os.Parcel
import android.os.Parcelable

// 日期下的数据
data class DateItem(
    val dateIdentifier: Int,            // YYYYMMDD 格式的唯一标识符
    val date: Date,                     // 当前日期
    var medicationData: MutableList<MedicationData>? = null
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readSerializable() as Date,
        parcel.createTypedArrayList(MedicationData.CREATOR)
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(dateIdentifier)
        parcel.writeSerializable(date)
        parcel.writeTypedList(medicationData)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DateItem> {
        override fun createFromParcel(parcel: Parcel): DateItem {
            return DateItem(parcel)
        }

        override fun newArray(size: Int): Array<DateItem?> {
            return arrayOfNulls(size)
        }
    }
}