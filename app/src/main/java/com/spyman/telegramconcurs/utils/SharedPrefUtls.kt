package com.spyman.telegramconcurs.utils

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.text.TextUtils
import com.google.gson.Gson

class SharedPrefUtls(val context: Context) {
    private val defaultSharedPrefsName = "default"
    fun <T> getObject(clazz: Class<T>) =
            getDefaultPref().getString(clazz.name, "").let {
                if (TextUtils.isEmpty(it)) {
                    return@let null
                }
                return@let Gson().fromJson(it, clazz)
            }

    fun putObject(obj: Any) {
        getDefaultPref().edit().putString(obj.javaClass.name, Gson().toJson(obj)).apply()
    }

    private fun getDefaultPref() =
            context.getSharedPreferences(defaultSharedPrefsName, MODE_PRIVATE)
}