package com.navi.file.view.fragment

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import androidx.annotation.StyleRes
import androidx.core.util.Preconditions
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import com.navi.file.HiltTestActivity
import com.navi.file.R
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import kotlin.reflect.full.declaredMembers
import kotlin.reflect.jvm.isAccessible

abstract class ViewModelTestHelper {
    /**
     * launchFragmentInContainer from the androidx.fragment:fragment-testing library
     * is NOT possible to use right now as it uses a hardcoded Activity under the hood
     * (i.e. [EmptyFragmentActivity]) which is not annotated with @AndroidEntryPoint.
     *
     * As a workaround, use this function that is equivalent. It requires you to add
     * [HiltTestActivity] in the debug folder and include it in the debug AndroidManifest.xml file
     * as can be found in this project.
     */
    inline fun <reified T : Fragment> launchFragmentInHiltContainer(
        fragmentArgs: Bundle? = null,
        @StyleRes themeResId: Int = R.style.Theme_NaviFile,
        crossinline action: Fragment.() -> Unit = {}
    ) {
        val startActivityIntent = Intent.makeMainActivity(
            ComponentName(
                ApplicationProvider.getApplicationContext(),
                HiltTestActivity::class.java
            )
        ).putExtra(
            "androidx.fragment.app.testing.FragmentScenario.EmptyFragmentActivity.THEME_EXTRAS_BUNDLE_KEY",
            themeResId
        )

        ActivityScenario.launch<HiltTestActivity>(startActivityIntent).onActivity { activity ->
            val fragment: Fragment = activity.supportFragmentManager.fragmentFactory.instantiate(
                Preconditions.checkNotNull(T::class.java.classLoader),
                T::class.java.name
            )
            fragment.arguments = fragmentArgs
            activity.supportFragmentManager
                .beginTransaction()
                .add(android.R.id.content, fragment, "")
                .commitNow()

            fragment.action()
        }
    }

    /**
     * Create ViewModel Factory for UI Testing.
     * This function will return mocked - userViewModelObject when you inject viewmodelprovider.factory
     * from this function.
     *
     * @param TargetViewModel Reified TargetViewModel - Target View Model Class you want to make
     * @param viewModelObject The mocked viewModelObject
     * @return A Mock-Ready ViewModelProvider Factory.
     */
    protected inline fun <reified TargetViewModel : ViewModel?> createViewModelFactory(viewModelObject: TargetViewModel): ViewModelProvider.Factory{
        return object: ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return if (modelClass.isAssignableFrom(TargetViewModel::class.java)) {
                    viewModelObject as T
                } else {
                    throw IllegalStateException()
                }
            }
        }
    }

    /**
     * Get Binding objects from Fragments, by Kotlin Reflection.
     * It will find member by 'bindingName'.
     *
     * @param T A Binding Type, i.e FragmentRegisterBinding
     * @param F A Location where binding stores.
     * @param objectCall An object that contains binding.
     * @param bindingName Binding Object[or variable] name.
     * @return A Binding Object.
     */
    protected inline fun<reified T, reified F> getBinding(objectCall: Any, bindingName: String): T {
        val memberProperty = F::class.declaredMembers.find { it.name == bindingName }!!.apply {
            isAccessible = true
        }

        return memberProperty.call(objectCall) as T
    }

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
}