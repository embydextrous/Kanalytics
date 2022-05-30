package com.github.embydextrous.kanalyticssample.analytics.dispatchers

import com.github.embydextrous.kanalytics.Dispatcher
import com.github.embydextrous.kanalytics.data.Gender
import com.github.embydextrous.kanalytics.transformer.KeyTransformerUtil
import com.github.embydextrous.kanalyticssample.analytics.AnalyticsDataStore
import com.github.embydextrous.kanalyticssample.analytics.AnalyticsLogger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConsoleDispatcher @Inject internal constructor(
    analyticsDataStore: AnalyticsDataStore
) : Dispatcher(
    id = DISPATCHER_ID,
    name = DISPATCHER_NAME,
    analyticsStore = analyticsDataStore,
    supportsSuperProperties = false,
    supportsIncrementalProfileProperties = false,
    supportsOnetimeProfileProperties = false,
    shouldAddDefaultEventProperties = false,
    shouldSendDefaultProfileProperties = false,
    eventKeyTransformer = KeyTransformerUtil.titleCaseToSnakeCaseTransformer(),
    profileKeyTransformer = KeyTransformerUtil.titleCaseToSnakeCaseTransformer()
) {

    override val logger = AnalyticsLogger(MixpanelDispatcher::class.java)

    override fun initialize() {
        logger.d { "Console Logger : Initialized" }
    }

    override fun onAppInstall(referrerProps: Map<String, Any>) {
        logger.d { "Console Logger : App Installed" }
    }

    override fun pushProfile(profileProperties: Map<String, Any>) {
        logger.d { "Console Logger : Pushed following profile properties: $profileProperties" }
    }

    override fun pushEvent(
        eventId: String,
        eventName: String,
        properties: Map<String, Any?>,
        superProperties: Map<String, Any?>,
        pushImmediate: Boolean
    ) {
        logger.d { "Console Logger : Pushed $eventName (event_id = $eventId) with properties $properties" }
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
        pushProfile(properties)
    }

    override fun onUserUpdate(
        userId: String,
        phoneNumber: String?,
        email: String?,
        userName: String?,
        gender: Gender?,
        properties: Map<String, Any>
    ) {
        pushProfile(properties)
    }

    override fun pushFcmToken(fcmToken: String) {
        logger.d { "Console Dispatcher : Pushed Fcm Token {$fcmToken}" }
    }

    override fun logout() {
        logger.d { "Console Dispatcher : Logged Out" }
    }

    override fun flush() {
        logger.d { "Console Dispatcher : Flushed Pending Events" }
    }

    companion object {
        const val DISPATCHER_ID: Byte = DispatcherIds.CONSOLE
        private const val DISPATCHER_NAME = "console"
    }
}
