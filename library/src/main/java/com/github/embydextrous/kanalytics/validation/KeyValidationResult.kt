package com.github.embydextrous.kanalytics.validation

enum class KeyValidationResult {
    /**
     * No validation is performed.
     */
    VALIDATION_NOT_PERFORMED,

    /**
     * Keys are not valid but can send the event or update properties.
     */
    INVALID_AND_CONTINUE,

    /**
     * Keys are not valid and don't send the event or update the properties.
     */
    INVALID_AND_ABORT,

    /**
     * Keys are valid.
     */
    VALID
}
