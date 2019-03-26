package com.spyman.telegramconcurs.extentions

import android.content.Context
import android.support.annotation.AttrRes
import android.util.TypedValue

fun Context.getColorFromAttr(
        @AttrRes attrColor: Int,
        typedValue: TypedValue = TypedValue(),
        resolveRefs: Boolean = true
): Int {
    theme.resolveAttribute(attrColor, typedValue, resolveRefs)
    return typedValue.data
}