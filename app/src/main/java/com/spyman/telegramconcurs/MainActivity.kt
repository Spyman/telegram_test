package com.spyman.telegramconcurs

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.spyman.telegramconcurs.data.ThemeVariant
import com.spyman.telegramconcurs.data_reader.DataReader
import com.spyman.telegramconcurs.utils.SharedPrefUtls
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(getThemeVariant())
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        diagram.setData(DataReader().readFromAssets(this, "chart_data.json").first().convertToDiagramValues())
        diagram.setXScale(10f)
        controlView.attachToGraph(diagram)
        legend.attachToGraph(diagram)

        change_theme.setOnClickListener {
            SharedPrefUtls(this).putObject(
                    ThemeVariant(if (getThemeVariant() == R.style.AppTheme) {
                        R.style.DarkTheme
                    } else {
                        R.style.AppTheme
                    })
            )
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    fun getThemeVariant() =
        SharedPrefUtls(this).getObject(ThemeVariant::class.java)?.theme ?: R.style.AppTheme
}
