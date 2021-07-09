package com.kangdroid.navi_arch.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import java.lang.reflect.Field
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import kotlin.reflect.KFunction
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.jvm.isAccessible

object ViewModelTestHelper {
    /* Copyright 2019 Google LLC.
SPDX-License-Identifier: Apache-2.0 */
    fun <T> LiveData<T>.getOrAwaitValue(
        time: Long = 2,
        timeUnit: TimeUnit = TimeUnit.SECONDS
    ): T {
        var data: T? = null
        val latch = CountDownLatch(1)
        val observer = object : Observer<T> {
            override fun onChanged(o: T?) {
                data = o
                latch.countDown()
                this@getOrAwaitValue.removeObserver(this)
            }
        }

        this.observeForever(observer)

        // Don't wait indefinitely if the LiveData is not set.
        if (!latch.await(time, timeUnit)) {
            throw TimeoutException("LiveData value was never set.")
        }

        @Suppress("UNCHECKED_CAST")
        return data as T
    }

    /**
     * getFields
     * B: Base Object[Where - which class do you get fields?]
     * T: Return Type[What should we return?]
     */
    inline fun<reified B, reified T> getFields(fieldName: String, baseObject: B): T {
        // Get Page Set
        val targetField: Field = B::class.java.getDeclaredField(fieldName).apply {
            isAccessible = true
        }

        @Suppress("UNCHECKED_CAST")
        return targetField.get(baseObject) as T
    }

    /**
     * getFields
     * B: Base Object[Where - which class do you get fields?]
     * T: To Set - what type should we set?
     */
    inline fun<reified B, reified T> setFields(fieldName: String, baseObject: B, toSet: T) {
        B::class.java.getDeclaredField(fieldName).apply {
            isAccessible = true
            set(baseObject, toSet)
        }
    }

    inline fun<reified B> getFunction(functionName: String): KFunction<*> {
        // Private Method testing
        return B::class.declaredMemberFunctions.find {
            it.name == functionName
        }!!.apply {
            isAccessible = true
        }
    }
}