package com.github.embydextrous.kanalytics.transformer

/**
 * Does not perform any transformation on keys.
 */
class EmptyKeyTransformer : KeyTransformer {

    override fun transformKey(inputKey: String) = inputKey
}
