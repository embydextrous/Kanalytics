package com.github.embydextrous.kanalytics

/** Abstract implementation to provide event id for each event and a property
 * containing [eventIdKey] as key and result of [generateEventId] as value to be sent with
 * [com.github.embydextrous.kanalytics.data.AnalyticsEvent.properties].
 *
 * @see [com.github.embydextrous.kanalytics.data.AnalyticsEvent.initEventIdentifierPair]
 */
abstract class CommonEventIdProvider {
    abstract val eventIdKey: String

    abstract fun generateEventId(): String

    internal fun generateCommonEventIdPair() = Pair(eventIdKey, generateEventId())
}
