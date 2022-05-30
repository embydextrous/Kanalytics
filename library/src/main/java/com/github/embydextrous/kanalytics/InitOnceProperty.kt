package com.github.embydextrous.kanalytics

import kotlin.IllegalStateException
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Delegate class to implement properties that can be initialized only once.
 */
class InitOnceProperty<T> : ReadWriteProperty<Any, T> {

    private object EMPTY
    private var value: Any? = EMPTY

    /**
     * @throws IllegalStateException if trying to read value and the property is not initialized.
     */
    @Suppress("UNCHECKED_CAST")
    override fun getValue(thisRef: Any, property: KProperty<*>): T {
        if (value == EMPTY) {
            throw IllegalStateException("Property isn't Initialized")
        } else {
            return value as T
        }
    }

    /**
     * @throws IllegalStateException if trying to write a property that has been already initialized.
     */
    override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
        if (this.value != EMPTY) {
            throw IllegalStateException("Cannot rewrite property.")
        }
        this.value = value
    }
}

inline fun <reified T> initOnce(): ReadWriteProperty<Any, T> = InitOnceProperty()
