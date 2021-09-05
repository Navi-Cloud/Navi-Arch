package com.navi.file.helper

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.navi.file.repository.server.user.UserRepository
import com.navi.file.viewmodel.AccountViewModel
import com.navi.file.viewmodel.LoginViewModel
import com.navi.file.viewmodel.RegisterViewModel
import javax.inject.Inject

class ViewModelFactory @Inject constructor(
    userRepository: UserRepository
) {

    /**
     * Create ViewModel Factory for UI Testing.
     * This function will return mocked - userViewModelObject when you inject viewmodelprovider.factory
     * from this function.
     *
     * @param TargetViewModel Reified TargetViewModel - Target View Model Class you want to make
     * @param viewModelObject The mocked viewModelObject
     * @return A Mock-Ready ViewModelProvider Factory.
     */
    private inline fun <reified TargetViewModel : ViewModel?> createViewModelFactory(viewModelObject: TargetViewModel): ViewModelProvider.Factory{
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

    // The Login View Model
    val loginViewModelFactory = createViewModelFactory(LoginViewModel(userRepository))

    // The Register View Model
    val registerViewModelFactory = createViewModelFactory(RegisterViewModel(userRepository))

    // AccountViewModelFactory
    val accountViewModelFactory = createViewModelFactory(AccountViewModel())
}