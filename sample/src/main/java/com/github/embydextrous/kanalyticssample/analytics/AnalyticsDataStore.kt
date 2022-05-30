package com.github.embydextrous.kanalyticssample.analytics

import android.content.Context
import android.os.Build
import com.google.gson.reflect.TypeToken
import com.github.embydextrous.kanalytics.AnalyticsStore
import com.github.embydextrous.kanalyticssample.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.TimeZone
import javax.inject.Inject

class AnalyticsDataStore @Inject internal constructor(
    @ApplicationContext private val context: Context,
    private val gsonUtil: GsonUtil,
) : AnalyticsStore {

    private val store = context.getSharedPreferences(STORE_NAME, Context.MODE_PRIVATE)
    private var anyMapType = object : TypeToken<Map<String, Any?>>() {}.type
    private val lock = Any()

    override val defaultEventProperties: Map<String, Any?>
        get() = defaultProperties

    override val defaultProfileProperties: Map<String, Any>
        get() = defaultProperties + Pair(AnalyticConstants.Property.TIMEZONE, TimeZone.getDefault().id)

    private var _superProperties: MutableMap<String, Any?>

    override val superProperties: Map<String, Any?>
        get() = _superProperties.toMap()

    init {
        val superPropertiesStr = store.getString(SUPER_PROPERTIES, null)
        _superProperties = if (superPropertiesStr != null) {
            gsonUtil.fromJson(superPropertiesStr, anyMapType)
        } else {
            mutableMapOf()
        }
    }
    override fun updateSuperProperties(superProperties: Map<String, Any?>) {
        synchronized(lock) {
            _superProperties.putAll(superProperties)
            persistSuperProperties()
        }
    }

    override fun updateOneTimeSuperProperties(oneTimeSuperProperties: Map<String, Any?>) {
        synchronized(lock) {
            _superProperties = (oneTimeSuperProperties + _superProperties).toMutableMap()
            persistSuperProperties()
        }
    }

    override fun updateIncrementalSuperProperties(incrementalSuperProperties: Map<String, Double>) {
        val incrementalMap: MutableMap<String, Any> = incrementalSuperProperties.toMutableMap()
        synchronized(lock) {
            for (incrementSuperProp in incrementalSuperProperties) {
                val key = incrementSuperProp.key
                val value = incrementSuperProp.value
                val existingValue = (_superProperties[key] as? Double) ?: 0.0
                val updatedValue = existingValue + value
                incrementalMap[key] = updatedValue
            }
            _superProperties.putAll(incrementalMap)
            persistSuperProperties()
        }
    }

    override fun isFcmTokenUpdated(): Boolean = store.getBoolean(KEY_FCM_TOKEN_UPDATED, false)

    override fun markFcmTokenUpdated(value: Boolean) {
        store.edit().run {
            putBoolean(KEY_FCM_TOKEN_UPDATED, value)
            apply()
        }
    }

    override fun shallUpdateUserOnLaunch(): Boolean = store.getBoolean(KEY_UPDATE_USER_ON_LAUNCH, true)

    override fun markUserUpdated(value: Boolean) {
        store.edit().run {
            putBoolean(KEY_UPDATE_USER_ON_LAUNCH, value)
            apply()
        }
    }

    private fun persistSuperProperties() {
        store.edit()
            .putString(SUPER_PROPERTIES, gsonUtil.toJson(_superProperties))
            .apply()
    }

    override fun unregisterSuperProperty(key: String) {
        synchronized(lock) {
            _superProperties.remove(key)
            persistSuperProperties()
        }
    }

    override fun clearData() {
        store.edit().run {
            clear()
            apply()
        }
    }

    companion object {
        private const val OS = "Android"

        private const val STORE_NAME = "analytics-store"
        private const val KEY_FCM_TOKEN_UPDATED = "fcm_token_updated"
        private const val KEY_UPDATE_USER_ON_LAUNCH = "update_user_on_launch"
        private const val SUPER_PROPERTIES = "super_properties"

        private val defaultProperties = mapOf(
            AnalyticConstants.Property.APP_VERSION_CODE to BuildConfig.VERSION_CODE,
            AnalyticConstants.Property.APP_VERSION_NAME to BuildConfig.VERSION_NAME,
            AnalyticConstants.Property.BRAND to Build.BRAND,
            AnalyticConstants.Property.MANUFACTURER to Build.MANUFACTURER,
            AnalyticConstants.Property.MODEL to Build.MODEL,
            AnalyticConstants.Property.OS to OS,
            AnalyticConstants.Property.OS_VERSION to Build.VERSION.RELEASE
        )
    }
}
