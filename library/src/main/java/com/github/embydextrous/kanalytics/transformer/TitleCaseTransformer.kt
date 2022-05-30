package com.github.embydextrous.kanalytics.transformer

/**
 * Converts key to title case.
 */
class TitleCaseTransformer : KeyTransformer {

    override fun transformKey(inputKey: String) =
        inputKey.split(" ").joinToString(" ") { it.capitalize() }
}
