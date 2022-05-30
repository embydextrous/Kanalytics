package com.github.embydextrous.kanalytics.transformer

import java.util.regex.Pattern

/**
 * @param patternMaps a list of [PatternMap] where each [PatternMap.patternToFind] is replaced
 * via [PatternMap.replaceWith]
 */
class PatternTransformer(private val patternMaps: List<PatternMap>) : KeyTransformer {

    override fun transformKey(inputKey: String) = findAndReplace(inputKey)

    private fun findAndReplace(inputKey: String): String {
        var outputKey = inputKey
        patternMaps.forEach { patternMap ->
            val matcher = patternMap.patternToFind.matcher(outputKey)
            outputKey = matcher.replaceAll(patternMap.replaceWith)
        }
        return outputKey
    }

    data class PatternMap(val patternToFind: Pattern, val replaceWith: String)
}
