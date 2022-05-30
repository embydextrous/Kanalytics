package com.github.embydextrous.kanalytics

import com.github.embydextrous.kanalytics.data.AnalyticsEvent

/**
 * Extension function for getting a [Dispatcher] from [Map] containing [Byte] keys and [Dispatcher] as values.
 *
 * @param dispatcherId id of the dispatcher to retrieve
 * @param eventName name of the associated event
 *
 * @return [Dispatcher] associated with [dispatcherId]
 *
 * @throws DispatcherNotFoundException if there is no [Dispatcher] against the key [dispatcherId]
 */
@Throws(DispatcherNotFoundException::class)
internal fun Map<Byte, Dispatcher>.getDispatcher(dispatcherId: Byte, eventName: String): Dispatcher {
    try {
        return this.getValue(dispatcherId)
    } catch (e: NoSuchElementException) {
        throw DispatcherNotFoundException(eventName, dispatcherId)
    }
}

fun AnalyticsEvent.Builder.push(analyticsManager: AnalyticsManager) {
    analyticsManager.pushAnalyticsEvent(this.build())
}

fun AnalyticsEvent.Builder.pushImmediate(analyticsManager: AnalyticsManager) {
    analyticsManager.pushAnalyticsEvent(this.build(), true)
}
