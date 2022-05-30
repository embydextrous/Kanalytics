package com.github.embydextrous.kanalytics.transformer

import java.util.regex.Pattern

object KeyTransformerUtil {

    fun titleCaseToSnakeCaseTransformer(): CombinationKeyTransformer {
        return CombinationKeyTransformer(
            listOf(
                LowerCaseTransformer(),
                PatternTransformer(
                    listOf(
                        PatternTransformer.PatternMap(Pattern.compile("[\\s]+"), "_")
                    )
                )
            )
        )
    }

    fun snakeCaseToTitleCaseTransformer(): CombinationKeyTransformer {
        return CombinationKeyTransformer(
            listOf(
                PatternTransformer(
                    listOf(
                        PatternTransformer.PatternMap(Pattern.compile("[_]+"), "\\s"),
                    )
                ),
                TitleCaseTransformer()
            )
        )
    }
}
