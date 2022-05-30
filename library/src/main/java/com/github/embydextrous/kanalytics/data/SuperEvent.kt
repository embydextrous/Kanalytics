package com.github.embydextrous.kanalytics.data

/**
 * Wrapper class for persisting `superProperties`
 * Unlike [AnalyticsEvent] these are sent to all [com.github.embydextrous.kanalytics.Dispatcher] instances
 * in [com.github.embydextrous.kanalytics.AnalyticsManager.dispatchers]
 *
 * @see com.github.embydextrous.kanalytics.AnalyticsStore.updateSuperProperties
 *
 * `superProperties` are those properties which once set are sent in all future events.
 * These properties do not survive re-installs, clearing data or [com.github.embydextrous.kanalytics.Analytics.logout]
 * `superProperties` are ignored is [com.github.embydextrous.kanalytics.Dispatcher.supportsSuperProperties] is `false`
 *
 * @property superProperties key-value pairs to be sent in all future events.
 * @property oneTimeSuperProperties key-value pairs to be sent in all future events.
 * However, these can be set only once. If a key in [oneTimeSuperProperties] already exists in
 * [com.github.embydextrous.kanalytics.AnalyticsStore.superProperties], it is ignored.
 * @see com.github.embydextrous.kanalytics.AnalyticsStore.updateOneTimeSuperProperties
 *
 * @property incrementalSuperProperties key-value pairs to be sent in all future events with value incremented.
 * If a key is in [com.github.embydextrous.kanalytics.AnalyticsStore.superProperties], its value is updated by incrementing
 * existing value with the value for key in [incrementalSuperProperties]
 */
class SuperEvent private constructor(builder: Builder) {

    internal val superProperties: Map<String, Any?>
    internal val oneTimeSuperProperties: Map<String, Any?>
    internal val incrementalSuperProperties: Map<String, Double>

    init {
        superProperties = builder.superProperties
        oneTimeSuperProperties = builder.oneTimeSuperProperties
        incrementalSuperProperties = builder.incrementalSuperProperties
    }

    internal class Builder {
        val superProperties = mutableMapOf<String, Any?>()
        val oneTimeSuperProperties = mutableMapOf<String, Any?>()
        val incrementalSuperProperties = mutableMapOf<String, Double>()

        fun addSuperProperty(key: String, value: Any?): Builder = apply {
            superProperties[key] = value
        }

        fun addOneTimeSuperProperty(key: String, value: Any?): Builder = apply {
            oneTimeSuperProperties[key] = value
        }

        fun addIncrementalSuperProperty(key: String, incrementBy: Double): Builder = apply {
            incrementalSuperProperties[key] = incrementBy
        }

        fun addSuperProperties(map: Map<String, Any?>): Builder = apply {
            superProperties.putAll(map)
        }

        internal fun addOneTimeSuperProperties(map: Map<String, Any?>): Builder = apply {
            oneTimeSuperProperties.putAll(map)
        }

        internal fun addIncrementalSuperProperties(map: Map<String, Double>): Builder = apply {
            incrementalSuperProperties.putAll(map)
        }

        fun build() = SuperEvent(this)
    }

    override fun toString(): String = "ProfileEvent{" +
            "\n superProperties=$superProperties" +
            "\n oneTimeSuperProperties=$oneTimeSuperProperties" +
            "\n incrementalSuperProperties=$incrementalSuperProperties" +
            "\n}"
}
