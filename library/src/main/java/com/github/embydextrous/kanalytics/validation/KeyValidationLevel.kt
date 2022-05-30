package com.github.embydextrous.kanalytics.validation

/**
 * Validation levels for verifying incoming data using [KeyValidator.validateEvent]
 */
enum class KeyValidationLevel {
    /**
     * No validation will be done.
     */
    NONE,

    /**
     * A warning log will be generated if event data is not valid.
     */
    LOG_ERROR,

    /**
     * Event will not be sent and aborted if event data is not valid.
     */
    ABORT
}
