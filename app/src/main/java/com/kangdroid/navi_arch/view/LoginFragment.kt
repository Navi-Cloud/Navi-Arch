package com.kangdroid.navi_arch.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.kangdroid.navi_arch.databinding.FragmentLoginBinding
import com.kangdroid.navi_arch.viewmodel.UserViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LoginFragment @Inject constructor(): Fragment() {
    // Log Tag
    private val logTag: String = this::class.java.simpleName

    // View Binding
    var loginBinding: FragmentLoginBinding? = null

    // View Model for Login/Register
    private val userViewModel: UserViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        loginBinding = FragmentLoginBinding.inflate(layoutInflater, container, false)
        return loginBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initBinding()

        initObserver()
    }

    private fun initObserver() {
        userViewModel.loginErrorData.observe(viewLifecycleOwner) {
            if (it != null) {
                // If login success, make Toast Message and clear livErrorData
                Log.e(logTag, "Error Message Observed")
                Log.e(logTag, it.stackTraceToString())
                Toast.makeText(context, "Login Error: ${it.message}", Toast.LENGTH_LONG)
                    .show()

                // Clear edit text
                loginBinding!!.idLogin.setText("")
                loginBinding!!.pwLogin.setText("")
            }
        }
    }

    private fun initBinding() {
        // Login Button
        loginBinding!!.button.setOnClickListener {
            val userId: String = loginBinding!!.idLogin.text.toString()
            val userPassword: String = loginBinding!!.pwLogin.text.toString()

            Log.i(logTag, "$userId , $userPassword")

            userViewModel.login(
                userId = userId,
                userPassword = userPassword
            )
        }

        // Join [Register] Button
        loginBinding!!.textView2.setOnClickListener {
            // fragment translation to Register Fragment
            userViewModel.requestRegisterPage()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        userViewModel.clearLoginErrorData()
        loginBinding = null
    }
}