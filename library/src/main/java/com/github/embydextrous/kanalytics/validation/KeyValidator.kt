package com.github.embydextrous.kanalytics.validation

import com.github.embydextrous.kanalytics.data.AnalyticsEvent
import java.util.regex.Pattern

/**
 * A class to validate if the event name and property keys follow the pattern defined by
 * regular expression [keyPattern].
 *
 * @property keyPattern A regular expression specifying how consumer is naming events and property keys
 * @property keyValidationLevel
 *
 * @see KeyValidationLevel
 */
class KeyValidator(
    private val keyPattern: Pattern,
    private val keyValidationLevel: KeyValidationLevel = KeyValidationLevel.NONE
) {

    internal fun validateEvent(analyticsEvent: AnalyticsEvent): KeyValidationResult {
        return when (keyValidationLevel) {
            KeyValidationLevel.NONE -> KeyValidationResult.VALIDATION_NOT_PERFORMED
            KeyValidationLevel.LOG_ERROR, KeyValidationLevel.ABORT -> {
                var isValid: Boolean
                analyticsEvent.run {
                    isValid = isValidKey(analyticsEvent.eventName) && isValidKey(eventIdPair.first)
                            && validatePropertiesInternal(properties)
                    if (!isValid) {
                        return@run
                    }
                    superEvent?.run {
                        isValid = isValid && validatePropertiesInternal(superProperties)
                                && validatePropertiesInternal(incrementalSuperProperties)
                                && validatePropertiesInternal(oneTimeSuperProperties)
                    }
                    if (!isValid) {
                        return@run
                    }
                    profileEvent?.run {
                        isValid = isValid && validatePropertiesInternal(profileProperties)
                                && validatePropertiesInternal(incrementalProfileProperties)
                                && validatePropertiesInternal(oneTimeProfileProperties)
                    }
                    return@run
                }
                resultOnValidation(isValid)
            }
        }
    }

    internal fun validateProperties(properties: Map<String, Any?>): KeyValidationResult {
        return when (keyValidationLevel) {
            KeyValidationLevel.NONE -> KeyValidationResult.VALIDATION_NOT_PERFORMED
            KeyValidationLevel.LOG_ERROR, KeyValidationLevel.ABORT -> {
                val isValid = validatePropertiesInternal(properties)
                resultOnValidation(isValid)
            }
        }
    }

    private fun resultOnValidation(isValid: Boolean): KeyValidationResult {
        return when {
            isValid -> KeyValidationResult.VALID
            keyValidationLevel == KeyValidationLevel.LOG_ERROR ->
                KeyValidationResult.INVALID_AND_CONTINUE
            else -> KeyValidationResult.INVALID_AND_ABORT
        }
    }

    private fun validatePropertiesInternal(properties: Map<String, Any?>): Boolean {
        return properties.keys.all { key -> isValidKey(key) }
    }

    private fun isValidKey(key: String) = keyPattern.matcher(key).matches()
}
