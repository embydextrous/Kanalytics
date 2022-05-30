package com.github.embydextrous.kanalytics

import com.github.embydextrous.kanalytics.data.AnalyticsEvent
import com.github.embydextrous.kanalytics.data.Gender
import com.github.embydextrous.kanalytics.data.ProfileEvent
import com.github.embydextrous.kanalytics.data.SuperEvent
import com.github.embydextrous.kanalytics.validation.KeyValidationResult
import com.github.embydextrous.kanalytics.validation.KeyValidator
import java.lang.RuntimeException
import kotlin.experimental.xor

/**
 * This class acts as interface between analytics layer and application layer.
 * Most of its methods call the corresponding methods from [Dispatcher] in [AnalyticsManager.dispatchers]
 *
 * @property dispatchers a list of [Dispatcher] on which events and profile needs to be pushed
 * @property analyticsStore a persistent storage for analytics level data
 * @property dispatcherResolver returns the list of dispatchers on which event needs to be pushed
 * @property commonEventIdProvider to generate event id pair for each event using
 * @property keyValidator a mechanism to validate if keys and event name follows [KeyValidator.keyPattern]
 * @property logger logger
 *
 * @see [Dispatcher]
 * @see [AnalyticsStore]
 * @see [DispatcherResolver]
 * @see [CommonEventIdProvider]
 * @see [com.github.embydextrous.kanalytics.validation.KeyValidationLevel]
 * @see [Logger]
 */
