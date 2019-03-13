package com.spyman.telegramconcurs.data_reader

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken



class DataReader() {
    fun readFromAssets(context: Context, assetName: String) =
        Gson().fromJson<List<DataModel>>(
                context.assets.open(assetName).bufferedReader(),
                object : TypeToken<ArrayList<DataModel>>() {}.type
        )
}