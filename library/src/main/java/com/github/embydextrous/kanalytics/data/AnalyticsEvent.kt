package com.github.embydextrous.kanalytics.data

import com.github.embydextrous.kanalytics.initOnce

/**
 * Wrapper class for sending analytics event via
 * [com.github.embydextrous.kanalytics.AnalyticsManager.pushAnalyticsEvent]
 *
 * @property eventName a unique String identifier for an [AnalyticsEvent]
 * @property properties key-value pairs associated with the event
 * @property superEvent contains superProperties wrapped in [SuperEvent] instance.
 * `null` if no superProperties are being sent with the event.
 * @property profileEvent contains profileProperties wrapped in [ProfileEvent] instance.
 * `null` if no profileProperties are being sent with the event.
 * @property eventId a unique string identifier for an event generated using [java.util.UUID]
 * This will be a common event identifier across platforms like mixpanel and clevertap.
 * @property eventIdPair key value pair for event id generated using
 * [com.github.embydextrous.kanalytics.CommonEventIdProvider.generateCommonEventIdPair]
 * @property dispatcherByte if this is a non-zero value [com.github.embydextrous.kanalytics.AnalyticsManager] will use this to resolve dispatchers
 * instead of [com.github.embydextrous.kanalytics.DispatcherResolver.resolveDispatchersForEvent]. Default value is zero.
 *
 * @see SuperEvent
 * @see ProfileEvent
 * @see com.github.embydextrous.kanalytics.AnalyticsManager.pushAnalyticsEvent
 * @see com.github.embydextrous.kanalytics.Dispatcher.pushAnalyticsEvent
 */
class AnalyticsEvent private constructor(builder: Builder) {
    val eventName: String = builder.eventName
    val properties: Map<String, Any?>
    val superEvent: SuperEvent?
    val profileEvent: ProfileEvent?
    var eventId: String by initOnce()
        private set
    var eventIdPair: Pair<String, String> by initOnce()
        private set
    val dispatcherByte: Byte

    init {
        properties = builder.properties
        superEvent =
            if (builder.superProperties.isEmpty() && builder.incrementalSuperProperties.isEmpty()
                && builder.oneTimeSuperProperties.isEmpty()
            ) {
                null
            } else {
                SuperEvent.Builder()
                    .addSuperProperties(builder.superProperties)
                    .addOneTimeSuperProperties(builder.oneTimeSuperProperties)
                    .addIncrementalSuperProperties(builder.incrementalSuperProperties)
                    .build()
            }
        profileEvent =
            if (builder.profileProperties.isEmpty() && builder.incrementalProfileProperties.isEmpty()
                && builder.oneTimeProfileProperties.isEmpty()
            ) {
                null
            } else {
                ProfileEvent.Builder()
                    .addProfileProperties(builder.profileProperties)
                    .addOneTimeProfileProperties(builder.oneTimeProfileProperties)
                    .addIncrementalProfileProperties(builder.incrementalProfileProperties)
                    .build()
            }
        dispatcherByte = builder.dispatcherByte
    }

    internal fun initEventIdentifierPair(eventIdPair: Pair<String, String>) {
        this.eventIdPair = eventIdPair
        this.eventId = eventIdPair.second
    }

    class Builder constructor(val eventName: String) {

        internal val properties = mutableMapOf<String, Any?>()
        internal val superProperties = mutableMapOf<String, Any>()
        internal val oneTimeSuperProperties = mutableMapOf<String, Any>()
        internal val incrementalSuperProperties = mutableMapOf<String, Double>()
        internal val profileProperties = mutableMapOf<String, Any>()
        internal val oneTimeProfileProperties = mutableMapOf<String, Any>()
        internal val incrementalProfileProperties = mutableMapOf<String, Double>()
        internal var dispatcherByte: Byte = 0

        fun addProperty(key: String, value: Any?): Builder = apply {
            properties[key] = value
        }

        fun addSuperProperty(key: String, value: Any): Builder = apply {
            superProperties[key] = value
        }

        fun addOneTimeSuperProperty(key: String, value: Any): Builder = apply {
            oneTimeSuperProperties[key] = value
        }

        fun addIncrementalSuperProperty(key: String, value: Double): Builder = apply {
            incrementalSuperProperties[key] = value
        }

        fun addProfileProperty(key: String, value: Any): Builder = apply {
            profileProperties[key] = value
        }

        fun addOneTimeProfileProperty(key: String, value: Any): Builder = apply {
            oneTimeProfileProperties[key] = value
        }

        fun addIncrementalProfileProperty(key: String, incrementBy: Double): Builder = apply {
            incrementalProfileProperties[key] = incrementBy
        }

        fun addSuperProfileProperty(key: String, value: Any): Builder = apply {
            superProperties[key] = value
            profileProperties[key] = value
        }

        fun addOneTimeSuperProfileProperty(key: String, value: Any): Builder = apply {
            oneTimeSuperProperties[key] = value
            oneTimeProfileProperties[key] = value
        }

        fun addIncrementalSuperProfileProperty(key: String, value: Double): Builder = apply {
            incrementalSuperProperties[key] = value
            incrementalProfileProperties[key] = value
        }

        fun addProperties(map: Map<String, Any?>): Builder = apply {
            properties.putAll(map)
        }

        fun addSuperProperties(map: Map<String, Any>): Builder = apply {
            superProperties.putAll(map)
        }

        fun addProfileProperties(map: Map<String, Any>): Builder = apply {
            profileProperties.putAll(map)
        }

        fun overrideDispatchers(dispatcherByte: Byte) {
            this.dispatcherByte = dispatcherByte
        }

        fun build(): AnalyticsEvent {
            return AnalyticsEvent(this)
        }
    }

    override fun toString(): String = "AnalyticEvent{" +
            "\n eventName=$eventName" +
            "\n properties=$properties" +
            "\n superEvent=$superEvent" +
            "\n profileEvent=$profileEvent" +
            "\n eventId=$eventId" +
            "\n}"
}
