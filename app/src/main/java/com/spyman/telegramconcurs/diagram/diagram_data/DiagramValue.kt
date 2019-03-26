package com.spyman.telegramconcurs.diagram.diagram_data

import android.os.Parcel
import android.os.Parcelable

data class DiagramValue(val x: Float, val y: Float): Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readFloat(),
            parcel.readFloat())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeFloat(x)
        parcel.writeFloat(y)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DiagramValue> {
        override fun createFromParcel(parcel: Parcel): DiagramValue {
            return DiagramValue(parcel)
        }

        override fun newArray(size: Int): Array<DiagramValue?> {
            return arrayOfNulls(size)
        }
    }
}