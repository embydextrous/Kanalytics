package com.github.embydextrous.kanalyticssample.analytics

import com.github.embydextrous.kanalytics.DispatcherResolver
import com.github.embydextrous.kanalyticssample.analytics.dispatchers.DispatcherIds
import javax.inject.Inject
import kotlin.experimental.or

class DispatcherResolverImpl @Inject internal constructor() : DispatcherResolver {

    private val eventDispatcherMap: Map<String, Byte> = mapOf(
        AnalyticConstants.Event.APP_STARTED to DispatcherIds.MIXPANEL,
        AnalyticConstants.Event.BUTTON_CLICKED to (DispatcherIds.MIXPANEL or DispatcherIds.CONSOLE)
    )

    override fun resolveDispatchersForEvent(eventName: String): Byte =
        eventDispatcherMap[eventName] ?: DEFAULT_DISPATCHERS

    companion object {
        private const val DEFAULT_DISPATCHERS = DispatcherIds.MIXPANEL
    }
}
