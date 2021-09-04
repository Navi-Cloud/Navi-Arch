package com.navi.file.view.fragment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlin.reflect.full.declaredMembers
import kotlin.reflect.jvm.isAccessible

abstract class ViewModelTestHelper {
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
}