package com.github.embydextrous.kanalytics

/**
 * This acts as a mechanism to resolve list of [Dispatcher] by using an eventName.
 * The externalization of events will be the responsibility of app.
 */
interface DispatcherResolver {
    fun resolveDispatchersForEvent(eventName: String): Byte
}
