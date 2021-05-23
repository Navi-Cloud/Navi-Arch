package com.kangdroid.navi_arch.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import junit.framework.TestCase
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.assertj.core.internal.bytebuddy.implementation.bytecode.Throw
import org.junit.Rule
import org.junit.Test
import java.lang.RuntimeException
import java.lang.reflect.Field
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import kotlin.reflect.KFunction
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.jvm.isAccessible

class UserViewModelTest {

    private val fakeServerManagement: FakeServerManagement = FakeServerManagement()

    private val userViewModel: UserViewModel = UserViewModel(fakeServerManagement)

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

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


    @Test
    fun register_doingwell(){

        userViewModel.register("userId","userName","userEmail","userPassword")
        val pageRequestData : PageRequest ?=
        userViewModel.pageRequest.getOrAwaitValue()

        assertThat(pageRequestData).isEqualTo(PageRequest.REQUEST_LOGIN)

    }

    @Test
    fun login_doingwell(){

        userViewModel.login("userId","userPassword")
        val pageRequestData : PageRequest ?=
            userViewModel.pageRequest.getOrAwaitValue()

        assertThat(pageRequestData).isEqualTo(PageRequest.REQUEST_MAIN)

    }

//    @Test
//    fun login_doingwrong(){
//
//        userViewModel.login("wrongId","wrongpassword")
//
//        val loginError : Throwable ?= userViewModel.loginErrorData.getOrAwaitValue()
//
//        assertThat(loginError).isNotEqualTo(null)
//
//    }

    @Test
    fun loginError_doingWell(){
        val exception : Throwable = Throwable()
        userViewModel.loginError(exception)
        val loginErrorData = userViewModel.loginErrorData.getOrAwaitValue()

        assertThat(loginErrorData).isEqualTo(exception)
    }

    @Test
    fun clearErrorData_doingwell(){
        userViewModel.clearErrorData()
        val loginErrorData = userViewModel.loginErrorData.getOrAwaitValue()
        assertThat(loginErrorData).isEqualTo(null)
    }

    @Test
    fun requestRegisterPage_doingwell(){
        userViewModel.requestRegisterPage()
        val pageRequestData : PageRequest ?=
            userViewModel.pageRequest.getOrAwaitValue()
        assertThat(pageRequestData).isEqualTo(PageRequest.REQUEST_REGISTER)
    }

}