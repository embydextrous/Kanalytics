package com.github.embydextrous.kanalytics

import com.github.embydextrous.kanalytics.data.AnalyticsEvent
import com.github.embydextrous.kanalytics.data.ProfileEvent
import com.github.embydextrous.kanalytics.transformer.KeyTransformer

/**
 * Abstract implementation of [Analytics].
 * One need to write concrete implementation of [Dispatcher] for each analytics platform and append it to [AnalyticsManager.dispatchers]
 *
 * @property id a unique byte identifier for dispatcher
 * @property name a unique string identifier for dispatcher for debugging purpose
 * @property analyticsStore a persistent store for analytics level data
 * @property supportsSuperProperties if `true` [com.github.embydextrous.kanalytics.data.SuperEvent] is honored, else ignored.
 * @property supportsIncrementalProfileProperties if `true` [ProfileEvent.incrementalProfileProperties] are honored, else ignored.
 * @property supportsOnetimeProfileProperties if `true` [ProfileEvent.oneTimeProfileProperties] are honored, else ignored.
 * @property shouldAddDefaultEventProperties if `true` [AnalyticsStore.defaultEventProperties] are sent with all events, else they are ignored.
 * @property shouldSendDefaultProfileProperties if `true` [AnalyticsStore.defaultProfileProperties] are pushed to profile, else they are ignored.
 * @property eventKeyTransformer uses this to transform event name and event properties keys to platform level format
 * @property profileKeyTransformer uses this to transform profile properties keys to platform level format
 */
