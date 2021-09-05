package com.navi.file.view.activity

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.*
import com.navi.file.R
import com.navi.file.databinding.ActivityAccountBinding
import com.navi.file.helper.ViewModelFactory
import com.navi.file.model.intercommunication.DisplayScreen
import com.navi.file.repository.server.factory.NaviRetrofitFactory
import com.navi.file.view.fragment.CustomFragmentFactory
import com.navi.file.view.fragment.LoginFragment
import com.navi.file.view.fragment.RegisterFragment
import com.navi.file.viewmodel.AccountViewModel
import okhttp3.HttpUrl

class AccountActivity: AppCompatActivity() {
    private val activityAccountBinding: ActivityAccountBinding by lazy {
        ActivityAccountBinding.inflate(layoutInflater)
    }

    private val accountViewModel: AccountViewModel by viewModels {
        ViewModelFactory.accountViewModelFactory
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Setup Retrofit
        NaviRetrofitFactory.createRetrofit(
            HttpUrl.Builder()
                .scheme("http")
                .host("192.168.0.46")
                .port(5000)
                .build()
        )

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
            }
        }

        // Set Custom Fragment Factory
        supportFragmentManager.fragmentFactory = CustomFragmentFactory()

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