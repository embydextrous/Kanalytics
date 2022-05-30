package com.github.embydextrous.kanalyticssample.analytics

import com.github.embydextrous.kanalytics.CommonEventIdProvider
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommonEventIdProviderImpl @Inject internal constructor() : CommonEventIdProvider() {
    override val eventIdKey = "Common Analytics Event Id"

    override fun generateEventId(): String = UUID.randomUUID().toString()
}