abstract class Dispatcher(
    protected val id: Byte,
    internal val name: String,
    protected val analyticsStore: AnalyticsStore,
    protected val supportsSuperProperties: Boolean = true,
    protected val supportsIncrementalProfileProperties: Boolean = false,
    protected val supportsOnetimeProfileProperties: Boolean = false,
    protected val shouldAddDefaultEventProperties: Boolean = true,
    protected val shouldSendDefaultProfileProperties: Boolean = true,
    protected val eventKeyTransformer: KeyTransformer,
    protected val profileKeyTransformer: KeyTransformer,
) : Analytics {

    abstract val logger: Logger

    /**
     * [Dispatcher] uses this method to actually push [profileProperties] to the underlying analytics platform.
     * Please note this method receives [profileProperties] after applying [profileKeyTransformer]
     *
     * For ex: `MixpanelDispatcher` whose underlying analytics platform is `Mixpanel`'s implementation
     * ```
     * override fun pushProfile(profileProperties: Map<String, Any>) {
     *      mixpanelAPI.people.setMap(profileProperties)
     * }
     * ```
     *
     * @param profileProperties
     */
    protected abstract fun pushProfile(profileProperties: Map<String, Any>)

    /**
     * [Dispatcher] uses this method to actually push event to the underlying analytics platform.
     * Please note this method receives [eventName] after applying [eventKeyTransformer]
     * Please note this method receives [AnalyticsStore.defaultEventProperties] appended to [properties]
     * if [shouldSendDefaultProfileProperties] is `true`. Otherwise [AnalyticsStore.defaultEventProperties] are ignored.
     * Please note this method receives [properties] after applying [eventKeyTransformer]
     * Please note this method receives [superProperties] after applying [eventKeyTransformer]
     *
     * For ex: `MixpanelDispatcher` whose underlying analytics platform is `Mixpanel`'s implementation
     * ```
     * override fun pushEvent(
     *          eventName: String,
     *          properties: Map<String, Any?>,
     *          superProperties: Map<String, Any>) {
     *          // Ignoring super properties here as they have been already registered
     *          mixpanelAPI.trackMap(eventName, properties)
     * }
     * ```
     *
     * @param eventId uuid associated with every event
     * @param eventName
     * @param properties
     * @param superProperties
     * @param pushImmediate instructs the underlying platform to flush events immediately
     */
    protected abstract fun pushEvent(
        eventId: String,
        eventName: String,
        properties: Map<String, Any?>,
        superProperties: Map<String, Any?> = emptyMap(),
        pushImmediate: Boolean
    )

    /**
     * Guard method to not to register [oneTimeProfileProperties] in concrete implementations
     * if [supportsOnetimeProfileProperties] is `false`
     *
     * @param oneTimeProfileProperties
     */
    override final fun registerOneTimeProfileProperties(oneTimeProfileProperties: Map<String, Any>) {
        if (supportsOnetimeProfileProperties) {
            logger.d { "Register oneTimeProfileProperties for $this: $oneTimeProfileProperties" }
            registerOneTimeProfilePropertiesInternal(oneTimeProfileProperties)
        } else {
            logger.d { "$this does not support oneTimeProfileProperties" }
        }
    }

    /**
     * Override this method to register [oneTimeProfileProperties] with underying platform.
     */
    protected open fun registerOneTimeProfilePropertiesInternal(oneTimeProfileProperties: Map<String, Any>) {
        TODO("Provide platform implementation here")
    }

    /**
     * Please use this method if analytics platform has in-built support for [ProfileEvent.incrementalProfileProperties]
     * Guard method to not to register [incrementalProfileProperties] in concrete implementations
     * if [supportsIncrementalProfileProperties] is `false`
     *
     * @param incrementalProfileProperties
     */
    override final fun registerIncrementalProfileProperties(incrementalProfileProperties: Map<String, Double>) {
        if (supportsIncrementalProfileProperties) {
            logger.d { "Register incrementalProfileProperties for $this: $incrementalProfileProperties" }
            registerIncrementalProfilePropertiesInternal(incrementalProfileProperties)
        } else {
            logger.d { "$this does not support incrementalProfileProperties" }
        }
    }

    /**
     * Override this method to register [incrementalProfileProperties] with underying platform.
     */
    protected open fun registerIncrementalProfilePropertiesInternal(incrementalProfileProperties: Map<String, Double>) {
        TODO("Provide platform implementation here")
    }

    /**
     * This method has three responsibilities:
     * 1. Calls [pushProfile] for [ProfileEvent.profileProperties] after applying
     * [profileKeyTransformer] using [transformProfileProperties]
     * 2. Calls [registerOneTimeProfileProperties] for [ProfileEvent.oneTimeProfileProperties] after applying
     * [profileKeyTransformer] using [transformProfileProperties].
     * 3. Calls [registerIncrementalProfileProperties] for [ProfileEvent.incrementalProfileProperties] after applying
     * [profileKeyTransformer] using [transformProfileProperties].
     *
     * @param profileEvent
     * @see registerIncrementalProfileProperties
     * @see registerOneTimeProfileProperties
     */
    final override fun pushProfileEvent(profileEvent: ProfileEvent) {
        pushProfile(transformProfileProperties(profileEvent.profileProperties))
        registerOneTimeProfileProperties(transformProfileProperties(profileEvent.oneTimeProfileProperties))
        registerIncrementalProfileProperties(transformProfileProperties(profileEvent.incrementalProfileProperties))
    }

    /**
     * This method calls [pushEvent] after combining properties in [AnalyticsEvent.properties] and
     * [AnalyticsStore.defaultEventProperties] after applying [eventKeyTransformer] to modify keys as per [eventKeyTransformer].
     *
     * Please see [AnalyticsStore.defaultEventProperties] are ignored is [shouldAddDefaultEventProperties] is `false`.
     *
     * It also uses [com.github.embydextrous.kanalytics.data.SuperEvent.superProperties] by calling [AnalyticsStore.superProperties]
     *
     * @param analyticsEvent
     * @see AnalyticsManager.persistSuperProperties
     */
    final override fun pushAnalyticsEvent(analyticsEvent: AnalyticsEvent, pushImmediate: Boolean) {
        val finalEventName = transformEventName(analyticsEvent)
        val properties = mutableMapOf<String, Any?>()
        properties.putAll(analyticsEvent.properties)
        properties += analyticsEvent.eventIdPair
        if (shouldAddDefaultEventProperties) {
            logger.d { "Add default properties: ${analyticsStore.defaultEventProperties}" }
            properties.putAll(analyticsStore.defaultEventProperties)
        }
        val finalProperties = transformEventProperties(analyticsEvent, properties)
        logger.d {
            """Pushing $finalEventName with eventId : ${analyticsEvent.eventId} and 
                |properties: $finalProperties on dispatcher: $id""".trimMargin()
        }
        val finalSuperProperties = if (supportsSuperProperties) {
            transformEventProperties(analyticsEvent, analyticsStore.superProperties)
        } else {
            emptyMap()
        }
        pushEvent(
            analyticsEvent.eventId,
            finalEventName,
            finalProperties,
            finalSuperProperties,
            pushImmediate
        )
    }

    protected open fun transformEventName(analyticsEvent: AnalyticsEvent) =
        eventKeyTransformer.transformKey(analyticsEvent.eventName)

    protected open fun transformEventProperties(
        analyticsEvent: AnalyticsEvent,
        properties: Map<String, Any?>
    ) = eventKeyTransformer.transformProperties(properties)

    protected open fun <T> transformProfileProperties(properties: Map<String, T>) =
        profileKeyTransformer.transformProperties(properties)

    override fun toString(): String = "Dispatcher{" +
            "\n id=$id" +
            "\n name=$name" +
            "\n supportsSuperProperties=$supportsSuperProperties" +
            "\n supportsIncrementalProfileProperties=$supportsIncrementalProfileProperties" +
            "\n supportsOnetimeProfileProperties=$supportsOnetimeProfileProperties" +
            "\n shouldAddDefaultEventProperties=$shouldAddDefaultEventProperties" +
            "\n shouldSendDefaultProfileProperties=$shouldSendDefaultProfileProperties" +
            "\n shouldSendDefaultProfileProperties=$shouldSendDefaultProfileProperties" +
            "\n}"
}
