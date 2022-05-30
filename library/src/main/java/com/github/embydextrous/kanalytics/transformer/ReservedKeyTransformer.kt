package com.github.embydextrous.kanalytics.transformer

/**
 * Replaces the key for its value in [reservedKeyMap]
 */
class ReservedKeyTransformer(
    private val reservedKeyMap: Map<String, String>
) : KeyTransformer {

    override fun transformKey(inputKey: String): String {
        return if (inputKey in reservedKeyMap) {
            reservedKeyMap[inputKey] ?: error("No input key found")
        } else {
            inputKey
        }
    }
}
