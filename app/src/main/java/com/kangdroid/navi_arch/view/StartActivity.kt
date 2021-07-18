package com.kangdroid.navi_arch.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.kangdroid.navi_arch.R
import com.kangdroid.navi_arch.databinding.ActivityStartBinding
import com.kangdroid.navi_arch.viewmodel.PageRequest
import com.kangdroid.navi_arch.viewmodel.UserViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class StartActivity : AppCompatActivity() {
    // View binding
     val startBinding: ActivityStartBinding by lazy {
        ActivityStartBinding.inflate(layoutInflater)
    }

    // UserViewModel
    private val userViewModel: UserViewModel by viewModels()

    // Register Fragment
    @set : Inject
    var registerFragment: RegisterFragment = RegisterFragment()

    // Login Fragment
    @set : Inject
    var loginFragment: LoginFragment = LoginFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(startBinding.root)

        // Init Observer
        initObserver()
        userViewModel.requestLoginPage()
    }

    private fun initObserver() {
        userViewModel.pageRequest.observe(this) {
            when(it) {
                PageRequest.REQUEST_MAIN -> switchActivity()
                PageRequest.REQUEST_LOGIN -> replaceFragment(loginFragment)
                PageRequest.REQUEST_REGISTER -> replaceFragment(registerFragment)
                else -> {}
            }
        }
    }

    // If login success, switch to MainActivity
    private fun switchActivity(){
        val intent: Intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    // for fragment by fragment transaction
    // default: addToBackStack
    private fun replaceFragment(fragment: Fragment, onBackStack: Boolean = false){
        val transaction: FragmentTransaction =
            supportFragmentManager.beginTransaction().replace(R.id.startActivityContainer, fragment)
        if(onBackStack) transaction.addToBackStack(null)
        transaction.commit()
    }
}