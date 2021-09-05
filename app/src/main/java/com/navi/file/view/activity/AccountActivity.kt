package com.navi.file.view.activity

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.*
import com.navi.file.R
import com.navi.file.databinding.ActivityAccountBinding
import com.navi.file.helper.ViewModelFactory
import com.navi.file.model.intercommunication.DisplayScreen
import com.navi.file.view.fragment.LoginFragment
import com.navi.file.view.fragment.RegisterFragment
import com.navi.file.viewmodel.AccountViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AccountActivity: AppCompatActivity() {
    // ViewModel Factory
    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    // Account ViewModel[Or Display ViewModel]
    private val accountViewModel: AccountViewModel by viewModels { viewModelFactory.accountViewModelFactory }

    // UI Binding
    private val activityAccountBinding: ActivityAccountBinding by lazy {
        ActivityAccountBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set UI
        setContentView(activityAccountBinding.root)

        // Observe
        accountViewModel.displayLiveData.observe(this) {
            when (it) {
                DisplayScreen.Login -> {
                    supportFragmentManager.popBackStack()
                }
                DisplayScreen.Register -> {
                    replaceFragment<RegisterFragment>(true)
                }
                else -> {} // Do Nothing at all.
            }
        }

        // Initial State is first fragment.
        replaceFragment<LoginFragment>()
    }

    private inline fun<reified T : Fragment> replaceFragment(backStack: Boolean = false) {
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            replace<T>(R.id.accountViewContainer, tag = "singleBackstack")

            if (backStack) addToBackStack("singleBackstack")
        }
    }
}