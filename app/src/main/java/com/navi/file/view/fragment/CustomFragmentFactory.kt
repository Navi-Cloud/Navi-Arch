package com.navi.file.view.fragment

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import com.navi.file.helper.ViewModelFactory

class CustomFragmentFactory: FragmentFactory() {
    override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
        return when (className) {
            LoginFragment::class.java.name -> LoginFragment(
                viewModelFactory = ViewModelFactory.loginViewModelFactory,
                accountViewModelFactory = ViewModelFactory.accountViewModelFactory
            ) // Everything should be null though
            RegisterFragment::class.java.name -> RegisterFragment(
                viewModelFactory = ViewModelFactory.registerViewModelFactory,
                accountViewModelFactory = ViewModelFactory.accountViewModelFactory
            )
            else -> super.instantiate(classLoader, className)
        }
    }
}