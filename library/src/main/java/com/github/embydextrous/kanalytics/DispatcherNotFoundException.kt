package com.github.embydextrous.kanalytics

import java.lang.RuntimeException

/**
 * This exception is thrown when a [Dispatcher] for a given key is not found in [AnalyticsManager.dispatchers].
 */
internal class DispatcherNotFoundException(eventName: String, dispatcherId: Byte) :
    RuntimeException("No dispatcher found with name `$dispatcherId` while sending `$eventName` event")
