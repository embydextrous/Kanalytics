package com.github.embydextrous.kanalytics

/**
 * This interface is meant to persist analytics layer data.
 * Please use a disk-based storage like [`android.content.SharedPreferences`](https://developer.android.com/reference/android/content/SharedPreferences).
 *
 * @property defaultEventProperties properties to be attached to all events by default if [Dispatcher.shouldAddDefaultEventProperties] is `true`
 * @property defaultProfileProperties properties to be pushed to profile by default if [Dispatcher.shouldSendDefaultProfileProperties] is `true`
 * @property superProperties properties to be pushed to every event by default if [Dispatcher.supportsSuperProperties] is `true`
 */
interface AnalyticsStore {

    val defaultEventProperties: Map<String, Any?>
    val defaultProfileProperties: Map<String, Any>
    val superProperties: Map<String, Any?>

    /**
     * Updates [superProperties] to existing [com.github.embydextrous.kanalytics.data.SuperEvent.superProperties] in persistent storage.
     *
     * @param superProperties
     */
    fun updateSuperProperties(superProperties: Map<String, Any?>)

    /**
     * Updates [oneTimeSuperProperties] to existing [com.github.embydextrous.kanalytics.data.SuperEvent.oneTimeSuperProperties] in persistent storage.
     *
     * @param oneTimeSuperProperties
     */
    fun updateOneTimeSuperProperties(oneTimeSuperProperties: Map<String, Any?>)

    /**
     * Updates [incrementalSuperProperties] to existing [com.github.embydextrous.kanalytics.data.SuperEvent.incrementalSuperProperties] in persistent storage.
     *
     * @param incrementalSuperProperties
     */
    fun updateIncrementalSuperProperties(incrementalSuperProperties: Map<String, Double>)

    /**
     * Removes the specified property from super property.
     *
     * @param key name of the property to be removed
     */
    fun unregisterSuperProperty(key: String)

    /**
     * Checks if FCM token has been updated.
     *
     * @return `true` if FCM token is updated, else `false`
     */
    fun isFcmTokenUpdated(): Boolean

    /**
     * Mark FCM token updated status. If [value] is `true` it means FCM token updated.
     *
     * @param value
     */
    fun markFcmTokenUpdated(value: Boolean)

    /**
     * Checks if user profile needs to be updated on launch using [AnalyticsManager.onUserUpdate].
     *
     * @return `true` if user profile needs to be updated on launch, else `false`
     */
    fun shallUpdateUserOnLaunch(): Boolean

    /**
     * Mark if you need to update user on launch. If [value] is `true` it means you need to update
     * userProfile on next launch using [AnalyticsManager.onUserUpdate].
     *
     * @param value
     */
    fun markUserUpdated(value: Boolean)

    /**
     * Clears all the data in the persistent storage.
     */
    fun clearData()
}