class AnalyticsManager private constructor(
    private val dispatchers: Map<Byte, Dispatcher>,
    private val analyticsStore: AnalyticsStore,
    private val dispatcherResolver: DispatcherResolver,
    private val commonEventIdProvider: CommonEventIdProvider,
    private val keyValidator: KeyValidator,
    private val logger: Logger
) : Analytics {

    companion object {
        private const val ZERO: Byte = 0

        @Volatile
        private var INSTANCE: AnalyticsManager? = null

        fun getInstance(
            dispatchers: Map<Byte, Dispatcher>,
            analyticsStore: AnalyticsStore,
            dispatcherResolver: DispatcherResolver,
            commonEventIdProvider: CommonEventIdProvider,
            keyValidator: KeyValidator,
            logger: Logger
        ): AnalyticsManager {
            logger.d {
                """Initiating ${AnalyticsManager::class.java.simpleName} instance 
                |with following dispatchers: ${dispatchers.values}""".trimMargin()
            }
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AnalyticsManager(
                    dispatchers,
                    analyticsStore,
                    dispatcherResolver,
                    commonEventIdProvider,
                    keyValidator,
                    logger
                ).also {
                    INSTANCE = it
                }
            }
        }
    }

    /**
     * Calls [Dispatcher.initialize] for each [Dispatcher] in [dispatchers]
     *
     * @see Analytics.initialize
     */
    override fun initialize() {
        dispatchers.values.forEach { dispatcher -> dispatcher.initialize() }
    }

    /**
     * Calls [Dispatcher.onAppInstall] for each [Dispatcher] in [dispatchers].
     * Registers [referrerProps] as super properties.
     *
     * @see Analytics.onAppInstall
     */
    override fun onAppInstall(referrerProps: Map<String, Any>) {
        registerSuperProperties(referrerProps)
        dispatchers.values.forEach { dispatcher -> dispatcher.onAppInstall(referrerProps) }
    }

    /**
     * Calls [Dispatcher.onUserLogin] for each [Dispatcher] in [dispatchers].
     * It is recommended to call this function on Application launch is [shallUpdateUserOnLaunch] is `true`
     *
     * @see Analytics.onUserLogin for details.
     */
    override fun onUserLogin(
        isNewUser: Boolean,
        userId: String,
        phoneNumber: String?,
        email: String?,
        userName: String?,
        gender: Gender?,
        properties: Map<String, Any>
    ) {
        logger.d {
            "onUserLogin `isNewUser:$isNewUser`, `userId:$userId`, `phoneNumber:$phoneNumber`," +
                    " `email:$email`, `userName:$userName`, `gender:$gender` and $properties on following " +
                    "dispatchers: ${dispatchers.values.map { it.name }.toList()}"
        }
        dispatchers.values.forEach { dispatcher ->
            dispatcher.onUserLogin(
                isNewUser,
                userId,
                phoneNumber,
                email,
                userName,
                gender,
                properties
            )
        }
    }

    /**
     * Calls [Dispatcher.onUserUpdate] for each [Dispatcher] in [dispatchers].
     * The Application should call this method immediately after user profile is updated on its platform or
     * if [shallUpdateUserOnLaunch] returns `true`.
     *
     * @see Analytics.onUserUpdate for details.
     */
    override fun onUserUpdate(
        userId: String,
        phoneNumber: String?,
        email: String?,
        userName: String?,
        gender: Gender?,
        properties: Map<String, Any>
    ) {
        logger.d {
            "onUserLogin `userId:$userId`, `phoneNumber:$phoneNumber`, `email:$email`, `userName:$userName`, " +
                    "`gender:$gender` and $properties on following " + "dispatchers: ${
                dispatchers.values.map { it.name }.toList()
            }"
        }
        dispatchers.values.forEach { dispatcher ->
            dispatcher.onUserUpdate(
                userId,
                phoneNumber,
                email,
                userName,
                gender,
                properties
            )
        }
    }

    /**
     * This method is responsible for three things:
     *
     * 1. Calls [pushProfileEvent] with [AnalyticsEvent.profileEvent] if it is not `null`.
     * 2. Calls [persistSuperProperties] with [AnalyticsEvent.superEvent] if it is not `null`.
     * 3. Calls [Dispatcher.pushAnalyticsEvent] for every [Dispatcher] found in [dispatchers] for key
     * in [DispatcherResolver.resolveDispatchersForEvent]
     *
     * Please note that [AnalyticsEvent.profileEvent], [AnalyticsEvent.superEvent] is pushed to
     * all [Dispatcher] in [dispatchers] but [analyticsEvent] is pushed only to [Dispatcher] described by [DispatcherResolver.resolveDispatchersForEvent]
     *
     * @see pushProfileEvent
     * @see persistSuperProperties
     *
     * @param analyticsEvent [AnalyticsEvent] to be pushed to [Dispatcher] in [dispatchers]
     */
    override fun pushAnalyticsEvent(analyticsEvent: AnalyticsEvent, pushImmediate: Boolean) {
        // Attach an event id first.
        analyticsEvent.initEventIdentifierPair(commonEventIdProvider.generateCommonEventIdPair())
        when (val keyValidationResult = keyValidator.validateEvent(analyticsEvent)) {
            KeyValidationResult.INVALID_AND_ABORT, KeyValidationResult.INVALID_AND_CONTINUE -> {
                logger.e(RuntimeException("Invalid Keys found in $analyticsEvent"))
                if (keyValidationResult == KeyValidationResult.INVALID_AND_ABORT) {
                    return
                }
            }
            else -> {
            }
        }
        val dispatcherByte = if (analyticsEvent.dispatcherByte == ZERO) {
            dispatcherResolver.resolveDispatchersForEvent(analyticsEvent.eventName)
        } else {
            analyticsEvent.dispatcherByte
        }
        logger.d {
            val dispatchersList = dispatcherByteToDispatchersList(dispatcherByte)
            "Pushing $analyticsEvent on following dispatchers: ${
                dispatchersList.map { it.name }.toList()
            }"
        }
        analyticsEvent.profileEvent?.run {
            pushProfileEvent(analyticsEvent.profileEvent)
        }
        analyticsEvent.superEvent?.run { persistSuperProperties(analyticsEvent.superEvent) }

        var dByte = dispatcherByte
        while (dByte > 0) {
            val firstSetBit = ((dByte - 1).inv() and dByte.toInt()).toByte()
            try {
                val dispatcher = dispatchers.getDispatcher(firstSetBit, analyticsEvent.eventName)
                dispatcher.pushAnalyticsEvent(analyticsEvent, pushImmediate)
                logger.d {
                    "Pushed AnalyticsEvent: ${analyticsEvent.eventName} with eventId: ${analyticsEvent.eventId} " +
                            "on dispatcher: ${dispatcher.name}"
                }
            } catch (e: DispatcherNotFoundException) {
                logger.e(e)
            }
            dByte = dByte xor firstSetBit
        }
    }

    /**
     * Calls [Dispatcher.pushFcmToken] for each [Dispatcher] in [dispatchers]
     * It also marks that FCM token has been updated in [analyticsStore]
     *
     * @see Analytics.pushFcmToken for details.
     */
    override fun pushFcmToken(fcmToken: String) {
        logger.d {
            "Pushed Fcm Token $fcmToken on following dispatchers: ${
                dispatchers.values.map { it.name }.toList()
            }"
        }
        dispatchers.values.forEach { dispatcher -> dispatcher.pushFcmToken(fcmToken) }
        analyticsStore.markFcmTokenUpdated(true)
    }

    /**
     * Calls [Dispatcher.logout] for each [Dispatcher] in [dispatchers]
     * It also clears all the data in [analyticsStore]
     *
     * @see Analytics.logout for details.
     */
    override fun logout() {
        logger.d {
            "Logging out from following dispatchers: ${
                dispatchers.values.map { it.name }.toList()
            }"
        }
        dispatchers.values.forEach { dispatcher -> dispatcher.logout() }
        analyticsStore.clearData()
    }

    /**
     * Calls [Dispatcher.flush] for each [Dispatcher] in [dispatchers]
     *
     * @see Analytics.flush for details.
     */
    override fun flush() {
        logger.d {
            "Flushing all pending events on following dispatchers: ${
                dispatchers.values.map { it.name }.toList()
            }"
        }
        dispatchers.values.forEach { dispatcher -> dispatcher.flush() }
    }

    /**
     * Marks for a need to call [onUserUpdate] to push updated user to [Dispatcher] in [dispatchers]
     * Application should call this method with `true` when the user profile is updated on its platform.
     *
     * @see Analytics.onUserUpdate
     *
     * @param shallUpdateUser to mark if the user needs to be updated.
     */
    fun markUpdateUserOnLaunch(shallUpdateUser: Boolean) {
        analyticsStore.markUserUpdated(shallUpdateUser)
    }

    /**
     * Check if you need to update user profile by calling [onUserUpdate]. If this returns `true`,
     * your application must call [onUserUpdate] as soon as possible.
     *
     * @see Analytics.onUserUpdate
     *
     * @return if user profile needs to be updated using [onUserUpdate]
     */
    fun shallUpdateUserOnLaunch() = analyticsStore.shallUpdateUserOnLaunch()

    /**
     * Check if you need to update FCM token by calling [pushFcmToken]. If this returns `true`,
     * your application must call [pushFcmToken] as soon as possible.
     *
     * @see Analytics.pushFcmToken
     *
     * @return if FCM token needs to be updated using [pushFcmToken]
     */
    fun isFcmTokenUpdated() = analyticsStore.isFcmTokenUpdated()

    /**
     * Pushes [key]:[value] to user profile.
     *
     * @param key
     * @param value
     */
    fun registerProfileProperty(key: String, value: Any) {
        registerProfileProperties(mapOf(key to value))
    }

    /**
     * Pushes [properties] to user profile.
     *
     * @param properties
     */
    fun registerProfileProperties(properties: Map<String, Any>) {
        when (val keyValidationResult = keyValidator.validateProperties(properties)) {
            KeyValidationResult.INVALID_AND_ABORT, KeyValidationResult.INVALID_AND_CONTINUE -> {
                logger.e(RuntimeException("Invalid Keys found in $properties"))
                if (keyValidationResult == KeyValidationResult.INVALID_AND_ABORT) {
                    return
                }
            }
            else -> {
            }
        }
        val profileEvent = ProfileEvent.Builder().addProfileProperties(properties).build()
        pushProfileEvent(profileEvent)
    }

    /**
     * Pushes [key]:[value] to user profile only once.
     * This will be ignored by [Dispatcher] if [Dispatcher.supportsOnetimeProfileProperties] is `false`
     * If [Dispatcher.supportsOnetimeProfileProperties] is `true`, [Dispatcher]'s platform
     * will ignore the update if [key] already exists on it.
     *
     * @see ProfileEvent.oneTimeProfileProperties
     *
     * @param key
     * @param value
     */
    fun registerOneTimeProfileProperty(key: String, value: Any) {
        registerOneTimeProfileProperties(mapOf(key to value))
    }

    /**
     * Pushes [oneTimeProfileProperties] to user profile only once.
     * These [oneTimeProfileProperties] will be ignored by [Dispatcher] if [Dispatcher.supportsOnetimeProfileProperties] is `false`.
     * If [Dispatcher.supportsOnetimeProfileProperties] is `true`, [Dispatcher]'s platform will ignore
     * the update for any `key` in [oneTimeProfileProperties] if it already exists on it.
     *
     * @see ProfileEvent.oneTimeProfileProperties
     *
     * @param oneTimeProfileProperties
     */
    override fun registerOneTimeProfileProperties(oneTimeProfileProperties: Map<String, Any>) {
        when (val keyValidationResult = keyValidator.validateProperties(oneTimeProfileProperties)) {
            KeyValidationResult.INVALID_AND_ABORT, KeyValidationResult.INVALID_AND_CONTINUE -> {
                logger.e(RuntimeException("Invalid Keys found in $oneTimeProfileProperties"))
                if (keyValidationResult == KeyValidationResult.INVALID_AND_ABORT) {
                    return
                }
            }
            else -> {
            }
        }
        val profileEvent =
            ProfileEvent.Builder().addOneTimeProfileProperties(oneTimeProfileProperties).build()
        pushProfileEvent(profileEvent)
    }

    /**
     * Pushes [key]:[value] to user profile incrementally.
     * These will be ignored by [Dispatcher] if [Dispatcher.supportsIncrementalProfileProperties] is `false`.
     * If [Dispatcher.supportsIncrementalProfileProperties] is `true`, [Dispatcher]'s platform will increment
     * the value of [key] by [value] if [key] already exists on it. If [key] does not exist it will add a new [key]:[value] pair.
     *
     * @see ProfileEvent.incrementalProfileProperties
     *
     * @param key
     * @param value
     */
    fun registerIncrementalProfileProperty(key: String, value: Double) {
        registerIncrementalProfileProperties(mapOf(key to value))
    }

    /**
     * Pushes [incrementalProfileProperties] to user profile incrementally.
     * These [incrementalProfileProperties] will be ignored by [Dispatcher] if [Dispatcher.supportsIncrementalProfileProperties] is `false`.
     * If [Dispatcher.supportsIncrementalProfileProperties] is `true`, [Dispatcher]'s platform will increment
     * the value of `key` in [incrementalProfileProperties] by `value` if `key` already exists on it. If `key` does not exist it will add a new `key`:`value` pair.
     *
     * @see ProfileEvent.incrementalProfileProperties
     *
     * @param incrementalProfileProperties
     */
    override fun registerIncrementalProfileProperties(incrementalProfileProperties: Map<String, Double>) {
        when (val keyValidationResult =
            keyValidator.validateProperties(incrementalProfileProperties)) {
            KeyValidationResult.INVALID_AND_ABORT, KeyValidationResult.INVALID_AND_CONTINUE -> {
                logger.e(RuntimeException("Invalid Keys found in $incrementalProfileProperties"))
                if (keyValidationResult == KeyValidationResult.INVALID_AND_ABORT) {
                    return
                }
            }
            else -> {
            }
        }
        val profileEvent =
            ProfileEvent.Builder().addIncrementalProfileProperties(incrementalProfileProperties)
                .build()
        pushProfileEvent(profileEvent)
    }

    /**
     * Registers [key]:[value] pair to be sent with all future events.
     * This will be ignored by [Dispatcher] if [Dispatcher.supportsSuperProperties] is `false`
     * Please note that [SuperEvent.superProperties] are cleared on [logout] or if the app is uninstalled.
     *
     * @see SuperEvent.superProperties
     *
     * @param key
     * @param value
     */
    fun registerSuperProperty(key: String, value: Any) {
        registerSuperProperties(mapOf(key to value))
    }

    /**
     * Registers [superProperties] to be sent with all future events.
     * This will be ignored by [Dispatcher] if [Dispatcher.supportsSuperProperties] is `false`
     * Please note that [SuperEvent.superProperties] are cleared on [logout] or if the app is uninstalled.
     *
     * @see SuperEvent.superProperties
     *
     * @param superProperties
     */
    fun registerSuperProperties(superProperties: Map<String, Any?>) {
        when (val keyValidationResult = keyValidator.validateProperties(superProperties)) {
            KeyValidationResult.INVALID_AND_ABORT, KeyValidationResult.INVALID_AND_CONTINUE -> {
                logger.e(RuntimeException("Invalid Keys found in $superProperties"))
                if (keyValidationResult == KeyValidationResult.INVALID_AND_ABORT) {
                    return
                }
            }
            else -> {
            }
        }
        val superEvent = SuperEvent.Builder().addSuperProperties(superProperties).build()
        persistSuperProperties(superEvent)
    }

    /**
     * Registers [key]:[value] pair to be sent with all future events only once.
     * This will be ignored by [Dispatcher] if [Dispatcher.supportsSuperProperties] is `false`.
     * If [AnalyticsStore.superProperties] already contains [key], this [value] will be ignored.
     *
     * @see SuperEvent.oneTimeSuperProperties
     *
     * @param key
     * @param value
     */
    fun registerOneTimeSuperProperty(key: String, value: Any) {
        registerOneTimeSuperProperties(mapOf(key to value))
    }

    /**
     * Registers [oneTimeSuperProperties] to be sent with all future events only once.
     * This will be ignored by [Dispatcher] if [Dispatcher.supportsSuperProperties] is `false`.
     * If [AnalyticsStore.superProperties] already contains a `key` in [oneTimeSuperProperties], this update will be ignored for that `key`.
     *
     * @see SuperEvent.oneTimeSuperProperties
     *
     * @param oneTimeSuperProperties
     */
    fun registerOneTimeSuperProperties(oneTimeSuperProperties: Map<String, Any>) {
        when (val keyValidationResult = keyValidator.validateProperties(oneTimeSuperProperties)) {
            KeyValidationResult.INVALID_AND_ABORT, KeyValidationResult.INVALID_AND_CONTINUE -> {
                logger.e(RuntimeException("Invalid Keys found in $oneTimeSuperProperties"))
                if (keyValidationResult == KeyValidationResult.INVALID_AND_ABORT) {
                    return
                }
            }
            else -> {
            }
        }
        val superEvent =
            SuperEvent.Builder().addOneTimeSuperProperties(oneTimeSuperProperties).build()
        persistSuperProperties(superEvent)
    }

    /**
     * Registers [key]:[value] pair to be sent with all future events only once incrementally.
     * This will be ignored by [Dispatcher] if [Dispatcher.supportsSuperProperties] is `false`.
     * If [AnalyticsStore.superProperties] already contains [key], this [value] will be
     * added to existing value otherwise [value] will be used.
     *
     * @see SuperEvent.incrementalSuperProperties
     *
     * @param key
     * @param value
     */
    fun registerIncrementalSuperProperty(key: String, value: Double) {
        registerIncrementalSuperProperties(mapOf(key to value))
    }

    /**
     * Registers [incrementalSuperProperties] to be sent with all future events only once incrementally.
     * This will be ignored by [Dispatcher] if [Dispatcher.supportsSuperProperties] is `false`.
     * If [AnalyticsStore.superProperties] already contains `key` in [incrementalSuperProperties],
     * the `value` will be added to existing value otherwise `value` will be used.
     *
     * @see SuperEvent.incrementalSuperProperties
     *
     * @param incrementalSuperProperties
     */
    fun registerIncrementalSuperProperties(incrementalSuperProperties: Map<String, Double>) {
        when (val keyValidationResult =
            keyValidator.validateProperties(incrementalSuperProperties)) {
            KeyValidationResult.INVALID_AND_ABORT, KeyValidationResult.INVALID_AND_CONTINUE -> {
                logger.e(RuntimeException("Invalid Keys found in $incrementalSuperProperties"))
                if (keyValidationResult == KeyValidationResult.INVALID_AND_ABORT) {
                    return
                }
            }
            else -> {
            }
        }
        val superEvent =
            SuperEvent.Builder().addIncrementalSuperProperties(incrementalSuperProperties).build()
        persistSuperProperties(superEvent)
    }

    fun unregisterSuperProperty(property: String) {
        analyticsStore.unregisterSuperProperty(property)
    }

    /**
     * Pushes [ProfileEvent.profileProperties], [ProfileEvent.oneTimeProfileProperties]
     * and [ProfileEvent.incrementalProfileProperties] to [Dispatcher] in [dispatchers]
     *
     * @see Dispatcher.pushProfileEvent
     *
     * @param profileEvent
     */
    override fun pushProfileEvent(profileEvent: ProfileEvent) {
        logger.d {
            "Pushing $profileEvent on following dispatchers: ${
                dispatchers.values.map { it.name }.toList()
            }"
        }
        dispatchers.forEach { (_, dispatcher) -> dispatcher.pushProfileEvent(profileEvent) }
    }

    /**
     * Persists [SuperEvent.superProperties], [SuperEvent.oneTimeSuperProperties]
     * and [SuperEvent.incrementalSuperProperties] to [AnalyticsStore]
     *
     * @param superEvent
     */
    private fun persistSuperProperties(superEvent: SuperEvent) {
        logger.d {
            "Pushing $superEvent on following dispatchers: ${
                dispatchers.values.map { it.name }.toList()
            }"
        }
        logger.d { "Saving superProperties: ${superEvent.superProperties}" }
        analyticsStore.updateSuperProperties(superEvent.superProperties)
        logger.d {
            "Saving oneTimeSuperProperties: " +
                    "${superEvent.oneTimeSuperProperties}"
        }
        analyticsStore.updateOneTimeSuperProperties(superEvent.oneTimeSuperProperties)
        logger.d {
            "Saving incrementalSuperProperties: " +
                    "${superEvent.incrementalSuperProperties}"
        }
        if (superEvent.incrementalSuperProperties.isNotEmpty()) {
            analyticsStore.updateIncrementalSuperProperties(superEvent.incrementalSuperProperties)
        }
    }

    private fun dispatcherByteToDispatchersList(dispatcherByte: Byte): List<Dispatcher> {
        val dispatchersList = mutableListOf<Dispatcher>()
        var dByte = dispatcherByte
        while (dByte > 0) {
            val firstSetBit = ((dByte - 1).inv() and dByte.toInt()).toByte()
            dispatchersList.add((dispatchers[firstSetBit] ?: error("Dispatcher not found")))
            dByte = dByte xor firstSetBit
        }
        return dispatchersList
    }
}
