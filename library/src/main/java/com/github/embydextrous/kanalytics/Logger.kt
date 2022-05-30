package com.github.embydextrous.kanalytics

interface Logger {

    val tag: Class<*>

    /** Log a verbose message.  */
    fun v(message: () -> String)

    /** Log an info message.  */
    fun i(message: () -> String)

    /** Log a debug message.  */
    fun d(message: () -> String)

    /** Log an error message.  */
    fun e(error: Throwable? = null, format: String? = null, vararg extra: Any?)
}
