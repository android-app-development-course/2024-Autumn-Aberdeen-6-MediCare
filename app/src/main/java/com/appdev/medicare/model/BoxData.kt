package com.appdev.medicare.model

import android.os.Parcel
import android.os.Parcelable

data class BoxData(
    var medicineBoxID: Int,
    var name: String,
    var type: String,
    var person: String,
    var medicineSet: MutableList<MedicationData>? = null,
    var remark: String?,
    var picture: String?
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.createTypedArrayList(MedicationData.CREATOR),
        parcel.readString(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(medicineBoxID)
        parcel.writeString(name)
        parcel.writeString(type)
        parcel.writeString(person)
        parcel.writeList(medicineSet)
        parcel.writeString(remark)
        parcel.writeString(picture)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<BoxData> {
        override fun createFromParcel(parcel: Parcel): BoxData {
            return BoxData(parcel)
        }

        override fun newArray(size: Int): Array<BoxData?> {
            return arrayOfNulls(size)
        }
    }
}