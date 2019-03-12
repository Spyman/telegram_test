package com.spyman.telegramconcurs

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.spyman.telegramconcurs.data_reader.DataReader
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        diagram.setData(DataReader().readFromAssets(this, "chart_data.json").first().convertToDiagramValues())
    }
}
