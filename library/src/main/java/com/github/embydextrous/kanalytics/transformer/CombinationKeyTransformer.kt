package com.github.embydextrous.kanalytics.transformer

/**
 * Applies all [KeyTransformer] in [keyTransformers] to input key in order.
 */
class CombinationKeyTransformer(
    private val keyTransformers: List<KeyTransformer>
) : KeyTransformer {

    override fun transformKey(inputKey: String): String {
        var outputKey = inputKey
        keyTransformers.forEach {
            outputKey = it.transformKey(outputKey)
        }
        return outputKey
    }
}
