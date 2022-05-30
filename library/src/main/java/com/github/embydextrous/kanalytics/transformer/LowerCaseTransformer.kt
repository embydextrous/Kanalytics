package com.github.embydextrous.kanalytics.transformer

/**
 * Converts the key to lower case.
 */
class LowerCaseTransformer : KeyTransformer {

    override fun transformKey(inputKey: String) = inputKey.toLowerCase()
}
