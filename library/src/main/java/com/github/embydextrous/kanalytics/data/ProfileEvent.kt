package com.github.embydextrous.kanalytics.data

/**
 * Wrapper class for sending `profileProperties` via [com.github.embydextrous.kanalytics.AnalyticsManager.pushProfileEvent]
 * Unlike [AnalyticsEvent] these are sent to all [com.github.embydextrous.kanalytics.Dispatcher] instances
 * in [com.github.embydextrous.kanalytics.AnalyticsManager.dispatchers]
 *
 * @see com.github.embydextrous.kanalytics.Dispatcher.pushProfileEvent
 * @see com.github.embydextrous.kanalytics.AnalyticsManager.pushProfileEvent
 *
 * `profileProperties` are those properties which are associated with a profile on a [com.github.embydextrous.kanalytics.Dispatcher].
 *
 * @property profileProperties key-value pairs to be updated for profile.
 * @property oneTimeProfileProperties key-value pairs to be updated for profile.
 * However, these can be set only once. If a key in [oneTimeProfileProperties] already exists on
 * [com.github.embydextrous.kanalytics.Dispatcher]'s backend, it is ignored.
 * Also, if [com.github.embydextrous.kanalytics.Dispatcher.supportsOnetimeProfileProperties] is `false`, these properties are not dispatched.
 *
 * @see com.github.embydextrous.kanalytics.Dispatcher.registerOneTimeProfileProperties
 *
 * @property incrementalProfileProperties key-value pairs to be updated for profile.
 * If a key is in [com.github.embydextrous.kanalytics.Dispatcher]'s backend, its value is updated by incrementing
 * existing value with the value for key in [incrementalProfileProperties]
 * Also, if [com.github.embydextrous.kanalytics.Dispatcher.supportsIncrementalProfileProperties] is `false`, these properties are not dispatched.
 * @see com.github.embydextrous.kanalytics.Dispatcher.registerIncrementalProfileProperties
 */
class ProfileEvent private constructor(builder: Builder) {

    internal val profileProperties: Map<String, Any>
    internal val oneTimeProfileProperties: Map<String, Any>
    internal val incrementalProfileProperties: Map<String, Double>

    init {
        profileProperties = builder.profileProperties
        oneTimeProfileProperties = builder.oneTimeProfileProperties
        incrementalProfileProperties = builder.incrementalProfileProperties
    }

    internal class Builder {
        val profileProperties = mutableMapOf<String, Any>()
        val oneTimeProfileProperties = mutableMapOf<String, Any>()
        val incrementalProfileProperties = mutableMapOf<String, Double>()

        fun addProfileProperty(key: String, value: Any): Builder = apply {
            profileProperties[key] = value
        }

        fun addOneTimeProfileProperty(key: String, value: Any): Builder = apply {
            oneTimeProfileProperties[key] = value
        }

        fun addIncrementalProfileProperty(key: String, incrementBy: Double): Builder = apply {
            incrementalProfileProperties[key] = incrementBy
        }

        fun addProfileProperties(map: Map<String, Any>): Builder = apply {
            profileProperties.putAll(map)
        }

        internal fun addOneTimeProfileProperties(map: Map<String, Any>): Builder = apply {
            oneTimeProfileProperties.putAll(map)
        }

        internal fun addIncrementalProfileProperties(map: Map<String, Double>): Builder = apply {
            incrementalProfileProperties.putAll(map)
        }

        fun build() = ProfileEvent(this)
    }

    override fun toString(): String = "ProfileEvent{" +
            "\n profileProperties=$profileProperties" +
            "\n oneTimeProfileProperties=$oneTimeProfileProperties" +
            "\n incrementalProfileProperties=$incrementalProfileProperties" +
            "\n}"
}
