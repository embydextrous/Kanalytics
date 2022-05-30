package com.github.embydextrous.kanalytics.transformer

/**
 * Prefixes [prefix] to key.
 */
class PrefixKeyTransformer(private val prefix: String) : KeyTransformer {

    override fun transformKey(inputKey: String) = prefix + inputKey
}
