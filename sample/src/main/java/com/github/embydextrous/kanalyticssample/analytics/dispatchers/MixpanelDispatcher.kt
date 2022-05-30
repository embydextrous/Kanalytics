package com.github.embydextrous.kanalyticssample.analytics.dispatchers

import com.github.embydextrous.kanalytics.Dispatcher
import com.github.embydextrous.kanalytics.data.AnalyticsEvent
import com.github.embydextrous.kanalytics.data.Gender
import com.github.embydextrous.kanalytics.transformer.EmptyKeyTransformer
import com.github.embydextrous.kanalytics.transformer.ReservedKeyTransformer
import com.github.embydextrous.kanalyticssample.analytics.AnalyticConstants
import com.github.embydextrous.kanalyticssample.analytics.AnalyticsDataStore
import com.github.embydextrous.kanalyticssample.analytics.AnalyticsLogger
import com.mixpanel.android.mpmetrics.MixpanelAPI
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MixpanelDispatcher @Inject internal constructor(
    private val mixpanelAPI: MixpanelAPI,
    analyticsDataStore: AnalyticsDataStore
) : Dispatcher(
    id = DISPATCHER_ID,
    name = DISPATCHER_NAME,
    analyticsStore = analyticsDataStore,
    supportsIncrementalProfileProperties = true,
    supportsOnetimeProfileProperties = true,
    shouldAddDefaultEventProperties = false,
    shouldSendDefaultProfileProperties = false,
    eventKeyTransformer = EmptyKeyTransformer(),
    profileKeyTransformer = ReservedKeyTransformer(
        reservedKeysMap
    )
) {
    override val logger = AnalyticsLogger(MixpanelDispatcher::class.java)

    override fun initialize() {
        mixpanelAPI.people.identify(mixpanelAPI.distinctId)
    }

    override fun onAppInstall(referrerProps: Map<String, Any>) {

    }

    override fun pushProfile(profileProperties: Map<String, Any>) {
         logger.d { "Pushed following profile properties to mixpanel: $profileProperties" }
         mixpanelAPI.people.setMap(profileProperties)
    }

    override fun pushEvent(
        eventId: String,
        eventName: String,
        properties: Map<String, Any?>,
        superProperties: Map<String, Any?>,
        pushImmediate: Boolean
    ) {
        mixpanelAPI.trackMap(eventName, properties + superProperties)
        if (pushImmediate) {
            mixpanelAPI.flush()
        }
        logger.d { "Pushed $eventName (event_id = $eventId) with properties $properties" }
    }

    override fun onUserLogin(
        isNewUser: Boolean,
        userId: String,
        phoneNumber: String?,
        email: String?,
        userName: String?,
        gender: Gender?,
        properties: Map<String, Any>
    ) {
        if (isNewUser) {
            mixpanelAPI.alias(userId, null)
        } else {
            mixpanelAPI.identify(userId)
        }
        mixpanelAPI.people.identify(mixpanelAPI.distinctId)
        val profileProperties = mutableMapOf<String, Any>(DISTINCT_ID to userId)
        phoneNumber?.run { profileProperties[PHONE] = phoneNumber }
        email?.run { profileProperties[EMAIL] = email }
        userName?.run { profileProperties[NAME] = userName }
        pushProfile(profileProperties)
    }

    override fun onUserUpdate(
        userId: String,
        phoneNumber: String?,
        email: String?,
        userName: String?,
        gender: Gender?,
        properties: Map<String, Any>
    ) {
        val profileProperties = mutableMapOf<String, Any>(DISTINCT_ID to userId)
        phoneNumber?.run { profileProperties[PHONE] = phoneNumber }
        email?.run { profileProperties[EMAIL] = email }
        userName?.run { profileProperties[NAME] = userName }
        pushProfile(profileProperties)
    }

    override fun pushFcmToken(fcmToken: String) {
        mixpanelAPI.run {
            people.pushRegistrationId = fcmToken
            flush()
        }
    }

    override fun registerIncrementalProfilePropertiesInternal(incrementalProfileProperties: Map<String, Double>) {
        incrementalProfileProperties.forEach { (key, incrementBy) ->
            mixpanelAPI.people.increment(key, incrementBy)
        }
    }

    override fun registerOneTimeProfilePropertiesInternal(oneTimeProfileProperties: Map<String, Any>) {
        mixpanelAPI.people.setOnceMap(oneTimeProfileProperties)
    }

    override fun logout() {
        mixpanelAPI.reset()
    }

    override fun flush() {
        mixpanelAPI.flush()
    }

    override fun transformEventName(analyticsEvent: AnalyticsEvent) = analyticsEvent.eventName

    override fun transformEventProperties(
        analyticsEvent: AnalyticsEvent,
        properties: Map<String, Any?>
    ): Map<String, Any?> = properties

    fun getDistinctId() = mixpanelAPI.distinctId

    companion object {
        const val DISPATCHER_ID: Byte = DispatcherIds.MIXPANEL
        private const val DISPATCHER_NAME = "mixpanel"

        private const val DISTINCT_ID = "\$distinct_id"
        private const val PHONE = "\$phone"
        private const val EMAIL = "\$email"
        private const val NAME = "\$name"

        val reservedKeysMap = mapOf(
            AnalyticConstants.Property.PHONE to PHONE,
            AnalyticConstants.Property.EMAIL to EMAIL,
            AnalyticConstants.Property.NAME to NAME
        )
    }
}
