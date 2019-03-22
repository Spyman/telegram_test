package com.spyman.telegramconcurs.diagram.diagram_data

import android.os.Parcel
import android.os.Parcelable
import com.spyman.telegramconcurs.diagram.OnValueChangeListener

data class LineDiagramData(val values: MutableList<DiagramValue>, val name: String, val color: Int): Parcelable {
    var visible = true
    set(visible) {
        onVisibleChangeListeners.forEach { it.onChange(visible) }
        field = visible
    }

    private val onVisibleChangeListeners: MutableList<OnValueChangeListener<Boolean>> = mutableListOf()

    fun addVisibilityChangeListener(listener: OnValueChangeListener<Boolean>) {
        onVisibleChangeListeners.add(listener)
    }

    fun removeVisibilityChangeListener(listener: OnValueChangeListener<Boolean>) {
        onVisibleChangeListeners.remove(listener)
    }

    private var _minimumValue: Float? = null
    var minimumValue: Float
        get() = _minimumValue.let {
            it ?: (values.minBy { it.y }?.y?:0f)
        }
        set(value) { _minimumValue = value}

    private var _maximumValue: Float? = null
    var maximumValue: Float
        get() = _maximumValue.let {
            it ?: (values.maxBy { it.y }?.y ?: 0f)
        }
        set(value) { _maximumValue = value}

    constructor(parcel: Parcel) : this(
            TODO("values"),
            parcel.readString(),
            parcel.readInt()) {
        _minimumValue = parcel.readValue(Float::class.java.classLoader) as? Float
        _maximumValue = parcel.readValue(Float::class.java.classLoader) as? Float
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeInt(color)
        parcel.writeValue(_minimumValue)
        parcel.writeValue(_maximumValue)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<LineDiagramData> {
        override fun createFromParcel(parcel: Parcel): LineDiagramData {
            return LineDiagramData(parcel)
        }

        override fun newArray(size: Int): Array<LineDiagramData?> {
            return arrayOfNulls(size)
        }
    }
}
