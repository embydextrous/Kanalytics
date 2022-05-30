package com.github.embydextrous.kanalyticssample.analytics

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GsonUtil @Inject internal constructor(private val gson: Gson) {

    fun toJson(`object`: Any): String {
        return gson.toJson(`object`)
    }

    fun toJson(`object`: Any, typeOfSrc: Type): String {
        return gson.toJson(`object`, typeOfSrc)
    }


    fun <T> fromJson(json: String, type: Class<T>): T = gson.fromJson(json, type)

    fun <T> fromJson(json: String, type: Type): T = gson.fromJson(json, type)

    fun <T> fromJson(json: String) = this.fromJson<T>(json, object : TypeToken<T>() {}.type)
}
