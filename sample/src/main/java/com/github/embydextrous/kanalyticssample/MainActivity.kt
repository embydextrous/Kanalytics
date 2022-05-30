package com.github.embydextrous.kanalyticssample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.github.embydextrous.kanalytics.AnalyticsManager
import com.github.embydextrous.kanalytics.data.AnalyticsEvent
import com.github.embydextrous.kanalytics.push
import com.github.embydextrous.kanalyticssample.R
import com.github.embydextrous.kanalyticssample.analytics.AnalyticConstants
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject internal lateinit var analyticsManager: AnalyticsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.button).setOnClickListener { sendButtonClickEvent(it as Button) }
    }

    private fun sendButtonClickEvent(button: Button) {
        AnalyticsEvent.Builder(AnalyticConstants.Event.BUTTON_CLICKED)
            .addProperty(AnalyticConstants.Property.BUTTON_TEXT, button.text.toString())
            .push(analyticsManager)
    }
}
