package com.github.embydextrous.kanalytics.transformer

/**
 * Abstract implementation of transforming input keys of events as required.
 */
interface KeyTransformer {

    /**
     * @param inputKey key to be transformed like event name or property key
     * Returns a string after applying transformation on [inputKey]
     */
    fun transformKey(inputKey: String): String

    /**
     * Uses [transformKey] to transform all keys in [properties] map.
     */
    fun <T> transformProperties(properties: Map<String, T>): Map<String, T> {
        val transformedProperties = mutableMapOf<String, T>()
        properties.forEach { (key, value) ->
            transformedProperties[transformKey(key)] = value
        }
        return transformedProperties
    }
}
