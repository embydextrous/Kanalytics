package com.github.embydextrous.kanalyticssample.analytics

import android.util.Log
import com.github.embydextrous.kanalytics.Logger

class AnalyticsLogger(_tag: Class<*>) : Logger {

    override val tag = _tag

    /** Log a verbose message.  */
    override fun v(message: () -> String) {
        Log.v(tag.canonicalName, message())
    }

    /** Log an info message.  */
    override fun i(message: () -> String) {
        Log.i(tag.canonicalName, message())
    }

    /** Log a debug message.  */
    override fun d(message: () -> String) {
        Log.d(tag.canonicalName, message())
    }

    /** Log an error message.  */
    override fun e(error: Throwable?, format: String?, vararg extra: Any?) {
        Log.e(tag.canonicalName, error?.message ?: "")
    }
}
