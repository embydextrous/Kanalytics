package com.github.embydextrous.kanalyticssample.di

import dagger.MapKey

@MapKey(unwrapValue = true)
internal annotation class ByteKey(val value: Byte)
