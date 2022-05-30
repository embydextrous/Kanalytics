package com.github.embydextrous.kanalyticssample.di

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.github.embydextrous.kanalytics.AnalyticsManager
import com.github.embydextrous.kanalytics.Dispatcher
import com.github.embydextrous.kanalytics.validation.KeyValidationLevel
import com.github.embydextrous.kanalytics.validation.KeyValidator
import com.github.embydextrous.kanalyticssample.SampleApp
import com.github.embydextrous.kanalyticssample.analytics.AnalyticsDataStore
import com.github.embydextrous.kanalyticssample.analytics.AnalyticsLogger
import com.github.embydextrous.kanalyticssample.analytics.CommonEventIdProviderImpl
import com.github.embydextrous.kanalyticssample.analytics.DispatcherResolverImpl
import com.github.embydextrous.kanalyticssample.analytics.dispatchers.ConsoleDispatcher
import com.github.embydextrous.kanalyticssample.analytics.dispatchers.MixpanelDispatcher
import com.mixpanel.android.mpmetrics.MixpanelAPI
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import java.util.regex.Pattern
import javax.inject.Named

@InstallIn(SingletonComponent::class)
@Module
object AnalyticsModule {

    @Provides
    fun provideMixpanelApi(@ApplicationContext context: Context): MixpanelAPI =
        MixpanelAPI.getInstance(context, SampleApp.MIXPANEL_TOKEN)

    @Reusable
    @Provides
    fun provideGson(): Gson = GsonBuilder().serializeNulls().create()

    @Provides
    @Named("analytics_key_regex")
    // Regex corresponds to title case with digits allowed in between
    fun provideAnalyticsKeyRegex(): String = "^\\s*(?:\\s*([A-Z][a-z0-9]*)\\s*\\b)+\\s*$"

    @Provides
    @IntoMap
    @ByteKey(MixpanelDispatcher.DISPATCHER_ID)
    fun provideMixpanelDispatcher(mixpanelDispatcher: MixpanelDispatcher): Dispatcher =
        mixpanelDispatcher

    @Provides
    @IntoMap
    @ByteKey(ConsoleDispatcher.DISPATCHER_ID)
    fun provideConsoleDispatcher(consoleDispatcher: ConsoleDispatcher): Dispatcher =
        consoleDispatcher

    @Provides
    @Reusable
    fun provideAnalyticsManager(
        dispatchers: Map<Byte, @JvmSuppressWildcards Dispatcher>,
        analyticsDataStore: AnalyticsDataStore,
        commonEventIdProviderImpl: CommonEventIdProviderImpl,
        dispatcherResolverImpl: DispatcherResolverImpl,
        @Named("analytics_key_regex") analyticsKeyRegex: String,
    ): AnalyticsManager = AnalyticsManager.getInstance(
        dispatchers,
        analyticsDataStore,
        dispatcherResolverImpl,
        commonEventIdProviderImpl,
        KeyValidator(
            Pattern.compile(analyticsKeyRegex),
            KeyValidationLevel.LOG_ERROR
        ),
        AnalyticsLogger(AnalyticsManager::class.java)
    )
}
