package com.github.embydextrous.kanalytics

import com.github.embydextrous.kanalytics.data.AnalyticsEvent
import com.github.embydextrous.kanalytics.data.Gender
import com.github.embydextrous.kanalytics.data.ProfileEvent

/**
 * This interface contains core contract required for analytics.
 * This contains methods common to [AnalyticsManager] and [Dispatcher]
 * @see AnalyticsManager
 * @see Dispatcher
 */
internal interface Analytics {

    /**
     * This method can be used to perform any initialization for [Dispatcher].
     *
     * @see AnalyticsManager.initialize
     */
    fun initialize() {
        // Default No Op
    }

    /**
     * Can be called from the site where app installed is tracked.
     */
    fun onAppInstall(referrerProps: Map<String, Any>) {
        // Default No Op
    }

    /**
     * This method must be used to update a user after sign up/login.
     * Please call this method only once when the user performs sign up/login.
     * Calling it multiple times may lead to identity management issues.
     * In case if you need to update user when it is not sign up/login, call [onUserUpdate] instead.
     *
     * @param isNewUser must be `true` when the user performs sign up in for the first time, else `false`
     * @param userId unique identifier for the user on your platform
     * @param phoneNumber user's phoneNumber with country code, or `null`. For ex. `+910123456789`
     * @param email email of the user, or `null`
     * @param userName name of the user, or `null`
     * @param gender [Gender.MALE] if user is male, [Gender.FEMALE] is user is female, else `null`
     * @param properties key value pairs which will be mapped to profile properties.
     *
     * @see AnalyticsManager.onUserLogin
     */
    fun onUserLogin(
        isNewUser: Boolean,
        userId: String,
        phoneNumber: String?,
        email: String?,
        userName: String?,
        gender: Gender?,
        properties: Map<String, Any>
    )

    /**
     * This method must be used to update a user except at the time of sign up/login.
     * Please don't call this method during sign up/login as it may lead to identity management issues.
     * While performing sign up/login instead call [onUserLogin] which manages identity.
     *
     * @param userId unique identifier for the user on your platform
     * @param phoneNumber user's phoneNumber with country code, or `null`. For ex. `+910123456789`
     * @param email email of the user, or `null`
     * @param userName name of the user, or `null`
     * @param gender [Gender.MALE] if user is male, [Gender.FEMALE] is user is female, else `null`
     * @param properties key value pairs which will be mapped to profile properties.
     *
     * @see AnalyticsManager.onUserUpdate
     */
    fun onUserUpdate(
        userId: String,
        phoneNumber: String?,
        email: String?,
        userName: String?,
        gender: Gender?,
        properties: Map<String, Any>
    )

    /**
     * @param profileEvent [ProfileEvent] to be pushed to [Dispatcher]
     *
     * @see AnalyticsManager.pushProfileEvent
     * @see Dispatcher.pushProfileEvent
     */
    fun pushProfileEvent(profileEvent: ProfileEvent)

    /**
     * @param analyticsEvent [AnalyticsEvent] to be pushed to [Dispatcher]
     * @param pushImmediate instructs dispatcher to push event immediately to respective server
     *
     * @see AnalyticsManager.pushAnalyticsEvent
     * @see Dispatcher.pushAnalyticsEvent
     */
    fun pushAnalyticsEvent(analyticsEvent: AnalyticsEvent, pushImmediate: Boolean = false)

    /**
     * Pushes a Firebase Cloud Messaging registration id to [Dispatcher].
     *
     * @param fcmToken firebase messaging token to be registered for receiving firebase cloud messages
     *
     * @see AnalyticsManager.pushFcmToken
     */
    fun pushFcmToken(fcmToken: String) {
        // Default No Op
    }

    /**
     * Register [oneTimeProfileProperties] to the [Dispatcher] only once.
     * If [Dispatcher.supportsOnetimeProfileProperties] is `false`, [oneTimeProfileProperties] are ignored.
     * If a key in [oneTimeProfileProperties] is already registered, its value will not be updated.
     *
     * @param oneTimeProfileProperties properties to be sent only once to profile
     *
     * @see AnalyticsManager.registerOneTimeProfileProperties
     * @see Dispatcher.registerOneTimeProfileProperties
     */
    fun registerOneTimeProfileProperties(oneTimeProfileProperties: Map<String, Any>)

    /**
     * Register [incrementalProfileProperties] to the [Dispatcher] for incrementing existing value
     * as specified in the key. If the key does not exists, its initial value is assumed to be `0.0`
     * If [Dispatcher.supportsIncrementalProfileProperties] is `false`, [incrementalProfileProperties] are ignored.
     *
     * @param incrementalProfileProperties properties to be sent for incrementing existing properties.
     *
     * @see AnalyticsManager.registerIncrementalProfileProperties
     * @see Dispatcher.registerIncrementalProfileProperties
     */
    fun registerIncrementalProfileProperties(incrementalProfileProperties: Map<String, Double>)

    /**
     * Erases user data for [Dispatcher] if required.
     *
     * @see AnalyticsManager.logout
     */
    fun logout() {
        // Default No Op
    }

    /**
     * Flushed all pending events or profile updates to user
     */
    fun flush()
}
