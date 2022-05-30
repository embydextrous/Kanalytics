package com.github.embydextrous.kanalyticssample

import android.app.Application
import android.util.Log
import com.github.embydextrous.kanalytics.AnalyticsManager
import com.github.embydextrous.kanalytics.data.AnalyticsEvent
import com.github.embydextrous.kanalytics.push
import com.github.embydextrous.kanalyticssample.analytics.AnalyticConstants
import dagger.hilt.android.HiltAndroidApp
import java.util.UUID
import javax.inject.Inject

@HiltAndroidApp
class SampleApp : Application() {

    @Inject internal lateinit var analyticsManager: AnalyticsManager

    init {
        Log.d(SampleApp::class.java.canonicalName, "Please replace mixpanel token with your token")
    }

    override fun onCreate() {
        super.onCreate()
        analyticsManager.initialize()
        analyticsManager.registerSuperProperty(
            AnalyticConstants.Property.DUMMY_RANDOM_PROPERTY, UUID.randomUUID().toString()
        )
        val ts = System.currentTimeMillis()

        registerProfileProperties()
        AnalyticsEvent.Builder(AnalyticConstants.Event.APP_STARTED)
            .addIncrementalSuperProfileProperty(
                AnalyticConstants.Property.TOTAL_TIME_APP_START, 1.0
            )
            .addOneTimeSuperProfileProperty(
                AnalyticConstants.Property.FIRST_TIME_APP_START, ts
            )
            .addSuperProfileProperty(
                AnalyticConstants.Property.LAST_TIME_APP_START, ts
            )
            .push(analyticsManager)
    }

    private fun registerProfileProperties() {
        analyticsManager.registerProfileProperties(mapOf(
            AnalyticConstants.Property.NAME to "Kanalytics",
            AnalyticConstants.Property.EMAIL to "arjit.agarwal.1000@gmail.com",
            AnalyticConstants.Property.PHONE to "9930560474"
        ))
    }

    companion object {
        // Please replace with your mixpanel token for testing.
        const val MIXPANEL_TOKEN = "6d38f4955b279eeaf77878ab76215eff"
    }
}
